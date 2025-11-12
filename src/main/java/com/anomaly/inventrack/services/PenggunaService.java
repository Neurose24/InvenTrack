package com.anomaly.inventrack.services;

import com.anomaly.inventrack.models.Gudang;
import com.anomaly.inventrack.models.Pengguna;
import com.anomaly.inventrack.repositories.PenggunaRepositories;
import com.anomaly.inventrack.repositories.GudangRepositories;
import com.anomaly.inventrack.services.exceptions.BusinessException;
import com.anomaly.inventrack.services.exceptions.NotFoundException;
import com.anomaly.inventrack.utils.PasswordUtil;

import java.util.Optional;

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
            return Optional.empty(); 
        }

        Pengguna pengguna = optPengguna.get();
        
        // Sekarang ini memanggil BCrypt.checkpw()
        if (PasswordUtil.verify(password, pengguna.getPasswordHash())) {
            return Optional.of(pengguna); 
        } else {
            return Optional.empty(); 
        }
    }
    
    /**
     * Mendapatkan informasi Gudang tempat pengguna berafiliasi.
     */
    public Gudang getGudangByUserId(int idGudang) {
        return gudangRepo.findById(idGudang) // Ini sekarang mengembalikan Optional<Gudang>
            .orElseThrow(() -> new NotFoundException("Gudang afiliasi dengan ID " + idGudang + " tidak ditemukan."));
    }
    
    /**
     * Registrasi pengguna baru.
     */
    public Pengguna registerNewUser(Pengguna newUser, String rawPassword) {
        if (penggunaRepo.findByUsername(newUser.getUsername()).isPresent()) {
            throw new BusinessException("Username '" + newUser.getUsername() + "' sudah terdaftar.");
        }
        
        // Sekarang ini memanggil BCrypt.hashpw()
        String passwordHash = PasswordUtil.hash(rawPassword);
        newUser.setPasswordHash(passwordHash);
        
        penggunaRepo.saveUser(newUser); 
        
        return newUser; 
    }
}