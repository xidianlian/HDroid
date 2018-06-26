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

/**
 *  
 * @ClassName:  Extractor   
 * @Description:TODO(特征提取器)   
 * @author: 练伟成 
 * @date:   2018年6月26日 下午4:05:53       
 *
 */
public class Extractor {
	
	private MysqlInfo sqlInfo;
	private Connection conn;
	private Statement stmt;
	private List<Set<Integer>> outValueCSV=new ArrayList<Set<Integer>>();
	private Set<Integer> csvLine=new HashSet<Integer>();
	 Extractor(MysqlInfo info){
		this.sqlInfo=info;
	}
	/**
	 * 
	 * @Title: connectMysql
	 * @Description: TODO(连接数据库)
	 */
	private void  connectMysql(){
		
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
	/**
	 * @Title: DecompilationAll
	 * @Description: TODO(反编译此文件目录下的所有apk)
	 * @param  fileDir 文件目录
	 * @return void    返回类型
	 * @throws
	 */
	private void DecompilationAll(String fileDir) throws IOException, InterruptedException
	{
		File file=new File(fileDir);
		File[] files = file.listFiles();
		int apkNumber=0;
		for(int i=0;i<files.length;i++)
		{
			if(files[i].isFile()==true)
			{
				String fileName=files[i].getName();
				if(fileName.endsWith(".apk"))
				{
					File dir=new File(fileDir);
					String command="cmd /c apktool.jar d "+fileName+" -o "+(++apkNumber);
					Runtime runtime=Runtime.getRuntime();
					Process process;
					process=runtime.exec(command,null,dir);
					process.waitFor();
					command="cmd /c rmdir /S /Q assets lib original res";
				    dir=new File(fileDir+"\\"+apkNumber);
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
	/**
	 * @Title: Decompilation
	 * @Description: TODO(调用DecompilationAll方法分别反编译恶意软件和良性软件)
	 */
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
	/**
	 * @Title: readFilesAndStore
	 * @Description: TODO(读取.smali文件，并且存储提取到的API)
	 * @param  filePath 该文件路径
	 * @return void    返回类型
	 * @throws
	 */
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
	/**
	 * @Title: iterateFileDir
	 * @Description: TODO(递归遍历反编译后的samli目录下的所有文件，当查到该文件是.smali就提取其中的API)
	 * @param dir    参数
	 * @return void    返回类型
	 * @throws
	 */
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
	/**
	 * @Title: writeCsvFile
	 * @Description: TODO(以csv格式存储每个apk和api之间的关系，此csv文件用于mrmr算法)
	 * @throws IOException    参数
	 * @return void    返回类型
	 * @throws
	 */
	private void writeCsvFile() throws IOException{
		CsvWriter cw=new CsvWriter("test.csv",',',Charset.forName("GBK"));
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
		//第0行
		for(int i=1;i<=count;i++){
			csvValues[i]=new Integer(i).toString();
		}
		cw.writeRecord(csvValues);
		for(Set<Integer> it:outValueCSV) {
			for(int i=0;i<=count;i++)csvValues[i]="0";
			for(Integer iter:it) {
				if(iter.equals(0)) {
					csvValues[0]="1";
					continue;
				}
				else if(iter.equals(-1)) {
					csvValues[0]="-1";
					continue;
				}
				csvValues[iter]="1";
			}
			cw.writeRecord(csvValues);
		}
		cw.close();
	}
	/**
	 * @Title: extractApi
	 * @Description: TODO(特征提取的进入方法)
	 */
	public void extractApi() throws SQLException, IOException{
		connectMysql();
		outValueCSV.clear();
		File file=new File("benign");
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
		file=new File("malware");
		files= file.listFiles();
		for(int i=0;i<files.length;i++){
			
			if(files[i].isDirectory()){
				csvLine.clear();
				csvLine.add(0);
				iterateFileDir(files[i].getPath());
				outValueCSV.add(new HashSet<Integer>(csvLine));
			}
		}	
		writeCsvFile();
		closeMysql();
	}
	/**
	 * @Title: storeNewApi
	 * @Description: TODO(mrmr算法生成了最大相关最小冗余的API,把这些API存入数据库)
	 * @param filePath
	 */
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
	/**
	 * @Title: mrmrAndStoreNewApi
	 * @Description: TODO(mrmr算法得出最大相关最小冗余的API)
	 * @param filePath 
	 * @return void    返回类型
	 * @throws
	 */
	public void mrmrAndStoreNewApi() {
		String filePath="test.csv";
		String command="cmd /c mrmr_win32.exe ./mrmr -i "+filePath+" -n 200 >output.mrmrout";
		Runtime runtime=Runtime.getRuntime();
		Process process;
		File dir=new File("").getAbsoluteFile();
		try {
			process=runtime.exec(command,null,dir);
			try {
				process.waitFor();
			} catch (InterruptedException e) {
				  
				// TODO Auto-generated catch block  
				e.printStackTrace();  
				
			}
		} catch (IOException e1) {
			  
			// TODO Auto-generated catch block  
			e1.printStackTrace();  
			
		}
		
		try {
			storeNewApi("output.mrmrout");
		} catch (IOException e) {
			  
			// TODO Auto-generated catch block  
			e.printStackTrace();  
			
		}		
	}
	public static void main(String[] args) throws SQLException, IOException  {
		// TODO Auto-generated method stub
		Extractor test=new Extractor(new MysqlInfo("127.0.0.1","3306","root","root1234","android"));
		test.Decompilation();
		test.extractApi();
		test.mrmrAndStoreNewApi();
	}

}
