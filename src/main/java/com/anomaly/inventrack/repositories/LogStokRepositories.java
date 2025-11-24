package com.anomaly.inventrack.repositories;

import com.anomaly.inventrack.models.LogStok;
import com.anomaly.inventrack.models.LogStok.TipeTransaksi;
import com.anomaly.inventrack.models.LogStokDetail;
import com.anomaly.inventrack.utils.Database;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class LogStokRepositories {
    
    public List<LogStok> findAll() {
        List<LogStok> list = new ArrayList<>();
        String sql = "SELECT * FROM log_stok ORDER BY tanggal_log DESC";

        try (Connection conn = Database.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                String tipeStr = rs.getString("tipe_transaksi");
                LogStok.TipeTransaksi tipeEnum = LogStok.TipeTransaksi.MASUK;
                try {
                    if (tipeStr != null) tipeEnum = LogStok.TipeTransaksi.valueOf(tipeStr);
                } catch (Exception e) {}
                LogStok log = new LogStok(
                        rs.getInt("id_log"),
                        rs.getInt("id_gudang"),
                        rs.getInt("id_barang"),
                        tipeEnum,
                        rs.getInt("jumlah_perubahan"),
                        rs.getTimestamp("tanggal_log").toLocalDateTime(),
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
        String sql = "SELECT l.*, b.namaBarang, g.namaGudang " +
                     "FROM log_stok l " +
                     "JOIN barang b ON l.id_barang = b.id_barang " +
                     "JOIN gudang g ON l.id_gudang = g.id_gudang " +
                     "ORDER BY l.tanggal_log DESC";
        
        List<LogStokDetail> list = new ArrayList<>();
        
        try (Connection conn = Database.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {

                LogStokDetail logDetail = new LogStokDetail();
                

                logDetail.setIdLog(rs.getInt("id_log"));
                logDetail.setIdGudang(rs.getInt("id_gudang"));
                logDetail.setIdBarang(rs.getInt("id_barang"));
                logDetail.setTipeTransaksi(rs.getObject("tipe_transaksi", TipeTransaksi.class));
                logDetail.setJumlahPerubahan(rs.getInt("jumlah_perubahan"));
                logDetail.setTanggalLog(rs.getTimestamp("tanggal_log").toLocalDateTime());
                logDetail.setKeterangan(rs.getString("keterangan"));
                

                logDetail.setNamaBarang(rs.getString("namaBarang"));
                logDetail.setNamaGudang(rs.getString("namaGudang"));
                
                list.add(logDetail);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Gagal mengambil detail log stok: " + e.getMessage(), e);
        }
        
        return list;
    }

    public void insert(Connection conn, LogStok log) throws SQLException {
    String sql = "INSERT INTO log_stok (id_gudang, id_barang, tipe_transaksi, jumlah_perubahan, tanggal_log, keterangan) " +
                 "VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, log.getIdGudang());
            ps.setInt(2, log.getIdBarang());
            ps.setObject(3, log.getTipeTransaksi().name());
            ps.setInt(4, log.getJumlahPerubahan());
            ps.setTimestamp(5, java.sql.Timestamp.valueOf(log.getTanggalLog()));
            ps.setString(6, log.getKeterangan());

            ps.executeUpdate();

            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    log.setIdLog(generatedKeys.getInt(1));
                }
            }

        }
    }
}
