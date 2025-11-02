package com.anomaly.inventrack.repositories;

import com.anomaly.inventrack.models.Stok;
import com.anomaly.inventrack.utils.Database;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class StokRepositories {

    public List<Stok> getAll() {
        List<Stok> list = new ArrayList<>();
        String sql = "SELECT * FROM stok";

        try (Connection conn = Database.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Stok stok = new Stok(
                        rs.getInt("idStok"),
                        rs.getInt("idGudang"),
                        rs.getInt("idBarang"),
                        rs.getInt("jumlahStok")
                );
                list.add(stok);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public Stok getById(int idStok) {
        String sql = "SELECT * FROM stok WHERE idStok = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, idStok);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Stok(
                            rs.getInt("idStok"),
                            rs.getInt("idGudang"),
                            rs.getInt("idBarang"),
                            rs.getInt("jumlahStok")
                    );
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean insert(Stok stok) {
        String sql = "INSERT INTO stok (idGudang, idBarang, jumlahStok) VALUES (?, ?, ?)";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, stok.getIdGudang());
            ps.setInt(2, stok.getIdBarang());
            ps.setInt(3, stok.getJumlahStok());
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean updateJumlahStok(int idStok, int jumlahStokBaru) {
        String sql = "UPDATE stok SET jumlahStok = ? WHERE idStok = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, jumlahStokBaru);
            ps.setInt(2, idStok);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean delete(int idStok) {
        String sql = "DELETE FROM stok WHERE idStok = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, idStok);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public List<Stok> getByGudang(int idGudang) {
        List<Stok> list = new ArrayList<>();
        String sql = "SELECT * FROM stok WHERE idGudang = ?";

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, idGudang);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Stok stok = new Stok(
                            rs.getInt("idStok"),
                            rs.getInt("idGudang"),
                            rs.getInt("idBarang"),
                            rs.getInt("jumlahStok")
                    );
                    list.add(stok);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
}
