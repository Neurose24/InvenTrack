package com.anomaly.inventrack.ui.panels;

import com.anomaly.inventrack.controllers.InventrackController;
import com.anomaly.inventrack.models.Pengguna;
import com.anomaly.inventrack.ui.InvenTrackApp;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Optional;

/**
 * Panel UI Swing untuk layar Login.
 */
public class LoginPanel extends JPanel {

    private final InvenTrackApp mainApp;
    private final InventrackController controller;

    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JButton btnLogin;
    private JLabel lblError;

    public LoginPanel(InvenTrackApp mainApp) {
        this.mainApp = mainApp;
        this.controller = mainApp.getController(); // Dapatkan controller dari App utama

        initComponents();
        layoutComponents();
        attachListeners();
    }

    private void initComponents() {
        txtUsername = new JTextField(20);
        txtPassword = new JPasswordField(20);
        btnLogin = new JButton("Login");
        lblError = new JLabel(" "); // Label untuk pesan error
        lblError.setForeground(Color.RED);
    }

    private void layoutComponents() {
        // Menggunakan GridBagLayout untuk form yang rapi
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 10, 5, 10); // Padding
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Baris 0: Label Username
        gbc.gridx = 0;
        gbc.gridy = 0;
        add(new JLabel("Username:"), gbc);

        // Baris 0: Textbox Username
        gbc.gridx = 1;
        gbc.gridy = 0;
        add(txtUsername, gbc);

        // Baris 1: Label Password
        gbc.gridx = 0;
        gbc.gridy = 1;
        add(new JLabel("Password:"), gbc);

        // Baris 1: Textbox Password
        gbc.gridx = 1;
        gbc.gridy = 1;
        add(txtPassword, gbc);

        // Baris 2: Label Error
        gbc.gridx = 1;
        gbc.gridy = 2;
        add(lblError, gbc);

        // Baris 3: Tombol Login
        gbc.gridx = 1;
        gbc.gridy = 3;
        gbc.fill = GridBagConstraints.NONE; // Jangan lebarkan tombol
        gbc.anchor = GridBagConstraints.EAST; // Rata kanan
        add(btnLogin, gbc);
    }

    private void attachListeners() {
        // Event handler saat tombol login ditekan
        btnLogin.addActionListener(this::handleLogin);
        
        // Juga handle saat menekan "Enter" di field password
        txtPassword.addActionListener(this::handleLogin);
    }

    /**
     * Logika yang dijalankan saat tombol Login ditekan.
     */
    private void handleLogin(ActionEvent e) {
        String username = txtUsername.getText();
        String password = new String(txtPassword.getPassword());

        // Validasi input
        if (username.isEmpty() || password.isEmpty()) {
            lblError.setText("Username dan Password wajib diisi.");
            return;
        }

        // Panggil Controller Backend
        Optional<Pengguna> loginResult = controller.login(username, password);

        // Proses hasil
        if (loginResult.isPresent()) {
            // Sukses
            lblError.setText(" ");
            mainApp.showDashboard(loginResult.get());
        } else {
            // Gagal
            lblError.setText("Login Gagal. Cek username/password.");
        }
    }
}