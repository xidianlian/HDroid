package com.api.extractor;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import csvreader.CsvWriter;
/**
 * @ClassName:  RelationMatrix   
 * @Description:TODO(����ÿ��apk�Ĺ�ϵ���󣬷ֱ���A��B��P��I����)   
 * @author: ��ΰ�� 
 * @date:   2018��6��26�� ����4:58:35
 */
public class RelationMatrix {
	private MysqlInfo sqlInfo;
	private Connection conn;
	private Statement stmt;
	private static final int FEATURE_SIZE=200; 
//	private int fileNum,benignNum; 
	private Byte[][] A_Matrix =new Byte[2][FEATURE_SIZE+1];  
	private Byte[][] B_Matrix =new Byte[FEATURE_SIZE+1][FEATURE_SIZE+1];
	private Byte[][] P_Matrix =new Byte[FEATURE_SIZE+1][FEATURE_SIZE+1];
	private Byte[][] I_Matrix =new Byte[FEATURE_SIZE+1][FEATURE_SIZE+1];
	private Map<String,Set<Integer>> pack=new HashMap<String,Set<Integer>>();
	private Map<String,Set<Integer>> invoke=new HashMap<String,Set<Integer>>();
//	public RelationMatrix(){
//		fileNum=0;
//		benignNum=0;
//	}
	
	private void initMatrix(){
		for(int j=0;j<=FEATURE_SIZE;j++) {
			A_Matrix[0][j]=0;
			A_Matrix[1][j]=0;
			B_Matrix[j][j]=0;
			P_Matrix[j][j]=0;
			I_Matrix[j][j]=0;
			for(int k=j+1;k<=FEATURE_SIZE;k++) {
				B_Matrix[j][k]=0;
				P_Matrix[j][k]=0;
				I_Matrix[j][k]=0;
				B_Matrix[k][j]=0;
				P_Matrix[k][j]=0;
				I_Matrix[k][j]=0;
			}
		}
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
	/**
	 * @Title: readFilesAndStore
	 * @Description: TODO(�ٴζ�ȡsmali���룬�˴�Ҫ����API֮��Ĺ�ϵ)
	 * @param filePath
	 * @return void    ��������
	 * @throws
	 */
	private void readFilesAndStore(String filePath) throws IOException, SQLException{
		BufferedReader br = new BufferedReader(new FileReader(filePath));
		boolean method=false;
		Set<Integer> api_set=new HashSet<Integer>();
        for (String line = br.readLine(); line != null; line = br.readLine()){
        	line=line.trim();
        	if(line.startsWith(".method")) {
        		method=true;
        		api_set.clear();
        		continue;
        	}
        	if(line.startsWith(".end method")) {
        		method=false;
        		//��B����
        		for(Integer value1:api_set) {
        			for(Integer value2:api_set) {
        				B_Matrix[value1][value2]=1;
        			}
        		}
        		continue;
        	}
        	if(!method)continue;//���ڷ�������
        	if(line.indexOf('<')!=-1)continue;
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
        		String sql1,sql2;
        		
        		sql1="select * from api where API='"+sub+"'";
        		ResultSet res1= stmt.executeQuery(sql1);
        		
        		if(res1.next()){
        			//��API�����ȡID,�ٸ���ID,��ѯ��api��mrmr_api�е�number��
        			int id=res1.getInt("ID");
        			sql2="select * from mrmr_api where api_id="+id;
        			ResultSet res2= stmt.executeQuery(sql2);
        			if(res2.next()){
        				int number=res2.getInt("number");
        				//A����
        				A_Matrix[1][number]=1;
        				//����B����
        				api_set.add(number);
        				//����P����
        				end=sub.indexOf(";");
        				String subsub=sub.substring(0,end);
        				Set<Integer> temp;
        				temp=pack.get(subsub);
        				if(temp==null)
        				{
        					temp=new HashSet<Integer>();
        				}
        				temp.add(number);
        				if(!subsub.equals(""))
        				pack.put(subsub,new HashSet<Integer>(temp));
        				//����I����
        				subsub="";
        				if(sub.startsWith("invoke-direct"))
        					subsub="invoke-direct";
        				else if(sub.startsWith("invoke-virtual"))
        					subsub="invoke-virtual";
        				else if(sub.startsWith("invoke-static"))
        					subsub="invoke-static";
        				else if(sub.startsWith("invoke-super"))
        					subsub="invoke-super";
        				else if(sub.startsWith("invoke-interface"))
        					subsub="invoke-interface";
        				if(!subsub.equals(""))
        				{
        					temp=invoke.get(subsub);
        					if(temp==null) {
        						temp=new HashSet<Integer>(); 
        					}
        					temp.add(number);
        					invoke.put(subsub, new HashSet<Integer>(temp));
        				}
        			}
        			res2.close();
        		}
        		res1.close();
        	}
        }
        br.close();
	}
	
	private void iterateFileDir(String dir){
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
	 * @Title: generateMatrix
	 * @Description: TODO(B��������֮ǰ��ȡsmali�ļ��Զ����ɣ�P,I����Ĺ�ϵҪ�������е�API)
	 * @param mp       apkӵ�е�APi���� ��һ��ӳ��
	 * @param which    �����ĸ�����P/I
	 * @return void    ��������
	 */
	private void generateMatrix(Map<String,Set<Integer>> mp,String which)
	{
		for(Set<Integer> set:mp.values()) {
			for(Integer value1:set) {
				for(Integer value2:set) {
					if(which.equals("P"))
					P_Matrix[value1][value2]=1;
					else if(which.equals("I"))
					I_Matrix[value1][value2]=1;
				}
			}
		}
	}
	/**
	 * @Title: outMatrixFile
	 * @Description: TODO(����������ļ�����ʽ�������)
	 * @param filePath
	 * @param matrix    ����
	 * @return void    ��������
	 */
	private void  outMatrixFile(String filePath,Byte[][] matrix)
	{
		 // ����CSVд����
		File file=new File(filePath);
		if(!file.exists())
		{
			try {
				file.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				System.out.println("������csv�ļ�ʧ��");
				e.printStackTrace();
			}
		}
        CsvWriter csvWriter = new CsvWriter(filePath,',', Charset.forName("GBK"));
        for(int i=1;i<matrix.length;i++){
        	String[] content=new String[FEATURE_SIZE]; 
        	
        	for(int j=1;j<matrix[i].length;j++)
        	{ 
        		
        		content[j-1]=matrix[i][j].toString();
        	}
        	try {
				csvWriter.writeRecord(content);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				csvWriter.close();
				e.printStackTrace();
			}
        }
       
        csvWriter.close();
	}
	/**
	 * @Title: outputYFile
	 * @Description: TODO(�����ǩ�ļ�)
	 * @param benignNum ����app����������app=����-benignNum
	 * @param fileNum    ����
	 * @return void    ��������
	 */
	private void outputYFile(int benignNum, int fileNum) {
		File file=new File("PrecomputedKernels");
		if(!file.exists()) {
			file.mkdirs();
		}
		String  filePath="PrecomputedKernels"+"//y_train";
	    file=new File(filePath);
	    file.delete();
		try {
			file.createNewFile();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println("�����º��ļ�ʧ��");
			e.printStackTrace();
		}
		FileWriter fw;
		try {
			fw = new FileWriter(file,true);
			BufferedWriter bw=new BufferedWriter(fw);
			for(int i=1;i<=fileNum;i++) {
				if(i<=benignNum) {
					bw.write("-1\r\n");
				}
				else {
					bw.write("1\r\n");
				}
			}
			bw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	public void getRelationMatFromSmali(){
		int fileNum=0;
		int benignNum=0;
		//�������ݿ�
		connectMysql();
		File file=new File("benign");
		File[] files= file.listFiles();
		for(int i=0;i<files.length;i++){
			if(files[i].isDirectory()){
				pack.clear();
				invoke.clear();
				initMatrix();
				iterateFileDir(files[i].getPath());
				generateMatrix(pack,"P");	
				generateMatrix(invoke,"I");
				fileNum++;
				file=new File("relationMatrix\\"+fileNum);
				if(!file.exists()){
					file.mkdirs();
				}
				outMatrixFile("relationMatrix\\"+fileNum+"\\A.csv",A_Matrix);
				outMatrixFile("relationMatrix\\"+fileNum+"\\B.csv",B_Matrix);
				outMatrixFile("relationMatrix\\"+fileNum+"\\P.csv",P_Matrix);
				outMatrixFile("relationMatrix\\"+fileNum+"\\I.csv",I_Matrix);
				
			}
			
		}
		benignNum=fileNum;
		
		file=new File("malware");
		files= file.listFiles();
		for(int i=0;i<files.length;i++){
			if(files[i].isDirectory()){
				pack.clear();
				invoke.clear();
				initMatrix();
				iterateFileDir(files[i].getPath());
				generateMatrix(pack,"P");
				generateMatrix(invoke,"I");
				fileNum++;
				file=new File("relationMatrix\\"+fileNum);
				if(!file.exists()){
					file.mkdirs();
				}
				outMatrixFile("relationMatrix\\"+fileNum+"\\A.csv",A_Matrix);
				outMatrixFile("relationMatrix\\"+fileNum+"\\B.csv",B_Matrix);
				outMatrixFile("relationMatrix\\"+fileNum+"\\P.csv",P_Matrix);
				outMatrixFile("relationMatrix\\"+fileNum+"\\I.csv",I_Matrix);
			}
		}	
		//�ر����ݿ�
		closeMysql();
		outputYFile(benignNum,fileNum);
	}
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		RelationMatrix rm=new RelationMatrix();
		rm.getRelationMatFromSmali();
	}

}
