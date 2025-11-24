package com.anomaly.inventrack.services;

import com.anomaly.inventrack.models.*;
import com.anomaly.inventrack.repositories.*;
import com.anomaly.inventrack.utils.Database;
import com.anomaly.inventrack.services.exceptions.BusinessException;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

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

    /**
     * @param idPengiriman
     * @param detailPenerimaanList
     */
    public void receivePengiriman(int idPengiriman, List<DetailPengiriman> detailPenerimaanList) {
        Connection conn = null;
        try {
            conn = Database.getConnection();
            conn.setAutoCommit(false); 

            Pengiriman pengiriman = pengirimanRepo.findById(idPengiriman)
                .orElseThrow(() -> new RuntimeException("Pengiriman tidak valid atau belum dikirim."));
            
            if (pengiriman.getStatusPengiriman() != Pengiriman.StatusPengiriman.DIKIRIM) {
                throw new RuntimeException("Pengiriman sudah diproses.");
            }
            
            Pengguna optPenerima = penggunaRepo.findById(pengiriman.getIdPenggunaPenerima())
                .orElseThrow(() -> new RuntimeException("Pengguna penerima tidak ditemukan."));
            int idGudangTujuan = optPenerima.getIdGudang();

            Pengguna optPengirim = penggunaRepo.findById(pengiriman.getIdPenggunaPengirim())
                .orElseThrow(() -> new RuntimeException("Pengguna pengirim tidak ditemukan."));
            int idGudangAsal = optPengirim.getIdGudang();

            for (DetailPengiriman detail : detailPenerimaanList) {
                
                detailPengirimanRepo.updatePenerimaan(
                    conn, 
                    detail.getIdDetailPengiriman(), 
                    detail.getJumlahDiterima(), 
                    detail.getStatusPenerimaan(), 
                    detail.getCatatanPenerimaan()
                ); 

                if (detail.getJumlahDiterima() > 0) {
                    inventoryService.tambahStok(
                        detail.getIdBarang(), 
                        idGudangTujuan, 
                        detail.getJumlahDiterima(), 
                        "Masuk dari Pengiriman ID: " + idPengiriman
                    );
                }

                int jumlahDikirim = detail.getJumlahDikirim();
                int jumlahDiterima = detail.getJumlahDiterima();
                int selisihRusak = jumlahDikirim - jumlahDiterima;

                if (selisihRusak > 0) {
                    inventoryService.tambahStok(
                        detail.getIdBarang(),
                        idGudangAsal,
                        selisihRusak,
                        "Retur Barang (Rusak/Kurang) dari Pengiriman ID: " + idPengiriman
                    );
                }
            }

            pengirimanRepo.updateStatus(conn, idPengiriman, Pengiriman.StatusPengiriman.DITERIMA);
            permintaanRepo.updateStatus(conn, pengiriman.getIdPermintaan(), Permintaan.StatusPermintaan.SELESAI);

            conn.commit();
            
        } catch (SQLException e) {
            try { if (conn != null) conn.rollback(); } catch (SQLException ex) {}
            throw new BusinessException("Gagal mencatat penerimaan. Transaksi dibatalkan.", e);
        } finally {
            try { if (conn != null) { conn.setAutoCommit(true); conn.close(); } } catch (SQLException ex) {}
        }
    }
}