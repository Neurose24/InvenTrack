package com.anomaly.inventrack.ui.panels;

import com.anomaly.inventrack.controllers.InventrackController;
import com.anomaly.inventrack.models.Pengguna;
import com.anomaly.inventrack.ui.InvenTrackApp;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Optional;

// Menggunakan warna dari BrandingPanel
import static com.anomaly.inventrack.ui.panels.BrandingPanel.COLOR_TEAL;
import static com.anomaly.inventrack.ui.panels.BrandingPanel.COLOR_WHITE;
import static com.anomaly.inventrack.ui.panels.BrandingPanel.COLOR_BLACK;

public class LoginPanel extends JPanel {

    private final InvenTrackApp mainApp;
    private final InventrackController controller;

    // Komponen form
    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JButton btnLogin;
    private JButton btnRegister;
    private JLabel lblError;

    public LoginPanel(InvenTrackApp mainApp) {
        this.mainApp = mainApp;
        this.controller = mainApp.getController();

        // Layout utama: 1 baris, 2 kolom
        setLayout(new GridLayout(1, 2));

        // Tambahkan panel branding (kiri) dan panel form (kanan)
        add(new BrandingPanel());
        add(createLoginFormPanel()); // Buat panel form
        
        // Listener harus dipasang setelah komponen dibuat
        attachListeners();
    }

    /**
     * Membuat Panel Form Login (Sisi Kanan)
     */
    private JPanel createLoginFormPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(COLOR_WHITE);
        panel.setBorder(new EmptyBorder(20, 30, 20, 30));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Inisialisasi komponen
        txtUsername = new JTextField(20);
        txtPassword = new JPasswordField(20);
        btnLogin = new JButton("Login");
        btnRegister = new JButton("Sign Up");
        lblError = new JLabel(" ");
        lblError.setForeground(Color.RED);
        lblError.setHorizontalAlignment(SwingConstants.CENTER);

        // Judul "LOGIN"
        JLabel lblTitle = new JLabel("LOGIN");
        lblTitle.setFont(new Font("Arial", Font.BOLD, 28));
        lblTitle.setForeground(COLOR_BLACK);
        lblTitle.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        gbc.insets = new Insets(10, 8, 20, 8);
        panel.add(lblTitle, gbc);

        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.gridwidth = 1;

        // Form fields
        gbc.gridx = 0; gbc.gridy = 1; panel.add(new JLabel("Username:"), gbc);
        gbc.gridx = 1; gbc.gridy = 1; panel.add(txtUsername, gbc);

        gbc.gridx = 0; gbc.gridy = 2; panel.add(new JLabel("Password:"), gbc);
        gbc.gridx = 1; gbc.gridy = 2; panel.add(txtPassword, gbc);

        // Label Error
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        panel.add(lblError, gbc);

        // Tombol Login (Utama)
        btnLogin.setBackground(COLOR_TEAL);
        btnLogin.setForeground(COLOR_BLACK);
        btnLogin.setFont(new Font("Arial", Font.BOLD, 14));
        btnLogin.setOpaque(true);
        btnLogin.setBorderPainted(false);
        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2;
        gbc.insets = new Insets(15, 8, 8, 8);
        panel.add(btnLogin, gbc);

        // "I don't have an account"
        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 1;
        gbc.insets = new Insets(20, 8, 8, 8);
        gbc.anchor = GridBagConstraints.EAST;
        gbc.fill = GridBagConstraints.NONE;
        panel.add(new JLabel("I don't have an account"), gbc);

        // Tombol Sign Up (Sekunder)
        btnRegister.setBackground(COLOR_WHITE);
        btnRegister.setForeground(Color.DARK_GRAY);
        gbc.gridx = 1; gbc.gridy = 5;
        gbc.insets = new Insets(20, 8, 8, 8);
        gbc.anchor = GridBagConstraints.WEST;
        panel.add(btnRegister, gbc);

        return panel;
    }

    private void attachListeners() {
        btnLogin.addActionListener(this::handleLogin);
        txtPassword.addActionListener(this::handleLogin);
        btnRegister.addActionListener(e -> mainApp.showRegisterScreen());
    }

    private void handleLogin(ActionEvent e) {
        String username = txtUsername.getText();
        String password = new String(txtPassword.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            lblError.setText("Username dan Password wajib diisi.");
            return;
        }

        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        Optional<Pengguna> loginResult = controller.login(username, password);
        setCursor(Cursor.getDefaultCursor());

        if (loginResult.isPresent()) {
            lblError.setText(" ");
            mainApp.showDashboard(loginResult.get());
        } else {
            lblError.setText("Login Gagal. Cek username/password.");
        }
    }
}