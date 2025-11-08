package com.anomaly.inventrack.services;

import com.anomaly.inventrack.models.*;
import com.anomaly.inventrack.repositories.*;

import java.util.List;
import java.util.Optional;

// Catatan: Anda dapat membuat kelas pengecualian bisnis (misalnya DataDuplikatException, DataTidakDitemukanException)
// untuk penanganan kesalahan yang lebih spesifik.

public class MasterDataService {

    private final BarangRepositories barangRepo;
    private final GudangRepositories gudangRepo;
    private final SupirRepositories supirRepo;
    // PenggunaRepo hanya digunakan untuk READ (get all/get by id)
    private final PenggunaRepositories penggunaRepo; 

    public MasterDataService() {
        this.barangRepo = new BarangRepositories();
        this.gudangRepo = new GudangRepositories();
        this.supirRepo = new SupirRepositories();
        this.penggunaRepo = new PenggunaRepositories();
    }
    
    // =========================================================
    // ==================== BARANG (ITEM) CRUD =================
    // =========================================================

    /**
     * Menyimpan Barang baru setelah melakukan validasi.
     * @param barang Objek Barang yang akan disimpan.
     * @return Barang yang sudah tersimpan (dengan ID terisi).
     * @throws RuntimeException jika validasi gagal atau SQL error.
     */
    public Barang createBarang(Barang barang) {
        // Validasi Bisnis: Nama Barang tidak boleh kosong
        if (barang.getNamaBarang() == null || barang.getNamaBarang().trim().isEmpty()) {
            throw new IllegalArgumentException("Nama Barang wajib diisi.");
        }
        
        // Cek duplikasi (Asumsi: Ada method findByNama di BarangRepositories)
        // Optional<Barang> existing = barangRepo.findByNama(barang.getNamaBarang());
        // if (existing.isPresent()) {
        //     throw new RuntimeException("Nama Barang sudah terdaftar.");
        // }

        // Simpan ke database
        // Catatan: Membutuhkan method insert(Barang) di BarangRepositories
        // barangRepo.insert(barang); //
        return barang;
    }

    public List<Barang> getAllBarang() {
        return barangRepo.findAll(); //
    }
    
    public Barang getBarangById(int idBarang) {
        // Menggunakan Optional (Asumsi: BarangRepositories sudah diperbaiki)
        return barangRepo.findById(idBarang) //
                   .orElseThrow(() -> new RuntimeException("Barang dengan ID " + idBarang + " tidak ditemukan."));
    }

    // =========================================================
    // ==================== GUDANG (WAREHOUSE) CRUD ==============
    // =========================================================

    public Gudang createGudang(Gudang gudang) {
        if (gudang.getNamaGudang() == null || gudang.getNamaGudang().trim().isEmpty()) {
            throw new IllegalArgumentException("Nama Gudang wajib diisi.");
        }
        
        // Simpan ke database
        // gudangRepo.insert(gudang); // Membutuhkan method insert(Gudang) di GudangRepositories
        return gudang;
    }
    
    public List<Gudang> getAllGudang() {
        return gudangRepo.findAll(); //
    }

    public Gudang updateGudang(Gudang gudang) {
        if (gudang.getIdGudang() == null) {
            throw new IllegalArgumentException("ID Gudang wajib diisi untuk operasi update.");
        }
        // Cek keberadaan data sebelum update
        getGudangById(gudang.getIdGudang()); 

        // gudangRepo.update(gudang); // Membutuhkan method update(Gudang) di GudangRepositories
        return gudang;
    }

    public Gudang getGudangById(int idGudang) {
        // Asumsi: findById di GudangRepositories sudah diubah ke Optional
        // Jika findById GudangRepositories masih mengembalikan Gudang (atau null)
        /*
        Gudang g = gudangRepo.findById(idGudang); //
        if (g == null) {
            throw new RuntimeException("Gudang dengan ID " + idGudang + " tidak ditemukan.");
        }
        return g;
        */
        // Menggunakan Optional (Jika sudah diperbaiki):
         return Optional.ofNullable(gudangRepo.findById(idGudang))
                .orElseThrow(() -> new RuntimeException("Gudang dengan ID " + idGudang + " tidak ditemukan."));
    }
    
    // =========================================================
    // ==================== SUPIR (DRIVER) CRUD ================
    // =========================================================

    public Supir createSupir(Supir supir) {
        if (supir.getNamaSupir() == null || supir.getNamaSupir().trim().isEmpty()) {
            throw new IllegalArgumentException("Nama Supir wajib diisi.");
        }
        
        // Cek duplikasi No Kendaraan
        // Optional<Supir> existing = supirRepo.findByNoKendaraan(supir.getNoKendaraan());
        // if (existing.isPresent()) {
        //     throw new RuntimeException("Nomor Kendaraan sudah terdaftar.");
        // }

        // supirRepo.insert(supir); // Membutuhkan method insert(Supir) di SupirRepositories
        return supir;
    }

    public List<Supir> getAllSupir() {
        return supirRepo.findAll(); //
    }

    public boolean deleteSupir(int idSupir) {
        // Validasi: Cek apakah Supir sedang terikat dengan Pengiriman yang aktif
        // Jika ya: throw new RuntimeException("Supir tidak dapat dihapus karena masih memiliki Pengiriman aktif.");
        
        // return supirRepo.delete(idSupir); // Membutuhkan method delete(int) di SupirRepositories
        return true; // Placeholder
    }
    
    // =========================================================
    // ==================== PENGGUNA (USER) READ ================
    // =========================================================

    public List<Pengguna> getAllPengguna() {
        // Hanya Read, karena operasi Create/Update Pengguna sebaiknya berada di PenggunaService 
        // untuk mengurus hashing password dan otorisasi.
        return penggunaRepo.findAll(); //
    }
}