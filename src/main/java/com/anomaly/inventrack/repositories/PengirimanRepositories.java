package com.anomaly.inventrack.repositories;

import com.anomaly.inventrack.models.Pengiriman;
import com.anomaly.inventrack.utils.Database;

import java.lang.StackWalker.Option;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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

    public Optional<Pengiriman> findById(int idPengiriman) {
        String sql = "SELECT * FROM pengiriman WHERE idPengiriman = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, idPengiriman);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(new Pengiriman(
                        rs.getInt("idPengiriman"),
                        rs.getInt("idPermintaan"),
                        rs.getInt("idPenggunaPengirim"),
                        rs.getInt("idPenggunaPenerima"),
                        rs.getInt("idSupir"),
                        rs.getTimestamp("tanggalPengiriman").toLocalDateTime(),
                        rs.getString("noKendaraan"),
                        rs.getObject("statusPengiriman", Pengiriman.StatusPengiriman.class),
                        rs.getString("keteranganPengiriman")
                    ));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Gagal mencari pengiriman berdasarkan ID: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    public void insert(Connection conn, Pengiriman pengiriman) throws SQLException {
        String sql = "INSERT INTO pengiriman (idPermintaan, idPenggunaPengirim, idPenggunaPenerima, idSupir, " +
                     "tanggalPengiriman, noKendaraan, statusPengiriman, keteranganPengiriman) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setObject(1, pengiriman.getIdPermintaan()); 
            ps.setInt(2, pengiriman.getIdPenggunaPengirim());
            ps.setInt(3, pengiriman.getIdPenggunaPenerima());
            ps.setInt(4, pengiriman.getIdSupir());
            ps.setTimestamp(5, Timestamp.valueOf(pengiriman.getTanggalPengiriman()));
            ps.setString(6, pengiriman.getNoKendaraan());
            ps.setObject(7, pengiriman.getStatusPengiriman());
            ps.setString(8, pengiriman.getKeteranganPengiriman());
            ps.executeUpdate();
            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    pengiriman.setIdPengiriman(generatedKeys.getInt(1));
                }
            }
        }
    }

    public void updateStatus(Connection conn, int idPengiriman, Pengiriman.StatusPengiriman status) throws SQLException {
    String sql = "UPDATE pengiriman SET statusPengiriman = ? WHERE idPengiriman = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, status.toString());
            ps.setInt(2, idPengiriman);
            ps.executeUpdate();
        }
    }
}