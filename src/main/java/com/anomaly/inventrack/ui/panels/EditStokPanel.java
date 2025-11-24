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
import java.util.ArrayList;
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
public class EditStokPanel extends javax.swing.JPanel {
    
    private StokRepositories stokRepo = new StokRepositories();
    private BarangRepositories barangRepo = new BarangRepositories();
    private GudangRepositories gudangRepo = new GudangRepositories();
    
    private DefaultTableModel tableModel;
    
    private Map<Integer, Barang> mapBarang = new HashMap<>();
    private Map<Integer, String> mapNamaGudang = new HashMap<>();
    
    private int currentUserGudangId = -1;

    /**
     * Creates new form DashboardPanel
     */
    public EditStokPanel() {
        initComponents();
        
        // Nanti dihapus
        if (java.beans.Beans.isDesignTime()) {
            return;
        }
        
        btnEditStok.addActionListener(e -> bukaWindowEdit());
        
        cmbKategori.addActionListener(e -> loadDataStok());
        cmbSatuan.addActionListener(e -> loadDataStok());
        txtCariBarang.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                loadDataStok();
            }
        });
        
        setupTable();
        loadDataPendukung();
        loadComboKategori();
        loadComboSatuan();
        loadDataStok();
        
        btnEditStok.setVisible(false);
    }
    
    public void setCurrentUser(Pengguna user) {
        if (user != null) {
            this.currentUserGudangId = user.getIdGudang();
            
            String namaGudang = mapNamaGudang.getOrDefault(this.currentUserGudangId, "Gudang Tidak Dikenal");
            
            lblNamaGudangUser.setText(namaGudang);
            
            loadDataStok();
        }
    }
    
    private void setupTable() {
        String[] judul = {"Pilih", "ID Stok", "ID Barang", "Nama Barang", "Kategori", "Jumlah", "Satuan"};
        
        tableModel = new DefaultTableModel(judul, 0) {
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 0) return Boolean.class;
                return super.getColumnClass(columnIndex);
            }
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 0;
            }
        };
        tblBarang.setModel(tableModel);
        tableModel.addTableModelListener(e -> cekPilihanCheckbox());
        tblBarang.getColumnModel().removeColumn(tblBarang.getColumnModel().getColumn(1));
        
        javax.swing.table.TableColumn colCheckbox = tblBarang.getColumnModel().getColumn(0);
        colCheckbox.setMinWidth(40);
        colCheckbox.setMaxWidth(40);
        colCheckbox.setPreferredWidth(40);
        
        javax.swing.table.TableColumn colId = tblBarang.getColumnModel().getColumn(1);
        colId.setMinWidth(60);
        colId.setMaxWidth(80);
        colId.setPreferredWidth(60);
        
        tblBarang.getColumnModel().getColumn(2).setPreferredWidth(250);
        
        tblBarang.setFillsViewportHeight(true);
    }
    
    private void cekPilihanCheckbox() {
        boolean adaYangDipilih = false;
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            Boolean isChecked = (Boolean) tableModel.getValueAt(i, 0);
            if (isChecked != null && isChecked) {
                adaYangDipilih = true;
                break;
            }
        }
        btnEditStok.setVisible(adaYangDipilih);
    }
    
    private void bukaWindowEdit() {
        List<Integer> selectedIdBarang = new ArrayList<>();
        
        for (int i = 0; i < tblBarang.getRowCount(); i++) {
            Boolean isChecked = (Boolean) tableModel.getValueAt(i, 0); 
            
            if (isChecked != null && isChecked) {
                int idBarang = (int) tableModel.getValueAt(i, 2); 
                selectedIdBarang.add(idBarang);
            }
        }
        
        if (!selectedIdBarang.isEmpty()) {
            javax.swing.JFrame parentFrame = (javax.swing.JFrame) javax.swing.SwingUtilities.getWindowAncestor(this);
            
            FormEditStok dialog = new FormEditStok(parentFrame, true, selectedIdBarang, currentUserGudangId);
            
            dialog.setVisible(true);
            loadDataStok();
            btnEditStok.setVisible(false);
        }
    }
    
    private void loadDataStok() {
        tableModel.setRowCount(0); 
        
        if (currentUserGudangId == -1) return;
        
        String selectedKategori = (String) cmbKategori.getSelectedItem();
        if (selectedKategori == null) selectedKategori = "Semua Kategori";
        
        String selectedSatuan = (String) cmbSatuan.getSelectedItem();
        if (selectedSatuan == null) selectedSatuan = "Semua Satuan";
        
        String keyword = txtCariBarang.getText().toLowerCase(); 

        List<Stok> listStokDb = stokRepo.getByGudang(currentUserGudangId);
        
        Map<Integer, Stok> mapStokCepat = new HashMap<>();
        for (Stok s : listStokDb) {
            mapStokCepat.put(s.getIdBarang(), s);
        }

        for (Barang b : mapBarang.values()) {
            
            Stok s = mapStokCepat.get(b.getIdBarang());
            
            int idStok      = (s != null) ? s.getIdStok() : -1;
            int jumlah      = (s != null) ? s.getJumlahStok() : 0;
            
            String namaBarang = b.getNamaBarang();
            String kategori   = (b.getKategori() != null) ? b.getKategori() : "-";
            String satuan     = (b.getSatuan() != null) ? b.getSatuan() : "-";
            String idBarangStr = String.valueOf(b.getIdBarang());
            
            boolean matchKategori = selectedKategori.equals("Semua Kategori") || 
                                    kategori.equalsIgnoreCase(selectedKategori);
            
            boolean matchSatuan   = selectedSatuan.equals("Semua Satuan") || 
                                    satuan.equalsIgnoreCase(selectedSatuan);

            boolean matchKeyword  = keyword.isEmpty() || 
                                    idBarangStr.contains(keyword) ||
                                    namaBarang.toLowerCase().contains(keyword) ||
                                    kategori.toLowerCase().contains(keyword) ||
                                    satuan.toLowerCase().contains(keyword);

            // 7. Masukkan ke Tabel
            if (matchKategori && matchKeyword && matchSatuan) {
                tableModel.addRow(new Object[]{
                    false,
                    idStok,
                    b.getIdBarang(),
                    namaBarang,
                    kategori,
                    jumlah,
                    satuan
                });
            }
        }
        
        // Reset tombol edit
        btnEditStok.setVisible(false);
        aturTinggiTabel();
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

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup1 = new javax.swing.ButtonGroup();
        jCheckBoxMenuItem1 = new javax.swing.JCheckBoxMenuItem();
        pnlHeader = new javax.swing.JPanel();
        lblNamaGudangUser = new javax.swing.JLabel();
        lblCari = new javax.swing.JLabel();
        txtCariBarang = new javax.swing.JTextField();
        cmbKategori = new javax.swing.JComboBox<>();
        lblPilihKategori = new javax.swing.JLabel();
        lblPilihSatuan = new javax.swing.JLabel();
        cmbSatuan = new javax.swing.JComboBox<>();
        btnEditStok = new javax.swing.JButton();
        pnlWadahTabel = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblBarang = new javax.swing.JTable();

        jCheckBoxMenuItem1.setSelected(true);
        jCheckBoxMenuItem1.setText("jCheckBoxMenuItem1");

        setLayout(new java.awt.BorderLayout());

        lblNamaGudangUser.setFont(new java.awt.Font("Arial", 1, 12)); // NOI18N

        lblCari.setFont(new java.awt.Font("Arial", 1, 12)); // NOI18N
        lblCari.setText("Cari Barang:");

        txtCariBarang.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N

        cmbKategori.setFont(new java.awt.Font("Arial", 1, 12)); // NOI18N
        cmbKategori.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        lblPilihKategori.setFont(new java.awt.Font("Arial", 1, 12)); // NOI18N
        lblPilihKategori.setText("Pilih Kategori");

        lblPilihSatuan.setFont(new java.awt.Font("Arial", 1, 12)); // NOI18N
        lblPilihSatuan.setText("Pilih Satuan");

        cmbSatuan.setFont(new java.awt.Font("Arial", 1, 12)); // NOI18N
        cmbSatuan.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        btnEditStok.setText("Edit Stok");

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
                        .addComponent(btnEditStok))
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
                    .addComponent(btnEditStok, javax.swing.GroupLayout.Alignment.TRAILING)))
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

    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnEditStok;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JComboBox<Object> cmbKategori;
    private javax.swing.JComboBox<Object> cmbSatuan;
    private javax.swing.JCheckBoxMenuItem jCheckBoxMenuItem1;
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
