/**  
 * Project Name:ApiExtractor  
 * File Name:Test.java  
 * Package Name:test  
 * Date:2018��6��26������7:56:05  
 * Copyright (c) 2018, chenzhou1025@126.com All Rights Reserved.  
 *  
*/  

/**  
 * @Title:  Test.java   
 * @Package test   
 * @Description:    TODO()   
 * @author: ��ΰ��
 * @date:   2018��6��26�� ����7:56:05   
 * @version V1.0  
 */
      
  
package test;  
/**  
 * ClassName:Test <br/>  
 * Function: TODO ADD FUNCTION. <br/>  
 * Reason:   TODO ADD REASON. <br/>  
 * Date:     2018��6��26�� ����7:56:05 <br/>  
 * @author   Lian  
 * @version    
 * @since    JDK 1.6  
 * @see        
 */

import java.io.IOException;
import java.sql.SQLException;

import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;

import com.api.extractor.Extractor;
import com.api.extractor.MetaPath;
import com.api.extractor.MultipleKernel;
import com.api.extractor.MysqlInfo;
import com.api.extractor.RelationMatrix;

public class Test {

	
	public static void main(String[] args) {
		//��ȡ����
//		Extractor extractor=new Extractor(new MysqlInfo("127.0.0.1","3306","root","root1234","android"));
//		extractor.Decompilation();
//		try {
//			extractor.extractApi();
//		} catch (SQLException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		extractor.mrmrAndStoreNewApi();
		
		//���ɹ�ϵ����
//		RelationMatrix rm=new RelationMatrix();
//		rm.getRelationMatFromSmali();
//		System.out.println("yes");
		//Ԫ·��
//		String[] str= {"AA", "ABA", "APA", "AIA", "ABPBA", "APBPA", "ABIBA", "AIBIA", "APIPA", "AIPIA",
//				"ABPIPBA","APBIPBA", "ABIPIBA", "AIBPBIA", "AIPBPIA", "APIBIPA"};
//		MetaPath mp=new MetaPath();
//		for(int i=0;i<str.length;i++) {
//			mp.getCommutingMatrix(str[i],15, 88);
//		}
		
		//���ѧϰ
		MultipleKernel mk=new MultipleKernel();
		mk.smvTrain();
		mk.smvPredict();
	}
}


  
