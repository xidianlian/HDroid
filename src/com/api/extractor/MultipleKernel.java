  /**  
 * @Title:  MultipleKernel.java   
 * @Package com.api.extractor   
 * @Description:    TODO()   
 * @author: 练伟成
 * @date:   2018年6月26日 下午8:24:06   
 * @version V1.0  
 */
      
package com.api.extractor;

import java.io.File;
import java.io.IOException;

/**   
 * @ClassName:  MultipleKernel   
 * @Description:TODO(多核svm算法的调用)      
 */

public class MultipleKernel {
	
	private void cmd(String command,File dir) {
		
		Runtime runtime=Runtime.getRuntime();
		Process process;
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
	}
	
	public void smvTrain() {
		
		String command="cmd /c "
		+ "svm-train.exe -s 0 -h 0 -m 400 -o 2.0 -a 7 -c 10.0 -l 1.0 -f 0 -j 1 -g 3 -k "
		+ "PrecomputedKernels\\kernelfile "
		+ "PrecomputedKernels\\y_train "
		+ "PrecomputedKernels\\model_file "
		+ ">svm_train.output";
		File dir=new File("").getAbsoluteFile();
		cmd(command,dir);
	}
	public void smvPredict() {
		
		String command="cmd /c "
		+ "SVM_Predict.exe "
		+ "PrecomputedKernels\\y_test "
		+ "PrecomputedKernels\\model_file "
		+ "PrecomputedKernels\\prediction "
		+ ">svm_predict.output";
		File dir=new File("").getAbsoluteFile();
		cmd(command,dir);
	}
}
  
