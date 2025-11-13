package com.anomaly.inventrack.ui.panels;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Panel branding (sisi kiri) yang dapat digunakan kembali
 * untuk layar Login dan Register.
 */
public class BrandingPanel extends JPanel {

    // Definisikan warna di sini agar bisa digunakan panel lain
    public static final Color COLOR_TEAL = new Color(170, 204, 213);
    public static final Color COLOR_WHITE = Color.WHITE;
    public static final Color COLOR_BLACK = Color.BLACK;
    public static final Color COLOR_LIGHT_GRAY = new Color(220, 220, 220);

    public BrandingPanel() {
        initComponents();
    }

    private void initComponents() {
        setLayout(new GridBagLayout());
        setBackground(COLOR_TEAL);
        setBorder(new EmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();

        // Placeholder untuk Logo (bisa diganti JLabel dengan ImageIcon)
        JLabel lblLogo;
        try {
            // Mengambil gambar dari folder resources (classpath)
            java.net.URL logoURL = getClass().getResource("/img/logo_inventrack.png");
            
            if (logoURL != null) {
                ImageIcon logoIcon = new ImageIcon(logoURL);
                lblLogo = new JLabel(logoIcon);
            } else {
                // Fallback jika gambar tidak ditemukan
                lblLogo = new JLabel("LOGO");
                lblLogo.setFont(new Font("Arial", Font.BOLD, 48));
                lblLogo.setForeground(COLOR_WHITE);
            }
        } catch (Exception e) {
            e.printStackTrace();
            // Fallback jika terjadi error
            lblLogo = new JLabel("LOGO (Error)");
            lblLogo.setFont(new Font("Arial", Font.BOLD, 48));
            lblLogo.setForeground(COLOR_WHITE);
        }

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weighty = 0.8; // Beri ruang lebih di atas
        add(lblLogo, gbc); // Tambahkan JLabel (baik teks atau gambar)

        JLabel lblCopyright = new JLabel("copyright © anomaly all rights reserved");
        lblCopyright.setFont(new Font("Arial", Font.PLAIN, 10));
        lblCopyright.setForeground(COLOR_WHITE);
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weighty = 0.1;
        gbc.anchor = GridBagConstraints.PAGE_END; // Ke bawah
        add(lblCopyright, gbc);
    }
}