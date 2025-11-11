package com.anomaly.inventrack.services;

import com.anomaly.inventrack.models.Gudang;
import com.anomaly.inventrack.models.Pengguna;
import com.anomaly.inventrack.repositories.PenggunaRepositories;
import com.anomaly.inventrack.repositories.GudangRepositories;
import com.anomaly.inventrack.services.exceptions.BusinessException; // Menggunakan exception yang Anda sediakan

import java.util.Optional;

// Catatan: Anda perlu membuat utilitas ini sendiri untuk keamanan nyata
class PasswordUtil {
    public static String hash(String rawPassword) {
        // Placeholder: Ganti dengan implementasi BCrypt
        return rawPassword + "_HASHED"; 
    }
    public static boolean verify(String rawPassword, String storedHash) {
        // Placeholder: Ganti dengan implementasi BCrypt
        return storedHash.equals(rawPassword + "_HASHED"); 
    }
}

public class PenggunaService {
    
    private final PenggunaRepositories penggunaRepo;
    private final GudangRepositories gudangRepo;

    public PenggunaService() {
        this.penggunaRepo = new PenggunaRepositories();
        this.gudangRepo = new GudangRepositories();
    }

    /**
     * Otentikasi pengguna berdasarkan username dan password.
     */
    public Optional<Pengguna> authenticate(String username, String password) {
        Optional<Pengguna> optPengguna = penggunaRepo.findByUsername(username);

        if (optPengguna.isEmpty()) {
            return Optional.empty(); // Username tidak ditemukan
        }

        Pengguna pengguna = optPengguna.get();
        
        if (PasswordUtil.verify(password, pengguna.getPasswordHash())) {
            return Optional.of(pengguna); // Otentikasi Sukses
        } else {
            return Optional.empty(); // Password salah
        }
    }
    
    /**
     * Mendapatkan informasi Gudang tempat pengguna berafiliasi.
     */
    public Gudang getGudangByUserId(int idGudang) {
        // Panggil GudangRepositories
        Gudang gudang = gudangRepo.findById(idGudang);
        
        // (Kita akan memperbaiki inkonsistensi Optional vs null di langkah berikutnya)
        return gudang;
    }
    
    /**
     * Registrasi pengguna baru.
     */
    public Pengguna registerNewUser(Pengguna newUser, String rawPassword) {
        // Aturan Bisnis 1: Pastikan username belum ada
        if (penggunaRepo.findByUsername(newUser.getUsername()).isPresent()) {
            // Gunakan BusinessException yang sudah Anda buat
            throw new BusinessException("Username '" + newUser.getUsername() + "' sudah terdaftar.");
        }
        
        // Aturan Bisnis 2: Hash password sebelum disimpan
        String passwordHash = PasswordUtil.hash(rawPassword);
        newUser.setPasswordHash(passwordHash);
        
        // PERBAIKAN: Memanggil metode save yang baru dibuat
        penggunaRepo.saveUser(newUser); 
        
        return newUser; 
    }
}