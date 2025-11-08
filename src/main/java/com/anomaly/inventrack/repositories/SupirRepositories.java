package com.anomaly.inventrack.repositories;

import com.anomaly.inventrack.models.Supir;
import com.anomaly.inventrack.utils.Database;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SupirRepositories {

    public List<Supir> findAll() {
        List<Supir> list = new ArrayList<>();
        String sql = "SELECT * FROM supir";

        try (Connection conn = Database.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                Supir s = new Supir(
                        rs.getInt("idSupir"),
                        rs.getString("namaSupir"),
                        rs.getString("noHp"),
                        rs.getString("noKendaraan")
                );
                list.add(s);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }

    public Optional<Supir> findById(int idSupir) {
        String sql = "SELECT * FROM supir WHERE idSupir = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, idSupir);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(new Supir(
                            rs.getInt("idSupir"),
                            rs.getString("namaSupir"),
                            rs.getString("noHp"),
                            rs.getString("noKendaraan")
                    ));
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }
}
