package com.anomaly.inventrack.ui.panels;

import com.anomaly.inventrack.models.Pengguna;
import com.anomaly.inventrack.ui.InvenTrackApp;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;

// Menggunakan warna dari BrandingPanel jika ada, atau definisikan di sini
import static com.anomaly.inventrack.ui.panels.BrandingPanel.COLOR_TEAL;
import static com.anomaly.inventrack.ui.panels.BrandingPanel.COLOR_WHITE;

public class DashboardPanel extends JPanel {

    private final InvenTrackApp mainApp;
    private final Pengguna pengguna;

    // Definisikan palet warna dari referensi
    private static final Color COLOR_SIDEBAR = new Color(3, 138, 255); // Biru Sidebar
    private static final Color COLOR_CONTENT_BG = new Color(240, 245, 247); // Latar belakang utama
    private static final Color COLOR_KARD_1 = new Color(138, 119, 239); // Ungu (Stok)
    private static final Color COLOR_KARD_2 = new Color(217, 100, 217); // Magenta (Permintaan)
    private static final Color COLOR_KARD_3 = new Color(245, 195, 80);  // Kuning (Pengiriman)
    private static final Color COLOR_STATUS_PENDING = new Color(188, 145, 255); // Ungu muda
    private static final Color COLOR_STATUS_APPROVED = new Color(100, 217, 100); // Hijau
    private static final Color COLOR_STATUS_REJECT = new Color(245, 195, 80);  // Kuning

    public DashboardPanel(InvenTrackApp mainApp, Pengguna pengguna) {
        this.mainApp = mainApp;
        this.pengguna = pengguna;
        
        // Layout utama panel ini
        setLayout(new BorderLayout());
        
        // 1. Buat Sidebar
        add(createSidebarPanel(), BorderLayout.WEST);
        
        // 2. Buat Konten Utama
        add(createMainContentPanel(), BorderLayout.CENTER);
    }

    /**
     * Membuat Panel Navigasi Samping (Sidebar)
     */
    private JPanel createSidebarPanel() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(COLOR_SIDEBAR);
        sidebar.setPreferredSize(new Dimension(200, 0)); // Lebar sidebar
        sidebar.setBorder(new EmptyBorder(10, 0, 10, 0));

        // Judul Aplikasi (Logo)
        JLabel lblLogo = new JLabel("InvenTrack");
        lblLogo.setFont(new Font("Arial", Font.BOLD, 24));
        lblLogo.setForeground(COLOR_WHITE);
        lblLogo.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblLogo.setBorder(new EmptyBorder(10, 10, 30, 10));
        sidebar.add(lblLogo);

        // Tambahkan tombol navigasi
        sidebar.add(createNavButton("Dashboard"));
        sidebar.add(createNavButton("Stok Barang"));
        sidebar.add(createNavButton("Permintaan"));
        sidebar.add(createNavButton("Pengiriman"));
        sidebar.add(createNavButton("Laporan"));
        
        // Spacer (mendorong logout ke bawah)
        sidebar.add(Box.createVerticalGlue()); 

        // Tombol Logout
        JButton btnLogout = createNavButton("Logout");
        btnLogout.addActionListener(e -> mainApp.showLoginScreen());
        sidebar.add(btnLogout);

        return sidebar;
    }

    /**
     * Helper untuk membuat tombol navigasi yang stylish (flat)
     */
    private JButton createNavButton(String text) {
        JButton button = new JButton(text);
        button.setForeground(COLOR_WHITE);
        button.setBackground(COLOR_SIDEBAR);
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setOpaque(true);
        button.setContentAreaFilled(true);
        button.setBorder(new EmptyBorder(15, 25, 15, 25)); // Padding
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setFocusPainted(false);
        
        // Efek hover (opsional sederhana)
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(COLOR_SIDEBAR.brighter());
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(COLOR_SIDEBAR);
            }
        });
        
        return button;
    }

    /**
     * Membuat Panel Konten Utama (Kanan)
     */
    private JPanel createMainContentPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBackground(COLOR_CONTENT_BG);
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        // 1. Panel "Kard" di atas
        mainPanel.add(createKardPanel(), BorderLayout.NORTH);

        // 2. Panel Tabel di tengah
        mainPanel.add(createTablePanel(), BorderLayout.CENTER);

        return mainPanel;
    }

    /**
     * Membuat 3 "Kard" Ringkasan di atas
     */
    private JPanel createKardPanel() {
        JPanel kardPanel = new JPanel(new GridLayout(1, 3, 15, 15)); // 1 Baris, 3 Kolom, 15px gap
        kardPanel.setOpaque(false); // Transparan (mengikuti latar belakang abu-abu)

        // TODO: Ganti angka $200000 dengan data nyata dari controller
        kardPanel.add(createKard("Stok Total", "$200000", "Increased by 60%", COLOR_KARD_1));
        kardPanel.add(createKard("Permintaan Pending", "15", "Menunggu approval", COLOR_KARD_2));
        kardPanel.add(createKard("Pengiriman Hari Ini", "5", "Dalam perjalanan", COLOR_KARD_3));
        
        return kardPanel;
    }

    /**
     * Helper untuk membuat satu "Kard"
     */
    private JPanel createKard(String title, String value, String subtext, Color bgColor) {
        JPanel kard = new JPanel(new BorderLayout());
        kard.setBackground(bgColor);
        kard.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        // Panel Teks (kiri)
        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setOpaque(false);

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Arial", Font.BOLD, 14));
        lblTitle.setForeground(COLOR_WHITE);
        
        JLabel lblValue = new JLabel(value);
        lblValue.setFont(new Font("Arial", Font.BOLD, 28));
        lblValue.setForeground(COLOR_WHITE);
        
        JLabel lblSubtext = new JLabel(subtext);
        lblSubtext.setFont(new Font("Arial", Font.PLAIN, 12));
        lblSubtext.setForeground(COLOR_WHITE.brighter());

        textPanel.add(lblTitle);
        textPanel.add(lblValue);
        textPanel.add(Box.createVerticalStrut(10)); // Spasi
        textPanel.add(lblSubtext);
        
        kard.add(textPanel, BorderLayout.CENTER);

        // TODO: Tambahkan Ikon di sisi kanan (BorderLayout.EAST)
        
        return kard;
    }

    /**
     * Membuat Panel Tabel di bawah Kard
     */
    private JPanel createTablePanel() {
        JPanel tableContainer = new JPanel(new BorderLayout());
        tableContainer.setBackground(COLOR_WHITE);
        tableContainer.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Judul Tabel
        JLabel lblTableTitle = new JLabel("Data Terbaru (Contoh)");
        lblTableTitle.setFont(new Font("Arial", Font.BOLD, 18));
        lblTableTitle.setBorder(new EmptyBorder(0, 5, 10, 5));
        tableContainer.add(lblTableTitle, BorderLayout.NORTH);

        // Data Contoh (sesuai referensi)
        String[] columnNames = {"Name", "Email", "User Type", "Joined", "Status"};
        Object[][] data = {
            {"Mike Bhand", "mikebhand@gmail.com", "Admin", "25 Apr, 2018", "PENDING"},
            {"Andrew Strauss", "andrewstrauss@gmail.com", "Editor", "25 Apr, 2018", "APPROVED"},
            {"Ross Kopelman", "rosskopelman@gmail.com", "Subscriber", "25 Apr, 2018", "APPROVED"},
            {"Mike Hussy", "mikehussy@gmail.com", "Admin", "25 Apr, 2018", "REJECT"},
            {"Kevin Pietersen", "kevinpietersen@gmail.com", "Admin", "25 Apr, 2018", "PENDING"},
        };

        // Buat tabel
        DefaultTableModel model = new DefaultTableModel(data, columnNames) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Membuat sel tidak bisa diedit
            }
        };
        JTable table = new JTable(model);
        
        // Styling Tabel
        table.setFont(new Font("Arial", Font.PLAIN, 14));
        table.setRowHeight(30);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 14));
        table.getTableHeader().setBackground(COLOR_CONTENT_BG);
        table.getTableHeader().setBorder(new EmptyBorder(5, 5, 5, 5));

        // 🆕 Terapkan Renderer Khusus untuk kolom "Status"
        table.getColumn("Status").setCellRenderer(new StatusCellRenderer());

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.getViewport().setBackground(COLOR_WHITE);
        scrollPane.setBorder(BorderFactory.createEmptyBorder()); // Hapus border scrollpane

        tableContainer.add(scrollPane, BorderLayout.CENTER);
        return tableContainer;
    }

    /**
     * Inner Class: Renderer kustom untuk membuat "badge" status berwarna
     */
    private static class StatusCellRenderer extends JLabel implements TableCellRenderer {

        public StatusCellRenderer() {
            setOpaque(true);
            setHorizontalAlignment(CENTER);
            setFont(new Font("Arial", Font.BOLD, 12));
            setBorder(new EmptyBorder(5, 10, 5, 10));
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus,
                                                       int row, int column) {
            String status = value.toString().toUpperCase();
            setText(status);
            
            // Atur warna berdasarkan status
            switch (status) {
                case "PENDING":
                    setBackground(COLOR_STATUS_PENDING);
                    setForeground(Color.WHITE);
                    break;
                case "APPROVED":
                    setBackground(COLOR_STATUS_APPROVED);
                    setForeground(Color.WHITE);
                    break;
                case "REJECT":
                    setBackground(COLOR_STATUS_REJECT);
                    setForeground(Color.WHITE);
                    break;
                default:
                    setBackground(Color.LIGHT_GRAY);
                    setForeground(Color.DARK_GRAY);
                    break;
            }

            return this;
        }
    }
}