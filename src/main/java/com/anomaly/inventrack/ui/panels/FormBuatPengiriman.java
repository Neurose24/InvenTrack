/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JDialog.java to edit this template
 */
package com.anomaly.inventrack.ui.panels;

import com.anomaly.inventrack.models.*;
import com.anomaly.inventrack.repositories.*;
import com.anomaly.inventrack.services.InventoryService;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author user
 */
public class FormBuatPengiriman extends javax.swing.JDialog {
    
    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(FormBuatPengiriman.class.getName());
    
    private final List<Integer> selectedIdBarang;
    private final int idUserPengirim;
    private final int idGudangPengirim;
    
    private final StokRepositories stokRepo = new StokRepositories();
    private final BarangRepositories barangRepo = new BarangRepositories();
    private final GudangRepositories gudangRepo = new GudangRepositories();
    private final PenggunaRepositories penggunaRepo = new PenggunaRepositories();
    private final SupirRepositories supirRepo = new SupirRepositories();
    private final PengirimanRepositories pengirimanRepo = new PengirimanRepositories();
    private final DetailPengirimanRepositories detailRepo = new DetailPengirimanRepositories();
    private final InventoryService inventoryService = new InventoryService();
    
    private DefaultTableModel tableModel;

    /**
     * Creates new form FormBuatPengiriman
     */
    public FormBuatPengiriman(java.awt.Frame parent, boolean modal, List<Integer> selectedIdBarang, int idUserPengirim, int idGudangPengirim) {
        super(parent, modal);
        initComponents();
        
        this.selectedIdBarang = selectedIdBarang;
        this.idUserPengirim = idUserPengirim;
        this.idGudangPengirim = idGudangPengirim;
        
        setupUI();
        loadData();
    }
    
    private void setupUI() {
        setTitle("Buat Pengiriman Baru");
        setLocationRelativeTo(null);
        
        String[] header = {"ID Barang", "Nama Barang", "Stok Tersedia", "Jml Dikirim"};
        
        tableModel = new DefaultTableModel(header, 0) {
            @Override public boolean isCellEditable(int row, int col) { return col == 3; }
            @Override public Class<?> getColumnClass(int col) { return (col >= 2) ? Integer.class : String.class; }
        };
        tblBarang.setModel(tableModel);
        
        tblBarang.getColumnModel().removeColumn(tblBarang.getColumnModel().getColumn(0));
        tblBarang.getColumnModel().getColumn(0).setPreferredWidth(200);
        tblBarang.setRowHeight(25);
        
        cmbGudangTujuan.removeAllItems();
        cmbGudangTujuan.addItem(new ComboItem("- Pilih Tujuan -", -1));
        for (Gudang g : gudangRepo.findAll()) {
            if (g.getIdGudang() != idGudangPengirim) {
                cmbGudangTujuan.addItem(new ComboItem(g.getNamaGudang(), g.getIdGudang()));
            }
        }
        
        cmbSupir.removeAllItems();
        cmbSupir.addItem(new ComboItem("- Pilih Supir -", -1));
        for (Supir s : supirRepo.findAll()) {
            cmbSupir.addItem(new ComboItem(s.getNamaSupir(), s.getIdSupir()));
        }
    }
    
    private void loadData() {
        tableModel.setRowCount(0);
        for (Integer idBarang : selectedIdBarang) {
            Optional<Barang> b = barangRepo.findById(idBarang);
            Optional<Stok> s = stokRepo.findByBarangAndGudang(idBarang, idGudangPengirim);
            
            if (b.isPresent()) {
                int stok = s.map(Stok::getJumlahStok).orElse(0);
                
                tableModel.addRow(new Object[]{
                    idBarang, 
                    b.get().getNamaBarang(),
                    stok,
                    0
                });
            }
        }
    }
    
    private void prosesKirim() {
        if (tblBarang.getCellEditor() != null) tblBarang.getCellEditor().stopCellEditing();
        
        ComboItem itemTujuan = (ComboItem) cmbGudangTujuan.getSelectedItem();
        if (itemTujuan == null || itemTujuan.getValue() == -1) {
            JOptionPane.showMessageDialog(this, "Pilih Gudang Tujuan!", "Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        ComboItem itemSupir = (ComboItem) cmbSupir.getSelectedItem();
        if (itemSupir == null || itemSupir.getValue() == -1) {
            JOptionPane.showMessageDialog(this, "Pilih Supir!", "Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int idGudangTujuan = itemTujuan.getValue();
        Optional<Pengguna> optPenerima = penggunaRepo.findAll().stream()
                .filter(u -> u.getIdGudang() == idGudangTujuan)
                .findFirst();
                
        if (optPenerima.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Tidak ada user admin di gudang tujuan! Tidak bisa mengirim.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        int idUserPenerima = optPenerima.get().getIdPengguna();

        try {
            List<DetailPengiriman> listDetail = new ArrayList<>();
            
            for (int i = 0; i < tableModel.getRowCount(); i++) {
                int idBarang = (int) tableModel.getValueAt(i, 0);
                int stokTersedia = (int) tableModel.getValueAt(i, 2);
                int jmlKirim = (int) tableModel.getValueAt(i, 3);
                String namaBarang = (String) tableModel.getValueAt(i, 1);
                
                if (jmlKirim < 0) throw new Exception("Jumlah tidak boleh negatif: " + namaBarang);
                if (jmlKirim == 0) continue;
                if (jmlKirim > stokTersedia) throw new Exception("Stok tidak cukup untuk: " + namaBarang);
                
                DetailPengiriman dp = new DetailPengiriman();
                dp.setIdBarang(idBarang);
                dp.setJumlahDikirim(jmlKirim);
                dp.setJumlahDiterima(0);
                dp.setStatusPenerimaan(DetailPengiriman.StatusPenerimaan.BELUM_DITERIMA);
                dp.setCatatanPenerimaan("");
                listDetail.add(dp);
            }
            
            if (listDetail.isEmpty()) throw new Exception("Masukkan jumlah barang yang akan dikirim (minimal 1 item).");
            
            int confirm = JOptionPane.showConfirmDialog(this, "Kirim " + listDetail.size() + " jenis barang ke " + itemTujuan + "?", "Konfirmasi", JOptionPane.YES_NO_OPTION);
            if (confirm != JOptionPane.YES_OPTION) return;

            java.sql.Connection conn = com.anomaly.inventrack.utils.Database.getConnection();
            conn.setAutoCommit(false);
            
            try {
                Pengiriman p = new Pengiriman();
                p.setIdPenggunaPengirim(idUserPengirim);
                p.setIdPenggunaPenerima(idUserPenerima);
                p.setIdSupir(itemSupir.getValue());
                p.setTanggalPengiriman(java.time.LocalDateTime.now());
                p.setStatusPengiriman(Pengiriman.StatusPengiriman.DIKIRIM);
                p.setKeteranganPengiriman(txtKeterangan.getText());
                p.setNoKendaraan("-"); 
                
                pengirimanRepo.insert(conn, p);
                int idPengiriman = p.getIdPengiriman();
                
                for (DetailPengiriman dp : listDetail) {
                    dp.setIdPengiriman(idPengiriman);
                    detailRepo.insert(conn, dp);
                    Optional<Stok> stokAsli = stokRepo.findByBarangAndGudang(dp.getIdBarang(), idGudangPengirim);
                    if(stokAsli.isPresent()) {
                        int sisa = stokAsli.get().getJumlahStok() - dp.getJumlahDikirim();
                        stokRepo.updateJumlahStok(conn, stokAsli.get().getIdStok(), sisa);
                    }
                }
                
                conn.commit();
                
                JOptionPane.showMessageDialog(this, "Pengiriman Berhasil Dibuat! ID: " + String.format("%011d", idPengiriman));
                dispose();
                
            } catch (Exception ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.close();
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Gagal: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
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

        jPanel2 = new javax.swing.JPanel();
        pnlWadahTabel = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblBarang = new javax.swing.JTable();
        pnlLogStok = new javax.swing.JPanel();
        cmbSupir = new javax.swing.JComboBox<>();
        lblPilihSupir = new javax.swing.JLabel();
        lblKeterangan = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        txtKeterangan = new javax.swing.JTextArea();
        btnSimpan = new javax.swing.JButton();
        btnBatal = new javax.swing.JButton();
        lblPilihGudangTujuan = new javax.swing.JLabel();
        cmbGudangTujuan = new javax.swing.JComboBox<>();

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

        cmbSupir.setFont(new java.awt.Font("Arial", 1, 12)); // NOI18N
        cmbSupir.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        lblPilihSupir.setFont(new java.awt.Font("Arial", 1, 12)); // NOI18N
        lblPilihSupir.setText("Pilih Supir");

        lblKeterangan.setFont(new java.awt.Font("Arial", 1, 12)); // NOI18N
        lblKeterangan.setText("Keterangan");

        txtKeterangan.setColumns(20);
        txtKeterangan.setRows(5);
        jScrollPane2.setViewportView(txtKeterangan);

        btnSimpan.setText("Kirim Barang");
        btnSimpan.addActionListener(this::btnSimpanActionPerformed);

        btnBatal.setText("Batal");
        btnBatal.addActionListener(this::btnBatalActionPerformed);

        lblPilihGudangTujuan.setFont(new java.awt.Font("Arial", 1, 12)); // NOI18N
        lblPilihGudangTujuan.setText("Gudang Tujuan");

        cmbGudangTujuan.setFont(new java.awt.Font("Arial", 1, 12)); // NOI18N
        cmbGudangTujuan.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

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
                            .addComponent(lblPilihGudangTujuan, javax.swing.GroupLayout.PREFERRED_SIZE, 87, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(33, 33, 33)
                        .addGroup(pnlLogStokLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(cmbSupir, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(cmbGudangTujuan, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(pnlLogStokLayout.createSequentialGroup()
                        .addGap(120, 120, 120)
                        .addComponent(btnSimpan, javax.swing.GroupLayout.PREFERRED_SIZE, 115, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(94, 94, 94)
                        .addComponent(btnBatal, javax.swing.GroupLayout.PREFERRED_SIZE, 115, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        pnlLogStokLayout.setVerticalGroup(
            pnlLogStokLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlLogStokLayout.createSequentialGroup()
                .addGap(22, 22, 22)
                .addGroup(pnlLogStokLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblPilihGudangTujuan, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cmbGudangTujuan, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
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
                .addContainerGap(428, Short.MAX_VALUE))
        );

        jPanel2.add(pnlLogStok);

        getContentPane().add(jPanel2, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnSimpanActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSimpanActionPerformed
        prosesKirim();
    }//GEN-LAST:event_btnSimpanActionPerformed

    private void btnBatalActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBatalActionPerformed
        dispose();
    }//GEN-LAST:event_btnBatalActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnBatal;
    private javax.swing.JButton btnSimpan;
    private javax.swing.JComboBox<Object> cmbGudangTujuan;
    private javax.swing.JComboBox<Object> cmbSupir;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JLabel lblKeterangan;
    private javax.swing.JLabel lblPilihGudangTujuan;
    private javax.swing.JLabel lblPilihSupir;
    private javax.swing.JPanel pnlLogStok;
    private javax.swing.JPanel pnlWadahTabel;
    private javax.swing.JTable tblBarang;
    private javax.swing.JTextArea txtKeterangan;
    // End of variables declaration//GEN-END:variables
}
