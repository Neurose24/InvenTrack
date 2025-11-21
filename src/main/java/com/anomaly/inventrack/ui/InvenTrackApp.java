package com.anomaly.inventrack.ui;

import com.anomaly.inventrack.controllers.InventrackController;
import com.anomaly.inventrack.models.Pengguna;
// Pastikan import panel dan frame yang sesuai
import com.anomaly.inventrack.ui.panels.LoginPanel;
// Kita import DashboardFrame (Window terpisah), BUKAN DashboardPanel
import com.anomaly.inventrack.ui.panels.DashboardFrame; 

import javax.swing.*;
import java.awt.*;

/**
 * Kelas Utama Aplikasi Swing.
 * Mengatur navigasi antar layar (Login <-> Register <-> Dashboard)
 */
public class InvenTrackApp {

    private JFrame mainFrame; // Frame untuk Login/Register
    private InventrackController controller;

    public static void main(String[] args) {
        EventQueue.invokeLater(() -> {
            try {
                // Terapkan Look and Feel Nimbus agar lebih modern
                for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                    if ("Nimbus".equals(info.getName())) {
                        UIManager.setLookAndFeel(info.getClassName());
                        break;
                    }
                }
            } catch (Exception e) {
                // Abaikan jika gagal, pakai default
            }
            
            try {
                InvenTrackApp app = new InvenTrackApp();
                app.start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public InvenTrackApp() {
        this.controller = new InventrackController();
    }

    public void start() {
        showLoginScreen();
    }

    /**
     * Menampilkan Layar Login.
     * Jika frame belum ada (atau sudah didispose), buat baru.
     */
    public void showLoginScreen() {
        // Cek apakah mainFrame perlu dibuat ulang (misal setelah logout)
        if (mainFrame == null || !mainFrame.isDisplayable()) {
            mainFrame = new JFrame("Inventrack - Login");
            mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            mainFrame.setResizable(false); // Login tidak perlu di-resize
        }

        LoginPanel loginPanel = new LoginPanel(this);
        mainFrame.setContentPane(loginPanel);
        
        mainFrame.pack(); // Sesuaikan ukuran frame dengan isi LoginPanel
        mainFrame.setLocationRelativeTo(null); // Taruh di tengah layar
        mainFrame.setVisible(true);
    }



    /**
     * Menampilkan Dashboard.
     * INI BAGIAN PENTING: Menutup Login Frame -> Membuka Dashboard Frame.
     */
    public void showDashboard(Pengguna pengguna) {
        // 1. Tutup (Dispose) Frame Login/Register
        if (mainFrame != null) {
            mainFrame.dispose(); 
            mainFrame = null; // Kosongkan referensi
        }

        // 2. Buat Frame Dashboard Baru (Layar Penuh)
        // Pastikan kamu sudah membuat file DashboardFrame.java
        DashboardFrame dashboard = new DashboardFrame(pengguna); 
        
        // Opsi tambahan: Kirim referensi 'this' jika dashboard butuh akses logout
        // DashboardFrame dashboard = new DashboardFrame(this, pengguna);
        
        dashboard.setVisible(true);
    }
    
    /**
     * Method untuk Logout (Dipanggil dari Dashboard).
     * Menutup dashboard dan kembali ke Login.
     */
    public void logout(JFrame dashboardFrame) {
        dashboardFrame.dispose(); // Tutup dashboard
        showLoginScreen(); // Buka lagi login screen baru
    }
    
    public InventrackController getController() {
        return controller;
    }
}