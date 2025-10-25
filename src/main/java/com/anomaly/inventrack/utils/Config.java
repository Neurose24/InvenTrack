package com.anomaly.inventrack.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Config {
    private static final Properties props = new Properties();

    static {
        try (InputStream in = Config.class.getClassLoader().getResourceAsStream("db.properties")) {
            if (in != null) {
                props.load(in);
            } else {
                throw new RuntimeException("db.properties not found in classpath. Copy db.properties.example -> db.properties");
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed load db.properties: " + e.getMessage(), e);
        }
    }

    public static String get(String key) {
        return props.getProperty(key);
    }

    public static int getInt(String key, int defaultValue) {
        try {
            String v = props.getProperty(key);
            return v == null ? defaultValue : Integer.parseInt(v);
        } catch (NumberFormatException ex) {
            return defaultValue;
        }
    }
}