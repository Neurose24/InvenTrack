package com.anomaly.inventrack.services;

import com.anomaly.inventrack.models.*;
import com.anomaly.inventrack.repositories.*;
import com.anomaly.inventrack.utils.Database;
import com.anomaly.inventrack.services.exceptions.BusinessException; 

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class PermintaanService {

    private final PermintaanRepositories permintaanRepo;
    private final DetailPermintaanRepositories detailPermintaanRepo;
    private final StokRepositories stokRepo;
    private final LogStokRepositories logStokRepo;
    private final PenggunaRepositories penggunaRepo;
    
    // INTEGRASI: Tambahkan InventoryService
    private final InventoryService inventoryService; 

    public PermintaanService() {
        this.permintaanRepo = new PermintaanRepositories();
        this.detailPermintaanRepo = new DetailPermintaanRepositories();
        this.stokRepo = new StokRepositories();
        this.logStokRepo = new LogStokRepositories();
        this.penggunaRepo = new PenggunaRepositories();
        this.inventoryService = new InventoryService(); // Inisialisasi InventoryService
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
        // Aturan Bisnis: Atur status dan tanggal awal
        permintaan.setStatusPermintaan(Permintaan.StatusPermintaan.MENUNGGU); //
        permintaan.setTanggalPermintaan(LocalDateTime.now());

        Connection conn = null;
        try {
            conn = Database.getConnection();
            conn.setAutoCommit(false); // Mulai transaksi

            // 1. Simpan Permintaan Induk
            // Method insert di Repo Anda menerima Connection dan mengupdate ID
            permintaanRepo.insert(conn, permintaan); 
            Integer idPermintaanBaru = permintaan.getIdPermintaan();
            
            // 2. Simpan Detail Permintaan
            for (DetailPermintaan detail : detailList) {
                detail.setIdPermintaan(idPermintaanBaru); // Kaitkan ID
                // Aturan Bisnis: Awalnya jumlah disetujui sama dengan jumlah diminta
                detail.setJumlahDisetujui(detail.getJumlahDiminta()); 
                
                detailPermintaanRepo.insert(conn, detail); 
            }

            conn.commit(); // Komit transaksi
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
            conn.setAutoCommit(false); // Mulai transaksi untuk operasi Permintaan/DetailPermintaan

            // 1. Ambil data Permintaan dan Detail
            // Asumsi findById di PermintaanRepositories sudah mengembalikan Optional
            Optional<Permintaan> optPermintaan = Optional.ofNullable(permintaanRepo.findById(idPermintaan)); 
            if (optPermintaan.isEmpty()) {
                throw new RuntimeException("Permintaan tidak ditemukan.");
            }
            Permintaan permintaan = optPermintaan.get();

            // 2. Cek Status (Aturan Bisnis)
            if (permintaan.getStatusPermintaan() != Permintaan.StatusPermintaan.MENUNGGU) {
                throw new RuntimeException("Permintaan sudah diproses (Status: " + permintaan.getStatusPermintaan() + ").");
            }

            // 3. Tentukan Gudang Peminta/Asal
            Optional<Pengguna> optPeminta = penggunaRepo.findById(permintaan.getIdPenggunaPeminta()); //
            if (optPeminta.isEmpty()) {
                throw new RuntimeException("Pengguna peminta tidak ditemukan.");
            }
            int idGudangAsal = optPeminta.get().getIdGudang(); // Gudang yang stoknya akan berkurang

            List<DetailPermintaan> detailList = detailPermintaanRepo.findByPermintaan(idPermintaan); //

            // 4. Proses Pengurangan Stok (Delegasi ke InventoryService)
            for (DetailPermintaan detail : detailList) {
                int idBarang = detail.getIdBarang();
                int jumlahDisetujui = detail.getJumlahDisetujui();

                // Panggil Service lain untuk mengurangi stok
                // InventoryService akan mengurus: 
                // a) Cek stok
                // b) Update tabel stok (transaksional)
                // c) Insert log stok (transaksional)
                inventoryService.kurangiStok(
                    idBarang, 
                    idGudangAsal, 
                    jumlahDisetujui, 
                    "Keluar karena Permintaan ID: " + idPermintaan
                );
            }

            // 5. Update Status Permintaan menjadi DISETUJUI
            // Ini adalah operasi database terakhir dalam transaksi PermintaanService
            permintaanRepo.updateStatus(conn, idPermintaan, Permintaan.StatusPermintaan.DISETUJUI); 

            conn.commit(); // Komit transaksi PermintaanService (hanya update status)
            
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