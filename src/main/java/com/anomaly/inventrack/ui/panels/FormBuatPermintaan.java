/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JDialog.java to edit this template
 */
package com.anomaly.inventrack.ui.panels;

import com.anomaly.inventrack.models.*;
import com.anomaly.inventrack.repositories.*;
import com.anomaly.inventrack.services.PermintaanService;
import java.time.format.DateTimeFormatter;
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
    
    private final boolean isViewOnly;
    private Map<Integer, Integer> selectedItems;
    private int idUserPeminta;
    private int idPermintaanView;

    private final StokRepositories stokRepo = new StokRepositories();
    private final BarangRepositories barangRepo = new BarangRepositories();
    private final GudangRepositories gudangRepo = new GudangRepositories();
    private final PermintaanService permintaanService = new PermintaanService();
    
    private final PermintaanRepositories permintaanRepo = new PermintaanRepositories();
    private final DetailPermintaanRepositories detailRepo = new DetailPermintaanRepositories();
    
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
        
        this.isViewOnly = false;
        this.selectedItems = selectedItems;
        this.idUserPeminta = idUserPeminta;
        
        setupUI();
        loadDataCreate();
    }
    
    public FormBuatPermintaan(java.awt.Frame parent, boolean modal, int idPermintaan) {
        super(parent, modal);
        initComponents();
        
        this.isViewOnly = true;
        this.idPermintaanView = idPermintaan;
        
        setupUI();
        loadDataView();
    }
    
    private void setupUI() {
        setTitle(isViewOnly ? "Detail Permintaan (Read Only)" : "Konfirmasi Permintaan Barang");
        setLocationRelativeTo(null);
        
        String colStok = isViewOnly ? "Jml Disetujui" : "Stok Tersedia";
        String[] header = {"ID Barang", "ID Gudang", "Nama Barang", "Dari Gudang", colStok, "Jml Diminta"};
        
        tableModel = new DefaultTableModel(header, 0) {
            @Override 
            public boolean isCellEditable(int row, int column) { 
                if (isViewOnly) return false;
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
        
        tblBarang.getColumnModel().getColumn(0).setPreferredWidth(200); // Nama
        tblBarang.getColumnModel().getColumn(1).setPreferredWidth(150); // Gudang
        tblBarang.setRowHeight(25);
        
        if (isViewOnly) {
            btnSimpan.setVisible(false);
            btnBatal.setText("Tutup");
            txtKeterangan.setEditable(false);
        } else {
            btnSimpan.setVisible(true);
            btnSimpan.setText("Kirim Permintaan");
            btnBatal.setText("Batal");
        }
    }
    
    private void loadDataCreate() {
        tableModel.setRowCount(0);
        for (Map.Entry<Integer, Integer> entry : selectedItems.entrySet()) {
            int idBarang = entry.getKey();
            int idGudangSumber = entry.getValue();
            
            Optional<Barang> b = barangRepo.findById(idBarang);
            Optional<Stok> s = stokRepo.findByBarangAndGudang(idBarang, idGudangSumber);
            Optional<Gudang> g = gudangRepo.findById(idGudangSumber);
            
            if (b.isPresent() && s.isPresent() && g.isPresent()) {
                tableModel.addRow(new Object[]{
                    idBarang, idGudangSumber, b.get().getNamaBarang(), g.get().getNamaGudang(),
                    s.get().getJumlahStok(), 0
                });
            }
        }
    }
    
    private void loadDataView() {
        tableModel.setRowCount(0);
        
        Optional<Permintaan> optP = permintaanRepo.findById(idPermintaanView);
        if (optP.isPresent()) {
            Permintaan p = optP.get();
            txtKeterangan.setText(p.getCatatanPermintaan());
            
            String info = "ID: " + String.format("%011d", p.getIdPermintaan()) + 
                          " | Status: " + p.getStatusPermintaan() + 
                          " | Tgl: " + p.getTanggalPermintaan().format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));
            lblKeterangan.setText("Info Permintaan: " + info);
            
            List<DetailPermintaan> listDetail = detailRepo.findByPermintaan(idPermintaanView);
            
            Optional<Gudang> gSumber = gudangRepo.findById(p.getIdGudangSumber());
            String namaGudang = gSumber.map(Gudang::getNamaGudang).orElse("-");
            
            for (DetailPermintaan dp : listDetail) {
                String namaBarang = barangRepo.findById(dp.getIdBarang())
                        .map(Barang::getNamaBarang).orElse("Unknown");
                
                tableModel.addRow(new Object[]{
                    dp.getIdBarang(),
                    p.getIdGudangSumber(),
                    namaBarang,
                    namaGudang,
                    dp.getJumlahDisetujui(),
                    dp.getJumlahDiminta()
                });
            }
        }
    }
    
    private void prosesSimpan() {
        if (isViewOnly) return;

        if (tblBarang.getCellEditor() != null) {
            tblBarang.getCellEditor().stopCellEditing();
        }

        try {
            Map<Integer, List<DetailPermintaan>> mapPermintaanPerGudang = new HashMap<>();
            
            for (int i = 0; i < tableModel.getRowCount(); i++) {
                int idBarang = (int) tableModel.getValueAt(i, 0);
                int idGudangSumber = (int) tableModel.getValueAt(i, 1);
                String namaBarang = (String) tableModel.getValueAt(i, 2);
                int stokTersedia = (int) tableModel.getValueAt(i, 4);
                
                Object objJumlah = tableModel.getValueAt(i, 5);
                int jmlDiminta = (objJumlah != null) ? Integer.parseInt(objJumlah.toString()) : 0;
                
                if (jmlDiminta <= 0) {
                    continue;
                }
                
                if (jmlDiminta > stokTersedia) {
                    throw new Exception("Jumlah diminta (" + jmlDiminta + ") melebihi stok tersedia (" + stokTersedia + ") untuk barang: " + namaBarang);
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
                        "Sistem akan otomatis memecahnya menjadi " + totalGudang + " Permintaan Terpisah.\n\n" +
                        "Apakah Anda ingin melanjutkan?";
                judul = "Konfirmasi Multi-Gudang";
            } else {
                int idSatuGudang = mapPermintaanPerGudang.keySet().iterator().next();
                String namaGudang = "Gudang Tujuan";
                Optional<Gudang> g = gudangRepo.findById(idSatuGudang);
                if(g.isPresent()) namaGudang = g.get().getNamaGudang();
                
                pesan = "Kirim permintaan barang ke " + namaGudang + "?";
                judul = "Konfirmasi Permintaan";
            }
            
            int confirm = JOptionPane.showConfirmDialog(this, pesan, judul, JOptionPane.YES_NO_OPTION);
            if (confirm != JOptionPane.YES_OPTION) return;

            int suksesCount = 0;
            StringBuilder logSukses = new StringBuilder();
            String catatanUser = txtKeterangan.getText().trim();
            
            for (Map.Entry<Integer, List<DetailPermintaan>> entry : mapPermintaanPerGudang.entrySet()) {
                int idSumber = entry.getKey();
                List<DetailPermintaan> listDetail = entry.getValue();
                
                Permintaan p = new Permintaan();
                p.setIdPenggunaPeminta(idUserPeminta);
                p.setIdGudangSumber(idSumber);
                p.setCatatanPermintaan(catatanUser);
                p.setTanggalPermintaan(java.time.LocalDateTime.now());
                p.setStatusPermintaan(Permintaan.StatusPermintaan.MENUNGGU);
                
                permintaanService.buatPermintaanBaru(p, listDetail);
                
                Optional<Gudang> g = gudangRepo.findById(idSumber);
                String namaG = g.isPresent() ? g.get().getNamaGudang() : "ID " + idSumber;
                logSukses.append("- Ke ").append(namaG).append(" (").append(listDetail.size()).append(" barang)\n");
                
                suksesCount++;
            }
            
            String pesanSukses;
            if (totalGudang > 1) {
                pesanSukses = "Berhasil membuat " + suksesCount + " Permintaan Baru:\n" + logSukses.toString();
            } else {
                pesanSukses = "Permintaan berhasil dikirim!";
            }
            
            JOptionPane.showMessageDialog(this, pesanSukses, "Sukses", JOptionPane.INFORMATION_MESSAGE);
            
            dispose();
            
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Jumlah harus berupa angka!", "Error Input", JOptionPane.ERROR_MESSAGE);
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
