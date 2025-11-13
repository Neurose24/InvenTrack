package com.anomaly.inventrack.ui;

import com.anomaly.inventrack.controllers.InventrackController;
import com.anomaly.inventrack.models.Pengguna;
import com.anomaly.inventrack.ui.panels.LoginPanel;
import com.anomaly.inventrack.ui.panels.DashboardPanel;
import com.anomaly.inventrack.ui.panels.RegisterPanel;

import javax.swing.*;
import java.awt.*;

/**
 * Kelas Utama Aplikasi Swing.
 * Bertanggung jawab untuk membuat Controller, Frame, dan menukar Panel.
 */
public class InvenTrackApp {

    private JFrame mainFrame;
    private InventrackController controller;

    private final Dimension FIXED_WINDOW_SIZE = new Dimension(1100, 700);

    public static void main(String[] args) {
        EventQueue.invokeLater(() -> {
            try {
                // Terapkan Look and Feel yang lebih baik (Nimbus)
                for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                    if ("Nimbus".equals(info.getName())) {
                        UIManager.setLookAndFeel(info.getClassName());
                        break;
                    }
                }
            } catch (Exception e) {
                // Jika Nimbus tidak tersedia, gunakan default
            }
            
            try {
                InvenTrackApp app = new InvenTrackApp();
                app.createAndShowGUI();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public InvenTrackApp() {
        this.controller = new InventrackController();
    }

    private void createAndShowGUI() {
        mainFrame = new JFrame("Inventrack - Sistem Inventaris");
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.setResizable(false); // Mencegah ukuran diubah

        showLoginScreen();

        mainFrame.setVisible(true);
    }

    public void showLoginScreen() {
        LoginPanel loginPanel = new LoginPanel(this);
        mainFrame.setContentPane(loginPanel);
        mainFrame.pack(); // Mengatur ukuran frame pas dengan LoginPanel
        mainFrame.setLocationRelativeTo(null); // Posisikan di tengah
        mainFrame.validate();
    }

    public void showDashboard(Pengguna pengguna) {
        DashboardPanel dashboardPanel = new DashboardPanel(this, pengguna);
        mainFrame.setContentPane(dashboardPanel);
        mainFrame.pack(); // Mengatur ukuran frame pas dengan DashboardPanel
        mainFrame.setLocationRelativeTo(null);
        mainFrame.validate();
        mainFrame.repaint();
    }

    public void showRegisterScreen() {
        RegisterPanel registerPanel = new RegisterPanel(this);
        mainFrame.setContentPane(registerPanel);
        mainFrame.pack();
        mainFrame.setLocationRelativeTo(null);
        mainFrame.validate();
        mainFrame.repaint();
        
        registerPanel.loadGudangData();
    }
    
    // Memberikan akses Controller ke semua panel
    public InventrackController getController() {
        return controller;
    }
}
