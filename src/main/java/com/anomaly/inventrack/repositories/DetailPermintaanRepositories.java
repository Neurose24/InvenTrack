package com.anomaly.inventrack.repositories;

import com.anomaly.inventrack.models.DetailPermintaan;
import com.anomaly.inventrack.utils.Database;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DetailPermintaanRepositories {

    public List<DetailPermintaan> findByPermintaan(int idPermintaan) {
        List<DetailPermintaan> list = new ArrayList<>();
        String sql = "SELECT * FROM detailPermintaan WHERE idPermintaan = ?";

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, idPermintaan);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                DetailPermintaan dp = new DetailPermintaan(
                        rs.getInt("idDetailPermintaan"),
                        rs.getInt("idPermintaan"),
                        rs.getInt("idBarang"),
                        rs.getInt("jumlahDiminta"),
                        rs.getInt("jumlahDisetujui"),
                        rs.getString("catatanAdmin")
                );
                list.add(dp);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }

    public void insert(Connection conn, DetailPermintaan detailPermintaan) throws SQLException {
        String sql = "INSERT INTO detailPermintaan (idPermintaan, idBarang, jumlahDiminta, jumlahDisetujui, catatanAdmin) " +
                     "VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, detailPermintaan.getIdPermintaan());
            ps.setInt(2, detailPermintaan.getIdBarang());
            ps.setInt(3, detailPermintaan.getJumlahDiminta());
            ps.setInt(4, detailPermintaan.getJumlahDisetujui());
            ps.setString(5, detailPermintaan.getCatatanAdmin());

            ps.executeUpdate();

            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    detailPermintaan.setIdDetailPermintaan(generatedKeys.getInt(1));
                }
            }
        }
    }
}
