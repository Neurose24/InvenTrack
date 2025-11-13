package com.anomaly.inventrack.ui.panels;

import com.anomaly.inventrack.controllers.InventrackController;
import com.anomaly.inventrack.models.Gudang;
import com.anomaly.inventrack.ui.InvenTrackApp;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.List;

// Menggunakan warna dari BrandingPanel
import static com.anomaly.inventrack.ui.panels.BrandingPanel.COLOR_TEAL;
import static com.anomaly.inventrack.ui.panels.BrandingPanel.COLOR_WHITE;
import static com.anomaly.inventrack.ui.panels.BrandingPanel.COLOR_BLACK;

public class RegisterPanel extends JPanel {

    private final InvenTrackApp mainApp;
    private final InventrackController controller;

    // Komponen Form
    private JTextField txtNamaPengguna;
    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JPasswordField txtKonfirmasiPassword;
    private JComboBox<Gudang> cmbGudang;
    private JButton btnRegister;
    private JButton btnLogin;
    private JLabel lblError;

    public RegisterPanel(InvenTrackApp mainApp) {
        this.mainApp = mainApp;
        this.controller = mainApp.getController();
        
        // Layout utama: 1 baris, 2 kolom
        setLayout(new GridLayout(1, 2));

        // PERBAIKAN: Gunakan BrandingPanel yang baru
        add(new BrandingPanel()); 
        
        // Buat dan tambahkan panel form
        add(createFormPanel());

        // Panggil pemuat data (ini harus dipanggil dari InvenTrackApp setelah panel terlihat)
        // loadGudangData(); 
    }

    /**
     * Membuat Panel Form (Sisi Kanan)
     */
    private JPanel createFormPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(COLOR_WHITE);
        panel.setBorder(new EmptyBorder(20, 30, 20, 30));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 8, 5, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Inisialisasi komponen
        txtNamaPengguna = new JTextField(20);
        txtUsername = new JTextField(20);
        txtPassword = new JPasswordField(20);
        txtKonfirmasiPassword = new JPasswordField(20);
        cmbGudang = new JComboBox<>();
        btnRegister = new JButton("Sign Up");
        btnLogin = new JButton("Login");
        lblError = new JLabel(" ");
        lblError.setForeground(Color.RED);
        lblError.setHorizontalAlignment(SwingConstants.CENTER);

        // Judul "SIGN UP"
        JLabel lblTitle = new JLabel("SIGN UP");
        lblTitle.setFont(new Font("Arial", Font.BOLD, 24));
        lblTitle.setForeground(COLOR_BLACK);
        lblTitle.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        gbc.insets = new Insets(5, 8, 15, 8);
        panel.add(lblTitle, gbc);

        gbc.insets = new Insets(5, 8, 5, 8);
        gbc.gridwidth = 1;

        // Form Fields
        gbc.gridx = 0; gbc.gridy = 1; panel.add(new JLabel("Nama Lengkap:"), gbc);
        gbc.gridx = 1; gbc.gridy = 1; panel.add(txtNamaPengguna, gbc);

        gbc.gridx = 0; gbc.gridy = 2; panel.add(new JLabel("Username:"), gbc);
        gbc.gridx = 1; gbc.gridy = 2; panel.add(txtUsername, gbc);

        gbc.gridx = 0; gbc.gridy = 3; panel.add(new JLabel("Password:"), gbc);
        gbc.gridx = 1; gbc.gridy = 3; panel.add(txtPassword, gbc);

        gbc.gridx = 0; gbc.gridy = 4; panel.add(new JLabel("Konfirmasi Password:"), gbc);
        gbc.gridx = 1; gbc.gridy = 4; panel.add(txtKonfirmasiPassword, gbc);

        gbc.gridx = 0; gbc.gridy = 5; panel.add(new JLabel("Gudang:"), gbc);
        gbc.gridx = 1; gbc.gridy = 5; panel.add(cmbGudang, gbc);

        // Label Error
        gbc.gridx = 0; gbc.gridy = 6; gbc.gridwidth = 2;
        panel.add(lblError, gbc);

        // Tombol Sign Up (Utama)
        btnRegister.setBackground(COLOR_TEAL);
        btnRegister.setForeground(COLOR_BLACK);
        btnRegister.setFont(new Font("Arial", Font.BOLD, 14));
        btnRegister.setOpaque(true);
        btnRegister.setBorderPainted(false);
        gbc.gridx = 0; gbc.gridy = 7; gbc.gridwidth = 2;
        gbc.insets = new Insets(10, 8, 5, 8);
        panel.add(btnRegister, gbc);

        // "I've an account"
        gbc.gridx = 0; gbc.gridy = 8; gbc.gridwidth = 1;
        gbc.insets = new Insets(15, 8, 8, 8);
        gbc.anchor = GridBagConstraints.EAST;
        gbc.fill = GridBagConstraints.NONE;
        panel.add(new JLabel("I've an account"), gbc);

        // Tombol Login (Sekunder)
        btnLogin.setBackground(COLOR_WHITE);
        btnLogin.setForeground(Color.DARK_GRAY);
        gbc.gridx = 1; gbc.gridy = 8; gbc.anchor = GridBagConstraints.WEST;
        panel.add(btnLogin, gbc);
        
        // Listeners
        btnRegister.addActionListener(this::handleRegister);
        btnLogin.addActionListener(e -> mainApp.showLoginScreen());

        return panel;
    }

    // Metode ini dipanggil oleh InvenTrackApp setelah panel terlihat
    public void loadGudangData() {
        try {
            List<Gudang> gudangList = controller.getAllGudang();
            DefaultComboBoxModel<Gudang> model = new DefaultComboBoxModel<>();
            for (Gudang g : gudangList) {
                model.addElement(g);
            }
            cmbGudang.setModel(model);

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

    private void handleRegister(ActionEvent e) {
        String nama = txtNamaPengguna.getText();
        String username = txtUsername.getText();
        String pass1 = new String(txtPassword.getPassword());
        String pass2 = new String(txtKonfirmasiPassword.getPassword());
        Object selectedItem = cmbGudang.getSelectedItem();

        // Validasi input
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

        Integer idGudang = ((Gudang) selectedItem).getIdGudang();

        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        try {
            // Memanggil controller
            String hasil = controller.registerPengguna(nama, username, pass1, idGudang); 
            
            if (hasil.startsWith("SUKSES")) {
                // Sukses
                JOptionPane.showMessageDialog(this, hasil, "Registrasi Berhasil", JOptionPane.INFORMATION_MESSAGE);
                mainApp.showLoginScreen();
            } else {
                lblError.setText(hasil);
            }
        } catch (Exception ex) {
            // Menangkap error dari database (misal: gagal koneksi, atau error 'save' dari repo)
            lblError.setText("Error sistem: " + ex.getMessage());
        } finally {
            setCursor(Cursor.getDefaultCursor());
        }
    }
}