package com.anomaly.inventrack.utils;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DBUtil {

    public static void closeQuietly(AutoCloseable ac) {
        if (ac != null) {
            try { ac.close(); } catch (Exception ignored) { }
        }
    }

    public static void beginTransaction(Connection conn) throws SQLException {
        conn.setAutoCommit(false);
    }

    public static void commit(Connection conn) throws SQLException {
        conn.commit();
        conn.setAutoCommit(true);
    }

    public static void rollback(Connection conn) {
        if (conn != null) {
            try { conn.rollback(); conn.setAutoCommit(true); } catch (SQLException ignored) { }
        }
    }
}