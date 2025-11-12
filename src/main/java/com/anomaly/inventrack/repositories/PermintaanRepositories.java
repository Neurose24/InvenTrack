package com.anomaly.inventrack.repositories;

import com.anomaly.inventrack.models.Permintaan;
import com.anomaly.inventrack.utils.Database;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PermintaanRepositories {

    public List<Permintaan> findAll() {
        List<Permintaan> list = new ArrayList<>();
        String sql = "SELECT * FROM permintaan";

        try (Connection conn = Database.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                Permintaan p = new Permintaan(
                        rs.getInt("id_permintaan"),
                        rs.getInt("id_pengguna_peminta"),
                        rs.getTimestamp("tanggal_permintaan").toLocalDateTime(),
                        rs.getObject("status_permintaan", Permintaan.StatusPermintaan.class),
                        rs.getString("catatan_permintaan")
                );
                list.add(p);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }

    public Optional<Permintaan> findById(int id_permintaan) {
        String sql = "SELECT * FROM permintaan WHERE id_permintaan = ?";

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id_permintaan);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(new Permintaan(
                            rs.getInt("id_permintaan"),
                            rs.getInt("id_pengguna_peminta"),
                            rs.getTimestamp("tanggal_permintaan").toLocalDateTime(),
                            rs.getObject("status_permintaan", Permintaan.StatusPermintaan.class),
                            rs.getString("catatan_permintaan")
                    ));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Gagal mencari permintaan by ID: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    public void insert(Connection conn, Permintaan permintaan) throws SQLException {
        String sql = "INSERT INTO permintaan (id_pengguna_peminta, tanggal_permintaan, status_permintaan, catatan_permintaan) " +
                     "VALUES (?, ?, ?, ?)";

        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, permintaan.getIdPenggunaPeminta());
            ps.setTimestamp(2, Timestamp.valueOf(permintaan.getTanggalPermintaan()));
            ps.setObject(3, permintaan.getStatusPermintaan());
            ps.setString(4, permintaan.getCatatanPermintaan());

            ps.executeUpdate();

            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    permintaan.setIdPermintaan(generatedKeys.getInt(1));
                }
            }
        }
    }

    public void updateStatus(Connection conn, int id_permintaan, Permintaan.StatusPermintaan status) throws SQLException {
    String sql = "UPDATE permintaan SET status_permintaan = ? WHERE id_permintaan = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, status.toString()); // Sesuaikan tipe data ENUM/String di DB
            ps.setInt(2, id_permintaan);
            ps.executeUpdate();
        }
    }
    
}
