package com.anomaly.inventrack.services;

import com.anomaly.inventrack.models.*;
import com.anomaly.inventrack.repositories.*;
import com.anomaly.inventrack.utils.Database;
import com.anomaly.inventrack.services.exceptions.BusinessException; 
import com.anomaly.inventrack.services.exceptions.NotFoundException;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

public class PermintaanService {

    private final PermintaanRepositories permintaanRepo;
    private final DetailPermintaanRepositories detailPermintaanRepo;
    private final PenggunaRepositories penggunaRepo;
    private final InventoryService inventoryService; 

    public PermintaanService() {
        this.permintaanRepo = new PermintaanRepositories();
        this.detailPermintaanRepo = new DetailPermintaanRepositories();
        this.penggunaRepo = new PenggunaRepositories();
        this.inventoryService = new InventoryService(); 
    }
    
    // =========================================================
    // ============== 1. LOGIKA PEMBUATAN PERMINTAAN ===========
    // =========================================================

    /**
     * Membuat Permintaan Baru (Permintaan + Detail Permintaan) dalam satu transaksi.
     * @param permintaan Objek Permintaan (tanpa ID).
     * @param detailList Daftar DetailPermintaan yang menyertai.
     * @throws RuntimeException Jika terjadi kesalahan database (rollback dilakukan).
     */
    public Permintaan buatPermintaanBaru(Permintaan permintaan, List<DetailPermintaan> detailList) {
        // 1. Validasi Input Dasar
        if (permintaan.getIdGudangSumber() == null) {
            throw new BusinessException("Harap pilih Gudang Sumber (Tujuan Permintaan).");
        }

        // 2. Validasi Self-Request (Mencegah minta ke diri sendiri)
        // Ambil data peminta untuk tahu dia dari gudang mana
        Pengguna peminta = penggunaRepo.findById(permintaan.getIdPenggunaPeminta())
                .orElseThrow(() -> new NotFoundException("Data peminta tidak valid."));
        
        if (peminta.getIdGudang().equals(permintaan.getIdGudangSumber())) {
             throw new BusinessException("Tidak dapat membuat permintaan ke gudang sendiri!");
        }

        for (DetailPermintaan detail : detailList) {
            try {
                // Cek stok barang di gudang sumber
                Stok stokSumber = inventoryService.getStok(detail.getIdBarang(), permintaan.getIdGudangSumber());
                
                // Cek apakah jumlahnya cukup
                if (stokSumber.getJumlahStok() < detail.getJumlahDiminta()) {
                    throw new BusinessException("Stok tidak mencukupi di Gudang Sumber untuk Barang ID: " + detail.getIdBarang() + 
                                                ". Tersedia: " + stokSumber.getJumlahStok() + 
                                                ", Diminta: " + detail.getJumlahDiminta());
                }
            } catch (BusinessException e) {
                // Tangkap jika barang tidak terdaftar sama sekali di gudang sumber
                // (InventoryService.getStok melempar exception jika stok null/tidak ditemukan)
                throw new BusinessException("Barang ID " + detail.getIdBarang() + " tidak tersedia/tidak terdaftar di Gudang Sumber.");
            }
        }

        // 3. Setup Default
        permintaan.setStatusPermintaan(Permintaan.StatusPermintaan.MENUNGGU);
        permintaan.setTanggalPermintaan(LocalDateTime.now());

        Connection conn = null;
        try {
            conn = Database.getConnection();
            conn.setAutoCommit(false); 

            // 4. Simpan Permintaan (Repo insert sekarang menyimpan idGudangSumber)
            permintaanRepo.insert(conn, permintaan); 
            Integer idPermintaanBaru = permintaan.getIdPermintaan();
            
            for (DetailPermintaan detail : detailList) {
                detail.setIdPermintaan(idPermintaanBaru); 
                detail.setJumlahDisetujui(detail.getJumlahDiminta()); 
                detailPermintaanRepo.insert(conn, detail); 
            }

            conn.commit(); 
            return permintaan;

        } catch (SQLException e) {
            try {
                if (conn != null) conn.rollback(); // Rollback jika ada yang gagal
            } catch (SQLException rollbackEx) {
                System.err.println("Rollback gagal: " + rollbackEx.getMessage());
            }
            // Lempar RuntimeException untuk ditangani di lapisan atas
            throw new RuntimeException("Gagal membuat permintaan baru. Transaksi dibatalkan.", e); 
        } finally {
            try {
                if (conn != null) {
                    conn.setAutoCommit(true);
                    conn.close();
                }
            } catch (SQLException closeEx) {
                System.err.println("Gagal menutup koneksi: " + closeEx.getMessage());
            }
        }
    }
    
    // =========================================================
    // ============= 2. LOGIKA PERSETUJUAN PERMINTAAN (REVISI) ==========
    // =========================================================

    /**
     * Menyetujui Permintaan, MENGGUNAKAN InventoryService untuk mengurangi stok di gudang peminta.
     * @param idPermintaan ID Permintaan yang akan disetujui.
     * @param idAdminYangMenyetujui ID Pengguna yang menyetujui.
     * @throws BusinessException Jika stok tidak memadai di gudang peminta.
     * @throws RuntimeException Jika terjadi kegagalan sistem.
     */
    public void approvePermintaan(int idPermintaan, int idAdminYangMenyetujui) throws BusinessException {

        Connection conn = null;
        try {
            conn = Database.getConnection();
            conn.setAutoCommit(false); 

            // 1. Validasi Permintaan
            Permintaan permintaan = permintaanRepo.findById(idPermintaan)
                .orElseThrow(() -> new NotFoundException("Permintaan dengan ID " + idPermintaan + " tidak ditemukan."));

            if (permintaan.getStatusPermintaan() != Permintaan.StatusPermintaan.MENUNGGU) {
                throw new BusinessException("Permintaan sudah diproses (Status: " + permintaan.getStatusPermintaan() + ").");
            }

            // 2. Identifikasi Gudang Sumber (Gudang milik Admin)
            Pengguna adminPenyetuju = penggunaRepo.findById(idAdminYangMenyetujui)
                .orElseThrow(() -> new NotFoundException("Admin penyetuju tidak ditemukan."));
            
            int idGudangSumber = adminPenyetuju.getIdGudang(); // INI SUMBER STOK

            // 3. Proses Pengurangan Stok (Dari Gudang Sumber)
            List<DetailPermintaan> detailList = detailPermintaanRepo.findByPermintaan(idPermintaan);

            for (DetailPermintaan detail : detailList) {
                inventoryService.kurangiStok(
                    detail.getIdBarang(), 
                    idGudangSumber, // 🆕 Stok keluar dari Gudang Admin
                    detail.getJumlahDisetujui(), 
                    "Keluar untuk Permintaan ID: " + idPermintaan
                );
            }

            // 4. Update Status & Simpan Gudang Sumber
            // Menggunakan method khusus yang baru kita buat di Repository
            permintaanRepo.approveRequest(conn, idPermintaan, idGudangSumber); 

            conn.commit(); 
            
        } catch (BusinessException e) {
            // Rollback hanya diperlukan jika ada operasi yang dilakukan di PermintaanService (saat ini belum ada)
            // InventoryService sudah melakukan rollback jika terjadi BusinessException/SQLException
            try {
                if (conn != null) conn.rollback(); // Rollback PermintaanService jika ada operasi yang gagal
            } catch (SQLException rollbackEx) {
                System.err.println("Rollback gagal: " + rollbackEx.getMessage());
            }
            throw e; // Lempar exception Stok Tidak Cukup ke lapisan Controller
        } catch (SQLException e) {
            try {
                if (conn != null) conn.rollback();
            } catch (SQLException rollbackEx) {
                System.err.println("Rollback gagal: " + rollbackEx.getMessage());
            }
            throw new RuntimeException("Gagal menyetujui permintaan. Transaksi dibatalkan.", e); 
        } finally {
            try {
                if (conn != null) {
                    conn.setAutoCommit(true);
                    conn.close();
                }
            } catch (SQLException closeEx) {
                System.err.println("Gagal menutup koneksi: " + closeEx.getMessage());
            }
        }
    }
}