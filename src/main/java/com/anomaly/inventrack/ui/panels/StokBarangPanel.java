/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package com.anomaly.inventrack.ui.panels;

import com.anomaly.inventrack.models.Barang;
import com.anomaly.inventrack.models.Gudang;
import com.anomaly.inventrack.models.Stok;
import com.anomaly.inventrack.repositories.BarangRepositories;
import com.anomaly.inventrack.repositories.GudangRepositories;
import com.anomaly.inventrack.repositories.StokRepositories;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author user
 */
public class StokBarangPanel extends javax.swing.JPanel {
    
    private StokRepositories stokRepo = new StokRepositories();
    private BarangRepositories barangRepo = new BarangRepositories();
    private GudangRepositories gudangRepo = new GudangRepositories();
    
    private DefaultTableModel tableModel;
    
    private Map<Integer, Barang> mapBarang = new HashMap<>();
    private Map<Integer, String> mapNamaGudang = new HashMap<>();

    /**
     * Creates new form DashboardPanel
     */
    public StokBarangPanel() {
        initComponents();
        
        cmbGudang.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                loadDataStok();
            }
        });
        
        cmbKategori.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                loadDataStok();
            }
        });
        
        cmbSatuan.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                loadDataStok();
            }
        });

        txtCariBarang.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                loadDataStok();
            }
        });
        setupTable();
        loadDataPendukung(); // Load data Barang & Gudang ke Memori
        loadComboGudang();   // Isi ComboBox
        loadComboKategori();
        loadComboSatuan();
        loadDataStok();
    }
    
    private void setupTable() {
        String[] judul = {"ID Stok", "ID Barang", "Nama Barang", "Kategori", "Lokasi Gudang", "Jumlah", "Satuan"};
        
        tableModel = new DefaultTableModel(judul, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tblBarang.setModel(tableModel);
        
        tblBarang.getColumnModel().removeColumn(tblBarang.getColumnModel().getColumn(0));
        tblBarang.getColumnModel().getColumn(0).setPreferredWidth(50); 
        tblBarang.getColumnModel().getColumn(1).setPreferredWidth(200); 
        tblBarang.getColumnModel().getColumn(2).setPreferredWidth(100);
        tblBarang.getColumnModel().getColumn(3).setPreferredWidth(150);
        tblBarang.getColumnModel().getColumn(4).setPreferredWidth(60);
    }
    
    private void loadDataPendukung() {
        List<Barang> listBarang = barangRepo.findAll();
        for (Barang b : listBarang) {
            mapBarang.put(b.getIdBarang(), b);
        }

        List<Gudang> listGudang = gudangRepo.findAll();
        for (Gudang g : listGudang) {
            mapNamaGudang.put(g.getIdGudang(), g.getNamaGudang());
        }
    }
    
    private void loadComboGudang() {
        cmbGudang.removeAllItems();
        cmbGudang.addItem(new ComboItem("Semua Gudang", -1));
        
        // Ambil data asli dari Repo
        List<Gudang> listGudang = gudangRepo.findAll();
        for (Gudang g : listGudang) {
            cmbGudang.addItem(new ComboItem(g.getNamaGudang(), g.getIdGudang()));
        }
    }
    
    private void loadComboKategori() {
        cmbKategori.removeAllItems();
        cmbKategori.addItem("Semua Kategori");

        java.util.Set<String> kategoriUnik = new java.util.HashSet<>();
        
        for (Barang b : mapBarang.values()) {
            if (b.getKategori() != null && !b.getKategori().isEmpty()) {
                kategoriUnik.add(b.getKategori());
            }
        }
        
        for (String k : kategoriUnik) {
            cmbKategori.addItem(k);
        }
    }
    
    private void loadComboSatuan() {
        cmbSatuan.removeAllItems();
        cmbSatuan.addItem("Semua Satuan");

        java.util.Set<String> satuanUnik = new java.util.HashSet<>();
        
        for (Barang b : mapBarang.values()) {
            if (b.getSatuan() != null && !b.getSatuan().isEmpty()) {
                satuanUnik.add(b.getSatuan());
            }
        }
        
        for (String s : satuanUnik) {
            cmbSatuan.addItem(s);
        }
    }
    
    private void loadDataStok() {
        tableModel.setRowCount(0); 
        
        ComboItem selectedItem = (ComboItem) cmbGudang.getSelectedItem();
        int idGudangPilih = (selectedItem != null) ? selectedItem.getValue() : -1;
        String selectedKategori = (String) cmbKategori.getSelectedItem();
        if (selectedKategori == null) selectedKategori = "Semua Kategori";
        String selectedSatuan = (String) cmbSatuan.getSelectedItem();
        if (selectedSatuan == null) selectedSatuan = "Semua Satuan";
        
        String keyword = txtCariBarang.getText().toLowerCase(); 

        List<Stok> listStok;
        if (idGudangPilih == -1) {
            listStok = stokRepo.getAll(); 
        } else {
            listStok = stokRepo.getByGudang(idGudangPilih); 
        }

        for (Stok s : listStok) {
            Barang b = mapBarang.get(s.getIdBarang());
            String namaGudang = mapNamaGudang.getOrDefault(s.getIdGudang(), "Unknown");
            
            String namaBarang = (b != null) ? b.getNamaBarang() : "Unknown";
            String kategori = (b != null) ? b.getKategori() : "-";
            String satuan = (b != null) ? b.getSatuan() : "-";
            
            String idBarangStr = String.valueOf(s.getIdBarang());
            
            boolean matchKategori = selectedKategori.equals("Semua Kategori") || 
                                    kategori.equalsIgnoreCase(selectedKategori);
            
            boolean matchSatuan = selectedSatuan.equals("Semua Satuan") || 
                                    satuan.equalsIgnoreCase(selectedSatuan);

            boolean matchKeyword = keyword.isEmpty() || 
                                   idBarangStr.contains(keyword) ||
                                   namaBarang.toLowerCase().contains(keyword) ||
                                   kategori.toLowerCase().contains(keyword) ||
                                   satuan.toLowerCase().contains(keyword);

            if (matchKategori && matchKeyword && matchSatuan) {
                tableModel.addRow(new Object[]{
                    s.getIdStok(),
                    s.getIdBarang(),
                    namaBarang,
                    kategori,
                    namaGudang,
                    s.getJumlahStok(),
                    satuan
                });
            }
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

        buttonGroup1 = new javax.swing.ButtonGroup();
        pnlHeader = new javax.swing.JPanel();
        lblPilihGudang = new javax.swing.JLabel();
        cmbGudang = new javax.swing.JComboBox<>();
        lblCari = new javax.swing.JLabel();
        txtCariBarang = new javax.swing.JTextField();
        cmbKategori = new javax.swing.JComboBox<>();
        lblPilihKategori = new javax.swing.JLabel();
        lblPilihSatuan = new javax.swing.JLabel();
        cmbSatuan = new javax.swing.JComboBox<>();
        pnlWadahTabel = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblBarang = new javax.swing.JTable();

        setLayout(new java.awt.BorderLayout());

        lblPilihGudang.setFont(new java.awt.Font("Arial", 1, 12)); // NOI18N
        lblPilihGudang.setText("Pilih Gudang");

        cmbGudang.setFont(new java.awt.Font("Arial", 1, 12)); // NOI18N
        cmbGudang.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        lblCari.setFont(new java.awt.Font("Arial", 1, 12)); // NOI18N
        lblCari.setText("Cari Barang:");

        txtCariBarang.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N

        cmbKategori.setFont(new java.awt.Font("Arial", 1, 12)); // NOI18N
        cmbKategori.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        lblPilihKategori.setFont(new java.awt.Font("Arial", 1, 12)); // NOI18N
        lblPilihKategori.setText("Pilih Kategori");

        lblPilihSatuan.setFont(new java.awt.Font("Arial", 1, 12)); // NOI18N
        lblPilihSatuan.setText("Pilih Kategori");

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
                        .addComponent(lblPilihSatuan, javax.swing.GroupLayout.PREFERRED_SIZE, 114, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cmbSatuan, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(pnlHeaderLayout.createSequentialGroup()
                        .addGroup(pnlHeaderLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(pnlHeaderLayout.createSequentialGroup()
                                .addComponent(lblPilihKategori, javax.swing.GroupLayout.PREFERRED_SIZE, 114, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(cmbKategori, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(pnlHeaderLayout.createSequentialGroup()
                                .addComponent(lblPilihGudang, javax.swing.GroupLayout.PREFERRED_SIZE, 114, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(cmbGudang, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 322, Short.MAX_VALUE)
                        .addComponent(lblCari, javax.swing.GroupLayout.PREFERRED_SIZE, 89, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtCariBarang, javax.swing.GroupLayout.PREFERRED_SIZE, 151, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(42, 42, 42))))
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
                            .addComponent(lblPilihGudang, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(cmbGudang, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
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

    class ComboItem {
        private String key;
        private int value;

        public ComboItem(String key, int value) {
            this.key = key;
            this.value = value;
        }

        public int getValue() { return value; }

        @Override
        public String toString() { return key; } // Ini yang akan muncul di layar
    }
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JComboBox<Object> cmbGudang;
    private javax.swing.JComboBox<Object> cmbKategori;
    private javax.swing.JComboBox<Object> cmbSatuan;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel lblCari;
    private javax.swing.JLabel lblPilihGudang;
    private javax.swing.JLabel lblPilihKategori;
    private javax.swing.JLabel lblPilihSatuan;
    private javax.swing.JPanel pnlHeader;
    private javax.swing.JPanel pnlWadahTabel;
    private javax.swing.JTable tblBarang;
    private javax.swing.JTextField txtCariBarang;
    // End of variables declaration//GEN-END:variables
}
