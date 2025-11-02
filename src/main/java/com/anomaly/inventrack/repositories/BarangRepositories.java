package com.anomaly.inventrack.repositories;

import com.anomaly.inventrack.models.Barang;
import com.anomaly.inventrack.utils.Database;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class BarangRepositories {
    public List<Barang> findAll() {
        String sql = "SELECT idBarang, namaBarang, kategori, satuan, deskripsi FROM barang";
        List<Barang> list = new ArrayList<>();
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(mapToBarang(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Gagal load barang: " + e.getMessage(), e);
        }
        return list;
    }

    public Optional<Barang> findById (Integer idBarang) {
        String sql = "SELECT idBarang, namaBarang, kategori, satuan, deskripsi FROM barang WHERE id_barang = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, idBarang);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapToBarang(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Gagal load barang by ID: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    public int save(Barang b) {
        String sql = "INSERT INTO barang (namaBarang, kategori, satuan, deskripsi) VALUES (?, ?, ?, ?)";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, b.getNamaBarang());
            ps.setString(2, b.getKategori());
            ps.setString(3, b.getSatuan());
            ps.setString(4, b.getDeskripsi());
            int affected = ps.executeUpdate();
            if (affected == 0) {
                throw new SQLException("Menyimpan barang gagal, tidak ada baris yang terpengaruh.");
            }
            try (ResultSet gk = ps.getGeneratedKeys()) {
                if (gk.next()) {
                    return gk.getInt(1);
                }
            }
            return -1;
        } catch (SQLException e) {
            throw new RuntimeException("Gagal menyimpan barang: " + e.getMessage(), e);
        }
    }

    public boolean update(Barang b) {
        String sql = "UPDATE barang SET namaBarang = ?, kategori = ?, satuan = ?, deskripsi = ? WHERE idBarang = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, b.getNamaBarang());
            ps.setString(2, b.getKategori());
            ps.setString(3, b.getSatuan());
            ps.setString(4, b.getDeskripsi());
            ps.setInt(5, b.getIdBarang());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Gagal update barang: " + e.getMessage(), e);
        }
    }

    public boolean delete(int id) {
        String sql = "DELETE FROM barang WHERE idBarang = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Gagal delete barang: " + e.getMessage(), e);
        }
    }

    private Barang mapToBarang(ResultSet rs) throws SQLException {
        Barang b = new Barang();
        b.setIdBarang(rs.getInt("idBarang"));
        b.setNamaBarang(rs.getString("namaBarang"));
        b.setKategori(rs.getString("kategori"));
        b.setSatuan(rs.getString("satuan"));
        b.setDeskripsi(rs.getString("deskripsi"));
        return b;
    }
}
