package com.anomaly.inventrack.repositories;

import com.anomaly.inventrack.models.Gudang;
import com.anomaly.inventrack.utils.Database;
import com.anomaly.inventrack.utils.DBUtil;

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
                        rs.getInt("idGudang"),
                        rs.getString("namaGudang"),
                        rs.getString("lokasi"),
                        rs.getString("keterangan")
                );
                list.add(g);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }

    public Gudang findById(int id) {
        String sql = "SELECT * FROM gudang WHERE idGudang = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return new Gudang(
                        rs.getInt("idGudang"),
                        rs.getString("namaGudang"),
                        rs.getString("lokasi"),
                        rs.getString("keterangan")
                );
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}