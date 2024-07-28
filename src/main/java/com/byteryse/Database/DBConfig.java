package com.byteryse.Database;

public class DBConfig {
    private final String url;
    private final String username;
    private final String password;

    public DBConfig(String url, String username, String password) {
        this.url = url;
        this.username = username;
        this.password = password;
    }

    public DBConfig() {
        this.url = System.getenv("DB_URL");
        this.username = System.getenv("DB_USERNAME");
        this.password = System.getenv("DB_PASSWORD");
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
