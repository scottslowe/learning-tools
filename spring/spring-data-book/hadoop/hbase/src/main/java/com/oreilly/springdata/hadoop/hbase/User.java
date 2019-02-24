package com.oreilly.springdata.hadoop.hbase;

public class User {

	private String name;
	private String email;
	private String password;
	
	public User(String name, String email, String password) {
		super();
		this.name = name;
		this.email = email;
		this.password = password;
	}
	
	public String getName() {
		return name;
	}

	public String getEmail() {
		return email;
	}

	public String getPassword() {
		return password;
	}

	@Override
	public String toString() {
		return "User [name=" + name + ", email=" + email + ", password="
				+ password + "]";
	}
	
	
}
