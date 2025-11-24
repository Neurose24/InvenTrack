package com.anomaly.inventrack.services;

import com.anomaly.inventrack.models.LogStok;
import com.anomaly.inventrack.models.Stok;
import com.anomaly.inventrack.repositories.LogStokRepositories;
import com.anomaly.inventrack.repositories.StokRepositories;
import com.anomaly.inventrack.services.exceptions.BusinessException; 
import com.anomaly.inventrack.utils.Database;
import com.anomaly.inventrack.services.exceptions.NotFoundException;
import com.anomaly.inventrack.models.LogStokDetail;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class InventoryService {

    private final StokRepositories stokRepo;
    private final LogStokRepositories logRepo;

    public InventoryService() {
        this.stokRepo = new StokRepositories();
        this.logRepo = new LogStokRepositories();
    }

    public List<Stok> getAllStok() {
        return stokRepo.getAll();
    }

    public Stok getStok(int idBarang, int idGudang) {
        return stokRepo.findByBarangAndGudang(idBarang, idGudang)
            .orElseThrow(() -> new NotFoundException("Stok tidak ditemukan untuk barang ID " + idBarang + " di gudang ID " + idGudang));
    }

    public void tambahStok(int idBarang, int idGudang, int jumlah, String keterangan) {
        if (jumlah <= 0) {
            throw new IllegalArgumentException("Jumlah penambahan harus lebih dari nol.");
        }
        
        Connection conn = null;
        try {
            conn = Database.getConnection();
            conn.setAutoCommit(false);
            
            Optional<Stok> optStok = stokRepo.findByBarangAndGudang(idBarang, idGudang);
            
            if (optStok.isEmpty()) {
                Stok newStok = new Stok(null, idGudang, idBarang, jumlah);
                stokRepo.insert(conn, newStok);
            } else {
                Stok stok = optStok.get();
                int jumlahBaru = stok.getJumlahStok() + jumlah;
                stokRepo.updateJumlahStok(conn, stok.getIdStok(), jumlahBaru);
            }

            LogStok log = new LogStok(
                    null,
                    idGudang,
                    idBarang,
                    LogStok.TipeTransaksi.MASUK,
                    jumlah,
                    LocalDateTime.now(),
                    keterangan
            );
            logRepo.insert(conn, log);

            conn.commit();

        } catch (SQLException e) {
            try {
                if (conn != null) conn.rollback();
            } catch (SQLException rollbackEx) {
                System.err.println("Rollback gagal: " + rollbackEx.getMessage());
            }
            throw new BusinessException("Gagal menambah stok. Transaksi dibatalkan.", e);
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

    public void tambahStokKontainer(int idBarang, int idGudang, int jumlah, String keterangan) {
        Connection conn = null;
        try {
            conn = Database.getConnection();
            conn.setAutoCommit(false); 
            
            Optional<Stok> optStok = stokRepo.findByBarangAndGudang(idBarang, idGudang);
            if (optStok.isEmpty()) {
                Stok newStok = new Stok(null, idGudang, idBarang, jumlah);
                stokRepo.insert(conn, newStok);
            } else {
                Stok stok = optStok.get();
                int jumlahBaru = stok.getJumlahStok() + jumlah;
                stokRepo.updateJumlahStok(conn, stok.getIdStok(), jumlahBaru);
            }

            LogStok log = new LogStok(
                    null, idGudang, idBarang,
                    LogStok.TipeTransaksi.KONTAINER,
                    jumlah, LocalDateTime.now(), keterangan
            );
            logRepo.insert(conn, log);

            conn.commit();
        } catch (SQLException e) {
            try { if (conn != null) conn.rollback(); } catch (SQLException ex) {}
            throw new BusinessException("Gagal input kontainer.", e);
        } finally {
            try { if (conn != null) { conn.setAutoCommit(true); conn.close(); } } catch (SQLException ex) {}
        }
    }

    public void kurangiStok(int idBarang, int idGudang, int jumlah, String keterangan) {
        if (jumlah <= 0) {
            throw new IllegalArgumentException("Jumlah pengurangan harus lebih dari nol.");
        }
        
        Connection conn = null;
        try {
            conn = Database.getConnection();
            conn.setAutoCommit(false);
            
            Stok stok = stokRepo.findByBarangAndGudang(idBarang, idGudang)
                .orElseThrow(() -> new BusinessException("Stok tidak mencukupi (barang tidak ditemukan)"));
            
            if (stok.getJumlahStok() < jumlah) {
                throw new BusinessException("Stok tidak mencukupi untuk dikurangi");
            }

            int jumlahBaru = stok.getJumlahStok() - jumlah;
            stokRepo.updateJumlahStok(conn, stok.getIdStok(), jumlahBaru);

            LogStok log = new LogStok(
                    null,
                    idGudang,
                    idBarang,
                    LogStok.TipeTransaksi.KELUAR,
                    jumlah,
                    LocalDateTime.now(),
                    keterangan
            );
            logRepo.insert(conn, log);

            conn.commit();

        } catch (SQLException e) {
            try {
                if (conn != null) conn.rollback();
            } catch (SQLException rollbackEx) {
                System.err.println("Rollback gagal: " + rollbackEx.getMessage());
            }
            throw new BusinessException("Gagal mengurangi stok. Transaksi dibatalkan.", e);
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

    public void rekonsiliasiStok(int idBarang, int idGudang, int jumlahFisik, String keterangan) {
        if (jumlahFisik < 0) {
            throw new IllegalArgumentException("Jumlah fisik tidak boleh negatif.");
        }

        Connection conn = null;
        try {
            conn = Database.getConnection();
            conn.setAutoCommit(false);
            Optional<Stok> optStok = stokRepo.findByBarangAndGudang(idBarang, idGudang);
            
            if (optStok.isEmpty()) {
                Stok newStok = new Stok(null, idGudang, idBarang, jumlahFisik);
                stokRepo.insert(conn, newStok);
                
                LogStok log = new LogStok(
                        null,
                        idGudang,
                        idBarang,
                        LogStok.TipeTransaksi.REKONSILIASI,
                        jumlahFisik,
                        LocalDateTime.now(),
                        keterangan + " (stok baru dibuat)"
                );
                logRepo.insert(conn, log);
                conn.commit();
                return;
            }

            Stok stok = optStok.get();

            int selisih = jumlahFisik - stok.getJumlahStok();
            if (selisih == 0) {
                conn.commit();
                return;
            }

            stokRepo.updateJumlahStok(conn, stok.getIdStok(), jumlahFisik);

            LogStok.TipeTransaksi tipe = (selisih > 0)
                    ? LogStok.TipeTransaksi.REKONSILIASI_TAMBAH
                    : LogStok.TipeTransaksi.REKONSILIASI_KURANG;

            LogStok log = new LogStok(
                    null,
                    idGudang,
                    idBarang,
                    tipe,
                    Math.abs(selisih),
                    LocalDateTime.now(),
                    keterangan + " (selisih " + selisih + ")"
            );
            logRepo.insert(conn, log);

            conn.commit();

        } catch (SQLException e) {
            try {
                if (conn != null) conn.rollback();
            } catch (SQLException rollbackEx) {
                System.err.println("Rollback gagal: " + rollbackEx.getMessage());
            }
            throw new BusinessException("Gagal melakukan rekonsiliasi stok. Transaksi dibatalkan.", e);
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

    public List<LogStok> getAllLog() {
        return logRepo.findAll();
    }

    public List<LogStokDetail> getLogStokDetails() {
        return logRepo.findDetailAll();
    }
}