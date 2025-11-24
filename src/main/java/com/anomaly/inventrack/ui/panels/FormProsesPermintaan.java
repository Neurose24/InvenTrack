/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JDialog.java to edit this template
 */
package com.anomaly.inventrack.ui.panels;

import com.anomaly.inventrack.models.*;
import com.anomaly.inventrack.repositories.*;
import com.anomaly.inventrack.services.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author user
 */
public class FormProsesPermintaan extends javax.swing.JDialog {
    
    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(FormProsesPermintaan.class.getName());
    
    private final int idPermintaan;
    private final int idUserGudangPengirim;
    private Permintaan permintaanSaatIni;
    
    private final PermintaanRepositories permintaanRepo = new PermintaanRepositories();
    private final DetailPermintaanRepositories detailRepo = new DetailPermintaanRepositories();
    private final BarangRepositories barangRepo = new BarangRepositories();
    private final StokRepositories stokRepo = new StokRepositories();
    private final SupirRepositories supirRepo = new SupirRepositories();
    private final PengirimanService pengirimanService = new PengirimanService();
    private final PermintaanService permintaanService = new PermintaanService(); 
    private final InventoryService inventoryService = new InventoryService(); 
    private final PengirimanRepositories pengirimanRepo = new PengirimanRepositories();
    private final DetailPengirimanRepositories detailPengirimanRepo = new DetailPengirimanRepositories();

    private DefaultTableModel tableModel;

    /**
     * Creates new form FormProsesPermintaan
     */
    public FormProsesPermintaan(java.awt.Frame parent, boolean modal, int idPermintaan, int idUserGudangPengirim) {
        super(parent, modal);
        initComponents();
        
        this.idPermintaan = idPermintaan;
        this.idUserGudangPengirim = idUserGudangPengirim;
        
        setupUI();
        loadData();
    }
    
    private void setupUI() {
        setTitle("Proses Permintaan Barang");
        setLocationRelativeTo(null);
        
        lblIdPermintaan.setText(String.format("%011d", idPermintaan)); 
        
        String[] header = {"ID Barang", "Nama Barang", "Kategori", "Jml Diminta", "Jml Disetujui"};
        
        tableModel = new DefaultTableModel(header, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 4;
            }
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                return (columnIndex >= 3) ? Integer.class : String.class;
            }
        };
        tblPermintaan.setModel(tableModel);
        
        tblPermintaan.getColumnModel().removeColumn(tblPermintaan.getColumnModel().getColumn(0));
        
        cmbSupir.removeAllItems();
        cmbSupir.addItem(new ComboItem("- Pilih Supir -", -1));
        
        List<Supir> listSupir = supirRepo.findAll();
        for (Supir s : listSupir) {
            cmbSupir.addItem(new ComboItem(s.getNamaSupir(), s.getIdSupir()));
        }
        
        btnSimpan.setText("Kirim Barang");
        btnCetakSuratJalan.setEnabled(false); // Baru nyala setelah disimpan
    }
    
    private void loadData() {
        Optional<Permintaan> optP = permintaanRepo.findById(idPermintaan);
        if (optP.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Permintaan tidak ditemukan!", "Error", JOptionPane.ERROR_MESSAGE);
            dispose();
            return;
        }
        permintaanSaatIni = optP.get();
        
        java.util.Map<Integer, Barang> mapBarang = new java.util.HashMap<>();
        for (Barang b : barangRepo.findAll()) {
            mapBarang.put(b.getIdBarang(), b);
        }
        
        List<DetailPermintaan> listDetail = detailRepo.findByPermintaan(idPermintaan);
        
        tableModel.setRowCount(0);
        for (DetailPermintaan dp : listDetail) {
            Barang b = mapBarang.get(dp.getIdBarang());
            
            String nama = (b != null) ? b.getNamaBarang() : "Unknown";
            String kat = (b != null) ? b.getKategori() : "-";
            
            tableModel.addRow(new Object[]{
                dp.getIdBarang(),
                nama,
                kat,
                dp.getJumlahDiminta(),
                dp.getJumlahDiminta()
            });
        }
    }
    
    private void prosesSimpan() {
        if (tblPermintaan.getCellEditor() != null) tblPermintaan.getCellEditor().stopCellEditing();
        
        ComboItem selectedSupir = (ComboItem) cmbSupir.getSelectedItem();
        if (selectedSupir == null || selectedSupir.getValue() == -1) {
            JOptionPane.showMessageDialog(this, "Harap pilih supir pengirim!", "Validasi", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        try {
            Pengiriman pengiriman = new Pengiriman();
            pengiriman.setIdPermintaan(idPermintaan);
            pengiriman.setIdPenggunaPengirim(idUserGudangPengirim);
            pengiriman.setIdPenggunaPenerima(permintaanSaatIni.getIdPenggunaPeminta());
            pengiriman.setIdSupir(selectedSupir.getValue());
            pengiriman.setNoKendaraan("-");
            pengiriman.setStatusPengiriman(Pengiriman.StatusPengiriman.DIKIRIM);
            pengiriman.setKeteranganPengiriman(txtKeterangan.getText());
            pengiriman.setTanggalPengiriman(java.time.LocalDateTime.now());
            
            List<FormTerimaPengiriman> listDetailKirim = new ArrayList<>();
            
            java.sql.Connection conn = com.anomaly.inventrack.utils.Database.getConnection();
            conn.setAutoCommit(false);
            
            try {
                pengirimanRepo.insert(conn, pengiriman);
                int idPengirimanBaru = pengiriman.getIdPengiriman();
                
                for (int i = 0; i < tableModel.getRowCount(); i++) {
                    int idBarang = (int) tableModel.getValueAt(i, 0);
                    int jmlDiminta = (int) tableModel.getValueAt(i, 3);
                    int jmlDisetujui = (int) tableModel.getValueAt(i, 4);
                    
                    if (jmlDisetujui > jmlDiminta) {
                        throw new Exception("Jumlah disetujui tidak boleh melebihi permintaan!");
                    }
                    
                    if (jmlDisetujui > 0) {
                        int idGudangSaya = permintaanSaatIni.getIdGudangSumber();
                        
                        Optional<Stok> stokSaya = stokRepo.findByBarangAndGudang(idBarang, idGudangSaya);
                        if (stokSaya.isEmpty() || stokSaya.get().getJumlahStok() < jmlDisetujui) {
                            throw new Exception("Stok tidak cukup untuk barang ID: " + idBarang);
                        }
                        
                        int sisaStok = stokSaya.get().getJumlahStok() - jmlDisetujui;
                        stokRepo.updateJumlahStok(conn, stokSaya.get().getIdStok(), sisaStok);
                        
                        LogStok log = new LogStok(
                            null, 
                            idGudangSaya, 
                            idBarang, 
                            LogStok.TipeTransaksi.KELUAR, 
                            jmlDisetujui, 
                            java.time.LocalDateTime.now(), 
                            "Pengiriman Keluar ID: " + idPengirimanBaru
                        );
                    }
                    
                    DetailPengiriman dp = new DetailPengiriman(); 
                    dp.setIdPengiriman(idPengirimanBaru);
                    dp.setIdBarang(idBarang);
                    dp.setJumlahDikirim(jmlDisetujui);
                    dp.setJumlahDiterima(0);

                    dp.setStatusPenerimaan(DetailPengiriman.StatusPenerimaan.BELUM_DITERIMA);

                    detailPengirimanRepo.insert(conn, dp);
                }
                
                permintaanRepo.updateStatus(conn, idPermintaan, Permintaan.StatusPermintaan.DISETUJUI);
                
                conn.commit();
                
                JOptionPane.showMessageDialog(this, "Pengiriman berhasil dibuat! ID Pengiriman: " + idPengirimanBaru);
                btnCetakSuratJalan.setEnabled(true);
                btnSimpan.setEnabled(false);
                
            } catch (Exception e) {
                conn.rollback();
                throw e;
            } finally {
                conn.close();
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Gagal: " + e.getMessage());
        }
    }
    
    private void prosesTolak() {
        int confirm = JOptionPane.showConfirmDialog(this, "Yakin menolak permintaan ini?", "Konfirmasi", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                java.sql.Connection conn = com.anomaly.inventrack.utils.Database.getConnection();
                permintaanRepo.updateStatus(conn, idPermintaan, Permintaan.StatusPermintaan.DITOLAK);
                conn.close();
                JOptionPane.showMessageDialog(this, "Permintaan Ditolak.");
                dispose();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Gagal menolak: " + e.getMessage());
            }
        }
    }
    
    class ComboItem {
        private String key;
        private int value;
        public ComboItem(String key, int value) { this.key = key; this.value = value; }
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

        jPanel2 = new javax.swing.JPanel();
        pnlWadahTabel = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblPermintaan = new javax.swing.JTable();
        pnlLogStok = new javax.swing.JPanel();
        cmbSupir = new javax.swing.JComboBox<>();
        lblPilihSupir = new javax.swing.JLabel();
        lblKeterangan = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        txtKeterangan = new javax.swing.JTextArea();
        btnSimpan = new javax.swing.JButton();
        btnTolakPermintaan = new javax.swing.JButton();
        btnBatal = new javax.swing.JButton();
        lblJudulIdPermintaan = new javax.swing.JLabel();
        lblIdPermintaan = new javax.swing.JLabel();
        btnCetakSuratJalan = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        jPanel2.setLayout(new java.awt.GridLayout(1, 0));

        pnlWadahTabel.setBorder(javax.swing.BorderFactory.createEmptyBorder(20, 40, 20, 40));
        pnlWadahTabel.setLayout(new java.awt.BorderLayout());

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
        tblPermintaan.setShowGrid(false);
        jScrollPane1.setViewportView(tblPermintaan);

        pnlWadahTabel.add(jScrollPane1, java.awt.BorderLayout.PAGE_START);

        jPanel2.add(pnlWadahTabel);

        cmbSupir.setFont(new java.awt.Font("Arial", 1, 12)); // NOI18N
        cmbSupir.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        lblPilihSupir.setFont(new java.awt.Font("Arial", 1, 12)); // NOI18N
        lblPilihSupir.setText("Pilih Supir");

        lblKeterangan.setFont(new java.awt.Font("Arial", 1, 12)); // NOI18N
        lblKeterangan.setText("Keterangan");

        txtKeterangan.setColumns(20);
        txtKeterangan.setRows(5);
        jScrollPane2.setViewportView(txtKeterangan);

        btnSimpan.setText("Simpan");
        btnSimpan.addActionListener(this::btnSimpanActionPerformed);

        btnTolakPermintaan.setText("Tolak Permintaan");
        btnTolakPermintaan.addActionListener(this::btnTolakPermintaanActionPerformed);

        btnBatal.setText("Batal");
        btnBatal.addActionListener(this::btnBatalActionPerformed);

        lblJudulIdPermintaan.setFont(new java.awt.Font("Arial", 1, 12)); // NOI18N
        lblJudulIdPermintaan.setText("Id Permintaan:");

        lblIdPermintaan.setFont(new java.awt.Font("Arial", 1, 12)); // NOI18N

        btnCetakSuratJalan.setText("Cetak Surat Jalan");

        javax.swing.GroupLayout pnlLogStokLayout = new javax.swing.GroupLayout(pnlLogStok);
        pnlLogStok.setLayout(pnlLogStokLayout);
        pnlLogStokLayout.setHorizontalGroup(
            pnlLogStokLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlLogStokLayout.createSequentialGroup()
                .addGroup(pnlLogStokLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlLogStokLayout.createSequentialGroup()
                        .addComponent(lblKeterangan, javax.swing.GroupLayout.PREFERRED_SIZE, 114, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(6, 6, 6)
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 324, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(pnlLogStokLayout.createSequentialGroup()
                        .addGroup(pnlLogStokLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblPilihSupir, javax.swing.GroupLayout.PREFERRED_SIZE, 87, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(lblJudulIdPermintaan, javax.swing.GroupLayout.PREFERRED_SIZE, 87, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(33, 33, 33)
                        .addGroup(pnlLogStokLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblIdPermintaan, javax.swing.GroupLayout.PREFERRED_SIZE, 87, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(cmbSupir, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(pnlLogStokLayout.createSequentialGroup()
                        .addGap(120, 120, 120)
                        .addGroup(pnlLogStokLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(btnCetakSuratJalan, javax.swing.GroupLayout.PREFERRED_SIZE, 324, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(pnlLogStokLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addComponent(btnTolakPermintaan, javax.swing.GroupLayout.PREFERRED_SIZE, 324, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGroup(pnlLogStokLayout.createSequentialGroup()
                                    .addComponent(btnSimpan, javax.swing.GroupLayout.PREFERRED_SIZE, 115, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(btnBatal, javax.swing.GroupLayout.PREFERRED_SIZE, 115, javax.swing.GroupLayout.PREFERRED_SIZE))))))
                .addContainerGap())
        );
        pnlLogStokLayout.setVerticalGroup(
            pnlLogStokLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlLogStokLayout.createSequentialGroup()
                .addGroup(pnlLogStokLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlLogStokLayout.createSequentialGroup()
                        .addGap(22, 22, 22)
                        .addComponent(lblJudulIdPermintaan, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlLogStokLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(lblIdPermintaan, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(18, 18, 18)
                .addGroup(pnlLogStokLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblPilihSupir, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(pnlLogStokLayout.createSequentialGroup()
                        .addGap(5, 5, 5)
                        .addComponent(cmbSupir, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(6, 6, 6)
                .addGroup(pnlLogStokLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblKeterangan, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 187, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(pnlLogStokLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnBatal)
                    .addComponent(btnSimpan))
                .addGap(18, 18, 18)
                .addComponent(btnTolakPermintaan)
                .addGap(18, 18, 18)
                .addComponent(btnCetakSuratJalan)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
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

    private void btnTolakPermintaanActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnTolakPermintaanActionPerformed
        prosesTolak();
    }//GEN-LAST:event_btnTolakPermintaanActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnBatal;
    private javax.swing.JButton btnCetakSuratJalan;
    private javax.swing.JButton btnSimpan;
    private javax.swing.JButton btnTolakPermintaan;
    private javax.swing.JComboBox<Object> cmbSupir;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JLabel lblIdPermintaan;
    private javax.swing.JLabel lblJudulIdPermintaan;
    private javax.swing.JLabel lblKeterangan;
    private javax.swing.JLabel lblPilihSupir;
    private javax.swing.JPanel pnlLogStok;
    private javax.swing.JPanel pnlWadahTabel;
    private javax.swing.JTable tblPermintaan;
    private javax.swing.JTextArea txtKeterangan;
    // End of variables declaration//GEN-END:variables
}
