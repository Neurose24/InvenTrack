package com.anomaly.inventrack.repositories;

import com.anomaly.inventrack.models.Pengguna;
import com.anomaly.inventrack.utils.Database;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PenggunaRepositories {

    public Optional<Pengguna> findByUsername(String username) { // <-- Return type diubah
        String sql = "SELECT * FROM pengguna WHERE username = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) { // <-- Gunakan try-with-resources untuk ResultSet
                if (rs.next()) {
                    return Optional.of(new Pengguna( // <-- Gunakan Optional.of()
                        rs.getInt("idPengguna"),
                        rs.getString("namaPengguna"),
                        rs.getString("username"),
                        rs.getString("passwordHash"),
                        rs.getInt("idGudang")
                    ));
                }
            }
        } catch (SQLException e) {
            // Lempar RuntimeException agar Service bisa menangkapnya
            throw new RuntimeException("Gagal mencari pengguna berdasarkan username: " + e.getMessage(), e); 
        }
        return Optional.empty(); // <-- Kembalikan Optional kosong jika tidak ditemukan
    }

    public Optional<Pengguna> findById(int idPengguna) {
        String sql = "SELECT * FROM pengguna WHERE idPengguna = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, idPengguna);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(new Pengguna(
                        rs.getInt("idPengguna"),
                        rs.getString("namaPengguna"),
                        rs.getString("username"),
                        rs.getString("passwordHash"),
                        rs.getInt("idGudang")
                    ));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Gagal mencari pengguna berdasarkan ID: " + e.getMessage(), e);
        }
        return Optional.empty();
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

    public int findGudangIdByPenggunaId(int idPengguna) {
        String sql = "SELECT idGudang FROM pengguna WHERE idPengguna = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, idPengguna);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getInt("idGudang");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1; // atau lempar exception sesuai kebutuhan
    }
}
