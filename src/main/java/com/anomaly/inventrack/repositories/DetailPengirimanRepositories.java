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
}