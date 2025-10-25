package com.anomaly.inventrack.utils;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;

public class Database {
    private static final HikariDataSource ds;

    static {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(Config.get("db.url"));
        config.setUsername(Config.get("db.user"));
        config.setPassword(Config.get("db.password"));
        config.setMaximumPoolSize(Config.getInt("db.pool.maximumPoolSize", 10));
        config.setConnectionTimeout(Long.parseLong(Config.get("db.pool.connectionTimeout") == null ? "30000" : Config.get("db.pool.connectionTimeout")));
        config.setPoolName("InventrackPool");

        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

        ds = new HikariDataSource(config);
    }

    private Database() {
    }

    public static Connection getConnection() throws SQLException {
        return ds.getConnection();
    }

    public static void shutdown() {
        if (ds != null && !ds.isClosed())
            ds.close();
    }
}