/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package com.anomaly.inventrack.ui.panels;

import com.anomaly.inventrack.models.*;
import com.anomaly.inventrack.repositories.*;
import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author user
 */
public class BuatPengirimanPanel extends javax.swing.JPanel {
    
    private final StokRepositories stokRepo = new StokRepositories();
    private final BarangRepositories barangRepo = new BarangRepositories();
    private final GudangRepositories gudangRepo = new GudangRepositories();
    
    private DefaultTableModel tableModel;
    
    private Map<Integer, Barang> mapBarang = new HashMap<>();
    private Map<Integer, String> mapNamaGudang = new HashMap<>();
    private int currentUserGudangId = -1;
    private int currentUserId = -1;
    
    

    /**
     * Creates new form BuatPengirimanPanel
     */
    public BuatPengirimanPanel() {
        initComponents();
        
        if (java.beans.Beans.isDesignTime()) return;
        
        setupListeners();
        setupTable();
        
        btnKirimBarang.setVisible(false);
        
        this.addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentShown(java.awt.event.ComponentEvent e) {
                loadDataMaster();
                loadDataStok();
            }
        });
    }
    
    public void setCurrentUser(Pengguna user) {
        if (user != null) {
            this.currentUserGudangId = user.getIdGudang();
            this.currentUserId = user.getIdPengguna();
            
            gudangRepo.findById(currentUserGudangId).ifPresent(g -> {
                lblNamaGudangUser.setText(g.getNamaGudang());
                lblNamaGudangUser.setForeground(new Color(0, 102, 204));
            });
            
            loadDataMaster();
            loadDataStok();
        }
    }
    
    private void setupTable() {
        String[] header = {"Pilih", "ID Barang", "Nama Barang", "Kategori", "Stok Tersedia", "Satuan"};
        
        tableModel = new DefaultTableModel(header, 0) {
            @Override public Class<?> getColumnClass(int col) { return (col == 0) ? Boolean.class : String.class; }
            @Override public boolean isCellEditable(int row, int col) { return col == 0; } // Cekbox Only
        };
        tblBarang.setModel(tableModel);
        
        tableModel.addTableModelListener(e -> {
            boolean ada = false;
            for (int i=0; i<tableModel.getRowCount(); i++) {
                if ((Boolean)tableModel.getValueAt(i, 0)) { ada = true; break; }
            }
            btnKirimBarang.setVisible(ada);
        });
        
        tblBarang.getColumnModel().removeColumn(tblBarang.getColumnModel().getColumn(1)); 
        
        tblBarang.getColumnModel().getColumn(0).setMaxWidth(40);
        tblBarang.getColumnModel().getColumn(1).setPreferredWidth(250);
        
        tblBarang.setFillsViewportHeight(true);
        tblBarang.setRowHeight(25);
    }
    
    private void loadDataStok() {
        tableModel.setRowCount(0);
        if (currentUserGudangId == -1) return;
        
        if (mapBarang.isEmpty()) loadDataMaster();
        
        String fKat = (String) cmbKategori.getSelectedItem();
        String fSat = (String) cmbSatuan.getSelectedItem();
        String keyword = txtCariBarang.getText().toLowerCase();
        if (fKat == null) fKat = "Semua Kategori";
        if (fSat == null) fSat = "Semua Satuan";

        List<Stok> listStok = stokRepo.getByGudang(currentUserGudangId);
        Map<Integer, Stok> mapStok = new HashMap<>();
        for(Stok s : listStok) mapStok.put(s.getIdBarang(), s);
        
        for (Barang b : mapBarang.values()) {
            String nama = b.getNamaBarang();
            String kat = (b.getKategori() != null) ? b.getKategori() : "-";
            String sat = (b.getSatuan() != null) ? b.getSatuan() : "-";
            
            boolean matchKat = fKat.equals("Semua Kategori") || kat.equalsIgnoreCase(fKat);
            boolean matchSat = fSat.equals("Semua Satuan") || sat.equalsIgnoreCase(fSat);
            boolean matchKey = keyword.isEmpty() || nama.toLowerCase().contains(keyword);
            
            if (!matchKat || !matchSat || !matchKey) continue;
            
            Stok s = mapStok.get(b.getIdBarang());
            int jumlah = (s != null) ? s.getJumlahStok() : 0;
            
            tableModel.addRow(new Object[]{
                false,
                b.getIdBarang(),
                nama,
                kat,
                jumlah,
                sat
            });
        }
        btnKirimBarang.setVisible(false);
        aturTinggiTabel();
    }
    
    private void bukaWindowKirim() {
        List<Integer> selectedIds = new ArrayList<>();
        
        for (int i=0; i<tableModel.getRowCount(); i++) {
            Boolean checked = (Boolean)tableModel.getValueAt(i, 0);
            if (checked != null && checked) {
                int idBarang = (int) tableModel.getValueAt(i, 1);
                selectedIds.add(idBarang);
            }
        }
        
        if (!selectedIds.isEmpty()) {
            JFrame parent = (JFrame) SwingUtilities.getWindowAncestor(this);
            FormBuatPengiriman dialog = new FormBuatPengiriman(parent, true, selectedIds, currentUserId, currentUserGudangId);
            dialog.setVisible(true);
            
            loadDataStok();
        }
    }
    
    private void loadDataMaster() {
        mapBarang.clear();
        for(Barang b : barangRepo.findAll()) mapBarang.put(b.getIdBarang(), b);
        
        loadComboFilters();
    }
    
    private void loadComboFilters() {
        Object selectedKat = cmbKategori.getSelectedItem();
        Object selectedSat = cmbSatuan.getSelectedItem();
        
        cmbKategori.removeAllItems();
        cmbKategori.addItem("Semua Kategori");
        cmbSatuan.removeAllItems();
        cmbSatuan.addItem("Semua Satuan");
        
        java.util.Set<String> kategoriUnik = new java.util.HashSet<>();
        java.util.Set<String> satuanUnik = new java.util.HashSet<>();
        
        for (Barang b : mapBarang.values()) {
            if (b.getKategori() != null) kategoriUnik.add(b.getKategori());
            if (b.getSatuan() != null) satuanUnik.add(b.getSatuan());
        }
        
        for (String k : kategoriUnik) cmbKategori.addItem(k);
        for (String s : satuanUnik) cmbSatuan.addItem(s);

        if (selectedKat != null) cmbKategori.setSelectedItem(selectedKat);
        if (selectedSat != null) cmbSatuan.setSelectedItem(selectedSat);
    }
    
    private void aturTinggiTabel() {
        int tinggi = tblBarang.getTableHeader().getPreferredSize().height + (tblBarang.getRowHeight() * tblBarang.getRowCount());
        if (tinggi < 100) tinggi = 100;
        if (tinggi > 400) tinggi = 400;
        
        tblBarang.setPreferredScrollableViewportSize(new java.awt.Dimension(tblBarang.getPreferredSize().width, tinggi));
        tblBarang.revalidate();
        tblBarang.repaint();
        
        if (tblBarang.getParent() != null && tblBarang.getParent().getParent() instanceof javax.swing.JScrollPane) {
            ((javax.swing.JScrollPane)tblBarang.getParent().getParent()).revalidate();
        }
    }
    
    private void setupListeners() {
        txtCariBarang.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) { loadDataStok(); }
        });
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
        lblCari = new javax.swing.JLabel();
        txtCariBarang = new javax.swing.JTextField();
        cmbKategori = new javax.swing.JComboBox<>();
        lblPilihKategori = new javax.swing.JLabel();
        lblPilihSatuan = new javax.swing.JLabel();
        cmbSatuan = new javax.swing.JComboBox<>();
        btnKirimBarang = new javax.swing.JButton();
        pnlWadahTabel = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblBarang = new javax.swing.JTable();

        setLayout(new java.awt.BorderLayout());

        lblNamaGudangUser.setFont(new java.awt.Font("Arial", 1, 12)); // NOI18N

        lblCari.setFont(new java.awt.Font("Arial", 1, 12)); // NOI18N
        lblCari.setText("Cari Barang:");

        txtCariBarang.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N

        cmbKategori.setFont(new java.awt.Font("Arial", 1, 12)); // NOI18N
        cmbKategori.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        cmbKategori.addActionListener(this::cmbKategoriActionPerformed);

        lblPilihKategori.setFont(new java.awt.Font("Arial", 1, 12)); // NOI18N
        lblPilihKategori.setText("Pilih Kategori");

        lblPilihSatuan.setFont(new java.awt.Font("Arial", 1, 12)); // NOI18N
        lblPilihSatuan.setText("Pilih Satuan");

        cmbSatuan.setFont(new java.awt.Font("Arial", 1, 12)); // NOI18N
        cmbSatuan.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        cmbSatuan.addActionListener(this::cmbSatuanActionPerformed);

        btnKirimBarang.setText("Kirim Barang");
        btnKirimBarang.addActionListener(this::btnKirimBarangActionPerformed);

        javax.swing.GroupLayout pnlHeaderLayout = new javax.swing.GroupLayout(pnlHeader);
        pnlHeader.setLayout(pnlHeaderLayout);
        pnlHeaderLayout.setHorizontalGroup(
            pnlHeaderLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlHeaderLayout.createSequentialGroup()
                .addGap(41, 41, 41)
                .addGroup(pnlHeaderLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlHeaderLayout.createSequentialGroup()
                        .addComponent(lblPilihSatuan, javax.swing.GroupLayout.PREFERRED_SIZE, 114, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cmbSatuan, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(btnKirimBarang))
                    .addGroup(pnlHeaderLayout.createSequentialGroup()
                        .addGroup(pnlHeaderLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(pnlHeaderLayout.createSequentialGroup()
                                .addComponent(lblPilihKategori, javax.swing.GroupLayout.PREFERRED_SIZE, 114, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(cmbKategori, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(lblNamaGudangUser, javax.swing.GroupLayout.PREFERRED_SIZE, 114, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 322, Short.MAX_VALUE)
                        .addComponent(lblCari, javax.swing.GroupLayout.PREFERRED_SIZE, 89, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtCariBarang, javax.swing.GroupLayout.PREFERRED_SIZE, 151, javax.swing.GroupLayout.PREFERRED_SIZE)))
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
                            .addComponent(txtCariBarang, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(pnlHeaderLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(lblNamaGudangUser, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(pnlHeaderLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(lblPilihKategori, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(cmbKategori, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(pnlHeaderLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlHeaderLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(lblPilihSatuan, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(cmbSatuan, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(btnKirimBarang, javax.swing.GroupLayout.Alignment.TRAILING)))
        );

        add(pnlHeader, java.awt.BorderLayout.PAGE_START);

        pnlWadahTabel.setBorder(javax.swing.BorderFactory.createEmptyBorder(20, 40, 20, 40));
        pnlWadahTabel.setLayout(new java.awt.BorderLayout());

        tblBarang.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        tblBarang.setModel(new javax.swing.table.DefaultTableModel(
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
        tblBarang.setShowGrid(false);
        jScrollPane1.setViewportView(tblBarang);

        pnlWadahTabel.add(jScrollPane1, java.awt.BorderLayout.CENTER);

        add(pnlWadahTabel, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents

    private void btnKirimBarangActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnKirimBarangActionPerformed
        bukaWindowKirim();
    }//GEN-LAST:event_btnKirimBarangActionPerformed

    private void cmbKategoriActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbKategoriActionPerformed
        loadDataStok();
    }//GEN-LAST:event_cmbKategoriActionPerformed

    private void cmbSatuanActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbSatuanActionPerformed
        loadDataStok();
    }//GEN-LAST:event_cmbSatuanActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnKirimBarang;
    private javax.swing.JComboBox<Object> cmbKategori;
    private javax.swing.JComboBox<Object> cmbSatuan;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel lblCari;
    private javax.swing.JLabel lblNamaGudangUser;
    private javax.swing.JLabel lblPilihKategori;
    private javax.swing.JLabel lblPilihSatuan;
    private javax.swing.JPanel pnlHeader;
    private javax.swing.JPanel pnlWadahTabel;
    private javax.swing.JTable tblBarang;
    private javax.swing.JTextField txtCariBarang;
    // End of variables declaration//GEN-END:variables
}
