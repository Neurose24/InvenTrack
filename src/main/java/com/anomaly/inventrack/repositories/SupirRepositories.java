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
                        rs.getInt("id_supir"),
                        rs.getString("nama_supir"),
                        rs.getString("no_hp"),
                        rs.getString("no_kendaraan")
                );
                list.add(s);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }

    public Optional<Supir> findById(int id_supir) {
        String sql = "SELECT * FROM supir WHERE id_supir = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id_supir);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(new Supir(
                            rs.getInt("id_supir"),
                            rs.getString("nama_supir"),
                            rs.getString("no_hp"),
                            rs.getString("no_kendaraan")
                    ));
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    public void saveSupir(Supir supir) {
        String sql = "INSERT INTO supir (nama_supir, no_hp, no_kendaraan) VALUES (?, ?, ?)";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, supir.getNamaSupir());
            ps.setString(2, supir.getNoHp());
            ps.setString(3, supir.getNoKendaraan());

            int affectedRows = ps.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Membuat supir gagal, tidak ada baris yang terpengaruh.");
            }

            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    supir.setIdSupir(generatedKeys.getInt(1));
                } else {
                    throw new SQLException("Membuat supir gagal, tidak mendapatkan ID.");
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean deleteSupir(int id_supir) {
        String sql = "DELETE FROM supir WHERE id_supir = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id_supir);
            int affectedRows = ps.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
