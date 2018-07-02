package com.api.extractor;

public class MysqlInfo {
	public String local;
	public String port;
	public String username;
	public String password;
	public String database;
	public MysqlInfo(String local,String port,String username,String psw,String database)
	{ 
		this.local=local;
		this.port=port;
		this.username=username;
		this.password=psw;
		this.database=database;
	}
	
}
