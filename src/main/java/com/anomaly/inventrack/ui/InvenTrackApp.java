package com.anomaly.inventrack.ui;

import com.anomaly.inventrack.controllers.InventrackController;
import com.anomaly.inventrack.models.Pengguna;
import com.anomaly.inventrack.ui.panels.LoginPanel;

import javax.swing.*;
import java.awt.*;

/**
 * Kelas Utama Aplikasi Swing.
 * Bertanggung jawab untuk membuat Controller, Frame, dan menukar Panel.
 */
public class InvenTrackApp {

    private JFrame mainFrame;
    private InventrackController controller;

    public static void main(String[] args) {
        // Menjalankan aplikasi di Event Dispatch Thread (EDT) - Wajib untuk Swing
        EventQueue.invokeLater(() -> {
            try {
                InvenTrackApp app = new InvenTrackApp();
                app.createAndShowGUI();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public InvenTrackApp() {
        // Inisialisasi Controller Backend
        this.controller = new InventrackController();
    }

    private void createAndShowGUI() {
        // Membuat Jendela Utama
        mainFrame = new JFrame("Inventrack - Sistem Inventaris");
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.setSize(400, 300); // Ukuran awal
        mainFrame.setLocationRelativeTo(null); // Tampilkan di tengah layar

        // Tampilkan LoginPanel saat startup
        showLoginScreen();

        mainFrame.setVisible(true);
    }

    public void showLoginScreen() {
        LoginPanel loginPanel = new LoginPanel(this); 
        mainFrame.setContentPane(loginPanel); 
        mainFrame.validate(); 
    }

    public void showDashboard(Pengguna pengguna) {
        // TODO: Ganti dengan panel Dashboard Anda
        // DashboardPanel dashboard = new DashboardPanel(this, pengguna);
        // mainFrame.setContentPane(dashboard);
        // mainFrame.validate();
        
        // Untuk saat ini, kita tutup saja
        JOptionPane.showMessageDialog(mainFrame, "Login Berhasil! Selamat datang, " + pengguna.getNamaPengguna());
        // System.exit(0);
    }
    
    // Memberikan akses Controller ke semua panel
    public InventrackController getController() {
        return controller;
    }
}
