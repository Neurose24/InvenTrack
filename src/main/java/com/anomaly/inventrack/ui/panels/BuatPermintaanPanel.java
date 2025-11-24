/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package com.anomaly.inventrack.ui.panels;

import com.anomaly.inventrack.models.Barang;
import com.anomaly.inventrack.models.Gudang;
import com.anomaly.inventrack.models.Pengguna;
import com.anomaly.inventrack.models.Stok;
import com.anomaly.inventrack.repositories.BarangRepositories;
import com.anomaly.inventrack.repositories.GudangRepositories;
import com.anomaly.inventrack.repositories.StokRepositories;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author user
 */
public class BuatPermintaanPanel extends javax.swing.JPanel {

    /**
     * Creates new form BuatPermintaanPanel
     */
    private final StokRepositories stokRepo = new StokRepositories();
    private final BarangRepositories barangRepo = new BarangRepositories();
    private final GudangRepositories gudangRepo = new GudangRepositories();
    
    private DefaultTableModel tableModel;
    
    // Cache Data
    private Map<Integer, Barang> mapBarang = new HashMap<>();
    private Map<Integer, String> mapNamaGudang = new HashMap<>();
    
    private int currentUserGudangId = -1;
    private int currentUserId = -1;

    public BuatPermintaanPanel() {
        initComponents();
        
        if (java.beans.Beans.isDesignTime()) return;
        
        setupListeners();
        setupTable();
        loadDataMaster();
        
        btnBuatPermintaan.setVisible(false);
    }
    
    public void setCurrentUser(Pengguna user) {
        if (user != null) {
            this.currentUserGudangId = user.getIdGudang();
            this.currentUserId = user.getIdPengguna();
            
            String namaGudang = mapNamaGudang.getOrDefault(this.currentUserGudangId, "Gudang Tidak Dikenal");
            lblNamaGudangUser.setText(namaGudang);
            
            loadComboGudang();
            
            loadDataStokEksternal();
        }
    }
    
    private void setupListeners() {
        btnBuatPermintaan.addActionListener(e -> bukaWindowPermintaan());
        
        cmbGudang.addActionListener(e -> loadDataStokEksternal());
        cmbKategori.addActionListener(e -> loadDataStokEksternal());
        cmbSatuan.addActionListener(e -> loadDataStokEksternal());
        txtCariBarang.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                loadDataStokEksternal();
            }
        });
    }
    
    private void setupTable() {
        // Kolom: Checkbox, ID Barang, ID Gudang, Nama Barang, Kategori, Gudang Sumber, Stok Tersedia, Satuan
        String[] header = {"Pilih", "ID Barang", "ID Gudang", "Nama Barang", "Kategori", "Gudang Sumber", "Tersedia", "Satuan"};
        
        tableModel = new DefaultTableModel(header, 0) {
            @Override
            public Class<?> getColumnClass(int col) { return (col == 0) ? Boolean.class : String.class; }
            @Override
            public boolean isCellEditable(int row, int col) { return col == 0; } // Hanya Checkbox Editable
        };
        
        tblBarang.setModel(tableModel);
        
        // Listener Checkbox untuk memunculkan tombol
        tableModel.addTableModelListener(e -> {
            boolean ada = false;
            for (int i=0; i<tableModel.getRowCount(); i++) {
                if ((Boolean)tableModel.getValueAt(i, 0)) { ada = true; break; }
            }
            btnBuatPermintaan.setVisible(ada);
        });
        
        // Hide ID Columns
        tblBarang.getColumnModel().removeColumn(tblBarang.getColumnModel().getColumn(1));
        tblBarang.getColumnModel().removeColumn(tblBarang.getColumnModel().getColumn(1));
        
        javax.swing.table.TableColumn colCheckbox = tblBarang.getColumnModel().getColumn(0);
        colCheckbox.setMinWidth(40);
        colCheckbox.setMaxWidth(40);
        colCheckbox.setPreferredWidth(40);
        
        tblBarang.getColumnModel().getColumn(1).setPreferredWidth(200);
        
        tblBarang.setFillsViewportHeight(true);
    }
    
    // Load Gudang untuk ComboBox (FILTER OUT Gudang Sendiri)
    private void loadComboGudang() {
        cmbGudang.removeAllItems();
        cmbGudang.addItem(new ComboItem("Semua Gudang Lain", -1));
        
        List<Gudang> listG = gudangRepo.findAll();
        for (Gudang g : listG) {
            // ATURAN: Tidak boleh pilih gudang sendiri
            if (g.getIdGudang() != currentUserGudangId) {
                cmbGudang.addItem(new ComboItem(g.getNamaGudang(), g.getIdGudang()));
            }
        }
        loadComboKategori();
        loadComboSatuan();
    }
    
    // Load Data Stok dari Gudang LAIN
    private void loadDataStokEksternal() {
        tableModel.setRowCount(0);
        if (currentUserGudangId == -1) return;
        
        // 1. Tentukan Target Gudang
        ComboItem selectedItem = (ComboItem) cmbGudang.getSelectedItem();
        int idTarget = (selectedItem != null) ? selectedItem.getValue() : -1;
        
        List<Gudang> targetGudangs;
        if (idTarget == -1) {
            // Ambil SEMUA gudang KECUALI gudang sendiri
            targetGudangs = new java.util.ArrayList<>();
            for (Gudang g : gudangRepo.findAll()) {
                if (g.getIdGudang() != currentUserGudangId) targetGudangs.add(g);
            }
        } else {
            targetGudangs = List.of(gudangRepo.findById(idTarget).get());
        }
        
        // 2. Ambil Data Stok (Grouping Matrix)
        List<Stok> allStok = stokRepo.getAll();
        Map<Integer, Map<Integer, Stok>> mapStokMatrix = new HashMap<>();
        for (Stok s : allStok) {
            mapStokMatrix.computeIfAbsent(s.getIdBarang(), k->new HashMap<>()).put(s.getIdGudang(), s);
        }
        
        // 3. Filter UI
        String fKategori = (String) cmbKategori.getSelectedItem();
        String fSatuan = (String) cmbSatuan.getSelectedItem();
        String keyword = txtCariBarang.getText().toLowerCase();
        
        // 4. Looping Data (Barang x Gudang Target)
        for (Barang b : mapBarang.values()) {
            // Filter Barang
            if (!matchFilter(b, fKategori, fSatuan, keyword)) continue;
            
            for (Gudang g : targetGudangs) {
                Stok s = null;
                if (mapStokMatrix.containsKey(b.getIdBarang())) {
                    s = mapStokMatrix.get(b.getIdBarang()).get(g.getIdGudang());
                }
                
                int jumlah = (s != null) ? s.getJumlahStok() : 0;
                
                tableModel.addRow(new Object[]{
                    false,
                    b.getIdBarang(),
                    g.getIdGudang(),
                    b.getNamaBarang(),
                    b.getKategori(),
                    g.getNamaGudang(),
                    jumlah,
                    b.getSatuan()
                });
            }
        }
        btnBuatPermintaan.setVisible(false);
        aturTinggiTabel();
    }
    
    private void bukaWindowPermintaan() {
        // Map<ID Barang, ID Gudang Sumber>
        Map<Integer, Integer> selectedItems = new HashMap<>();
        
        for (int i=0; i<tableModel.getRowCount(); i++) {
            if ((Boolean)tableModel.getValueAt(i, 0)) {
                int idBarang = (int) tableModel.getValueAt(i, 1);
                int idGudangSumber = (int) tableModel.getValueAt(i, 2);
                
                selectedItems.put(idBarang, idGudangSumber);
            }
        }
        
        if (!selectedItems.isEmpty()) {
            JFrame parent = (JFrame) SwingUtilities.getWindowAncestor(this);
            // Buka Dialog
            FormBuatPermintaan dialog = new FormBuatPermintaan(parent, true, selectedItems, currentUserId);
            dialog.setVisible(true);
            
            // Refresh setelah tutup
            loadDataStokEksternal();
        }
    }
    
    // --- Helpers ---
    private void loadDataMaster() {
        mapBarang.clear();
        mapNamaGudang.clear();
        for(Barang b : barangRepo.findAll()) mapBarang.put(b.getIdBarang(), b);
        for(Gudang g : gudangRepo.findAll()) mapNamaGudang.put(g.getIdGudang(), g.getNamaGudang());
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
    
    private boolean matchFilter(Barang b, String k, String s, String key) {
        return true;
    }
    
    private void aturTinggiTabel() {
        int tinggiHeader = tblBarang.getTableHeader().getPreferredSize().height;
        int tinggiBaris = tblBarang.getRowHeight();
        int jumlahBaris = tblBarang.getRowCount();
        
        int totalTinggi = tinggiHeader + (tinggiBaris * jumlahBaris);

        int minTinggi = 100;
        int maxTinggi = 500;
        
        if (totalTinggi < minTinggi) totalTinggi = minTinggi;
        if (totalTinggi > maxTinggi) totalTinggi = maxTinggi;
        
        // 3. Terapkan ke Tabel (Viewport)
        java.awt.Dimension dim = new java.awt.Dimension(
            tblBarang.getPreferredSize().width,
            totalTinggi
        );
        
        tblBarang.setPreferredScrollableViewportSize(dim);
        
        tblBarang.revalidate();
        tblBarang.repaint();
        
        if (tblBarang.getParent() != null && tblBarang.getParent().getParent() instanceof javax.swing.JScrollPane) {
            ((javax.swing.JScrollPane)tblBarang.getParent().getParent()).revalidate();
        }
    }
    
    class ComboItem {
    private String key;
    private int value;

    public ComboItem(String key, int value) {
        this.key = key;
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    @Override
    public String toString() {
        return key; // Ini teks yang akan muncul di layar
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
        lblPilihGudang = new javax.swing.JLabel();
        cmbGudang = new javax.swing.JComboBox<>();
        lblCari = new javax.swing.JLabel();
        txtCariBarang = new javax.swing.JTextField();
        cmbKategori = new javax.swing.JComboBox<>();
        lblPilihKategori = new javax.swing.JLabel();
        btnBuatPermintaan = new javax.swing.JButton();
        lblNamaGudangUser = new javax.swing.JLabel();
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

        btnBuatPermintaan.setText("Buat Permintaan");

        lblNamaGudangUser.setFont(new java.awt.Font("Arial", 1, 12)); // NOI18N

        lblPilihSatuan.setFont(new java.awt.Font("Arial", 1, 12)); // NOI18N
        lblPilihSatuan.setText("Pilih Kategori");

        cmbSatuan.setFont(new java.awt.Font("Arial", 1, 12)); // NOI18N
        cmbSatuan.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        javax.swing.GroupLayout pnlHeaderLayout = new javax.swing.GroupLayout(pnlHeader);
        pnlHeader.setLayout(pnlHeaderLayout);
        pnlHeaderLayout.setHorizontalGroup(
            pnlHeaderLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlHeaderLayout.createSequentialGroup()
                .addGap(40, 40, 40)
                .addGroup(pnlHeaderLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlHeaderLayout.createSequentialGroup()
                        .addGroup(pnlHeaderLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblNamaGudangUser, javax.swing.GroupLayout.PREFERRED_SIZE, 114, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(lblPilihGudang, javax.swing.GroupLayout.PREFERRED_SIZE, 114, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(6, 6, 6)
                        .addComponent(cmbGudang, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(312, 312, 312)
                        .addComponent(lblCari, javax.swing.GroupLayout.PREFERRED_SIZE, 89, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(txtCariBarang, javax.swing.GroupLayout.PREFERRED_SIZE, 151, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(pnlHeaderLayout.createSequentialGroup()
                        .addComponent(lblPilihKategori, javax.swing.GroupLayout.PREFERRED_SIZE, 114, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(6, 6, 6)
                        .addComponent(cmbKategori, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(lblPilihSatuan, javax.swing.GroupLayout.PREFERRED_SIZE, 114, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(6, 6, 6)
                        .addComponent(cmbSatuan, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(242, 242, 242)
                        .addComponent(btnBuatPermintaan))))
        );
        pnlHeaderLayout.setVerticalGroup(
            pnlHeaderLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlHeaderLayout.createSequentialGroup()
                .addGap(6, 6, 6)
                .addGroup(pnlHeaderLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlHeaderLayout.createSequentialGroup()
                        .addComponent(lblNamaGudangUser, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(6, 6, 6)
                        .addComponent(lblPilihGudang, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(pnlHeaderLayout.createSequentialGroup()
                        .addGap(41, 41, 41)
                        .addComponent(cmbGudang, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(pnlHeaderLayout.createSequentialGroup()
                        .addGap(28, 28, 28)
                        .addComponent(lblCari, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(pnlHeaderLayout.createSequentialGroup()
                        .addGap(33, 33, 33)
                        .addComponent(txtCariBarang, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGroup(pnlHeaderLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlHeaderLayout.createSequentialGroup()
                        .addGap(13, 13, 13)
                        .addComponent(btnBuatPermintaan))
                    .addGroup(pnlHeaderLayout.createSequentialGroup()
                        .addGap(11, 11, 11)
                        .addGroup(pnlHeaderLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblPilihSatuan, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(lblPilihKategori, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(pnlHeaderLayout.createSequentialGroup()
                                .addGap(5, 5, 5)
                                .addGroup(pnlHeaderLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(cmbSatuan, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(cmbKategori, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))))
                .addGap(7, 7, 7))
        );

        add(pnlHeader, java.awt.BorderLayout.PAGE_START);

        pnlWadahTabel.setBorder(javax.swing.BorderFactory.createEmptyBorder(20, 40, 20, 40));
        pnlWadahTabel.setLayout(new java.awt.BorderLayout());

        jScrollPane1.setPreferredSize(new java.awt.Dimension(452, 1500));

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


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnBuatPermintaan;
    private javax.swing.JComboBox<Object> cmbGudang;
    private javax.swing.JComboBox<Object> cmbKategori;
    private javax.swing.JComboBox<Object> cmbSatuan;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel lblCari;
    private javax.swing.JLabel lblNamaGudangUser;
    private javax.swing.JLabel lblPilihGudang;
    private javax.swing.JLabel lblPilihKategori;
    private javax.swing.JLabel lblPilihSatuan;
    private javax.swing.JPanel pnlHeader;
    private javax.swing.JPanel pnlWadahTabel;
    private javax.swing.JTable tblBarang;
    private javax.swing.JTextField txtCariBarang;
    // End of variables declaration//GEN-END:variables
}
