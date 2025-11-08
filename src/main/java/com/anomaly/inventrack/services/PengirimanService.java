package com.anomaly.inventrack.services;

import com.anomaly.inventrack.models.*;
import com.anomaly.inventrack.repositories.*;
import com.anomaly.inventrack.utils.Database;

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
    
    // INTEGRASI: Service untuk manipulasi stok
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
            conn.setAutoCommit(false); // Mulai transaksi untuk Pengiriman/Permintaan/DetailPengiriman

            // 1. Validasi Pengiriman
            // Asumsi findById di PengirimanRepositories mengembalikan Optional
            Optional<Pengiriman> optPengiriman = pengirimanRepo.findById(idPengiriman); 
            if (optPengiriman.isEmpty() || optPengiriman.get().getStatusPengiriman() != Pengiriman.StatusPengiriman.DIKIRIM) {
                throw new RuntimeException("Pengiriman tidak valid atau belum dikirim.");
            }
            Pengiriman pengiriman = optPengiriman.get();
            
            // 2. Tentukan Gudang Tujuan (tempat stok akan bertambah)
            Optional<Pengguna> optPenerima = penggunaRepo.findById(pengiriman.getIdPenggunaPenerima()); 
            if (optPenerima.isEmpty()) {
                throw new RuntimeException("Pengguna penerima tidak ditemukan.");
            }
            int idGudangTujuan = optPenerima.get().getIdGudang(); //

            boolean isSemuaDiterima = true;

            // 3. Proses Stok dan Update Detail Penerimaan
            for (DetailPengiriman detail : detailPenerimaanList) {
                
                // A. Update Detail Pengiriman
                // Memerlukan method updatePenerimaan transaksional di DetailPengirimanRepositories
                // Catatan: DetailPengirimanRepositories harus menerima Connection
                detailPengirimanRepo.updatePenerimaan( 
                    conn, 
                    detail.getIdDetailPengiriman(), 
                    detail.getJumlahDiterima(), 
                    detail.getStatusPenerimaan(), 
                    detail.getCatatanPenerimaan()
                ); 

                // Cek status total
                if (detail.getStatusPenerimaan() != DetailPengiriman.StatusPenerimaan.DITERIMA) {
                    isSemuaDiterima = false;
                }
                
                // B. Tambah Stok di Gudang Tujuan (Delegasi ke InventoryService)
                if (detail.getJumlahDiterima() > 0) {
                    int idBarang = detail.getIdBarang();
                    int jumlahMasuk = detail.getJumlahDiterima();

                    // Panggil Service lain untuk menambah stok
                    // InventoryService akan mengurus: 
                    // a) Cek stok, insert baru jika belum ada
                    // b) Update tabel stok (transaksional)
                    // c) Insert log stok (transaksional)
                    inventoryService.tambahStok(
                        idBarang, 
                        idGudangTujuan, 
                        jumlahMasuk, 
                        "Masuk dari Pengiriman ID: " + idPengiriman
                    );
                }
            }

            // 4. Update Status Pengiriman Induk
            pengirimanRepo.updateStatus(conn, idPengiriman, Pengiriman.StatusPengiriman.DITERIMA);
            
            // 5. Update Status Permintaan
            // Setelah Pengiriman DITERIMA, Permintaan disetel ke SELESAI
            permintaanRepo.updateStatus(conn, pengiriman.getIdPermintaan(), Permintaan.StatusPermintaan.SELESAI); //

            conn.commit(); // Komit transaksi PengirimanService
            
        } catch (SQLException e) {
            try {
                if (conn != null) conn.rollback(); // Rollback PengirimanService
            } catch (SQLException rollbackEx) {
                System.err.println("Rollback gagal: " + rollbackEx.getMessage());
            }
            // Lempar kembali sebagai BusinessException/RuntimeException
            throw new RuntimeException("Gagal mencatat penerimaan. Transaksi dibatalkan.", e);
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