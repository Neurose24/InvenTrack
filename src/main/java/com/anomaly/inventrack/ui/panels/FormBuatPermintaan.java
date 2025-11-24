/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JDialog.java to edit this template
 */
package com.anomaly.inventrack.ui.panels;

import com.anomaly.inventrack.models.Barang;
import com.anomaly.inventrack.models.DetailPermintaan;
import com.anomaly.inventrack.models.Gudang;
import com.anomaly.inventrack.models.Permintaan;
import com.anomaly.inventrack.models.Stok;
import com.anomaly.inventrack.repositories.BarangRepositories;
import com.anomaly.inventrack.repositories.GudangRepositories;
import com.anomaly.inventrack.repositories.StokRepositories;
import com.anomaly.inventrack.services.PermintaanService;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author user
 */
public class FormBuatPermintaan extends javax.swing.JDialog {
    
    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(FormBuatPermintaan.class.getName());
    
    private final Map<Integer, Integer> selectedItems;
    private final int idUserPeminta; 
    
    // Dependencies
    private final StokRepositories stokRepo = new StokRepositories();
    private final BarangRepositories barangRepo = new BarangRepositories();
    private final GudangRepositories gudangRepo = new GudangRepositories();
    private final PermintaanService permintaanService = new PermintaanService();
    
    private DefaultTableModel tableModel;

    /**
     * Constructor Utama
     * @param parent
     * @param modal
     * @param selectedItems Map<ID Barang, ID Gudang Sumber>
     * @param idUserPeminta
     */
    public FormBuatPermintaan(java.awt.Frame parent, boolean modal, Map<Integer, Integer> selectedItems, int idUserPeminta) {
        super(parent, modal);
        initComponents();
        
        this.selectedItems = selectedItems;
        this.idUserPeminta = idUserPeminta;
        
        setupUI();
        loadData();
    }
    
    private void setupUI() {
        setTitle("Konfirmasi Permintaan Barang");
        setLocationRelativeTo(null);
        
        String[] header = {"ID Barang", "ID Gudang", "Nama Barang", "Dari Gudang", "Stok Tersedia", "Jml Diminta"};
        
        tableModel = new DefaultTableModel(header, 0) {
            @Override 
            public boolean isCellEditable(int row, int column) { 
                return column == 5;
            }
            
            @Override 
            public Class<?> getColumnClass(int columnIndex) {
                return (columnIndex >= 4) ? Integer.class : String.class;
            }
        };
        tblBarang.setModel(tableModel);
        
        tblBarang.getColumnModel().removeColumn(tblBarang.getColumnModel().getColumn(0));
        tblBarang.getColumnModel().removeColumn(tblBarang.getColumnModel().getColumn(0));
        
        tblBarang.getColumnModel().getColumn(0).setPreferredWidth(200);
        tblBarang.getColumnModel().getColumn(1).setPreferredWidth(150);
        
        tblBarang.setFillsViewportHeight(true);
    }
    
    private void loadData() {
        tableModel.setRowCount(0);
        
        for (Map.Entry<Integer, Integer> entry : selectedItems.entrySet()) {
            int idBarang = entry.getKey();
            int idGudangSumber = entry.getValue();
            
            Optional<Barang> b = barangRepo.findById(idBarang);
            Optional<Stok> s = stokRepo.findByBarangAndGudang(idBarang, idGudangSumber);
            Optional<Gudang> g = gudangRepo.findById(idGudangSumber);
            
            if (b.isPresent() && s.isPresent() && g.isPresent()) {
                tableModel.addRow(new Object[]{
                    idBarang,
                    idGudangSumber,
                    b.get().getNamaBarang(),
                    g.get().getNamaGudang(),
                    s.get().getJumlahStok(),
                    0
                });
            }
        }
    }
    
    private void prosesSimpan() {
        if (tblBarang.getCellEditor() != null) {
            tblBarang.getCellEditor().stopCellEditing();
        }
        
        try {
            Map<Integer, List<DetailPermintaan>> mapPermintaanPerGudang = new HashMap<>();
            
            for (int i = 0; i < tableModel.getRowCount(); i++) {
                int idBarang = (int) tableModel.getValueAt(i, 0);
                int idGudangSumber = (int) tableModel.getValueAt(i, 1);
                int stokTersedia = (int) tableModel.getValueAt(i, 4);
                int jmlDiminta = (int) tableModel.getValueAt(i, 5);
                String namaBarang = (String) tableModel.getValueAt(i, 2);
                
                if (jmlDiminta <= 0) continue;
                if (jmlDiminta > stokTersedia) {
                    throw new Exception("Jumlah diminta melebihi stok tersedia untuk: " + namaBarang);
                }
                
                DetailPermintaan detail = new DetailPermintaan();
                detail.setIdBarang(idBarang);
                detail.setJumlahDiminta(jmlDiminta);
                
                mapPermintaanPerGudang
                    .computeIfAbsent(idGudangSumber, k -> new ArrayList<>())
                    .add(detail);
            }
            
            if (mapPermintaanPerGudang.isEmpty()) {
                throw new Exception("Mohon isi jumlah permintaan minimal untuk satu barang.");
            }
            
            int totalGudang = mapPermintaanPerGudang.size();
            String pesan;
            String judul;
            
            if (totalGudang > 1) {
                pesan = "Peringatan: Anda meminta barang dari " + totalGudang + " GUDANG BERBEDA.\n" +
                        "Sistem akan otomatis memecahnya menjadi " + totalGudang + " ID Permintaan terpisah.\n\n" +
                        "Lanjutkan proses ini?";
                judul = "Konfirmasi Multi-Gudang";
            } else {
                int idSatuGudang = mapPermintaanPerGudang.keySet().iterator().next();
                Optional<Gudang> g = gudangRepo.findById(idSatuGudang);
                String namaG = g.isPresent() ? g.get().getNamaGudang() : "Gudang Tujuan";
                
                pesan = "Kirim permintaan barang ke " + namaG + "?";
                judul = "Konfirmasi Permintaan";
            }
            
            int confirm = JOptionPane.showConfirmDialog(this, pesan, judul, JOptionPane.YES_NO_OPTION);
            if (confirm != JOptionPane.YES_OPTION) return;

            int suksesCount = 0;
            StringBuilder logSukses = new StringBuilder();
            
            for (Map.Entry<Integer, List<DetailPermintaan>> entry : mapPermintaanPerGudang.entrySet()) {
                int idSumber = entry.getKey();
                List<DetailPermintaan> listDetail = entry.getValue();
                
                Permintaan p = new Permintaan();
                p.setIdPenggunaPeminta(idUserPeminta);
                p.setIdGudangSumber(idSumber);
                p.setCatatanPermintaan(txtKeterangan.getText().trim());
                
                permintaanService.buatPermintaanBaru(p, listDetail);
                
                Optional<Gudang> g = gudangRepo.findById(idSumber);
                String namaG = g.isPresent() ? g.get().getNamaGudang() : "ID " + idSumber;
                logSukses.append("- Ke ").append(namaG).append(" (").append(listDetail.size()).append(" barang)\n");
                
                suksesCount++;
            }
            
            if (totalGudang > 1) {
                JOptionPane.showMessageDialog(this, 
                    "Berhasil membuat " + suksesCount + " Permintaan Baru:\n" + logSukses.toString(),
                    "Sukses", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Permintaan berhasil dikirim!", "Sukses", JOptionPane.INFORMATION_MESSAGE);
            }
            
            dispose();
            
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Gagal memproses: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
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
        java.awt.GridBagConstraints gridBagConstraints;

        jPanel2 = new javax.swing.JPanel();
        pnlWadahTabel = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblBarang = new javax.swing.JTable();
        pnlLogStok = new javax.swing.JPanel();
        lblKeterangan = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        txtKeterangan = new javax.swing.JTextArea();
        btnSimpan = new javax.swing.JButton();
        btnBatal = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        jPanel2.setLayout(new java.awt.GridLayout());

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

        pnlWadahTabel.add(jScrollPane1, java.awt.BorderLayout.PAGE_START);

        jPanel2.add(pnlWadahTabel);

        lblKeterangan.setFont(new java.awt.Font("Arial", 1, 12)); // NOI18N
        lblKeterangan.setText("Keterangan");

        txtKeterangan.setColumns(20);
        txtKeterangan.setRows(5);
        jScrollPane2.setViewportView(txtKeterangan);

        btnSimpan.setText("Simpan");
        btnSimpan.addActionListener(this::btnSimpanActionPerformed);

        btnBatal.setText("Batal");
        btnBatal.addActionListener(this::btnBatalActionPerformed);

        javax.swing.GroupLayout pnlLogStokLayout = new javax.swing.GroupLayout(pnlLogStok);
        pnlLogStok.setLayout(pnlLogStokLayout);
        pnlLogStokLayout.setHorizontalGroup(
            pnlLogStokLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlLogStokLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblKeterangan, javax.swing.GroupLayout.PREFERRED_SIZE, 114, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlLogStokLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 324, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(pnlLogStokLayout.createSequentialGroup()
                        .addComponent(btnSimpan)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(btnBatal)))
                .addGap(83, 83, 83))
        );
        pnlLogStokLayout.setVerticalGroup(
            pnlLogStokLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlLogStokLayout.createSequentialGroup()
                .addGap(21, 21, 21)
                .addGroup(pnlLogStokLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblKeterangan, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 255, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(pnlLogStokLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btnSimpan)
                    .addComponent(btnBatal))
                .addGap(433, 433, 433))
        );

        jPanel2.add(pnlLogStok);

        getContentPane().add(jPanel2, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnSimpanActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSimpanActionPerformed
        prosesSimpan();
    }//GEN-LAST:event_btnSimpanActionPerformed

    private void btnBatalActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBatalActionPerformed
        dispose();
    }//GEN-LAST:event_btnBatalActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnBatal;
    private javax.swing.JButton btnSimpan;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JLabel lblKeterangan;
    private javax.swing.JPanel pnlLogStok;
    private javax.swing.JPanel pnlWadahTabel;
    private javax.swing.JTable tblBarang;
    private javax.swing.JTextArea txtKeterangan;
    // End of variables declaration//GEN-END:variables
}
