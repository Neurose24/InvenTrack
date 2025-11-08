package com.anomaly.inventrack.repositories;

import com.anomaly.inventrack.models.LogStok;
import com.anomaly.inventrack.models.LogStok.TipeTransaksi;
import com.anomaly.inventrack.models.LogStokDetail;
import com.anomaly.inventrack.utils.Database;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class LogStokRepositories {
    
    public List<LogStok> findAll() {
        List<LogStok> list = new ArrayList<>();
        String sql = "SELECT * FROM log_stok ORDER BY tanggal DESC";

        try (Connection conn = Database.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                LogStok log = new LogStok(
                        rs.getInt("idLog"),
                        rs.getInt("idGudang"),
                        rs.getInt("idBarang"),
                        rs.getObject("tipeTransaksi", LogStok.TipeTransaksi.class),
                        rs.getInt("jumlahPerubahan"),
                        rs.getTimestamp("tanggalLog").toLocalDateTime(),
                        rs.getString("keterangan")
                );
                list.add(log);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }

    public List<LogStokDetail> findDetailAll() {
        // Query SQL yang menggabungkan (JOIN) log_stok dengan barang dan gudang
        String sql = "SELECT l.*, b.namaBarang, g.namaGudang " +
                     "FROM log_stok l " +
                     "JOIN barang b ON l.idBarang = b.idBarang " +
                     "JOIN gudang g ON l.idGudang = g.idGudang " +
                     "ORDER BY l.tanggalLog DESC";
        
        List<LogStokDetail> list = new ArrayList<>();
        
        try (Connection conn = Database.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                // Inisialisasi LogStokDetail
                LogStokDetail logDetail = new LogStokDetail();
                
                // 1. Set properti dari LogStok (dasar)
                logDetail.setIdLog(rs.getInt("idLog"));
                logDetail.setIdGudang(rs.getInt("idGudang"));
                logDetail.setIdBarang(rs.getInt("idBarang"));
                logDetail.setTipeTransaksi(rs.getObject("tipeTransaksi", TipeTransaksi.class));
                logDetail.setJumlahPerubahan(rs.getInt("jumlahPerubahan"));
                logDetail.setTanggalLog(rs.getTimestamp("tanggalLog").toLocalDateTime());
                logDetail.setKeterangan(rs.getString("keterangan"));
                
                // 2. Set properti tambahan dari hasil JOIN
                logDetail.setNamaBarang(rs.getString("namaBarang"));
                logDetail.setNamaGudang(rs.getString("namaGudang"));
                
                list.add(logDetail);
            }

        } catch (SQLException e) {
            // Lempar RuntimeException untuk ditangani di lapisan Service (LaporanService)
            throw new RuntimeException("Gagal mengambil detail log stok: " + e.getMessage(), e);
        }
        
        return list;
    }

    public void insert(Connection conn, LogStok log) throws SQLException {
    // Catatan: Gunakan conn yang diterima, jangan buat koneksi baru
    String sql = "INSERT INTO log_stok (idGudang, idBarang, tipeTransaksi, jumlahPerubahan, tanggalLog, keterangan) " +
                 "VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, log.getIdGudang());
            ps.setInt(2, log.getIdBarang());
            ps.setObject(3, log.getTipeTransaksi().name()); // Asumsi ENUM disimpan sebagai String
            ps.setInt(4, log.getJumlahPerubahan());
            ps.setTimestamp(5, java.sql.Timestamp.valueOf(log.getTanggalLog()));
            ps.setString(6, log.getKeterangan());

            ps.executeUpdate();

            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    log.setIdLog(generatedKeys.getInt(1));
                }
            }

        } // PreparedStatement akan ditutup, Connection tetap terbuka
    }
}
