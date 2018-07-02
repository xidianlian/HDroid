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
	private double trainDataRatio=0.8;
	public MetaPath(double ratio) {
		this.trainDataRatio=ratio;
		//保存路径
		File file=new File("PrecomputedKernels");
		if(!file.exists()) {
			file.mkdirs();
		}
		file=new File("PrecomputedKernels\\kernelfile");
		if(file.exists())file.delete();
		try {
			file.createNewFile();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public MetaPath() {
		//保存路径
		File file=new File("PrecomputedKernels");
		if(!file.exists()) {
			file.mkdirs();
		}
		file=new File("PrecomputedKernels\\kernelfile");
		if(file.exists())file.delete();
		try {
			file.createNewFile();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
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
	private void outputKernelMatrixAndYFile(Matrix mat,String fileName,int benignApp,int totalApp) {
		
		String  filePath="PrecomputedKernels"+"\\"+fileName;
		//kernel文件
		File kernelfile=new File("PrecomputedKernels\\kernelfile");
		FileWriter kf;
		try {
			kf = new FileWriter(kernelfile,true);
			BufferedWriter bwkf=new BufferedWriter(kf);
			bwkf.write("-t 4 -f PrecomputedKernels/"+fileName+"\r\n");
			bwkf.close();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		//核矩阵文件
	    File kernelTrain=new File(filePath);
	    File kernelTest=new File(filePath+".test");
	    //y文件
	    File yTrain=new File("PrecomputedKernels\\y_train");
	    File yTest=new File("PrecomputedKernels\\y_test");
	    
	    kernelTrain.delete();
	    kernelTest.delete();
	    yTrain.delete();
	    yTest.delete();
	    
		try {
			kernelTrain.createNewFile();
			kernelTest.createNewFile();
			yTrain.createNewFile();
			yTest.createNewFile();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println("创建新核文件失败");
			e.printStackTrace();
		}

		try {
			//核文件
			FileWriter fwTrain = new FileWriter(kernelTrain,true);
			BufferedWriter bwTrain=new BufferedWriter(fwTrain);
			FileWriter fwTest = new FileWriter(kernelTest,true);
			BufferedWriter bwTest=new BufferedWriter(fwTest);
			//y文件
			FileWriter fwTrainY = new FileWriter(yTrain,true);
			BufferedWriter bwTrainY=new BufferedWriter(fwTrainY);
			FileWriter fwTestY = new FileWriter(yTest,true);
			BufferedWriter bwTestY=new BufferedWriter(fwTestY);
			int r=mat.getR();
			int c=mat.getC();
			if(r<=0||c<=0)
			{
				bwTestY.close();
				bwTrainY.close();
				bwTest.close();
				bwTrain.close();
				
				return;
			}
			int malwareApp=totalApp-benignApp;
			int trainBenign=(int)(benignApp*trainDataRatio);
			int trainMalware=(int)(malwareApp*trainDataRatio);
			int trainNumber=trainBenign+trainMalware;
			int testNumber=totalApp-trainNumber;
			bwTrain.write(""+trainNumber+" "+trainNumber+"\r\n");
			bwTest.write(""+testNumber+" "+trainNumber+"\r\n");
			int malwareCnt=0;
			for(int i=1;i<r;i++) {
				String str="";
				int flag=0;
				for(int j=1;j<c;j++) {
					if(j<=trainBenign||(j>benignApp&&j<=benignApp+trainMalware)) {
						if(flag!=0) {
							str+=" ";
						}
						str+=mat.getX()[i][j];
						flag=1;
					}
				}
				if(i<=benignApp) {
					if(i<=trainBenign) {
						bwTrain.write(str+"\r\n");
						bwTrainY.write("-1\r\n");
					}
					else {
						bwTest.write(str+"\r\n");
						bwTestY.write("-1\r\n");
					}
						
				}
				else {
					malwareCnt++;
					if(malwareCnt<=trainMalware) {
						bwTrain.write(str+"\r\n");
						bwTrainY.write("1\r\n");
					}
						
					else {
						bwTest.write(str+"\r\n");
						bwTestY.write("1\r\n");
					}
						
				}
			}
			bwTestY.close();
			bwTrainY.close();
			bwTest.close();
			bwTrain.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public void getCommutingMatrix(String metapath,int benignApp,int totalApp) {
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
		outputKernelMatrixAndYFile(cMatrix,kernelFileName,benignApp,totalApp);
	}

}
