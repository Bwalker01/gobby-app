package com.byteryse.Database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import org.apache.commons.dbutils.QueryRunner;

public abstract class DataAccessObject {
	private final String url;
	private final String username;
	private final String password;
	final QueryRunner run;

	DataAccessObject(String url, String username, String password) {
		this.url = url;
		this.username = username;
		this.password = password;
		run = new QueryRunner();
	}

	DataAccessObject() {
		this.url = System.getenv("DB_URL");
		this.username = System.getenv("DB_USERNAME");
		this.password = System.getenv("DB_PASSWORD");
		run = new QueryRunner();
	}

	Connection connect() {
		try {
			return DriverManager.getConnection(getUrl(), getUsername(), getPassword());
		} catch (SQLException e) {
			System.err.println(e.getStackTrace());
			return null;
		}
	}

	void handleException(Exception e) {
		if (!e.getMessage().equals("No results were returned by the query.")) {
			e.printStackTrace();
		}
	}

	public String getPassword() {
		return password;
	}

	public String getUrl() {
		return url;
	}

	public String getUsername() {
		return username;
	}
}
