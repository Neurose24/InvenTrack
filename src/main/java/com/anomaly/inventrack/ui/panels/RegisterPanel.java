package com.anomaly.inventrack.ui.panels;

import com.anomaly.inventrack.controllers.InventrackController;
import com.anomaly.inventrack.models.Gudang;
import com.anomaly.inventrack.ui.InvenTrackApp;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.List;

public class RegisterPanel extends JPanel {

    private final InvenTrackApp mainApp;
    private final InventrackController controller;

    private JTextField txtNamaPengguna;
    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JPasswordField txtKonfirmasiPassword;
    private JComboBox<Gudang> cmbGudang; // Dropdown untuk gudang
    private JButton btnRegister;
    private JButton btnKembali;
    private JLabel lblError;

    public RegisterPanel(InvenTrackApp mainApp) {
        this.mainApp = mainApp;
        this.controller = mainApp.getController();

        initComponents();
        layoutComponents();
        attachListeners();
    }

    private void initComponents() {
        txtNamaPengguna = new JTextField(20);
        txtUsername = new JTextField(20);
        txtPassword = new JPasswordField(20);
        txtKonfirmasiPassword = new JPasswordField(20);
        cmbGudang = new JComboBox<>();
        btnRegister = new JButton("Daftar");
        btnKembali = new JButton("Kembali");
        lblError = new JLabel(" ");
        lblError.setForeground(Color.RED);
    }

    private void layoutComponents() {
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 10, 5, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Nama Pengguna
        gbc.gridx = 0; gbc.gridy = 0; add(new JLabel("Nama Lengkap:"), gbc);
        gbc.gridx = 1; gbc.gridy = 0; add(txtNamaPengguna, gbc);

        // Username
        gbc.gridx = 0; gbc.gridy = 1; add(new JLabel("Username:"), gbc);
        gbc.gridx = 1; gbc.gridy = 1; add(txtUsername, gbc);

        // Password
        gbc.gridx = 0; gbc.gridy = 2; add(new JLabel("Password:"), gbc);
        gbc.gridx = 1; gbc.gridy = 2; add(txtPassword, gbc);

        // Konfirmasi Password
        gbc.gridx = 0; gbc.gridy = 3; add(new JLabel("Konfirmasi Password:"), gbc);
        gbc.gridx = 1; gbc.gridy = 3; add(txtKonfirmasiPassword, gbc);

        // Gudang
        gbc.gridx = 0; gbc.gridy = 4; add(new JLabel("Gudang:"), gbc);
        gbc.gridx = 1; gbc.gridy = 4; add(cmbGudang, gbc);

        // Label Error
        gbc.gridx = 1; gbc.gridy = 5; add(lblError, gbc);

        // Tombol
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        buttonPanel.add(btnKembali);
        buttonPanel.add(btnRegister);
        gbc.gridx = 1; gbc.gridy = 6; add(buttonPanel, gbc);
    }

    /**
     * Mengambil data gudang dari controller dan mengisinya ke JComboBox
     */
    public void loadGudangData() {
        try {
            List<Gudang> gudangList = controller.getAllGudang();
            DefaultComboBoxModel<Gudang> model = new DefaultComboBoxModel<>();
            for (Gudang g : gudangList) {
                model.addElement(g);
            }
            cmbGudang.setModel(model);

            // Mengatur bagaimana objek Gudang ditampilkan di dropdown
            cmbGudang.setRenderer(new DefaultListCellRenderer() {
                @Override
                public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                    super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                    if (value instanceof Gudang) {
                        setText(((Gudang) value).getNamaGudang());
                    }
                    return this;
                }
            });

        } catch (Exception e) {
            lblError.setText("Gagal memuat data gudang: " + e.getMessage());
        }
    }

    private void attachListeners() {
        btnRegister.addActionListener(this::handleRegister);
        btnKembali.addActionListener(e -> mainApp.showLoginScreen());
    }

    /**
     * Logika yang dijalankan saat tombol Daftar ditekan.
     */
    private void handleRegister(ActionEvent e) {
        String nama = txtNamaPengguna.getText();
        String username = txtUsername.getText();
        String pass1 = new String(txtPassword.getPassword());
        String pass2 = new String(txtKonfirmasiPassword.getPassword());
        Object selectedItem = cmbGudang.getSelectedItem();

        // 1. Validasi Input
        if (nama.isEmpty() || username.isEmpty() || pass1.isEmpty()) {
            lblError.setText("Semua field wajib diisi.");
            return;
        }
        if (!pass1.equals(pass2)) {
            lblError.setText("Password tidak cocok.");
            return;
        }
        if (!(selectedItem instanceof Gudang)) {
            lblError.setText("Gudang harus dipilih.");
            return;
        }

        // 2. Ambil ID Gudang
        Integer idGudang = ((Gudang) selectedItem).getIdGudang();

        // 3. Panggil Controller Backend
        try {
            String hasil = controller.registerPengguna(nama, username, pass1, idGudang);
            
            if (hasil.startsWith("SUKSES")) {
                JOptionPane.showMessageDialog(this, hasil, "Registrasi Berhasil", JOptionPane.INFORMATION_MESSAGE);
                mainApp.showLoginScreen(); // Kembali ke login
            } else {
                lblError.setText(hasil); // Tampilkan pesan error (misal: "Username sudah terdaftar")
            }
        } catch (Exception ex) {
            lblError.setText("Error sistem: " + ex.getMessage());
        }
    }
}