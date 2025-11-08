package com.anomaly.inventrack.services;

import com.anomaly.inventrack.models.LogStok;
import com.anomaly.inventrack.models.Stok;
import com.anomaly.inventrack.repositories.LogStokRepositories;
import com.anomaly.inventrack.repositories.StokRepositories;
import com.anomaly.inventrack.services.exceptions.BusinessException; 
import com.anomaly.inventrack.utils.Database;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

public class InventoryService {

    private final StokRepositories stokRepo;
    private final LogStokRepositories logRepo;

    public InventoryService() {
        this.stokRepo = new StokRepositories();
        this.logRepo = new LogStokRepositories();
    }

    // ✅ Ambil semua stok
    public List<Stok> getAllStok() {
        return stokRepo.getAll(); //
    }

    // ✅ Ambil stok berdasarkan barang & gudang
    public Stok getStok(int idBarang, int idGudang) {
        Stok stok = stokRepo.findByBarangAndGudang(idBarang, idGudang); //
        if (stok == null) {
            throw new BusinessException("Stok tidak ditemukan untuk barang ID " + idBarang + " di gudang ID " + idGudang);
        }
        return stok;
    }

    // =========================================================
    // ============== 1. LOGIKA TAMBAH STOK (TRANSACIONAL) ============
    // =========================================================
    public void tambahStok(int idBarang, int idGudang, int jumlah, String keterangan) {
        if (jumlah <= 0) {
            throw new IllegalArgumentException("Jumlah penambahan harus lebih dari nol.");
        }
        
        Connection conn = null;
        try {
            conn = Database.getConnection();
            conn.setAutoCommit(false); // Mulai transaksi
            
            Stok stok = stokRepo.findByBarangAndGudang(idBarang, idGudang); //
            
            if (stok == null) {
                // Insert stok baru jika belum ada
                Stok newStok = new Stok(null, idGudang, idBarang, jumlah); //
                stokRepo.insert(conn, newStok); // Transaksional
            } else {
                // Update stok jika sudah ada
                int jumlahBaru = stok.getJumlahStok() + jumlah; //
                stokRepo.updateJumlahStok(conn, stok.getIdStok(), jumlahBaru); // Transaksional
            }

            // Catat Log Stok
            LogStok log = new LogStok(
                    null,
                    idGudang,
                    idBarang,
                    LogStok.TipeTransaksi.MASUK, //
                    jumlah,
                    LocalDateTime.now(),
                    keterangan
            );
            logRepo.insert(conn, log); // Transaksional

            conn.commit(); // Komit jika semua operasi berhasil

        } catch (SQLException e) {
            try {
                if (conn != null) conn.rollback(); // Rollback jika ada yang gagal
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

    // =========================================================
    // ============== 2. LOGIKA KURANGI STOK (TRANSACIONAL) ============
    // =========================================================
    public void kurangiStok(int idBarang, int idGudang, int jumlah, String keterangan) {
        if (jumlah <= 0) {
            throw new IllegalArgumentException("Jumlah pengurangan harus lebih dari nol.");
        }
        
        Connection conn = null;
        try {
            conn = Database.getConnection();
            conn.setAutoCommit(false); // Mulai transaksi
            
            Stok stok = stokRepo.findByBarangAndGudang(idBarang, idGudang); //
            
            if (stok == null || stok.getJumlahStok() < jumlah) { //
                throw new BusinessException("Stok tidak mencukupi untuk dikurangi");
            }

            // Update stok
            int jumlahBaru = stok.getJumlahStok() - jumlah;
            stokRepo.updateJumlahStok(conn, stok.getIdStok(), jumlahBaru); // Transaksional

            // Catat Log Stok
            LogStok log = new LogStok(
                    null,
                    idGudang,
                    idBarang,
                    LogStok.TipeTransaksi.KELUAR, //
                    jumlah,
                    LocalDateTime.now(),
                    keterangan
            );
            logRepo.insert(conn, log); // Transaksional

            conn.commit(); // Komit jika semua operasi berhasil

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

    // =========================================================
    // ============== 3. LOGIKA REKONSILIASI STOK (TRANSACIONAL) ============
    // =========================================================
    public void rekonsiliasiStok(int idBarang, int idGudang, int jumlahFisik, String keterangan) {
        if (jumlahFisik < 0) {
            throw new IllegalArgumentException("Jumlah fisik tidak boleh negatif.");
        }

        Connection conn = null;
        try {
            conn = Database.getConnection();
            conn.setAutoCommit(false); // Mulai transaksi
            
            Stok stok = stokRepo.findByBarangAndGudang(idBarang, idGudang); //
            
            if (stok == null) {
                // Jika belum ada stok, buat baru
                Stok newStok = new Stok(null, idGudang, idBarang, jumlahFisik); //
                stokRepo.insert(conn, newStok); // Transaksional
                
                // Catat log
                LogStok log = new LogStok(
                        null,
                        idGudang,
                        idBarang,
                        LogStok.TipeTransaksi.REKONSILIASI, //
                        jumlahFisik,
                        LocalDateTime.now(),
                        keterangan + " (stok baru dibuat)"
                );
                logRepo.insert(conn, log);
                conn.commit();
                return;
            }

            int selisih = jumlahFisik - stok.getJumlahStok(); //
            if (selisih == 0) {
                conn.commit(); // Tetap commit jika tidak ada operasi yang dilakukan
                return;
            }

            // Update stok
            stokRepo.updateJumlahStok(conn, stok.getIdStok(), jumlahFisik); // Transaksional

            // Catat Log Stok
            LogStok.TipeTransaksi tipe = (selisih > 0)
                    ? LogStok.TipeTransaksi.REKONSILIASI_TAMBAH //
                    : LogStok.TipeTransaksi.REKONSILIASI_KURANG; //

            LogStok log = new LogStok(
                    null,
                    idGudang,
                    idBarang,
                    tipe,
                    Math.abs(selisih),
                    LocalDateTime.now(),
                    keterangan + " (selisih " + selisih + ")"
            );
            logRepo.insert(conn, log); // Transaksional

            conn.commit(); // Komit jika semua operasi berhasil

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

    // ✅ Lihat semua log stok
    public List<LogStok> getAllLog() {
        return logRepo.findAll(); //
    }
}