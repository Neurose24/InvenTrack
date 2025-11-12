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
                        rs.getInt("id_pengguna"),
                        rs.getString("nama_pengguna"),
                        rs.getString("username"),
                        rs.getString("password"),
                        rs.getInt("id_gudang")
                    ));
                }
            }
        } catch (SQLException e) {
            // Lempar RuntimeException agar Service bisa menangkapnya
            throw new RuntimeException("Gagal mencari pengguna berdasarkan username: " + e.getMessage(), e); 
        }
        return Optional.empty(); // <-- Kembalikan Optional kosong jika tidak ditemukan
    }

    public Optional<Pengguna> findById(int id_pengguna) {
        String sql = "SELECT * FROM pengguna WHERE id_pengguna = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id_pengguna);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(new Pengguna(
                        rs.getInt("id_pengguna"),
                        rs.getString("nama_pengguna"),
                        rs.getString("username"),
                        rs.getString("password"),
                        rs.getInt("id_gudang")
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
                        rs.getInt("id_pengguna"),
                        rs.getString("nama_pengguna"),
                        rs.getString("username"),
                        rs.getString("password"),
                        rs.getInt("id_gudang")
                );
                list.add(p);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public int findGudangIdByPenggunaId(int id_pengguna) {
        String sql = "SELECT id_gudang FROM pengguna WHERE id_pengguna = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id_pengguna);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getInt("id_gudang");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1; // atau lempar exception sesuai kebutuhan
    }

    public Pengguna saveUser(Pengguna pengguna) {
        String sql = "INSERT INTO pengguna (nama_pengguna, username, password, id_gudang) VALUES (?, ?, ?, ?)";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            ps.setString(1, pengguna.getNamaPengguna());
            ps.setString(2, pengguna.getUsername());
            ps.setString(3, pengguna.getPasswordHash());
            ps.setInt(4, pengguna.getIdGudang());
            
            int affectedRows = ps.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Gagal menyimpan pengguna, tidak ada baris yang terpengaruh.");
            }

            try (ResultSet gk = ps.getGeneratedKeys()) {
                if (gk.next()) {
                    pengguna.setIdPengguna(gk.getInt(1));
                }
            }
            return pengguna;
        } catch (SQLException e) {
            // Lempar RuntimeException agar service tahu
            throw new RuntimeException("Gagal menyimpan pengguna baru: " + e.getMessage(), e);
        }
    }
}
