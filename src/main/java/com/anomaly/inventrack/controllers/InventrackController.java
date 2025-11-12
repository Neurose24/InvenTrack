package com.anomaly.inventrack.controllers;

import com.anomaly.inventrack.models.*;
import com.anomaly.inventrack.services.*;
import com.anomaly.inventrack.services.exceptions.BusinessException; // Exception yang tersedia
import com.anomaly.inventrack.repositories.PenggunaRepositories;

import java.util.List;
import java.util.Optional;

/**
 * Controller Layer: Menerima permintaan dari UI/API dan mendelegasikannya ke Service Layer.
 */
public class InventrackController {

    // Inisialisasi Service yang tersedia
    private final PenggunaService penggunaService; 
    private final PermintaanService permintaanService;
    private final PengirimanService pengirimanService;
    private final InventoryService inventoryService;
    private final PenggunaRepositories penggunaRepo;

    public InventrackController() {
        this.penggunaService = new PenggunaService();
        this.permintaanService = new PermintaanService();
        this.pengirimanService = new PengirimanService();
        this.inventoryService = new InventoryService();
        this.penggunaRepo = new PenggunaRepositories();
    }

    // =========================================================
    // ============== FUNGSI OTENTIKASI & REGISTRASI ============
    // =========================================================

    public Optional<Pengguna> login(String username, String password) {
        try {
            return penggunaService.authenticate(username, password);
        } catch (BusinessException e) {
            System.err.println("Gagal Login: " + e.getMessage());
            return Optional.empty();
        } catch (Exception e) {
            System.err.println("Kesalahan Sistem saat Login: " + e.getMessage());
            return Optional.empty();
        }
    }

    public String registerPengguna(String namaPengguna, String username, String rawPassword, Integer idGudang) {
        try {
            Pengguna penggunaBaru = new Pengguna();
            penggunaBaru.setNamaPengguna(namaPengguna);
            penggunaBaru.setUsername(username);
            penggunaBaru.setIdGudang(idGudang);

            penggunaService.registerNewUser(penggunaBaru, rawPassword);
            
            return "SUKSES: Pengguna '" + username + "' berhasil didaftarkan.";
        } catch (BusinessException e) {
            return "ERROR BISNIS: Gagal registrasi. " + e.getMessage();
        } catch (Exception e) {
            return "ERROR SISTEM: Gagal registrasi. " + e.getMessage();
        }
    }

    // =========================================================
    // ============== FUNGSI ALUR UTAMA =================
    // =========================================================

    public String buatPermintaanBaru(Permintaan permintaan, List<DetailPermintaan> detailList) {
        try {
            // Asumsi metode di PermintaanService bernama createPermintaan
            // (sebelumnya adalah buatPermintaanBaru)
            permintaanService.buatPermintaanBaru(permintaan, detailList); 
            return "SUKSES: Permintaan barang berhasil dibuat dengan ID: " + permintaan.getIdPermintaan();
        } catch (BusinessException e) {
            return "ERROR BISNIS: " + e.getMessage();
        } catch (Exception e) {
            return "ERROR SISTEM: Gagal memproses permintaan. " + e.getMessage();
        }
    }

    public String terimaPengiriman(int idPengiriman, List<DetailPengiriman> detailPenerimaanList) {
        try {
            pengirimanService.receivePengiriman(idPengiriman, detailPenerimaanList);
            return "SUKSES: Penerimaan pengiriman ID " + idPengiriman + " berhasil dicatat dan stok telah ditambahkan.";
        } catch (BusinessException e) {
            return "ERROR BISNIS (Penerimaan): " + e.getMessage();
        } catch (RuntimeException e) {
            return "ERROR TRANSAKSI: Penerimaan gagal. " + e.getMessage();
        }
    }
    
    // =========================================================
    // =============== FUNGSI LAPORAN (DIPERBAIKI) =============
    // =========================================================
    
    /**
     * Endpoint: Mendapatkan Laporan Stok per Gudang (Data mentah).
     */
    public List<Stok> getLaporanStok() {
        try {
            // Panggilan ini sudah benar
            return inventoryService.getAllStok();
        } catch (BusinessException e) {
            System.err.println("ERROR BISNIS LAPORAN STOK: " + e.getMessage());
            return List.of(); 
        }
    }
    
    /**
     * Endpoint: Mendapatkan Laporan Log Stok Detail (Perlu LogStokDetail.java dan findDetailAll()).
     */
    public List<LogStokDetail> getLaporanLogStokDetail() {
        try {
            // PERBAIKAN: Memanggil service, bukan repository
            return inventoryService.getLogStokDetails(); 
        } catch (RuntimeException e) {
            System.err.println("ERROR DATABASE: Gagal memuat log stok detail. " + e.getMessage());
            return List.of(); 
        }
    }
    
    // =========================================================
    // ================ SIMULASI PENGGUNAAN BARU ====================
    // =========================================================

    public static void main(String[] args) {
        InventrackController controller = new InventrackController();
        
        System.out.println("--- SIMULASI REGISTRASI PENGGUNA ---");
        String regStatus = controller.registerPengguna("Budi Santoso", "budi.gudangA", "rahasia123", 1);
        System.out.println("Status Registrasi: " + regStatus);

        System.out.println("\n--- SIMULASI LOGIN ---");
        Optional<Pengguna> user = controller.login("budi.gudangA", "rahasia123");
        if (user.isPresent()) {
            System.out.println("Login Sukses! Selamat datang, " + user.get().getNamaPengguna());
        } else {
            System.out.println("Login Gagal. Cek pesan error di atas.");
        }
    }
}