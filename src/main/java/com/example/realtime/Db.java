package com.example.realtime;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class Db {
    static public Connection initialize() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/javap", "root", "");
            return conn;
        } catch (Exception e) {
            System.out.println("Failed to connect to the database");
            e.printStackTrace();
            return null;
        }
    }
}
