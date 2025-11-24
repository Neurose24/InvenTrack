/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package com.anomaly.inventrack.ui.panels;

import com.anomaly.inventrack.models.Barang;
import com.anomaly.inventrack.models.Gudang;
import com.anomaly.inventrack.models.LogStok;
import com.anomaly.inventrack.models.LogStok.TipeTransaksi;
import com.anomaly.inventrack.models.Pengguna;
import com.anomaly.inventrack.repositories.BarangRepositories;
import com.anomaly.inventrack.repositories.GudangRepositories;
import com.anomaly.inventrack.repositories.LogStokRepositories;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author user
 */
public class LogStokPanel extends javax.swing.JPanel {
    
    private final LogStokRepositories logRepo = new LogStokRepositories();
    private final BarangRepositories barangRepo = new BarangRepositories();
    private final GudangRepositories gudangRepo = new GudangRepositories();
    
    private DefaultTableModel tableModel;
    
    private Map<Integer, Barang> mapBarang = new HashMap<>();
    private Map<Integer, String> mapNamaGudang = new HashMap<>();
    
    private int currentUserGudangId = -1;

    /**
     * Creates new form LogStokPanel
     */
    public LogStokPanel() {
        initComponents();
        
        if (java.beans.Beans.isDesignTime()) return;
        
        setupUI();
        setupListeners();
        
        this.addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentShown(java.awt.event.ComponentEvent e) {
                loadDataMaster();
                loadDataLog();
            }
        });
    }
    
    public void setCurrentUser(Pengguna user) {
        if (user != null) {
            this.currentUserGudangId = user.getIdGudang();
            loadDataMaster();
            loadDataLog();
        }
    }
    
    private void setupUI() {
        cmbTipeTransaksi.removeAllItems();
        cmbTipeTransaksi.addItem("Semua Tipe");
        for (TipeTransaksi t : TipeTransaksi.values()) {
            cmbTipeTransaksi.addItem(t.name());
        }
        
        String[] header = {"ID Log", "Tanggal", "Gudang", "Nama Barang", "Kategori", "Tipe", "Jumlah", "Keterangan"};
        
        tableModel = new DefaultTableModel(header, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
            @Override public Class<?> getColumnClass(int columnIndex) {
                return (columnIndex == 6) ? Integer.class : String.class; // Kolom Jumlah angka
            }
        };
        
        tblLogStok.setModel(tableModel);

        tblLogStok.getColumnModel().getColumn(0).setPreferredWidth(80);  // ID
        tblLogStok.getColumnModel().getColumn(1).setPreferredWidth(120); // Tanggal
        tblLogStok.getColumnModel().getColumn(2).setPreferredWidth(120); // Gudang
        tblLogStok.getColumnModel().getColumn(3).setPreferredWidth(200); // Nama Barang
        tblLogStok.getColumnModel().getColumn(7).setPreferredWidth(200); // Keterangan
        
        tblLogStok.setRowHeight(25);
    }
    
    private void setupListeners() {
        cmbTipeTransaksi.addActionListener(e -> loadDataLog());
        cmbKategori.addActionListener(e -> loadDataLog());
        cmbSatuan.addActionListener(e -> loadDataLog());
        
        txtCariBarang.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                loadDataLog();
            }
        });
    }
    
    private void loadDataMaster() {
        mapBarang.clear();
        mapNamaGudang.clear();
        
        List<Barang> listBarang = barangRepo.findAll();
        for (Barang b : listBarang) mapBarang.put(b.getIdBarang(), b);
        
        List<Gudang> listGudang = gudangRepo.findAll();
        for (Gudang g : listGudang) mapNamaGudang.put(g.getIdGudang(), g.getNamaGudang());
        
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
    
    private void loadDataLog() {
        if (currentUserGudangId == -1) return;
        
        if (mapBarang.isEmpty()) loadDataMaster();
        
        tableModel.setRowCount(0);

        List<LogStok> listLog = logRepo.findAll();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");

        String filterTipe = (String) cmbTipeTransaksi.getSelectedItem();
        String filterKategori = (String) cmbKategori.getSelectedItem();
        String filterSatuan = (String) cmbSatuan.getSelectedItem();
        String keyword = txtCariBarang.getText().toLowerCase();
        
        for (LogStok log : listLog) {
            if (log.getIdGudang() != currentUserGudangId) continue;

            Barang b = mapBarang.get(log.getIdBarang());
            String namaBarang = (b != null) ? b.getNamaBarang() : "Unknown ID:" + log.getIdBarang();
            String kategori = (b != null) ? b.getKategori() : "-";
            String satuan = (b != null) ? b.getSatuan() : "-";
            String namaGudang = mapNamaGudang.getOrDefault(log.getIdGudang(), "-");

            boolean matchTipe = filterTipe.equals("Semua Tipe") || log.getTipeTransaksi().name().equals(filterTipe);
            
            boolean matchKategori = filterKategori == null || filterKategori.equals("Semua Kategori") || 
                                    kategori.equalsIgnoreCase(filterKategori);
            
            boolean matchSatuan = filterSatuan == null || filterSatuan.equals("Semua Satuan") || 
                                  satuan.equalsIgnoreCase(filterSatuan);
            
            boolean matchKeyword = keyword.isEmpty() || 
                                   namaBarang.toLowerCase().contains(keyword) ||
                                   log.getKeterangan().toLowerCase().contains(keyword) ||
                                   String.valueOf(log.getIdLog()).contains(keyword);
            
            if (matchTipe && matchKategori && matchSatuan && matchKeyword) {
                String idTampil = String.format("%011d", log.getIdLog());
                
                tableModel.addRow(new Object[]{
                    idTampil,
                    log.getTanggalLog().format(formatter),
                    namaGudang,
                    namaBarang,
                    kategori,
                    log.getTipeTransaksi(),
                    log.getJumlahPerubahan(),
                    log.getKeterangan()
                });
            }
        }
        
        aturTinggiTabel();
    }
    
    private void aturTinggiTabel() {
        int tinggi = tblLogStok.getTableHeader().getPreferredSize().height + (tblLogStok.getRowHeight() * tblLogStok.getRowCount());
        if (tinggi < 100) tinggi = 100;
        if (tinggi > 400) tinggi = 400;
        
        tblLogStok.setPreferredScrollableViewportSize(new java.awt.Dimension(tblLogStok.getPreferredSize().width, tinggi));
        tblLogStok.revalidate();
        tblLogStok.repaint();
        
        if (tblLogStok.getParent() != null && tblLogStok.getParent().getParent() instanceof javax.swing.JScrollPane) {
            ((javax.swing.JScrollPane)tblLogStok.getParent().getParent()).revalidate();
        }
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
        lblTipeTransaksi = new javax.swing.JLabel();
        cmbTipeTransaksi = new javax.swing.JComboBox<>();
        lblCari = new javax.swing.JLabel();
        txtCariBarang = new javax.swing.JTextField();
        cmbKategori = new javax.swing.JComboBox<>();
        lblPilihKategori = new javax.swing.JLabel();
        lblPilihSatuan = new javax.swing.JLabel();
        cmbSatuan = new javax.swing.JComboBox<>();
        pnlWadahTabel = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblLogStok = new javax.swing.JTable();

        setLayout(new java.awt.BorderLayout());

        lblTipeTransaksi.setFont(new java.awt.Font("Arial", 1, 12)); // NOI18N
        lblTipeTransaksi.setText("Pilih Tipe Transaksi");

        cmbTipeTransaksi.setFont(new java.awt.Font("Arial", 1, 12)); // NOI18N
        cmbTipeTransaksi.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        lblCari.setFont(new java.awt.Font("Arial", 1, 12)); // NOI18N
        lblCari.setText("Cari Barang / IdLog:");

        txtCariBarang.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N

        cmbKategori.setFont(new java.awt.Font("Arial", 1, 12)); // NOI18N
        cmbKategori.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        lblPilihKategori.setFont(new java.awt.Font("Arial", 1, 12)); // NOI18N
        lblPilihKategori.setText("Pilih Kategori");

        lblPilihSatuan.setFont(new java.awt.Font("Arial", 1, 12)); // NOI18N
        lblPilihSatuan.setText("Pilih Satuan");

        cmbSatuan.setFont(new java.awt.Font("Arial", 1, 12)); // NOI18N
        cmbSatuan.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        javax.swing.GroupLayout pnlHeaderLayout = new javax.swing.GroupLayout(pnlHeader);
        pnlHeader.setLayout(pnlHeaderLayout);
        pnlHeaderLayout.setHorizontalGroup(
            pnlHeaderLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlHeaderLayout.createSequentialGroup()
                .addGap(41, 41, 41)
                .addGroup(pnlHeaderLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlHeaderLayout.createSequentialGroup()
                        .addComponent(lblTipeTransaksi, javax.swing.GroupLayout.PREFERRED_SIZE, 114, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cmbTipeTransaksi, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(pnlHeaderLayout.createSequentialGroup()
                        .addComponent(lblPilihSatuan, javax.swing.GroupLayout.PREFERRED_SIZE, 114, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cmbSatuan, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(pnlHeaderLayout.createSequentialGroup()
                        .addComponent(lblPilihKategori, javax.swing.GroupLayout.PREFERRED_SIZE, 114, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cmbKategori, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 281, Short.MAX_VALUE)
                .addComponent(lblCari)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txtCariBarang, javax.swing.GroupLayout.PREFERRED_SIZE, 151, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(62, 62, 62))
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
                        .addGroup(pnlHeaderLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(lblTipeTransaksi, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(cmbTipeTransaksi, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(pnlHeaderLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(lblPilihKategori, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(cmbKategori, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(pnlHeaderLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblPilihSatuan, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cmbSatuan, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
        );

        add(pnlHeader, java.awt.BorderLayout.PAGE_START);

        pnlWadahTabel.setBorder(javax.swing.BorderFactory.createEmptyBorder(20, 40, 20, 40));
        pnlWadahTabel.setPreferredSize(new java.awt.Dimension(100, 80));
        pnlWadahTabel.setLayout(new java.awt.BorderLayout());

        jScrollPane1.setPreferredSize(new java.awt.Dimension(0, 0));

        tblLogStok.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        tblLogStok.setModel(new javax.swing.table.DefaultTableModel(
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
        tblLogStok.setMaximumSize(new java.awt.Dimension(2147483647, 30000));
        tblLogStok.setShowGrid(false);
        jScrollPane1.setViewportView(tblLogStok);

        pnlWadahTabel.add(jScrollPane1, java.awt.BorderLayout.CENTER);

        add(pnlWadahTabel, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox<Object> cmbKategori;
    private javax.swing.JComboBox<Object> cmbSatuan;
    private javax.swing.JComboBox<Object> cmbTipeTransaksi;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel lblCari;
    private javax.swing.JLabel lblPilihKategori;
    private javax.swing.JLabel lblPilihSatuan;
    private javax.swing.JLabel lblTipeTransaksi;
    private javax.swing.JPanel pnlHeader;
    private javax.swing.JPanel pnlWadahTabel;
    private javax.swing.JTable tblLogStok;
    private javax.swing.JTextField txtCariBarang;
    // End of variables declaration//GEN-END:variables
}
