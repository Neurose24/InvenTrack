package com.anomaly.inventrack.repositories;

import com.anomaly.inventrack.models.Permintaan;
import com.anomaly.inventrack.utils.Database;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class PermintaanRepositories {

    public List<Permintaan> findAll() {
        List<Permintaan> list = new ArrayList<>();
        String sql = "SELECT * FROM permintaan";

        try (Connection conn = Database.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                Permintaan p = new Permintaan(
                        rs.getInt("idPermintaan"),
                        rs.getInt("idPenggunaPeminta"),
                        rs.getTimestamp("tanggalPermintaan").toLocalDateTime(),
                        rs.getObject("statusPermintaan", Permintaan.StatusPermintaan.class),
                        rs.getString("catatanPermintaan")
                );
                list.add(p);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }

    public Permintaan findById(int idPermintaan) {
        String sql = "SELECT * FROM permintaan WHERE idPermintaan = ?";

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, idPermintaan);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Permintaan(
                            rs.getInt("idPermintaan"),
                            rs.getInt("idPenggunaPeminta"),
                            rs.getTimestamp("tanggalPermintaan").toLocalDateTime(),
                            rs.getObject("statusPermintaan", Permintaan.StatusPermintaan.class),
                            rs.getString("catatanPermintaan")
                    );
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void insert(Connection conn, Permintaan permintaan) throws SQLException {
        String sql = "INSERT INTO permintaan (idPenggunaPeminta, tanggalPermintaan, statusPermintaan, catatanPermintaan) " +
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

    public void updateStatus(Connection conn, int idPermintaan, Permintaan.StatusPermintaan status) throws SQLException {
    String sql = "UPDATE permintaan SET statusPermintaan = ? WHERE idPermintaan = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, status.toString()); // Sesuaikan tipe data ENUM/String di DB
            ps.setInt(2, idPermintaan);
            ps.executeUpdate();
        }
    }
    
}
