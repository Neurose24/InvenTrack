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
}
