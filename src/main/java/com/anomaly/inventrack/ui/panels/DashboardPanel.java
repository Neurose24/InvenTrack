package com.anomaly.inventrack.ui.panels;

import com.anomaly.inventrack.models.Pengguna;
import com.anomaly.inventrack.ui.InvenTrackApp;

import javax.swing.*;
import java.awt.*;

/**
 * Panel UI Swing untuk Dashboard utama setelah login.
 */
public class DashboardPanel extends JPanel {

    private final InvenTrackApp mainApp;
    private final Pengguna pengguna;

    public DashboardPanel(InvenTrackApp mainApp, Pengguna pengguna) {
        this.mainApp = mainApp;
        this.pengguna = pengguna;

        initComponents();
    }

    private void initComponents() {
        // Menggunakan BorderLayout untuk tata letak utama
        setLayout(new BorderLayout(10, 10)); // (HGap, VGap)

        // 1. Panel Header (Selamat Datang)
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel lblWelcome = new JLabel("Selamat Datang, " + pengguna.getNamaPengguna() + "!");
        lblWelcome.setFont(new Font("Arial", Font.BOLD, 16));
        headerPanel.add(lblWelcome);
        add(headerPanel, BorderLayout.NORTH);

        // 2. Panel Navigasi (Tombol-Tombol Fitur)
        JPanel navigationPanel = new JPanel();
        // Menggunakan GridLayout untuk tombol-tombol yang rapi
        navigationPanel.setLayout(new GridLayout(5, 1, 10, 10)); // (Rows, Cols, HGap, VGap)
        
        JButton btnLihatStok = new JButton("Lihat Stok");
        JButton btnBuatPermintaan = new JButton("Buat Permintaan Baru");
        JButton btnTerimaBarang = new JButton("Terima Pengiriman Barang");
        JButton btnLaporan = new JButton("Lihat Laporan Log");
        JButton btnLogout = new JButton("Logout");

        navigationPanel.add(btnLihatStok);
        navigationPanel.add(btnBuatPermintaan);
        navigationPanel.add(btnTerimaBarang);
        navigationPanel.add(btnLaporan);
        navigationPanel.add(btnLogout);
        
        // Menambahkan panel navigasi ke bagian tengah (CENTER)
        // Kita beri padding agar tidak menempel ke tepi
        JPanel centerPanel = new JPanel(new FlowLayout());
        centerPanel.add(navigationPanel);
        add(centerPanel, BorderLayout.CENTER);

        // 3. Menambahkan Event Listeners (Masih Kosong)
        btnLihatStok.addActionListener(e -> {
            // TODO: Tampilkan panel lihat stok
            JOptionPane.showMessageDialog(this, "Fitur 'Lihat Stok' belum dibuat.");
        });

        btnBuatPermintaan.addActionListener(e -> {
            // TODO: Tampilkan panel buat permintaan
            JOptionPane.showMessageDialog(this, "Fitur 'Buat Permintaan' belum dibuat.");
        });
        
        btnLogout.addActionListener(e -> {
            // Kembali ke layar login
            mainApp.showLoginScreen();
        });
    }
}