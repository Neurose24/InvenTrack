package com.anomaly.inventrack.repositories;

import com.anomaly.inventrack.models.DetailPermintaan;
import com.anomaly.inventrack.utils.Database;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DetailPermintaanRepositories {

    public List<DetailPermintaan> findByPermintaan(int id_permintaan) {
        List<DetailPermintaan> list = new ArrayList<>();
        String sql = "SELECT * FROM detail_permintaan WHERE id_permintaan = ?";

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id_permintaan);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                DetailPermintaan dp = new DetailPermintaan(
                        rs.getInt("id_detail_permintaan"),
                        rs.getInt("id_permintaan"),
                        rs.getInt("id_barang"),
                        rs.getInt("jumlah_diminta"),
                        rs.getInt("jumlah_disetujui"),
                        rs.getString("catatan_admin")
                );
                list.add(dp);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }

    public void insert(Connection conn, DetailPermintaan detailPermintaan) throws SQLException {
        String sql = "INSERT INTO detail_permintaan (id_permintaan, id_barang, jumlah_diminta, jumlah_disetujui, catatan_admin) " +
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
