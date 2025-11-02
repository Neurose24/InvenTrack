package com.anomaly.inventrack.repositories;

import com.anomaly.inventrack.models.Pengguna;
import com.anomaly.inventrack.utils.Database;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PenggunaRepositories {

    public Pengguna findByUsername(String username) {
        String sql = "SELECT * FROM pengguna WHERE username = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return new Pengguna(
                        rs.getInt("idPengguna"),
                        rs.getString("namaPengguna"),
                        rs.getString("username"),
                        rs.getString("passwordHash"),
                        rs.getInt("idGudang")
                );
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<Pengguna> findAll() {
        List<Pengguna> list = new ArrayList<>();
        String sql = "SELECT * FROM pengguna";

        try (Connection conn = Database.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                Pengguna p = new Pengguna(
                        rs.getInt("idPengguna"),
                        rs.getString("namaPengguna"),
                        rs.getString("username"),
                        rs.getString("passwordHash"),
                        rs.getInt("idGudang")
                );
                list.add(p);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
}
