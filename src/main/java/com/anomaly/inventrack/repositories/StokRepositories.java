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

    public Optional<Stok> getById(int id_stok) {
        String sql = "SELECT * FROM stok WHERE id_stok = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id_stok);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToStok(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Gagal mencari stok by ID: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    public Optional<Stok> findByBarangAndGudang(int id_barang, int id_gudang) {
        String sql = "SELECT * FROM stok WHERE id_barang = ? AND id_gudang = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id_barang);
            ps.setInt(2, id_gudang);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToStok(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Gagal mencari stok by Barang/Gudang: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    public void insert(Connection conn, Stok stok) throws SQLException {
    String sql = "INSERT INTO stok (id_gudang, id_barang, jumlah_stok) VALUES (?, ?, ?)";
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

    public void updateJumlahStok(Connection conn, int id_stok, int jumlah_stokBaru) throws SQLException {
    // Catatan: Gunakan conn yang diterima, jangan buat koneksi baru
    String sql = "UPDATE stok SET jumlah_stok = ?, tanggal_update = ? WHERE id_stok = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, jumlah_stokBaru);
            ps.setTimestamp(2, java.sql.Timestamp.valueOf(LocalDateTime.now()));
            ps.setInt(3, id_stok);
            
            ps.executeUpdate();

        } // PreparedStatement akan ditutup, Connection tetap terbuka
    }

    public void delete(Connection conn, int id_stok) throws SQLException {
    // Catatan: Gunakan conn yang diterima, jangan buat koneksi baru
    String sql = "DELETE FROM stok WHERE id_stok = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id_stok);
            ps.executeUpdate();

        } // PreparedStatement akan ditutup, Connection tetap terbuka
    }

    public List<Stok> getByGudang(int id_gudang) {
        List<Stok> list = new ArrayList<>();
        String sql = "SELECT * FROM stok WHERE id_gudang = ?";

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id_gudang);
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
        s.setIdStok(rs.getInt("id_stok"));
        s.setIdGudang(rs.getInt("id_gudang"));
        s.setIdBarang(rs.getInt("id_barang"));
        s.setJumlahStok(rs.getInt("jumlah_stok"));
        s.setStokMinimum(rs.getInt("stok_minimum"));
        try {
            s.setTanggalUpdate(rs.getTimestamp("tanggal_update").toLocalDateTime());
        } catch (Exception ignored) {}
        return s;
    }
}
