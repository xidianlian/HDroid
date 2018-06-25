package com.api.extractor;

import java.io.File;
import java.io.IOException;

public class Decompilation {
	private String fileDir;
	public Decompilation(String str) 
	{
		fileDir=str;
	}
	public String getFileDir() {
		return fileDir;
	}
	public void setFileDir(String fileDir) {
		this.fileDir = fileDir;
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
	
}
