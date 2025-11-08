package com.anomaly.inventrack.repositories;

import com.anomaly.inventrack.models.Stok;
import com.anomaly.inventrack.utils.Database;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class StokRepositories {

    // ==========================================================
    // =========== 1. METHOD BIASA (tanpa transaksi) ============
    // ==========================================================

    public List<Stok> getAll() {
        List<Stok> list = new ArrayList<>();
        String sql = "SELECT * FROM stok";

        try (Connection conn = Database.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                list.add(mapResultSetToStok(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public Stok getById(int idStok) {
        String sql = "SELECT * FROM stok WHERE idStok = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, idStok);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToStok(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Stok findByBarangAndGudang(int idBarang, int idGudang) {
        String sql = "SELECT * FROM stok WHERE idBarang = ? AND idGudang = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, idBarang);
            ps.setInt(2, idGudang);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToStok(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void insert(Connection conn, Stok stok) throws SQLException {
    // Catatan: Gunakan conn yang diterima, jangan buat koneksi baru
    String sql = "INSERT INTO stok (idGudang, idBarang, jumlahStok) VALUES (?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, stok.getIdGudang());
            ps.setInt(2, stok.getIdBarang());
            ps.setInt(3, stok.getJumlahStok());
            
            ps.executeUpdate();

            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    stok.setIdStok(generatedKeys.getInt(1));
                }
            }

        } // PreparedStatement akan ditutup, Connection tetap terbuka
    }

    public void updateJumlahStok(Connection conn, int idStok, int jumlahStokBaru) throws SQLException {
    // Catatan: Gunakan conn yang diterima, jangan buat koneksi baru
    String sql = "UPDATE stok SET jumlahStok = ?, tanggalUpdate = ? WHERE idStok = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, jumlahStokBaru);
            ps.setTimestamp(2, java.sql.Timestamp.valueOf(LocalDateTime.now()));
            ps.setInt(3, idStok);
            
            ps.executeUpdate();

        } // PreparedStatement akan ditutup, Connection tetap terbuka
    }

    public void delete(Connection conn, int idStok) throws SQLException {
    // Catatan: Gunakan conn yang diterima, jangan buat koneksi baru
    String sql = "DELETE FROM stok WHERE idStok = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, idStok);
            ps.executeUpdate();

        } // PreparedStatement akan ditutup, Connection tetap terbuka
    }

    public List<Stok> getByGudang(int idGudang) {
        List<Stok> list = new ArrayList<>();
        String sql = "SELECT * FROM stok WHERE idGudang = ?";

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, idGudang);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToStok(rs));
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // ==========================================================
    // ============== 3. HELPER UNTUK MAPPING ==================
    // ==========================================================

    private Stok mapResultSetToStok(ResultSet rs) throws SQLException {
        Stok s = new Stok();
        s.setIdStok(rs.getInt("idStok"));
        s.setIdGudang(rs.getInt("idGudang"));
        s.setIdBarang(rs.getInt("idBarang"));
        s.setJumlahStok(rs.getInt("jumlahStok"));
        s.setStokMinimum(rs.getInt("stokMinimum"));
        try {
            s.setTanggalUpdate(rs.getTimestamp("tanggalUpdate").toLocalDateTime());
        } catch (Exception ignored) {}
        return s;
    }
}
