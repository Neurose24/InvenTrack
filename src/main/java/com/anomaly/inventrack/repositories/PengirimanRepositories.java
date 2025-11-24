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
                String statusStr = rs.getString("status_pengiriman");
                Pengiriman.StatusPengiriman statusEnum = Pengiriman.StatusPengiriman.DIKIRIM;
                
                if (statusStr != null) {
                    try {
                        statusEnum = Pengiriman.StatusPengiriman.valueOf(statusStr);
                    } catch (IllegalArgumentException e) {
                        System.err.println("Status tidak dikenal: " + statusStr);
                    }
                }
                Pengiriman p = new Pengiriman(
                        rs.getInt("id_pengiriman"),
                        rs.getInt("id_permintaan"),
                        rs.getInt("id_pengguna_pengirim"),
                        rs.getInt("id_pengguna_penerima"),
                        rs.getInt("id_supir"),
                        rs.getTimestamp("tanggal_pengiriman").toLocalDateTime(),
                        rs.getString("no_kendaraan"),
                        statusEnum,
                        rs.getString("keterangan_pengiriman")
                );
                list.add(p);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public Optional<Pengiriman> findById(int id_pengiriman) {
        String sql = "SELECT * FROM pengiriman WHERE id_pengiriman = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id_pengiriman);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String statusStr = rs.getString("status_pengiriman");
                    Pengiriman.StatusPengiriman statusEnum = Pengiriman.StatusPengiriman.DIKIRIM;
                    if (statusStr != null) {
                        try {
                            statusEnum = Pengiriman.StatusPengiriman.valueOf(statusStr);
                        } catch (Exception e) {}
                    }
                    return Optional.of(new Pengiriman(
                        rs.getInt("id_pengiriman"),
                        rs.getInt("id_permintaan"),
                        rs.getInt("id_pengguna_pengirim"),
                        rs.getInt("id_pengguna_penerima"),
                        rs.getInt("id_supir"),
                        rs.getTimestamp("tanggal_pengiriman").toLocalDateTime(),
                        rs.getString("no_kendaraan"),
                        statusEnum,
                        rs.getString("keterangan_pengiriman")
                    ));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Gagal mencari pengiriman berdasarkan ID: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    public void insert(Connection conn, Pengiriman pengiriman) throws SQLException {
        String sql = "INSERT INTO pengiriman (id_permintaan, id_pengguna_pengirim, id_pengguna_penerima, id_supir, " +
                     "tanggal_pengiriman, no_kendaraan, status_pengiriman, keterangan_pengiriman) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setObject(1, pengiriman.getIdPermintaan()); 
            ps.setInt(2, pengiriman.getIdPenggunaPengirim());
            ps.setInt(3, pengiriman.getIdPenggunaPenerima());
            if (pengiriman.getIdSupir() != null) ps.setInt(4, pengiriman.getIdSupir());
            else ps.setNull(4, java.sql.Types.INTEGER);
            ps.setTimestamp(5, Timestamp.valueOf(pengiriman.getTanggalPengiriman()));
            ps.setString(6, pengiriman.getNoKendaraan());
            ps.setString(7, pengiriman.getStatusPengiriman().name()); 
            ps.setString(8, pengiriman.getKeteranganPengiriman());
            ps.executeUpdate();
            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    pengiriman.setIdPengiriman(generatedKeys.getInt(1));
                }
            }
        }
    }

    public void updateStatus(Connection conn, int id_pengiriman, Pengiriman.StatusPengiriman status) throws SQLException {
        String sql = "UPDATE pengiriman SET status_pengiriman = ? WHERE id_pengiriman = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status.name());
            ps.setInt(2, id_pengiriman);
            ps.executeUpdate();
        }
    }
}