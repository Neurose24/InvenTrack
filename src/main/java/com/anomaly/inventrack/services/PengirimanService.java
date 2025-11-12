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

// Mengintegrasikan InventoryService
public class PengirimanService {

    private final PengirimanRepositories pengirimanRepo;
    private final DetailPengirimanRepositories detailPengirimanRepo;
    private final PermintaanRepositories permintaanRepo;
    private final PenggunaRepositories penggunaRepo;
    private final InventoryService inventoryService; 

    public PengirimanService() {
        this.pengirimanRepo = new PengirimanRepositories();
        this.detailPengirimanRepo = new DetailPengirimanRepositories();
        this.permintaanRepo = new PermintaanRepositories();
        this.penggunaRepo = new PenggunaRepositories();
        this.inventoryService = new InventoryService();
    }
    
    // ... (Metode createPengirimanFromPermintaan tetap sama) ...

    // =========================================================
    // =============== 2. LOGIKA PENERIMAAN BARANG (REVISI) =============
    // =========================================================

    /**
     * Mencatat penerimaan barang di gudang tujuan, MENGGUNAKAN InventoryService untuk menambah stok.
     * @param idPengiriman ID Pengiriman yang diterima.
     * @param detailPenerimaanList Daftar DetailPengiriman yang diperbarui (dengan jumlahDiterima, statusPenerimaan, catatan).
     */
    public void receivePengiriman(int idPengiriman, List<DetailPengiriman> detailPenerimaanList) {
        
        Connection conn = null;
        try {
            conn = Database.getConnection();
            conn.setAutoCommit(false); 

            // 1. Validasi Pengiriman
            Pengiriman pengiriman = pengirimanRepo.findById(idPengiriman)
                .orElseThrow(() -> new RuntimeException("Pengiriman tidak valid atau belum dikirim."));
            
            if (pengiriman.getStatusPengiriman() != Pengiriman.StatusPengiriman.DIKIRIM) {
                throw new RuntimeException("Pengiriman sudah diproses (Status: " + pengiriman.getStatusPengiriman() + ").");
            }
            
            // 2. Tentukan Gudang Tujuan
            Pengguna optPenerima = penggunaRepo.findById(pengiriman.getIdPenggunaPenerima())
                .orElseThrow(() -> new RuntimeException("Pengguna penerima tidak ditemukan."));
            
            int idGudangTujuan = optPenerima.getIdGudang();

            boolean isSemuaDiterima = true; // Tetap simpan ini untuk referensi di masa depan

            // 3. Proses Stok dan Update Detail Penerimaan
            for (DetailPengiriman detail : detailPenerimaanList) {
                
                detailPengirimanRepo.updatePenerimaan(
                    conn, 
                    detail.getIdDetailPengiriman(), 
                    detail.getJumlahDiterima(), 
                    detail.getStatusPenerimaan(), 
                    detail.getCatatanPenerimaan()
                ); 

                if (detail.getStatusPenerimaan() != DetailPengiriman.StatusPenerimaan.DITERIMA) {
                    isSemuaDiterima = false;
                }
                
                if (detail.getJumlahDiterima() > 0) {
                    inventoryService.tambahStok(
                        detail.getIdBarang(), 
                        idGudangTujuan, 
                        detail.getJumlahDiterima(), 
                        "Masuk dari Pengiriman ID: " + idPengiriman
                    );
                }
            }

            // 4. Update Status Pengiriman Induk
            pengirimanRepo.updateStatus(conn, idPengiriman, Pengiriman.StatusPengiriman.DITERIMA);

            // (Catatan: Jika nanti Anda ingin status DITERIMA_SEBAGIAN, 
            // Anda harus mengubah model Pengiriman.java dan mengembalikan logika if-else)
            
            // 5. Update Status Permintaan
            permintaanRepo.updateStatus(conn, pengiriman.getIdPermintaan(), Permintaan.StatusPermintaan.SELESAI);

            conn.commit();
            
        } catch (SQLException e) {
            try {
                if (conn != null) conn.rollback(); 
            } catch (SQLException rollbackEx) {
                System.err.println("Rollback gagal: " + rollbackEx.getMessage());
            }
            throw new BusinessException("Gagal mencatat penerimaan. Transaksi dibatalkan.", e);
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