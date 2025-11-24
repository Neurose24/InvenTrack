/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JDialog.java to edit this template
 */
package com.anomaly.inventrack.ui.panels;

import com.anomaly.inventrack.models.*;
import com.anomaly.inventrack.repositories.*;
import com.anomaly.inventrack.services.InventoryService;
import com.anomaly.inventrack.services.PengirimanService;
import java.awt.Color;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author user
 */
public class FormTerimaPengiriman extends javax.swing.JDialog {
    
    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(FormTerimaPengiriman.class.getName());
    
    private final int idPengiriman;
    private final int idUserPenerima;
    private final boolean isViewOnly;
    
    private final PengirimanRepositories pengirimanRepo = new PengirimanRepositories();
    private final DetailPengirimanRepositories detailPengirimanRepo = new DetailPengirimanRepositories();
    private final BarangRepositories barangRepo = new BarangRepositories();
    private final GudangRepositories gudangRepo = new GudangRepositories();
    private final SupirRepositories supirRepo = new SupirRepositories();
    private final PenggunaRepositories penggunaRepo = new PenggunaRepositories();
    private final PengirimanService pengirimanService = new PengirimanService();
    
    private DefaultTableModel tableModel;
    private List<DetailPengiriman> listDetailAsli;

    /**
     * Creates new form FormTerimaPengiriman
     */
    public FormTerimaPengiriman(java.awt.Frame parent, boolean modal, int idPengiriman, int idUserPenerima, boolean isViewOnly) {
        super(parent, modal);
        initComponents();
        
        this.idPengiriman = idPengiriman;
        this.idUserPenerima = idUserPenerima;
        this.isViewOnly = isViewOnly;
        
        setupUI();
        loadData();
    }
    
    private void setupUI() {
        String judul = isViewOnly ? "Detail Pengiriman (Read Only)" : "Penerimaan Barang Masuk";
        setTitle(judul + " - ID: " + idPengiriman);
        setLocationRelativeTo(null);
        
        lblIdPermintaan.setText("-");
        lblGudangAsal.setText("-");
        lblGudangTujuan.setText("-");
        lblNamaSupir.setText("-");
        lblTanggalPengiriman.setText("-");
        lblStatusPengiriman.setText("-");
        
        String[] header = {"ID Detail", "Nama Barang", "Jml Dikirim", "Jml Diterima", "Catatan", "Status Barang"};
        
        tableModel = new DefaultTableModel(header, 0) {
            @Override 
            public boolean isCellEditable(int row, int col) {
                if (isViewOnly) return false;
                return col == 3 || col == 4; 
            }
            @Override 
            public Class<?> getColumnClass(int columnIndex) {
                return (columnIndex == 2 || columnIndex == 3) ? Integer.class : String.class;
            }
        };
        tblBarang.setModel(tableModel);
        tblBarang.getColumnModel().removeColumn(tblBarang.getColumnModel().getColumn(0));
        tblBarang.getColumnModel().getColumn(0).setPreferredWidth(150);
        tblBarang.setRowHeight(25);
        
        if (isViewOnly) {
            btnSimpan.setVisible(false);
            btnBatal.setText("Tutup");
        } else {
            btnSimpan.setVisible(true);
            btnBatal.setText("Batal");
        }
    }
    
    private void loadData() {
        Optional<Pengiriman> optP = pengirimanRepo.findById(idPengiriman);
        if (optP.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Data pengiriman tidak ditemukan!");
            dispose();
            return;
        }
        Pengiriman p = optP.get();
        
        lblIdPermintaan.setText(String.format("%011d", p.getIdPermintaan()));
        lblTanggalPengiriman.setText(p.getTanggalPengiriman().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm")));
        lblStatusPengiriman.setText(p.getStatusPengiriman().name());
        if (isViewOnly) {
            txtKeterangan.setEditable(false);
        }
        
        penggunaRepo.findById(p.getIdPenggunaPengirim()).ifPresent(user -> 
            gudangRepo.findById(user.getIdGudang()).ifPresent(g -> lblGudangAsal.setText(g.getNamaGudang())));
        
        penggunaRepo.findById(p.getIdPenggunaPenerima()).ifPresent(user -> 
            gudangRepo.findById(user.getIdGudang()).ifPresent(g -> lblGudangTujuan.setText(g.getNamaGudang())));
        
        if (p.getIdSupir() != null) {
            supirRepo.findById(p.getIdSupir()).ifPresent(s -> lblNamaSupir.setText(s.getNamaSupir()));
        }

        java.util.Map<Integer, String> mapNamaBarang = new java.util.HashMap<>();
        for (Barang b : barangRepo.findAll()) mapNamaBarang.put(b.getIdBarang(), b.getNamaBarang());

        listDetailAsli = detailPengirimanRepo.findByPengiriman(idPengiriman);
        tableModel.setRowCount(0);
        
        for (DetailPengiriman dp : listDetailAsli) {
            String namaBarang = mapNamaBarang.getOrDefault(dp.getIdBarang(), "Unknown");
            int displayDiterima = isViewOnly ? dp.getJumlahDiterima() : dp.getJumlahDikirim();
            
            tableModel.addRow(new Object[]{
                dp.getIdDetailPengiriman(),
                namaBarang,
                dp.getJumlahDikirim(),
                displayDiterima,
                dp.getCatatanPenerimaan(),
                dp.getStatusPenerimaan().name()
            });
        }
    }
    
    private void prosesTerima() {
        if (isViewOnly) return;

        if (tblBarang.getCellEditor() != null) {
            tblBarang.getCellEditor().stopCellEditing();
        }
        
        int confirm = JOptionPane.showConfirmDialog(this, 
                "Pastikan perhitungan fisik barang sudah benar.\n" +
                "Stok akan otomatis ditambahkan ke gudang Anda.\n\n" +
                "Lanjutkan proses penerimaan?", 
                "Konfirmasi Penerimaan", JOptionPane.YES_NO_OPTION);
        
        if (confirm != JOptionPane.YES_OPTION) return;

        try {
            List<DetailPengiriman> listUpdate = new ArrayList<>();

            for (int i = 0; i < tableModel.getRowCount(); i++) {
                int idDetail = (int) tableModel.getValueAt(i, 0);
                int jmlDikirim = (int) tableModel.getValueAt(i, 2);
                
                Object objDiterima = tableModel.getValueAt(i, 3);
                if (objDiterima == null) throw new Exception("Jumlah diterima tidak boleh kosong pada baris ke-" + (i+1));
                int jmlDiterima = Integer.parseInt(objDiterima.toString());
                
                String catatan = (String) tableModel.getValueAt(i, 4);
                if (catatan == null) catatan = "";

                if (jmlDiterima < 0) {
                    throw new Exception("Jumlah diterima tidak boleh negatif pada baris ke-" + (i+1));
                }
                
                if (jmlDiterima > jmlDikirim) {
                    throw new Exception("Jumlah diterima (" + jmlDiterima + ") melebihi jumlah yang dikirim (" + jmlDikirim + ") pada baris ke-" + (i+1));
                }

                boolean found = false;
                for (DetailPengiriman dpAsli : listDetailAsli) {
                    if (dpAsli.getIdDetailPengiriman() == idDetail) {
                        dpAsli.setJumlahDiterima(jmlDiterima);
                        dpAsli.setCatatanPenerimaan(catatan);
                        
                        if (jmlDiterima == jmlDikirim) {
                            dpAsli.setStatusPenerimaan(DetailPengiriman.StatusPenerimaan.DITERIMA);
                        } else {
                            dpAsli.setStatusPenerimaan(DetailPengiriman.StatusPenerimaan.RUSAK);
                            if (catatan.trim().isEmpty()) {
                                throw new Exception("Harap isi 'Catatan' untuk barang yang jumlahnya kurang/rusak pada baris ke-" + (i+1));
                            }
                        }
                        
                        listUpdate.add(dpAsli);
                        found = true;
                        break;
                    }
                }
                
                if (!found) {
                    throw new Exception("Data detail tidak sinkron (ID Detail tidak ditemukan). Silakan tutup dan buka ulang.");
                }
            }
            
            pengirimanService.receivePengiriman(idPengiriman, listUpdate);
            
            JOptionPane.showMessageDialog(this, "Penerimaan Selesai!\nStok gudang telah diperbarui.", "Sukses", JOptionPane.INFORMATION_MESSAGE);
            dispose();
            
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Input jumlah harus berupa angka!", "Error Input", JOptionPane.ERROR_MESSAGE);
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

        jPanel2 = new javax.swing.JPanel();
        pnlWadahTabel = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblBarang = new javax.swing.JTable();
        pnlLogStok = new javax.swing.JPanel();
        lblPilihSupir = new javax.swing.JLabel();
        lblKeterangan = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        txtKeterangan = new javax.swing.JTextArea();
        btnSimpan = new javax.swing.JButton();
        btnBatal = new javax.swing.JButton();
        lblJudulIdPermintaan = new javax.swing.JLabel();
        lblJudulGudangAsal = new javax.swing.JLabel();
        lblJudulGudangTujuan = new javax.swing.JLabel();
        lblJudulTanggalPengiriman = new javax.swing.JLabel();
        lblJudulStatusPengiriman = new javax.swing.JLabel();
        lblIdPermintaan = new javax.swing.JLabel();
        lblNamaSupir = new javax.swing.JLabel();
        lblGudangAsal = new javax.swing.JLabel();
        lblGudangTujuan = new javax.swing.JLabel();
        lblTanggalPengiriman = new javax.swing.JLabel();
        lblStatusPengiriman = new javax.swing.JLabel();

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

        lblPilihSupir.setFont(new java.awt.Font("Arial", 1, 12)); // NOI18N
        lblPilihSupir.setText("Nama Supir:");

        lblKeterangan.setFont(new java.awt.Font("Arial", 1, 12)); // NOI18N
        lblKeterangan.setText("Keterangan");

        txtKeterangan.setColumns(20);
        txtKeterangan.setRows(5);
        jScrollPane2.setViewportView(txtKeterangan);

        btnSimpan.setText("Simpan");
        btnSimpan.addActionListener(this::btnSimpanActionPerformed);

        btnBatal.setText("Batal");
        btnBatal.addActionListener(this::btnBatalActionPerformed);

        lblJudulIdPermintaan.setFont(new java.awt.Font("Arial", 1, 12)); // NOI18N
        lblJudulIdPermintaan.setText("Id Permintaan:");

        lblJudulGudangAsal.setFont(new java.awt.Font("Arial", 1, 12)); // NOI18N
        lblJudulGudangAsal.setText("Gudang Asal:");

        lblJudulGudangTujuan.setFont(new java.awt.Font("Arial", 1, 12)); // NOI18N
        lblJudulGudangTujuan.setText("Gudang Tujuan:");

        lblJudulTanggalPengiriman.setFont(new java.awt.Font("Arial", 1, 12)); // NOI18N
        lblJudulTanggalPengiriman.setText("Tanggal Pengiriman:");

        lblJudulStatusPengiriman.setFont(new java.awt.Font("Arial", 1, 12)); // NOI18N
        lblJudulStatusPengiriman.setText("Status Pengiriman:");

        lblIdPermintaan.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N

        lblNamaSupir.setFont(new java.awt.Font("Arial", 1, 12)); // NOI18N

        lblGudangAsal.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N

        lblGudangTujuan.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N

        lblTanggalPengiriman.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N

        lblStatusPengiriman.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N

        javax.swing.GroupLayout pnlLogStokLayout = new javax.swing.GroupLayout(pnlLogStok);
        pnlLogStok.setLayout(pnlLogStokLayout);
        pnlLogStokLayout.setHorizontalGroup(
            pnlLogStokLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlLogStokLayout.createSequentialGroup()
                .addGroup(pnlLogStokLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlLogStokLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addGroup(pnlLogStokLayout.createSequentialGroup()
                            .addGap(120, 120, 120)
                            .addComponent(btnSimpan, javax.swing.GroupLayout.PREFERRED_SIZE, 115, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGap(94, 94, 94)
                            .addComponent(btnBatal, javax.swing.GroupLayout.PREFERRED_SIZE, 115, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGroup(pnlLogStokLayout.createSequentialGroup()
                            .addGroup(pnlLogStokLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addGroup(pnlLogStokLayout.createSequentialGroup()
                                    .addComponent(lblKeterangan, javax.swing.GroupLayout.PREFERRED_SIZE, 114, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGap(6, 6, 6))
                                .addGroup(pnlLogStokLayout.createSequentialGroup()
                                    .addGroup(pnlLogStokLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                        .addComponent(lblJudulIdPermintaan, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(lblPilihSupir, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                    .addGap(4, 4, 4)))
                            .addGroup(pnlLogStokLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 324, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(lblGudangAsal, javax.swing.GroupLayout.PREFERRED_SIZE, 116, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(lblNamaSupir, javax.swing.GroupLayout.PREFERRED_SIZE, 116, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(lblIdPermintaan, javax.swing.GroupLayout.PREFERRED_SIZE, 116, javax.swing.GroupLayout.PREFERRED_SIZE))))
                    .addGroup(pnlLogStokLayout.createSequentialGroup()
                        .addGroup(pnlLogStokLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(lblJudulGudangAsal, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(lblJudulGudangTujuan, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(lblJudulTanggalPengiriman, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(lblJudulStatusPengiriman, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(pnlLogStokLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblGudangTujuan, javax.swing.GroupLayout.PREFERRED_SIZE, 116, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(lblTanggalPengiriman, javax.swing.GroupLayout.PREFERRED_SIZE, 116, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(lblStatusPengiriman, javax.swing.GroupLayout.PREFERRED_SIZE, 116, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap())
        );
        pnlLogStokLayout.setVerticalGroup(
            pnlLogStokLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlLogStokLayout.createSequentialGroup()
                .addGap(22, 22, 22)
                .addGroup(pnlLogStokLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(lblJudulIdPermintaan, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblIdPermintaan, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlLogStokLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(pnlLogStokLayout.createSequentialGroup()
                        .addGroup(pnlLogStokLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(lblPilihSupir, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(lblNamaSupir, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(pnlLogStokLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlLogStokLayout.createSequentialGroup()
                                .addGroup(pnlLogStokLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlLogStokLayout.createSequentialGroup()
                                        .addGroup(pnlLogStokLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(lblJudulGudangAsal, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addComponent(lblGudangAsal, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(lblJudulGudangTujuan, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addComponent(lblGudangTujuan, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(lblJudulTanggalPengiriman, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(lblTanggalPengiriman, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lblJudulStatusPengiriman, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(lblStatusPengiriman, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlLogStokLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblKeterangan, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 187, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(pnlLogStokLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnBatal)
                    .addComponent(btnSimpan))
                .addContainerGap(214, Short.MAX_VALUE))
        );

        jPanel2.add(pnlLogStok);

        getContentPane().add(jPanel2, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnSimpanActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSimpanActionPerformed
        prosesTerima();
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
    private javax.swing.JLabel lblGudangAsal;
    private javax.swing.JLabel lblGudangTujuan;
    private javax.swing.JLabel lblIdPermintaan;
    private javax.swing.JLabel lblJudulGudangAsal;
    private javax.swing.JLabel lblJudulGudangTujuan;
    private javax.swing.JLabel lblJudulIdPermintaan;
    private javax.swing.JLabel lblJudulStatusPengiriman;
    private javax.swing.JLabel lblJudulTanggalPengiriman;
    private javax.swing.JLabel lblKeterangan;
    private javax.swing.JLabel lblNamaSupir;
    private javax.swing.JLabel lblPilihSupir;
    private javax.swing.JLabel lblStatusPengiriman;
    private javax.swing.JLabel lblTanggalPengiriman;
    private javax.swing.JPanel pnlLogStok;
    private javax.swing.JPanel pnlWadahTabel;
    private javax.swing.JTable tblBarang;
    private javax.swing.JTextArea txtKeterangan;
    // End of variables declaration//GEN-END:variables
}
