/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package com.anomaly.inventrack.ui.panels;

import com.anomaly.inventrack.models.Gudang;
import com.anomaly.inventrack.models.Pengguna;
import com.anomaly.inventrack.models.Pengiriman;
import com.anomaly.inventrack.repositories.GudangRepositories;
import com.anomaly.inventrack.repositories.PenggunaRepositories;
import com.anomaly.inventrack.repositories.PengirimanRepositories;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author user
 */
public class DaftarPengirimanPanel extends javax.swing.JPanel {
    
    private final PengirimanRepositories pengirimanRepo = new PengirimanRepositories();
    private final GudangRepositories gudangRepo = new GudangRepositories();
    private final PenggunaRepositories penggunaRepo = new PenggunaRepositories();

    private DefaultTableModel tableModelSemua;
    private DefaultTableModel tableModelProses;

    private Map<Integer, String> mapNamaGudang = new HashMap<>();
    private Map<Integer, Integer> mapUserToGudang = new HashMap<>();
    
    private int currentUserGudangId = -1;
    private int currentUserId = -1;

    /**
     * Creates new form DaftarPengirimanPanel
     */
    public DaftarPengirimanPanel() {
        initComponents(); 
        
        if (java.beans.Beans.isDesignTime()) return;
        
        setupUI();
        setupInteractions();
    }
    
    public void setCurrentUser(Pengguna user) {
        if (user != null) {
            this.currentUserGudangId = user.getIdGudang();
            this.currentUserId = user.getIdPengguna();
            
            Optional<Gudang> g = gudangRepo.findById(currentUserGudangId);
            if (g.isPresent()) {
                lblNamaGudangUser.setText(g.get().getNamaGudang());
                lblNamaGudangUser.setForeground(new Color(0, 102, 204));
            }
            
            loadDataPendukung();
            loadDataPengiriman();
        }
    }
    
    private void setupUI() {
        cmbStatusPngiriman.removeAllItems();
        cmbStatusPngiriman.addItem("Semua Status");
        for (Pengiriman.StatusPengiriman s : Pengiriman.StatusPengiriman.values()) {
            cmbStatusPngiriman.addItem(s.name());
        }
        
        String[] header = {"ID Pengiriman", "Gudang Pengirim", "Gudang Tujuan", "Tanggal", "Status"};
        
        tableModelSemua = new DefaultTableModel(header, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        tblPengiriman.setModel(tableModelSemua);
        setupHyperlinkColumn(tblPengiriman);
        
        tableModelProses = new DefaultTableModel(header, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        tblProsesPengriman.setModel(tableModelProses);
        setupHyperlinkColumn(tblProsesPengriman);
        
        tblProsesPengriman.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (isLinkColumnClicked(tblProsesPengriman, e)) {
                    int row = tblProsesPengriman.rowAtPoint(e.getPoint());
                    int idPengiriman = (int) tblProsesPengriman.getValueAt(row, 0);
                    bukaFormTerimaBarang(idPengiriman);
                }
            }
        });
        
        tblPengiriman.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (isLinkColumnClicked(tblPengiriman, e)) {
                    int row = tblPengiriman.rowAtPoint(e.getPoint());
                    int idPengiriman = (int) tblPengiriman.getValueAt(row, 0);
                    JOptionPane.showMessageDialog(null, "Detail View Only ID: " + idPengiriman);
                }
            }
        });
    }
    
    private void setupInteractions() {
        cmbStatusPngiriman.addActionListener(e -> loadDataPengiriman());
        txtCariPengiriman.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                loadDataPengiriman();
            }
        });
    }
    
    private void loadDataPengiriman() {
        if (currentUserGudangId == -1) return;
        
        if (mapNamaGudang.isEmpty()) loadDataPendukung();
        
        tableModelSemua.setRowCount(0);
        tableModelProses.setRowCount(0);
        
        List<Pengiriman> listSemua = pengirimanRepo.findAll();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");
        
        String filterStatus = (String) cmbStatusPngiriman.getSelectedItem();
        String keyword = txtCariPengiriman.getText().toLowerCase(); 

        for (Pengiriman p : listSemua) {
            // Resolve ID Gudang
            int idGudangPengirim = mapUserToGudang.getOrDefault(p.getIdPenggunaPengirim(), -1);
            int idGudangPenerima = mapUserToGudang.getOrDefault(p.getIdPenggunaPenerima(), -1);

            // Access Control: Hanya tampilkan jika gudang saya terlibat
            boolean isMasuk = (idGudangPenerima == currentUserGudangId);
            boolean isKeluar = (idGudangPengirim == currentUserGudangId);
            
            if (!isMasuk && !isKeluar) continue; 

            String namaPengirim = mapNamaGudang.getOrDefault(idGudangPengirim, "Unknown");
            String namaPenerima = mapNamaGudang.getOrDefault(idGudangPenerima, "Unknown");

            // Filter UI (Hanya Tabel Atas yang difilter)
            boolean matchStatus = filterStatus.equals("Semua Status") || p.getStatusPengiriman().name().equals(filterStatus);
            boolean matchKeyword = keyword.isEmpty() || String.valueOf(p.getIdPengiriman()).contains(keyword) || 
                                   namaPengirim.toLowerCase().contains(keyword) || namaPenerima.toLowerCase().contains(keyword);

            String idTampil = String.format("%011d", p.getIdPengiriman());
            
            if (matchStatus && matchKeyword) {
                tableModelSemua.addRow(new Object[]{
                    idTampil,
                    namaPengirim, 
                    namaPenerima,
                    p.getTanggalPengiriman().format(formatter), 
                    p.getStatusPengiriman()
                });
            }

            if (isMasuk && p.getStatusPengiriman() == Pengiriman.StatusPengiriman.DIKIRIM) {
                tableModelProses.addRow(new Object[]{
                    idTampil,
                    namaPengirim, 
                    namaPenerima,
                    p.getTanggalPengiriman().format(formatter), 
                    "BUTUH DITERIMA"
                });
            }
        }
        
        aturTinggiTabel(tblPengiriman);
        aturTinggiTabel(tblProsesPengriman);
    }
    
    private void bukaFormTerimaBarang(int idPengiriman) {
        javax.swing.JFrame parent = (javax.swing.JFrame) SwingUtilities.getWindowAncestor(this);
        FormTerimaPengiriman dialog = new FormTerimaPengiriman(parent, true, idPengiriman, currentUserId);
        dialog.setVisible(true);
        loadDataPengiriman();
    }
    
    private void loadDataPendukung() {
        mapNamaGudang.clear();
        mapUserToGudang.clear();
        for (Pengguna p : penggunaRepo.findAll()) mapUserToGudang.put(p.getIdPengguna(), p.getIdGudang());
        for (Gudang g : gudangRepo.findAll()) mapNamaGudang.put(g.getIdGudang(), g.getNamaGudang());
    }
    
    private void setupHyperlinkColumn(JTable table) {
        table.getColumnModel().getColumn(0).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setText("<html><u><font color='blue'>" + value + "</font></u></html>");
                return this;
            }
        });
        table.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                if (table.columnAtPoint(e.getPoint()) == 0) table.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                else table.setCursor(Cursor.getDefaultCursor());
            }
        });
    }
    
    private boolean isLinkColumnClicked(JTable table, MouseEvent e) {
        int row = table.rowAtPoint(e.getPoint());
        int col = table.columnAtPoint(e.getPoint());
        return row >= 0 && col == 0;
    }
    
    private void aturTinggiTabel(JTable table) {
        int tinggi = table.getTableHeader().getPreferredSize().height + (table.getRowHeight() * table.getRowCount());
        if (tinggi < 100) tinggi = 100;
        if (tinggi > 300) tinggi = 300;
        table.setPreferredScrollableViewportSize(new java.awt.Dimension(table.getPreferredSize().width, tinggi));
        table.revalidate(); table.repaint();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup1 = new javax.swing.ButtonGroup();
        pnlHeader = new javax.swing.JPanel();
        lblNamaGudangUser = new javax.swing.JLabel();
        lblCariPengiriman = new javax.swing.JLabel();
        txtCariPengiriman = new javax.swing.JTextField();
        lblPilihStatusPengiriman = new javax.swing.JLabel();
        cmbStatusPngiriman = new javax.swing.JComboBox<>();
        pnlWadahTabel = new javax.swing.JPanel();
        jScrollPane3 = new javax.swing.JScrollPane();
        jPanel1 = new javax.swing.JPanel();
        jPanel3 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblPengiriman = new javax.swing.JTable();
        lblPerluDiproses = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        tblProsesPengriman = new javax.swing.JTable();

        setLayout(new java.awt.BorderLayout());

        lblNamaGudangUser.setFont(new java.awt.Font("Arial", 1, 12)); // NOI18N

        lblCariPengiriman.setFont(new java.awt.Font("Arial", 1, 12)); // NOI18N
        lblCariPengiriman.setText("Cari Pengiriman");

        txtCariPengiriman.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N

        lblPilihStatusPengiriman.setFont(new java.awt.Font("Arial", 1, 12)); // NOI18N
        lblPilihStatusPengiriman.setText("Status Pengiriman");

        cmbStatusPngiriman.setFont(new java.awt.Font("Arial", 1, 12)); // NOI18N
        cmbStatusPngiriman.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        javax.swing.GroupLayout pnlHeaderLayout = new javax.swing.GroupLayout(pnlHeader);
        pnlHeader.setLayout(pnlHeaderLayout);
        pnlHeaderLayout.setHorizontalGroup(
            pnlHeaderLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlHeaderLayout.createSequentialGroup()
                .addGap(41, 41, 41)
                .addGroup(pnlHeaderLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlHeaderLayout.createSequentialGroup()
                        .addComponent(lblNamaGudangUser, javax.swing.GroupLayout.PREFERRED_SIZE, 114, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(pnlHeaderLayout.createSequentialGroup()
                        .addComponent(lblPilihStatusPengiriman, javax.swing.GroupLayout.PREFERRED_SIZE, 114, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cmbStatusPngiriman, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 309, Short.MAX_VALUE)
                        .addComponent(lblCariPengiriman, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtCariPengiriman, javax.swing.GroupLayout.PREFERRED_SIZE, 151, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(44, 44, 44))))
        );
        pnlHeaderLayout.setVerticalGroup(
            pnlHeaderLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlHeaderLayout.createSequentialGroup()
                .addContainerGap(30, Short.MAX_VALUE)
                .addComponent(lblNamaGudangUser, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(pnlHeaderLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblPilihStatusPengiriman, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cmbStatusPngiriman, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblCariPengiriman, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtCariPengiriman, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
        );

        add(pnlHeader, java.awt.BorderLayout.PAGE_START);

        pnlWadahTabel.setBorder(javax.swing.BorderFactory.createEmptyBorder(20, 40, 20, 40));
        pnlWadahTabel.setLayout(new java.awt.BorderLayout());

        jPanel3.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));

        tblPengiriman.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        tblPengiriman.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        tblPengiriman.setFillsViewportHeight(true);
        tblPengiriman.setShowGrid(false);
        jScrollPane1.setViewportView(tblPengiriman);

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                .addComponent(jScrollPane1)
                .addGap(0, 0, 0))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 439, Short.MAX_VALUE)
                .addContainerGap())
        );

        lblPerluDiproses.setFont(new java.awt.Font("Arial", 1, 12)); // NOI18N
        lblPerluDiproses.setText("Perlu Diproses");

        jPanel2.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));

        tblProsesPengriman.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        tblProsesPengriman.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        tblProsesPengriman.setFillsViewportHeight(true);
        tblProsesPengriman.setShowGrid(false);
        jScrollPane2.setViewportView(tblProsesPengriman);

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 797, Short.MAX_VALUE)
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 213, Short.MAX_VALUE)
                .addContainerGap())
        );

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(1, 1, 1))
            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel1Layout.createSequentialGroup()
                    .addComponent(lblPerluDiproses, javax.swing.GroupLayout.PREFERRED_SIZE, 94, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGap(0, 706, Short.MAX_VALUE)))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 39, Short.MAX_VALUE)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel1Layout.createSequentialGroup()
                    .addGap(449, 449, 449)
                    .addComponent(lblPerluDiproses, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addContainerGap(236, Short.MAX_VALUE)))
        );

        jScrollPane3.setViewportView(jPanel1);

        pnlWadahTabel.add(jScrollPane3, java.awt.BorderLayout.PAGE_START);

        add(pnlWadahTabel, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents

    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JComboBox<Object> cmbStatusPngiriman;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JLabel lblCariPengiriman;
    private javax.swing.JLabel lblNamaGudangUser;
    private javax.swing.JLabel lblPerluDiproses;
    private javax.swing.JLabel lblPilihStatusPengiriman;
    private javax.swing.JPanel pnlHeader;
    private javax.swing.JPanel pnlWadahTabel;
    private javax.swing.JTable tblPengiriman;
    private javax.swing.JTable tblProsesPengriman;
    private javax.swing.JTextField txtCariPengiriman;
    // End of variables declaration//GEN-END:variables
}
