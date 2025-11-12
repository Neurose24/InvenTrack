package com.anomaly.inventrack.utils;

// Impor library BCrypt yang baru saja Anda tambahkan
import org.mindrot.jbcrypt.BCrypt;

public class PasswordUtil {

    /**
     * Membuat hash password yang aman menggunakan BCrypt.
     * @param rawPassword Password teks biasa.
     * @return String hash yang aman.
     */
    public static String hash(String rawPassword) {
        // BCrypt.gensalt() secara otomatis menangani "salt"
        return BCrypt.hashpw(rawPassword, BCrypt.gensalt());
    }

    /**
     * Memverifikasi apakah password teks biasa cocok dengan hash yang tersimpan.
     * @param rawPassword Password teks biasa yang dimasukkan pengguna.
     * @param storedHash Hash yang disimpan di database.
     * @return true jika cocok, false jika tidak.
     */
    public static boolean verify(String rawPassword, String storedHash) {
        try {
            return BCrypt.checkpw(rawPassword, storedHash);
        } catch (Exception e) {
            // Tangani jika storedHash memiliki format yang tidak valid
            return false;
        }
    }
}