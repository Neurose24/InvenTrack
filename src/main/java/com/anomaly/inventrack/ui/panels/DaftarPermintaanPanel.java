/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package com.anomaly.inventrack.ui.panels;

import com.anomaly.inventrack.models.Gudang;
import com.anomaly.inventrack.models.Pengguna;
import com.anomaly.inventrack.models.Permintaan;
import com.anomaly.inventrack.repositories.GudangRepositories;
import com.anomaly.inventrack.repositories.PenggunaRepositories;
import com.anomaly.inventrack.repositories.PermintaanRepositories;
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
public class DaftarPermintaanPanel extends javax.swing.JPanel {
    
    private final PermintaanRepositories permintaanRepo = new PermintaanRepositories();
    private final GudangRepositories gudangRepo = new GudangRepositories();
    private final PenggunaRepositories penggunaRepo = new PenggunaRepositories();
    
    private DefaultTableModel tableModelSemua;
    private DefaultTableModel tableModelProses;
    
    private int currentUserGudangId = -1; 
    private int currentUserId = -1;
    
    // Cache Data
    private final Map<Integer, String> mapNamaGudang = new HashMap<>();
    private final Map<Integer, Integer> mapUserToGudang = new HashMap<>();
    
    /**
     * Creates new form DaftarPengirimanPanel
     */
    public DaftarPermintaanPanel() {
        initComponents();
        
        if (java.beans.Beans.isDesignTime()) return;
        
        setupUI();
        setupListeners();
        
        this.addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentShown(java.awt.event.ComponentEvent e) {
                loadData(); 
            }
        });
    }
    
    public void setCurrentUser(Pengguna user) {
        if (user != null) {
            this.currentUserGudangId = user.getIdGudang();
            this.currentUserId = user.getIdPengguna();
            
            Optional<Gudang> g = gudangRepo.findById(this.currentUserGudangId);
            if (g.isPresent()) {
                lblNamaGudangUser.setText(g.get().getNamaGudang());
                lblNamaGudangUser.setForeground(new java.awt.Color(0, 102, 204));
            }
            
            loadData();
        }
    }
    
    private void setupUI() {
        cmbStatusPermintaan.removeAllItems();
        cmbStatusPermintaan.addItem("Semua Status");
        for (Permintaan.StatusPermintaan s : Permintaan.StatusPermintaan.values()) {
            cmbStatusPermintaan.addItem(s.name());
        }
        
        String[] header = {"ID", "Gudang Peminta", "Gudang Sumber", "Tanggal", "Status"};
        
        tableModelSemua = new DefaultTableModel(header, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        tblPermintaan.setModel(tableModelSemua);
        setupHyperlinkColumn(tblPermintaan, 0);
        
        tableModelProses = new DefaultTableModel(header, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        tblProsesPermintaan.setModel(tableModelProses);
        setupHyperlinkColumn(tblProsesPermintaan, 0);
        
        tblPermintaan.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = tblPermintaan.rowAtPoint(e.getPoint());
                int col = tblPermintaan.columnAtPoint(e.getPoint());

                if (row >= 0 && col == 0) {
                    try {
                        // Ambil ID (String -> Int)
                        Object value = tblPermintaan.getValueAt(row, 0);
                        int idPermintaan = Integer.parseInt(value.toString());

                        bukaDetailPermintaan(idPermintaan); // Buka Form View Only

                    } catch (NumberFormatException ex) {
                        System.err.println("Error parsing ID: " + ex.getMessage());
                    }
                }
            }
        });
        tblProsesPermintaan.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = tblProsesPermintaan.rowAtPoint(e.getPoint());
                int col = tblProsesPermintaan.columnAtPoint(e.getPoint());
                
                if (row >= 0 && col == 0) {
                    try {
                        // 1. Ambil sebagai Object -> String
                        Object value = tblProsesPermintaan.getValueAt(row, 0);
                        
                        // 2. Parse String "00000000001" menjadi int 1
                        int idPermintaan = Integer.parseInt(value.toString());
                        
                        // 3. Buka Form
                        bukaFormProses(idPermintaan);
                        
                    } catch (NumberFormatException ex) {
                        System.err.println("Error parsing ID: " + ex.getMessage());
                    }
                }
            }
        });
    }
    
    private void setupListeners() {
        cmbStatusPermintaan.addActionListener(e -> loadData());
        txtCariPermintaan.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                loadData();
            }
        });
    }
    
    private void loadData() {
        if (currentUserGudangId == -1) return;
        
        tableModelSemua.setRowCount(0);
        tableModelProses.setRowCount(0);
        
        if (mapNamaGudang.isEmpty()) {
            for (Gudang g : gudangRepo.findAll()) mapNamaGudang.put(g.getIdGudang(), g.getNamaGudang());
            for (Pengguna p : penggunaRepo.findAll()) mapUserToGudang.put(p.getIdPengguna(), p.getIdGudang());
        }
        
        String statusFilter = (String) cmbStatusPermintaan.getSelectedItem();
        String keyword = txtCariPermintaan.getText().toLowerCase();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");
        
        List<Permintaan> listData = permintaanRepo.findAll();
        
        for (Permintaan p : listData) {
            Integer idGudangPeminta = mapUserToGudang.get(p.getIdPenggunaPeminta());
            String namaGudangPeminta = mapNamaGudang.getOrDefault(idGudangPeminta, "Unknown");
            String namaGudangSumber = mapNamaGudang.getOrDefault(p.getIdGudangSumber(), "Belum Ditentukan");
            
            boolean isMasuk = (p.getIdGudangSumber() != null && p.getIdGudangSumber() == currentUserGudangId);
            boolean isKeluar = (idGudangPeminta != null && idGudangPeminta == currentUserGudangId);
            
            if (!isMasuk && !isKeluar) continue;
            
            boolean matchStatus = statusFilter.equals("Semua Status") || p.getStatusPermintaan().name().equalsIgnoreCase(statusFilter);
            boolean matchKeyword = keyword.isEmpty() || String.valueOf(p.getIdPermintaan()).contains(keyword) || 
                                   namaGudangPeminta.toLowerCase().contains(keyword) || namaGudangSumber.toLowerCase().contains(keyword);
            
            String idTampil = String.format("%011d", p.getIdPermintaan());
            if (matchStatus && matchKeyword) {
                tableModelSemua.addRow(new Object[]{
                    idTampil,
                    namaGudangPeminta, 
                    namaGudangSumber, 
                    p.getTanggalPermintaan().format(formatter), 
                    p.getStatusPermintaan()
                });
            }
            
            if (p.getStatusPermintaan() == Permintaan.StatusPermintaan.MENUNGGU && isMasuk) {
                tableModelProses.addRow(new Object[]{
                    idTampil,
                    namaGudangPeminta, 
                    namaGudangSumber, 
                    p.getTanggalPermintaan().format(formatter), 
                    "BUTUH RESPON"
                });
            }
        }
        
        aturTinggiTabel(tblPermintaan);
        aturTinggiTabel(tblProsesPermintaan);
    }
    
    private void bukaFormProses(int idPermintaan) {
        javax.swing.JFrame parent = (javax.swing.JFrame) SwingUtilities.getWindowAncestor(this);
        
        FormProsesPermintaan form = new FormProsesPermintaan(parent, true, idPermintaan, currentUserId);
        form.setVisible(true);
        
        loadData();
    }
    
    private void bukaDetailPermintaan(int idPermintaan) {
        javax.swing.JFrame parent = (javax.swing.JFrame) SwingUtilities.getWindowAncestor(this);
        
        FormBuatPermintaan form = new FormBuatPermintaan(parent, true, idPermintaan);
        form.setVisible(true);
    }

    private void setupHyperlinkColumn(JTable table, int colIndex) {
        table.getColumnModel().getColumn(colIndex).setCellRenderer(new DefaultTableCellRenderer() {
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
                if (table.columnAtPoint(e.getPoint()) == colIndex) table.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                else table.setCursor(Cursor.getDefaultCursor());
            }
        });
    }
    
    private void aturTinggiTabel(JTable table) {
        int tinggi = table.getTableHeader().getPreferredSize().height + (table.getRowHeight() * table.getRowCount());
        if (tinggi < 100) tinggi = 100;
        if (tinggi > 300) tinggi = 300;
        table.setPreferredScrollableViewportSize(new java.awt.Dimension(table.getPreferredSize().width, tinggi));
        table.revalidate(); 
        table.repaint();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        pnlHeader = new javax.swing.JPanel();
        lblNamaGudangUser = new javax.swing.JLabel();
        lblCariPermintaan = new javax.swing.JLabel();
        txtCariPermintaan = new javax.swing.JTextField();
        lblPilihStatusPermintaan = new javax.swing.JLabel();
        cmbStatusPermintaan = new javax.swing.JComboBox<>();
        pnlWadahTabel = new javax.swing.JPanel();
        jScrollPane3 = new javax.swing.JScrollPane();
        jPanel1 = new javax.swing.JPanel();
        jPanel3 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblPermintaan = new javax.swing.JTable();
        jPanel2 = new javax.swing.JPanel();
        lblPerluDiproses = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        tblProsesPermintaan = new javax.swing.JTable();

        setLayout(new java.awt.BorderLayout());

        lblNamaGudangUser.setFont(new java.awt.Font("Arial", 1, 12)); // NOI18N

        lblCariPermintaan.setFont(new java.awt.Font("Arial", 1, 12)); // NOI18N
        lblCariPermintaan.setText("Cari Permintaan");

        txtCariPermintaan.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N

        lblPilihStatusPermintaan.setFont(new java.awt.Font("Arial", 1, 12)); // NOI18N
        lblPilihStatusPermintaan.setText("Status Permintaan");

        cmbStatusPermintaan.setFont(new java.awt.Font("Arial", 1, 12)); // NOI18N
        cmbStatusPermintaan.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

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
                        .addComponent(lblPilihStatusPermintaan, javax.swing.GroupLayout.PREFERRED_SIZE, 114, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cmbStatusPermintaan, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 347, Short.MAX_VALUE)
                        .addComponent(lblCariPermintaan, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtCariPermintaan, javax.swing.GroupLayout.PREFERRED_SIZE, 151, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(44, 44, 44))))
        );
        pnlHeaderLayout.setVerticalGroup(
            pnlHeaderLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlHeaderLayout.createSequentialGroup()
                .addContainerGap(30, Short.MAX_VALUE)
                .addComponent(lblNamaGudangUser, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(pnlHeaderLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlHeaderLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(lblCariPermintaan, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(txtCariPermintaan, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(pnlHeaderLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(lblPilihStatusPermintaan, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(cmbStatusPermintaan, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
        );

        add(pnlHeader, java.awt.BorderLayout.PAGE_START);

        pnlWadahTabel.setBorder(javax.swing.BorderFactory.createEmptyBorder(20, 40, 20, 40));
        pnlWadahTabel.setLayout(new java.awt.GridLayout());

        jPanel1.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        jPanel1.setLayout(new java.awt.GridLayout(2, 1, 0, 20));

        jPanel3.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        jPanel3.setLayout(new java.awt.BorderLayout());

        jScrollPane1.setPreferredSize(null);

        tblPermintaan.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        tblPermintaan.setModel(new javax.swing.table.DefaultTableModel(
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
        tblPermintaan.setFillsViewportHeight(true);
        tblPermintaan.setShowGrid(true);
        tblPermintaan.setShowVerticalLines(false);
        jScrollPane1.setViewportView(tblPermintaan);

        jPanel3.add(jScrollPane1, java.awt.BorderLayout.CENTER);

        jPanel1.add(jPanel3);

        jPanel2.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        jPanel2.setLayout(new java.awt.BorderLayout());

        lblPerluDiproses.setFont(new java.awt.Font("Arial", 1, 12)); // NOI18N
        lblPerluDiproses.setText("Perlu Diproses");
        jPanel2.add(lblPerluDiproses, java.awt.BorderLayout.NORTH);

        jScrollPane2.setPreferredSize(null);

        tblProsesPermintaan.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        tblProsesPermintaan.setModel(new javax.swing.table.DefaultTableModel(
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
        tblProsesPermintaan.setFillsViewportHeight(true);
        tblProsesPermintaan.setShowGrid(true);
        tblProsesPermintaan.setShowHorizontalLines(true);
        jScrollPane2.setViewportView(tblProsesPermintaan);

        jPanel2.add(jScrollPane2, java.awt.BorderLayout.CENTER);

        jPanel1.add(jPanel2);

        jScrollPane3.setViewportView(jPanel1);

        pnlWadahTabel.add(jScrollPane3);

        add(pnlWadahTabel, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox<Object> cmbStatusPermintaan;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JLabel lblCariPermintaan;
    private javax.swing.JLabel lblNamaGudangUser;
    private javax.swing.JLabel lblPerluDiproses;
    private javax.swing.JLabel lblPilihStatusPermintaan;
    private javax.swing.JPanel pnlHeader;
    private javax.swing.JPanel pnlWadahTabel;
    private javax.swing.JTable tblPermintaan;
    private javax.swing.JTable tblProsesPermintaan;
    private javax.swing.JTextField txtCariPermintaan;
    // End of variables declaration//GEN-END:variables
}
