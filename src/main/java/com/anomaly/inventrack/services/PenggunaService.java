package com.anomaly.inventrack.services;

import com.anomaly.inventrack.models.Gudang;
import com.anomaly.inventrack.models.Pengguna;
import com.anomaly.inventrack.repositories.PenggunaRepositories;
import com.anomaly.inventrack.repositories.GudangRepositories;

import java.util.Optional;

// Catatan: Anda perlu membuat utilitas ini sendiri untuk keamanan nyata
class PasswordUtil {
    public static String hash(String rawPassword) {
        // Hashing yang aman (misalnya BCrypt)
        return rawPassword + "_HASHED"; 
    }
    public static boolean verify(String rawPassword, String storedHash) {
        // Logika verifikasi hash (misalnya BCrypt.checkpw)
        return storedHash.equals(rawPassword + "_HASHED"); 
    }
}

public class PenggunaService {
    
    // Gunakan final untuk Dependency Injection yang sederhana
    private final PenggunaRepositories penggunaRepo;
    private final GudangRepositories gudangRepo;

    // Constructor untuk "Dependency Injection"
    public PenggunaService() {
        this.penggunaRepo = new PenggunaRepositories();
        this.gudangRepo = new GudangRepositories();
    }

    /**
     * Otentikasi pengguna berdasarkan username dan password.
     * @param username Username.
     * @param password Password (plain text).
     * @return Optional<Pengguna> yang berisi objek Pengguna jika otentikasi sukses.
     */
    public Optional<Pengguna> authenticate(String username, String password) {
        // Menggunakan Optional dari Repositories yang sudah diperbaiki
        Optional<Pengguna> optPengguna = penggunaRepo.findByUsername(username);

        if (optPengguna.isEmpty()) {
            return Optional.empty(); // Username tidak ditemukan
        }

        Pengguna pengguna = optPengguna.get();
        
        // 1. Verifikasi Password (Logika Bisnis Kritis)
        if (PasswordUtil.verify(password, pengguna.getPasswordHash())) {
            return Optional.of(pengguna); // Otentikasi Sukses
        } else {
            return Optional.empty(); // Password salah
        }
    }
    
    /**
     * Mendapatkan informasi Gudang tempat pengguna berafiliasi.
     * @param idGudang ID Gudang dari objek Pengguna.
     * @return Objek Gudang, atau null jika tidak ditemukan.
     */
    public Gudang getGudangByUserId(int idGudang) {
        // Panggil GudangRepositories
        Gudang gudang = gudangRepo.findById(idGudang); //
        
        // Catatan: Jika Anda menerapkan Optional pada GudangRepositories, 
        // Anda harus mengubah return type ini menjadi Optional<Gudang>
        return gudang;
    }
    
    /**
     * Registrasi pengguna baru.
     * @param newUser Objek Pengguna baru (Password harus dalam bentuk plain text di sini)
     * @param rawPassword Password dalam bentuk plain text
     * @return Pengguna yang sudah tersimpan (dengan ID dan passwordHash yang aman)
     * @throws RuntimeException Jika username sudah terdaftar
     */
    public Pengguna registerNewUser(Pengguna newUser, String rawPassword) {
        // Aturan Bisnis 1: Pastikan username belum ada
        if (penggunaRepo.findByUsername(newUser.getUsername()).isPresent()) {
            throw new RuntimeException("Username '" + newUser.getUsername() + "' sudah terdaftar.");
        }
        
        // Aturan Bisnis 2: Hash password sebelum disimpan
        String passwordHash = PasswordUtil.hash(rawPassword);
        newUser.setPasswordHash(passwordHash);
        
        // TODO: Anda perlu menambahkan method insert(Pengguna) di PenggunaRepositories
        // penggunaRepo.insert(newUser); 
        
        return newUser; 
    }
}