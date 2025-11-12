package com.anomaly.inventrack.ui.panels;

import com.anomaly.inventrack.controllers.InventrackController;
import com.anomaly.inventrack.models.Pengguna;
import com.anomaly.inventrack.ui.InvenTrackApp;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Optional;

public class LoginPanel extends JPanel {

    private final InvenTrackApp mainApp;
    private final InventrackController controller;

    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JButton btnLogin;
    private JButton btnRegister; // 🆕 Tombol baru
    private JLabel lblError;

    public LoginPanel(InvenTrackApp mainApp) {
        this.mainApp = mainApp;
        this.controller = mainApp.getController();

        initComponents();
        layoutComponents();
        attachListeners();
    }

    private void initComponents() {
        txtUsername = new JTextField(20);
        txtPassword = new JPasswordField(20);
        btnLogin = new JButton("Login");
        btnRegister = new JButton("Register"); // 🆕 Inisialisasi tombol
        lblError = new JLabel(" ");
        lblError.setForeground(Color.RED);
    }

    private void layoutComponents() {
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 10, 5, 10);
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

        // =========================================================
        // 🆕 PERBAIKAN: Membuat panel untuk menampung kedua tombol
        // =========================================================
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0)); // Rata kanan
        buttonPanel.add(btnRegister); // Tambahkan tombol Register
        buttonPanel.add(btnLogin);    // Tambahkan tombol Login

        // Baris 3: Panel Tombol
        gbc.gridx = 1;
        gbc.gridy = 3;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.EAST;
        add(buttonPanel, gbc);
    }

    private void attachListeners() {
        btnLogin.addActionListener(this::handleLogin);
        txtPassword.addActionListener(this::handleLogin);
        
        // 🆕 Tambahkan listener untuk tombol Register
        btnRegister.addActionListener(this::handleRegister);
    }

    /**
     * Logika yang dijalankan saat tombol Login ditekan.
     */
    private void handleLogin(ActionEvent e) {
        String username = txtUsername.getText();
        String password = new String(txtPassword.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            lblError.setText("Username dan Password wajib diisi.");
            return;
        }

        Optional<Pengguna> loginResult = controller.login(username, password);

        if (loginResult.isPresent()) {
            lblError.setText(" ");
            mainApp.showDashboard(loginResult.get());
        } else {
            lblError.setText("Login Gagal. Cek username/password.");
        }
    }

    /**
     * 🆕 Logika yang dijalankan saat tombol Register ditekan.
     */
    private void handleRegister(ActionEvent e) {
        // Panggil metode di main app untuk beralih layar
        mainApp.showRegisterScreen();
    }
}