package com.byteryse.Database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class DatabaseController {
    private final Connection connection;
    private final DBConfig db;

    public DatabaseController() {
        this.db = new DBConfig();
        this.connection = connect();
    }

    private Connection connect() {
        try {
            return DriverManager.getConnection(db.getUrl(), db.getUsername(), db.getPassword());
        } catch (SQLException e) {
            System.err.println(e.getMessage());
            System.err.println(e.getStackTrace());
            return null;
        }
    }

    public ArrayList<ArrayList<String>> executeSQL(String sql, String... params) {
        try {
            PreparedStatement statement = this.connection.prepareStatement(sql);
            int i = 1;
            for (String param : params) {
                statement.setString(i, param);
                i++;
            }
            ResultSet resultSet = statement.executeQuery();
            int columnCount = resultSet.getMetaData().getColumnCount();
            ArrayList<ArrayList<String>> results = new ArrayList<>();
            while (resultSet.next()) {
                int x = 1;
                ArrayList<String> row = new ArrayList<>();
                while (x <= columnCount) {
                    row.add(resultSet.getString(x));
                    x++;
                }
                results.add(row);
            }
            return results;
        } catch (SQLException e) {
            System.err.println(e.getMessage());
            System.err.println(e.getStackTrace());
            return null;
        }
    }
}
