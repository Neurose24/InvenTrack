/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package com.anomaly.inventrack.ui.panels;

import com.anomaly.inventrack.models.Gudang;
import com.anomaly.inventrack.models.Supir;
import com.anomaly.inventrack.repositories.GudangRepositories;
import com.anomaly.inventrack.repositories.SupirRepositories;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.JTable;

/**
 *
 * @author user
 */
public class SupirPanel extends javax.swing.JPanel {
    
    private final SupirRepositories supirRepo = new SupirRepositories();
    private final GudangRepositories gudangRepo = new GudangRepositories();
    
    private DefaultTableModel tableModel;
    private Map<Integer, String> mapNamaGudang = new HashMap<>();

    /**
     * Creates new form SupirPanel
     */
    public SupirPanel() {
        initComponents();
        
        if (java.beans.Beans.isDesignTime()) return;
        
        setupUI();
        setupListeners();
        
        this.addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentShown(java.awt.event.ComponentEvent e) {
                loadDataMaster();
                loadDataSupir();
            }
        });
    }
    
    private void setupUI() {
        String[] header = {"ID Supir", "Nama Supir", "No HP", "No Kendaraan", "Gudang Asal"};
        
        tableModel = new DefaultTableModel(header, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        tblSupir.setModel(tableModel);
        
        tblSupir.getColumnModel().getColumn(0).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setText("<html><u><font color='blue'>" + value + "</font></u></html>");
                return this;
            }
        });
        
        tblSupir.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = tblSupir.rowAtPoint(e.getPoint());
                int col = tblSupir.columnAtPoint(e.getPoint());
                
                if (row >= 0 && col == 0) {
                    try {
                        String idStr = tblSupir.getValueAt(row, 0).toString();
                        int idSupir = Integer.parseInt(idStr);
                        bukaFormEdit(idSupir);
                    } catch (NumberFormatException ex) {
                        System.err.println("Error parsing ID: " + ex.getMessage());
                    }
                }
            }
            
            @Override
            public void mouseMoved(MouseEvent e) {
                int col = tblSupir.columnAtPoint(e.getPoint());
                if (col == 0) tblSupir.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                else tblSupir.setCursor(Cursor.getDefaultCursor());
            }
        });
        
        tblSupir.setRowHeight(25);
    }
    
    private void setupListeners() {
        btnTambahSupir.addActionListener(e -> bukaFormTambah());
        
        cmbGudang.addActionListener(e -> loadDataSupir());
        txtCariSupir.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) { loadDataSupir(); }
        });
    }
    
    private void loadDataMaster() {
        mapNamaGudang.clear();
        cmbGudang.removeAllItems();
        cmbGudang.addItem(new ComboItem("Semua Gudang", -1));
        
        for (Gudang g : gudangRepo.findAll()) {
            mapNamaGudang.put(g.getIdGudang(), g.getNamaGudang());
            cmbGudang.addItem(new ComboItem(g.getNamaGudang(), g.getIdGudang()));
        }
    }
    
    private void loadDataSupir() {
        tableModel.setRowCount(0);
        
        ComboItem itemGudang = (ComboItem) cmbGudang.getSelectedItem();
        int idGudangFilter = (itemGudang != null) ? itemGudang.getValue() : -1;
        String keyword = txtCariSupir.getText().toLowerCase();
        
        List<Supir> listSupir = supirRepo.findAll();
        
        for (Supir s : listSupir) {
            int idGudangSupir = -1;
            if (s.getIdGudang() != null) {
                idGudangSupir = s.getIdGudang();
            }
            
            if (idGudangFilter != -1 && idGudangSupir != idGudangFilter) {
                continue;
            }
            
            String namaGudang = mapNamaGudang.getOrDefault(idGudangSupir, "Tidak Ada Gudang");
            String idTampil = String.format("%05d", s.getIdSupir());
            
            boolean matchKeyword = keyword.isEmpty() || 
                                   s.getNamaSupir().toLowerCase().contains(keyword) ||
                                   s.getNoKendaraan().toLowerCase().contains(keyword) ||
                                   namaGudang.toLowerCase().contains(keyword);
            
            if (matchKeyword) {
                tableModel.addRow(new Object[]{
                    idTampil,
                    s.getNamaSupir(),
                    s.getNoHp(),
                    s.getNoKendaraan(),
                    namaGudang
                });
            }
        }
    }
    
    private void bukaFormTambah() {
        JFrame parent = (JFrame) SwingUtilities.getWindowAncestor(this);
        FormEditSupir dialog = new FormEditSupir(parent, true);
        dialog.setVisible(true);
        loadDataSupir();
    }
    
    private void bukaFormEdit(int idSupir) {
        JFrame parent = (JFrame) SwingUtilities.getWindowAncestor(this);
        FormEditSupir dialog = new FormEditSupir(parent, true, idSupir);
        dialog.setVisible(true);
        loadDataSupir();
    }
    
    class ComboItem {
        private String key; private int value;
        public ComboItem(String k, int v) { key=k; value=v; }
        public int getValue() { return value; }
        @Override public String toString() { return key; }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        pnlHeader = new javax.swing.JPanel();
        lblNamaGudangUser = new javax.swing.JLabel();
        lblCari = new javax.swing.JLabel();
        txtCariSupir = new javax.swing.JTextField();
        cmbGudang = new javax.swing.JComboBox<>();
        lblPilihGudang = new javax.swing.JLabel();
        btnTambahSupir = new javax.swing.JButton();
        pnlWadahTabel = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblSupir = new javax.swing.JTable();

        jPanel1.setLayout(new java.awt.BorderLayout());

        lblNamaGudangUser.setFont(new java.awt.Font("Arial", 1, 12)); // NOI18N

        lblCari.setFont(new java.awt.Font("Arial", 1, 12)); // NOI18N
        lblCari.setText("Cari Barang:");

        txtCariSupir.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N

        cmbGudang.setFont(new java.awt.Font("Arial", 1, 12)); // NOI18N
        cmbGudang.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        lblPilihGudang.setFont(new java.awt.Font("Arial", 1, 12)); // NOI18N
        lblPilihGudang.setText("Pilih Gudang");

        btnTambahSupir.setText("Tambah Supir");
        btnTambahSupir.addActionListener(this::btnTambahSupirActionPerformed);

        javax.swing.GroupLayout pnlHeaderLayout = new javax.swing.GroupLayout(pnlHeader);
        pnlHeader.setLayout(pnlHeaderLayout);
        pnlHeaderLayout.setHorizontalGroup(
            pnlHeaderLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlHeaderLayout.createSequentialGroup()
                .addGap(41, 41, 41)
                .addGroup(pnlHeaderLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlHeaderLayout.createSequentialGroup()
                        .addComponent(lblPilihGudang, javax.swing.GroupLayout.PREFERRED_SIZE, 114, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cmbGudang, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(btnTambahSupir))
                    .addGroup(pnlHeaderLayout.createSequentialGroup()
                        .addComponent(lblNamaGudangUser, javax.swing.GroupLayout.PREFERRED_SIZE, 114, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 153, Short.MAX_VALUE)
                        .addComponent(lblCari, javax.swing.GroupLayout.PREFERRED_SIZE, 89, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtCariSupir, javax.swing.GroupLayout.PREFERRED_SIZE, 151, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(42, 42, 42))
        );
        pnlHeaderLayout.setVerticalGroup(
            pnlHeaderLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlHeaderLayout.createSequentialGroup()
                .addGroup(pnlHeaderLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlHeaderLayout.createSequentialGroup()
                        .addGap(34, 34, 34)
                        .addGroup(pnlHeaderLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(lblCari, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(txtCariSupir, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(pnlHeaderLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(lblNamaGudangUser, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(pnlHeaderLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblPilihGudang, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cmbGudang, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlHeaderLayout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(btnTambahSupir))
        );

        jPanel1.add(pnlHeader, java.awt.BorderLayout.PAGE_START);

        pnlWadahTabel.setBorder(javax.swing.BorderFactory.createEmptyBorder(20, 40, 20, 40));
        pnlWadahTabel.setLayout(new java.awt.BorderLayout());

        tblSupir.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        tblSupir.setModel(new javax.swing.table.DefaultTableModel(
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
        tblSupir.setShowGrid(false);
        jScrollPane1.setViewportView(tblSupir);

        pnlWadahTabel.add(jScrollPane1, java.awt.BorderLayout.CENTER);

        jPanel1.add(pnlWadahTabel, java.awt.BorderLayout.CENTER);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 596, Short.MAX_VALUE)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                    .addGap(0, 0, Short.MAX_VALUE)
                    .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, 596, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGap(0, 0, Short.MAX_VALUE)))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 415, Short.MAX_VALUE)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                    .addGap(0, 0, Short.MAX_VALUE)
                    .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, 415, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGap(0, 0, Short.MAX_VALUE)))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void btnTambahSupirActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnTambahSupirActionPerformed
        bukaFormTambah();
    }//GEN-LAST:event_btnTambahSupirActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnTambahSupir;
    private javax.swing.JComboBox<Object> cmbGudang;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel lblCari;
    private javax.swing.JLabel lblNamaGudangUser;
    private javax.swing.JLabel lblPilihGudang;
    private javax.swing.JPanel pnlHeader;
    private javax.swing.JPanel pnlWadahTabel;
    private javax.swing.JTable tblSupir;
    private javax.swing.JTextField txtCariSupir;
    // End of variables declaration//GEN-END:variables
}
