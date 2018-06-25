package com.api.extractor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import csvreader.CsvWriter;


public class Extractor {
	private String fileDir; 
	private MysqlInfo sqlInfo;
	private Connection conn;
	private Statement stmt;
	private List<Set<Integer>> outValueCSV=new ArrayList<Set<Integer>>();
	private Set<Integer> csvLine=new HashSet<Integer>();
	public Extractor(String str ) {
		fileDir=str;
	}
	public String getFileDir(){
		return fileDir;
	}
	private void  connectMysql(){
		sqlInfo=new MysqlInfo("127.0.0.1","3306","root","root1234","android");
		String mysqlURL="jdbc:mysql://localhost:3306/android?useUnicode=true&characterEncoding=utf8&useSSL=true";
   		try {
			conn = DriverManager.getConnection(mysqlURL,sqlInfo.username,sqlInfo.password);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
   		try {
			stmt = conn.createStatement();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}
	private void closeMysql(){
		try {
			stmt.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			try {
				conn.close();
			} catch (SQLException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			e.printStackTrace();
		}
		try {
			conn.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void DecompilationAll(String fileDir) throws IOException, InterruptedException
	{
		File file=new File(fileDir);
		File[] files = file.listFiles();
		for(int i=0;i<files.length;i++)
		{
			if(files[i].isFile()==true)
			{
				String fileName=files[i].getName();
				if(fileName.endsWith(".apk"))
				{
					File dir=new File(fileDir);
					String command="cmd /c apktool.jar d "+fileName;
					Runtime runtime=Runtime.getRuntime();
					Process process;
					process=runtime.exec(command,null,dir);
					process.waitFor();
					command="cmd /c rmdir /S /Q assets lib original res";
				    dir=new File(fileDir+"\\"+fileName.split("\\.")[0]);
					process=runtime.exec(command,null,dir);
					process.waitFor();
				}
				/*命令模式：
				String command="cmd /c apktool.jar d *.apk";
				File dir=new File(fileDir);
				Runtime runtime=Runtime.getRuntime();
				Process process=runtime.exec(command,null,dir);
				InputStream is = process.getInputStream();
				BufferedReader reader = new BufferedReader(new InputStreamReader(is));
				process.waitFor();
				
				if (process.exitValue() != 0) {
				    System.out.println("命令执行失败");
				}
				//打印输出信息
				String s = null;
				while ((s = reader.readLine()) != null) {
				    System.out.println(s);
				}
				*/
				
			}
		}
		
	}
	public void Decompilation()
	{
		try {
			DecompilationAll("benign");
			DecompilationAll("malware");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	private void readFilesAndStore(String filePath) throws IOException, SQLException{
		FileInputStream fis=new FileInputStream(filePath);
		InputStreamReader isr=new InputStreamReader(fis);
		BufferedReader br = new BufferedReader(isr);
        for (String line = br.readLine(); line != null; line = br.readLine()){
        	if(line.indexOf('<')!=-1)continue;
        	line=line.trim();
        	if(line.startsWith("invoke")){
        		int begin=line.indexOf('L');
        		int end=line.indexOf('(');
        		if(begin+1>end){
        			//System.out.println(line+"***");
        			continue;
        		}
        		String sub=line.substring(begin+1,end);
        		if(!sub.startsWith("java")&&!sub.startsWith("org")){       		
        			continue;
        		}
        		
        		String sql;
        		
        		sql="select * from api where API='"+sub+"'";
        		ResultSet res= stmt.executeQuery(sql);
        		
        		if(res.next()){
        			csvLine.add(res.getInt("ID"));
        			//System.out.println(res.getInt("ID"));
        			res.close();
        			continue;
        		}
        		sql="select count(*)totalCount from api";
        		res= stmt.executeQuery(sql);
        		int num=0;
        		if(res.next()){
        		  num=res.getInt("totalCount")+1;
        		}
    			sql="insert into api values('"+sub+"',"+num+")";
    			stmt.executeUpdate(sql);
    			csvLine.add(num);
    			res.close();
    			
        	}
        }
        br.close();
	}
	private void iterateFileDir(String dir) {
		File file=new File(dir);
		File[] files=file.listFiles();
		for(int i=0;i<files.length;i++){
			String fileName=files[i].getName();
			if(files[i].isDirectory()){
				iterateFileDir(dir+"\\"+fileName);
			}
			else if(files[i].isFile()){
				if(fileName.endsWith(".smali")){
					try {
						readFilesAndStore(dir+"\\"+fileName);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (SQLException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
				}
			}
				
		}
	}
	private void writeCsvFile() throws IOException{
		CsvWriter cw=new CsvWriter(fileDir+"\\test.csv",',',Charset.forName("GBK"));
		String sql="select count(*)totalCount from api";
		int count=0;
		try {
			ResultSet res=stmt.executeQuery(sql);
			if(res.next())count=res.getInt("totalCount");
			res.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		String[] csvValues=new String[count+1];
		csvValues[0]="class";
		for(int i=1;i<=count;i++){
			csvValues[i]=new Integer(i).toString();
		}
		cw.writeRecord(csvValues);
		Iterator<Set<Integer>> it=outValueCSV.iterator();
		while(it.hasNext()){
			for(int i=0;i<=count;i++)csvValues[i]="0";
			csvLine=it.next();
			Iterator<Integer> iter=csvLine.iterator();
			if(iter.hasNext()){
				csvValues[0]=iter.next().toString();
			}
			while(iter.hasNext()){
				csvValues[iter.next()]="1";
			}
			
			cw.writeRecord(csvValues);
		}
		 
		cw.close();
	}
	//特征提取的方法
	public void extractApi() throws SQLException, IOException{
		connectMysql();
		outValueCSV.clear();
		File file=new File(fileDir+"\\benign");
		File[] files= file.listFiles();
		for(int i=0;i<files.length;i++){
			
			if(files[i].isDirectory()){
				csvLine.clear();
				csvLine.add(-1);
				
				iterateFileDir(files[i].getPath());
				outValueCSV.add(new HashSet<Integer>(csvLine));
				//System.out.println(csvLine.size());
			}
			
		}
		
		System.out.println();
		file=new File(fileDir+"\\malware");
		files= file.listFiles();
		for(int i=0;i<files.length;i++){
			
			if(files[i].isDirectory()){
				csvLine.clear();
				csvLine.add(1);
				iterateFileDir(files[i].getPath());
				outValueCSV.add(new HashSet<Integer>(csvLine));
				//System.out.println(csvLine.size());
			}
		}	
		//System.out.println();
		writeCsvFile();
		closeMysql();
	}
	private void storeNewApi(String filePath) throws IOException{
		connectMysql();
		//读取output.mrmrout
		File file=new File(filePath);
		
		BufferedReader br=new BufferedReader(new FileReader(file));
		String line,sql;
		boolean flag=false;
		for( line=br.readLine();line!=null;line=br.readLine()){
			line=line.trim();
			if(line.equals("*** mRMR features ***")){
				flag=true;
				continue;
			}
			if(line.equals("Order 	 Fea 	 Name 	 Score"))
				continue;
			if(line.equals("*** This program and the respective minimum Redundancy Maximum Relevance (mRMR)"))
				break;
			if(flag){
				String[] str=line.split("\\s+");
				if(str.length>=2)
				{
					sql="update mrmr_api set api_id="+str[1]+" where number="+str[0];
					try {
						//System.out.println(sql);
						stmt.executeUpdate(sql);
					} catch (SQLException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}	
				}

			}
		}
		br.close();
		closeMysql();
	}
	public void mrmrAndStoreNewApi(String filePath) throws IOException, InterruptedException{
		String command="cmd /c mrmr_win32.exe ./mrmr -i "+filePath+" -n 200 >output.mrmrout";
		Runtime runtime=Runtime.getRuntime();
		Process process;
		File dir=new File(new File(filePath).getParent());
		process=runtime.exec(command,null,dir);
		process.waitFor();
		storeNewApi(dir+"\\output.mrmrout");		
	}
	public static void main(String[] args) throws SQLException, IOException  {
		// TODO Auto-generated method stub
		Extractor test=new Extractor("E:\\lian_workspace\\ApiExtractor");
		test.extractApi();
		//test.storeNewApi("E:\\lian_workspace\\\\ApiExtractor\\output.mrmrout");
	
	}

}
