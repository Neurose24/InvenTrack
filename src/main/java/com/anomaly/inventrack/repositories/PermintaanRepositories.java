package com.anomaly.inventrack.repositories;

import com.anomaly.inventrack.models.Permintaan;
import com.anomaly.inventrack.utils.Database;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PermintaanRepositories {

    public List<Permintaan> findAll() {
        List<Permintaan> list = new ArrayList<>();
        // Pastikan nama kolom sesuai dengan database Anda (snake_case)
        String sql = "SELECT * FROM permintaan";

        try (Connection conn = Database.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                list.add(mapResultSetToPermintaan(rs)); // Gunakan helper method
            }

        } catch (SQLException e) {
            // Sebaiknya throw RuntimeException agar tidak silent failure
            throw new RuntimeException("Gagal load semua permintaan: " + e.getMessage(), e);
        }
        return list;
    }

    public Optional<Permintaan> findById(int idPermintaan) {
        String sql = "SELECT * FROM permintaan WHERE id_permintaan = ?";

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, idPermintaan);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToPermintaan(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Gagal mencari permintaan by ID: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    public void insert(Connection conn, Permintaan permintaan) throws SQLException {
        // PERBAIKAN: Tambahkan id_gudang_sumber ke dalam query INSERT
        String sql = "INSERT INTO permintaan (id_pengguna_peminta, id_gudang_sumber, tanggal_permintaan, status_permintaan, catatan_permintaan) " +
                     "VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, permintaan.getIdPenggunaPeminta());
            
            // PERBAIKAN: Set id_gudang_sumber (tidak boleh null jika logika Anda Direct Request)
            if (permintaan.getIdGudangSumber() == null) {
                throw new SQLException("Gudang sumber (tujuan permintaan) harus dipilih!");
            }
            ps.setInt(2, permintaan.getIdGudangSumber());
            
            ps.setTimestamp(3, Timestamp.valueOf(permintaan.getTanggalPermintaan()));
            ps.setString(4, permintaan.getStatusPermintaan().name());
            ps.setString(5, permintaan.getCatatanPermintaan());

            ps.executeUpdate();

            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    permintaan.setIdPermintaan(generatedKeys.getInt(1));
                }
            }
        }
    }

    // 🆕 METHOD BARU: Approve Request (Update Status & Sumber)
    public void approveRequest(Connection conn, int idPermintaan, int idGudangSumber) throws SQLException {
        String sql = "UPDATE permintaan SET status_permintaan = ?, id_gudang_sumber = ? WHERE id_permintaan = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, Permintaan.StatusPermintaan.DISETUJUI.name());
            ps.setInt(2, idGudangSumber); // Simpan ID Gudang Sumber
            ps.setInt(3, idPermintaan);
            ps.executeUpdate();
        }
    }

    public void updateStatus(Connection conn, int idPermintaan, Permintaan.StatusPermintaan status) throws SQLException {
        String sql = "UPDATE permintaan SET status_permintaan = ? WHERE id_permintaan = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status.name());
            ps.setInt(2, idPermintaan);
            ps.executeUpdate();
        }
    }
    
    // Helper untuk mapping ResultSet ke Object (Mencegah duplikasi kode)
    private Permintaan mapResultSetToPermintaan(ResultSet rs) throws SQLException {
        Permintaan p = new Permintaan();
        p.setIdPermintaan(rs.getInt("id_permintaan"));
        p.setIdPenggunaPeminta(rs.getInt("id_pengguna_peminta"));
        
        // Handle id_gudang_sumber yang bisa NULL
        int idSumber = rs.getInt("id_gudang_sumber");
        if (!rs.wasNull()) {
            p.setIdGudangSumber(idSumber);
        }

        p.setTanggalPermintaan(rs.getTimestamp("tanggal_permintaan").toLocalDateTime());
        
        // Konversi String DB ke Enum Java
        try {
            p.setStatusPermintaan(Permintaan.StatusPermintaan.valueOf(rs.getString("status_permintaan")));
        } catch (IllegalArgumentException | NullPointerException e) {
            // Fallback jika enum tidak cocok
            p.setStatusPermintaan(Permintaan.StatusPermintaan.MENUNGGU); 
        }
        
        p.setCatatanPermintaan(rs.getString("catatan_permintaan"));
        return p;
    }
}