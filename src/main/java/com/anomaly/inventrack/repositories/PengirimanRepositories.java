package com.anomaly.inventrack.repositories;

import com.anomaly.inventrack.models.Pengiriman;
import com.anomaly.inventrack.utils.Database;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class PengirimanRepositories {

    public List<Pengiriman> findAll() {
        List<Pengiriman> list = new ArrayList<>();
        String sql = "SELECT * FROM pengiriman";

        try (Connection conn = Database.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                Pengiriman p = new Pengiriman(
                        rs.getInt("idPengiriman"),
                        rs.getInt("idPermintaan"),
                        rs.getInt("idPenggunaPengirim"),
                        rs.getInt("idPenggunaPenerima"),
                        rs.getInt("idSupir"),
                        rs.getTimestamp("tanggalPengiriman").toLocalDateTime(),
                        rs.getString("noKendaraan"),
                        rs.getObject("statusPengiriman", Pengiriman.StatusPengiriman.class),
                        rs.getString("keteranganPengiriman")
                );
                list.add(p);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
}