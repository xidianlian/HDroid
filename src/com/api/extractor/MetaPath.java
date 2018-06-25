package com.api.extractor;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import csvreader.CsvReader;

public class MetaPath {
	//private static final int MAX_APP_NUM=1000;
	private static final int FEATURE_SIZE=200;
	private Matrix cMatrix;
	
	private int[][] getMatrixFromCsv(int appID,String which) {
		int[][] matrix;
		if(which.equals("A")) {
			matrix = new int[2][FEATURE_SIZE+1];
		}
		else {
			matrix = new int[FEATURE_SIZE+1][FEATURE_SIZE+1];
		}
		int lineId=1;
		String filePath="relationMatrix//"+appID+"//"+which+".csv";
		try {
			CsvReader csvReader = new CsvReader(filePath);
			while (csvReader.readRecord()){
			    // 读一整行
				String line=csvReader.getRawRecord();
				String[] str=line.split(",");
				for(int i=0;i<str.length;i++) {
					matrix[lineId][i+1]=str[i].charAt(0)-'0';
				}
				lineId++;
			}
			csvReader.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return matrix;
	}
	
	private String getKernelFileName(String metapath) {
		String[] str= {"AA", "ABA", "APA", "AIA", "ABPBA", "APBPA", "ABIBA", "AIBIA", "APIPA", "AIPIA",
				"ABPIPBA","APBIPBA", "ABIPIBA", "AIBPBIA", "AIPBPIA", "APIBIPA"};
		String res="kernel_";
		for(int i=0;i<str.length;i++) {
			if(metapath.equals(str[i])) {
				res+=i;
				break;
			}
		}
		return res;
	}
	public void outputMatrix(Matrix mat,String fileName) {
		//保存路径
		File file=new File("PrecomputedKernels");
		if(!file.exists()) {
			file.mkdirs();
		}
		String  filePath="PrecomputedKernels"+"//"+fileName;
	    file=new File(filePath);
	    file.delete();
		try {
			file.createNewFile();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println("创建新核文件失败");
			e.printStackTrace();
		}

		try {
			FileWriter fw = new FileWriter(file,true);
			BufferedWriter bw=new BufferedWriter(fw);
			int r=mat.getR();
			int c=mat.getC();
			if(r<=0||c<=0)
			{
				bw.close();
				return;
			}
			bw.write(""+(r-1)+" "+(c-1)+"\r\n");
			for(int i=1;i<r;i++) {
				String str="";
				for(int j=1;j<c;j++) {
					str+=mat.getX()[i][j];
					if(j!=c-1) {
						str+=" ";
					}
				}
				bw.write(str+"\r\n");
			}
			bw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public void getCommutingMatrix(String metapath) {
		File file=new File("relationMatrix");
		String[] files=file.list();
		int AppNumber=files.length;
		cMatrix=new Matrix( new int[AppNumber+1][AppNumber+1]);
		//矩阵行数和列数从1开始
		Matrix A1=new Matrix(2,FEATURE_SIZE+1);
		Matrix A2=new Matrix(2,FEATURE_SIZE+1);
		Matrix B1,B2,I1,I2,P1,P2;
		for(int i=1;i<=AppNumber;i++){
			for(int j=i;j<=AppNumber;j++){
				for(int k=0;k<=metapath.length()/2;k++) {
					if(k%2==0&&k==metapath.length()/2)
						break;
					switch(metapath.charAt(k)){
					case 'A':
						A1=new Matrix(getMatrixFromCsv(i, "A"));
						A2=new Matrix(getMatrixFromCsv(j, "A"));
						break;
					case 'B':
						B1=new Matrix(getMatrixFromCsv(i, "B"));
						B2=new Matrix(getMatrixFromCsv(j, "B"));
						if(k==metapath.length()/2) {
							//最中间的矩阵，先用两个矩阵相与，再计算到A1上
							B1=B1.andMatrix(B2);
							A1=A1.mulMatrix(B1);
						}
						else {
							A1=A1.mulMatrix(B1);
							A2=A2.mulMatrix(B2);
						}
						break;
					case 'P':
						P1=new Matrix(getMatrixFromCsv(i, "P"));
						P2=new Matrix(getMatrixFromCsv(j, "P"));
						if(k==metapath.length()/2) {
							P1=P1.andMatrix(P2);
							A1=A1.mulMatrix(P1);
						}
						else {
							A1=A1.mulMatrix(P1);
							A2=A2.mulMatrix(P2);
						}
						
						break;
					case 'I':
						I1=new Matrix(getMatrixFromCsv(i, "I"));
						I2=new Matrix(getMatrixFromCsv(j, "I"));
						if(k==metapath.length()/2) {
							I1=I1.andMatrix(I2);
							A1=A1.mulMatrix(I1);
						}
						else {
							A1=A1.mulMatrix(I1);
							A2=A2.mulMatrix(I2);
						}
						break;
					default:
						System.out.println("metapath.charAt() 无此字符");
					}
					
				}
				cMatrix.setX(i,j,A1.mulMatrix(A2.T()).getX()[1][1] );
				cMatrix.setX(j,i,A1.mulMatrix(A2.T()).getX()[1][1] );
			}
		}
		String kernelFileName=getKernelFileName(metapath);
		outputMatrix(cMatrix,kernelFileName);
	}
	
	public static void main(String[] args) {
		new MetaPath().getCommutingMatrix("AA");
//		for(int i=0;i<res.getR();i++) {
//			for(int j=0;j<res.getC();j++) {
//				System.out.print(res.getX()[i][j]+" ");
//			}
//			System.out.println();
//		}
		
	}

}
