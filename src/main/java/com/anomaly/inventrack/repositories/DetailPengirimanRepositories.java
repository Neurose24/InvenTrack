package com.anomaly.inventrack.repositories;

import com.anomaly.inventrack.models.DetailPengiriman;
import com.anomaly.inventrack.utils.Database;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DetailPengirimanRepositories {

    public List<DetailPengiriman> findByPengiriman(int id_pengiriman) {
        List<DetailPengiriman> list = new ArrayList<>();
        String sql = "SELECT * FROM detail_pengiriman WHERE id_pengiriman = ?";

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id_pengiriman);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                DetailPengiriman dp = new DetailPengiriman(
                        rs.getInt("id_detail_pengiriman"),
                        rs.getInt("id_pengiriman"),
                        rs.getInt("id_barang"),
                        rs.getInt("jumlah_dikirim"),
                        rs.getInt("jumlah_diterima"),
                        rs.getObject("status_penerimaan", DetailPengiriman.StatusPenerimaan.class),
                        rs.getString("catatan_penerimaan")
                );
                list.add(dp);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public void insert(Connection conn, DetailPengiriman detailPengiriman) throws SQLException {
        String sql = "INSERT INTO detail_pengiriman (id_pengiriman, id_barang, jumlah_dikirim, jumlah_diterima, status_penerimaan, catatan_penerimaan) " +
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

    public void updatePenerimaan(Connection conn, int id_detail_pengiriman, int jumlah_diterima, DetailPengiriman.StatusPenerimaan status, String catatan) throws SQLException {
    String sql = "UPDATE detail_pengiriman SET jumlah_diterima = ?, status_penerimaan = ?, catatan_penerimaan = ? WHERE id_detail_pengiriman = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, jumlah_diterima);
            ps.setObject(2, status.toString()); 
            ps.setString(3, catatan);
            ps.setInt(4, id_detail_pengiriman);
            ps.executeUpdate();
        }
    }
}