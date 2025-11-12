package com.anomaly.inventrack.repositories;

import com.anomaly.inventrack.models.Gudang;
import com.anomaly.inventrack.utils.Database;
import com.anomaly.inventrack.utils.DBUtil;
import java.util.Optional;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class GudangRepositories {

    public List<Gudang> findAll() {
        List<Gudang> list = new ArrayList<>();
        String sql = "SELECT * FROM gudang";

        try (Connection conn = Database.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                Gudang g = new Gudang(
                        rs.getInt("id_gudang"),
                        rs.getString("nama_gudang"),
                        rs.getString("lokasi"),
                        rs.getString("kontak_admin")
                );
                list.add(g);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }

    public Optional<Gudang> findById(int id) {
        String sql = "SELECT * FROM gudang WHERE id_gudang = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {
                    // Bungkus hasil dengan Optional.of()
                    return Optional.of(new Gudang(
                            rs.getInt("id_gudang"),
                            rs.getString("nama_gudang"),
                            rs.getString("lokasi"),
                            rs.getString("kontak_admin") //
                    ));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Gagal mencari gudang by ID: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    public Gudang save(Gudang gudang) {
        String sql = "INSERT INTO gudang (nama_gudang, lokasi, kontak_admin) VALUES (?, ?, ?)";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, gudang.getNamaGudang());
            ps.setString(2, gudang.getLokasi());
            ps.setString(3, gudang.getKontakAdmin()); // Menggunakan getter yang sesuai dari model
            
            int affectedRows = ps.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Gagal menyimpan gudang, tidak ada baris yang terpengaruh.");
            }

            try (ResultSet gk = ps.getGeneratedKeys()) {
                if (gk.next()) {
                    gudang.setIdGudang(gk.getInt(1));
                }
            }
            return gudang;
        } catch (SQLException e) {
            throw new RuntimeException("Gagal menyimpan gudang baru: " + e.getMessage(), e);
        }
    }

    public boolean update(Gudang gudang) {
        String sql = "UPDATE gudang SET nama_gudang = ?, lokasi = ?, kontak_admin = ? WHERE id_gudang = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, gudang.getNamaGudang());
            ps.setString(2, gudang.getLokasi());
            ps.setString(3, gudang.getKontakAdmin()); // Menggunakan getter yang sesuai dari model
            ps.setInt(4, gudang.getIdGudang());
            
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Gagal update gudang: " + e.getMessage(), e);
        }
    }

    public boolean delete(int id) {
        String sql = "DELETE FROM gudang WHERE id_gudang = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Gagal delete gudang: " + e.getMessage(), e);
        }
    }
}