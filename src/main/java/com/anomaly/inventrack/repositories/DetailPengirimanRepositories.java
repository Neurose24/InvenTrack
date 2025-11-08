package com.anomaly.inventrack.repositories;

import com.anomaly.inventrack.models.DetailPengiriman;
import com.anomaly.inventrack.utils.Database;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DetailPengirimanRepositories {

    public List<DetailPengiriman> findByPengiriman(int idPengiriman) {
        List<DetailPengiriman> list = new ArrayList<>();
        String sql = "SELECT * FROM detailPengiriman WHERE idPengiriman = ?";

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, idPengiriman);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                DetailPengiriman dp = new DetailPengiriman(
                        rs.getInt("idDetailPengiriman"),
                        rs.getInt("idPengiriman"),
                        rs.getInt("idBarang"),
                        rs.getInt("jumlahDikirim"),
                        rs.getInt("jumlahDiterima"),
                        rs.getObject("statusPenerimaan", DetailPengiriman.StatusPenerimaan.class),
                        rs.getString("catatanPenerimaan")
                );
                list.add(dp);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public void insert(Connection conn, DetailPengiriman detailPengiriman) throws SQLException {
        String sql = "INSERT INTO detailPengiriman (idPengiriman, idBarang, jumlahDikirim, jumlahDiterima, statusPenerimaan, catatanPenerimaan) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, detailPengiriman.getIdPengiriman());
            ps.setInt(2, detailPengiriman.getIdBarang());
            ps.setInt(3, detailPengiriman.getJumlahDikirim());
            ps.setInt(4, detailPengiriman.getJumlahDiterima());
            ps.setObject(5, detailPengiriman.getStatusPenerimaan());
            ps.setString(6, detailPengiriman.getCatatanPenerimaan());

            ps.executeUpdate();

            // Opsional: set generated key ke objek DetailPengiriman
            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    detailPengiriman.setIdDetailPengiriman(generatedKeys.getInt(1));
                }
            }
        }
    }

    public void updatePenerimaan(Connection conn, int idDetailPengiriman, int jumlahDiterima, DetailPengiriman.StatusPenerimaan status, String catatan) throws SQLException {
    String sql = "UPDATE detailPengiriman SET jumlahDiterima = ?, statusPenerimaan = ?, catatanPenerimaan = ? WHERE idDetailPengiriman = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, jumlahDiterima);
            ps.setObject(2, status.toString()); 
            ps.setString(3, catatan);
            ps.setInt(4, idDetailPengiriman);
            ps.executeUpdate();
        }
    }
}