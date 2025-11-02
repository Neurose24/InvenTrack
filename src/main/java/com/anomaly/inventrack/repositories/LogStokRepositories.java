package com.anomaly.inventrack.repositories;

import com.anomaly.inventrack.models.LogStok;
import com.anomaly.inventrack.utils.Database;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class LogStokRepositories {

    public List<LogStok> findAll() {
        List<LogStok> list = new ArrayList<>();
        String sql = "SELECT * FROM log_stok ORDER BY tanggal DESC";

        try (Connection conn = Database.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                LogStok log = new LogStok(
                        rs.getInt("idLog"),
                        rs.getInt("idGudang"),
                        rs.getInt("idBarang"),
                        rs.getObject("tipeTransaksi", LogStok.TipeTransaksi.class),
                        rs.getInt("jumlahPerubahan"),
                        rs.getTimestamp("tanggalLog").toLocalDateTime(),
                        rs.getString("keterangan")
                );
                list.add(log);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }
}
