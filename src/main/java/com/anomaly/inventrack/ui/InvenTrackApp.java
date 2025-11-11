package com.anomaly.inventrack.ui;

import com.anomaly.inventrack.controllers.InventrackController;

import javax.swing.*;
import java.awt.*;

/**
 * Kelas utama aplikasi desktop berbasis Swing.
 */
public class InvenTrackApp {

    // Controller utama (backend)
    private InventrackController controller;

    // Frame utama aplikasi
    private JFrame mainFrame;

    public InvenTrackApp() {
        // Inisialisasi controller backend
        controller = new InventrackController();

        // Inisialisasi tampilan utama
        initUI();
    }

    private void initUI() {
        mainFrame = new JFrame("Inventrack - Sistem Inventaris Multi-Gudang");
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.setSize(800, 600);
        mainFrame.setLocationRelativeTo(null); // Posisikan di tengah layar

        // Contoh tampilan awal (sementara)
        JPanel panel = new JPanel(new BorderLayout());
        JLabel label = new JLabel("Selamat datang di Inventrack (versi Swing)", SwingConstants.CENTER);
        label.setFont(new Font("Segoe UI", Font.BOLD, 18));
        panel.add(label, BorderLayout.CENTER);

        JButton btnKeluar = new JButton("Keluar");
        btnKeluar.addActionListener(e -> System.exit(0));
        panel.add(btnKeluar, BorderLayout.SOUTH);

        mainFrame.add(panel);
        mainFrame.setVisible(true);
    }

    public static void main(String[] args) {
        // Jalankan aplikasi Swing di Event Dispatch Thread
        SwingUtilities.invokeLater(() -> new InvenTrackApp());
    }

    public InventrackController getController() {
        return controller;
    }
}
