/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JDialog.java to edit this template
 */
package com.anomaly.inventrack.ui.panels;

import com.anomaly.inventrack.models.Barang;
import com.anomaly.inventrack.models.Gudang;
import com.anomaly.inventrack.models.LogStok;
import com.anomaly.inventrack.models.Stok;
import com.anomaly.inventrack.repositories.BarangRepositories;
import com.anomaly.inventrack.repositories.GudangRepositories;
import com.anomaly.inventrack.repositories.StokRepositories;
import com.anomaly.inventrack.services.InventoryService;
import java.awt.Color;
import java.util.List;
import java.util.Optional;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author user
 */
public class FormEditStok extends javax.swing.JDialog {
    
    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(FormEditStok.class.getName());
    
    private final List<Integer> listIdStok;
    private final int idGudangUser;
    private static final int ID_GUDANG_UTAMA = 11011;
    
    private final StokRepositories stokRepo;
    private final BarangRepositories barangRepo;
    private final GudangRepositories gudangRepo;
    private final InventoryService inventoryService;
    
    private DefaultTableModel tableModel;

    /**
     * Creates new form FormEditStok
     */
    public FormEditStok(java.awt.Frame parent, boolean modal, List<Integer> listIdStok, int idGudangUser) {
        super(parent, modal);
        initComponents();
        
        this.listIdStok = listIdStok;
        this.idGudangUser = idGudangUser;
        
        this.stokRepo = new StokRepositories();
        this.barangRepo = new BarangRepositories();
        this.gudangRepo = new GudangRepositories();
        this.inventoryService = new InventoryService();
        
        setupUI();
        loadData();
    }
    
    private void setupUI() {
        setTitle("Form Transaksi & Edit Stok");
        setLocationRelativeTo(null);
        
        // 1. Tampilkan Nama Gudang
        Optional<Gudang> optGudang = gudangRepo.findById(idGudangUser);
        if (optGudang.isPresent()) {
            lblNamaGudangUser.setText(optGudang.get().getNamaGudang());
            lblNamaGudangUser.setForeground(new Color(0, 102, 204));
        }
        
        // 2. SETUP COMBOBOX (Menggunakan Enum LogStok)
        // Kita gunakan DefaultComboBoxModel agar tipe datanya aman
        DefaultComboBoxModel<LogStok.TipeTransaksi> comboModel = new DefaultComboBoxModel<>();
        
        // Kita hanya masukkan tipe 'AKSI' yang boleh dipilih user manual
        comboModel.addElement(LogStok.TipeTransaksi.MASUK);
        comboModel.addElement(LogStok.TipeTransaksi.KELUAR);
        comboModel.addElement(LogStok.TipeTransaksi.REKONSILIASI);
        
        if (this.idGudangUser == ID_GUDANG_UTAMA) {
            comboModel.addElement(LogStok.TipeTransaksi.KONTAINER);
        }
        
        cmbTipeTransaksi.setModel((javax.swing.ComboBoxModel) comboModel);
        
        cmbTipeTransaksi.addActionListener(e -> updateTableHeader());
        
        // 3. Setup Tabel
        String[] header = {
            "ID Stok",
            "ID Barang",
            "Nama Barang",
            "Kategori",
            "Stok Min",
            "Jml Saat Ini",
            "Jml Perubahan"
        };
        
        tableModel = new DefaultTableModel(header, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 4 || column == 6;
            }
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex >= 4) return Integer.class; 
                return String.class;
            }
        };
        tblBarang.setModel(tableModel);
        
        tblBarang.getColumnModel().removeColumn(tblBarang.getColumnModel().getColumn(0));
        
        tblBarang.getColumnModel().getColumn(0).setPreferredWidth(50);
        tblBarang.getColumnModel().getColumn(1).setPreferredWidth(200);
        
        btnSimpan.addActionListener(e -> prosesSimpan());
        btnBatal.addActionListener(e -> dispose());
        
        updateTableHeader();
    }
    
    private void updateTableHeader() {
        LogStok.TipeTransaksi tipe = (LogStok.TipeTransaksi) cmbTipeTransaksi.getSelectedItem();
        String judulKolomInput = "Jml Perubahan";
        
        if (tipe == LogStok.TipeTransaksi.MASUK) judulKolomInput = "Tambah (+)";
        else if (tipe == LogStok.TipeTransaksi.KELUAR) judulKolomInput = "Kurang (-)";
        else if (tipe == LogStok.TipeTransaksi.REKONSILIASI) judulKolomInput = "Stok Fisik (Baru)";
        else if (tipe == LogStok.TipeTransaksi.KONTAINER) judulKolomInput = "Jml Import (+)"; // <--- Judul Kolom
        
        tblBarang.getColumnModel().getColumn(5).setHeaderValue(judulKolomInput);
        tblBarang.getTableHeader().repaint();
    }
    
    private void loadData() {
        tableModel.setRowCount(0);
        
        for (Integer idStok : listIdStok) {
            Optional<Stok> optStok = stokRepo.getById(idStok);
            if (optStok.isPresent()) {
                Stok s = optStok.get();
                
                String namaBarang = "Unknown";
                String kategori = "-";
                
                Optional<Barang> optBarang = barangRepo.findById(s.getIdBarang());
                if (optBarang.isPresent()) {
                    namaBarang = optBarang.get().getNamaBarang();
                    kategori = optBarang.get().getKategori();
                }
                
                tableModel.addRow(new Object[]{
                    s.getIdStok(),
                    s.getIdBarang(),
                    namaBarang,
                    kategori,
                    s.getStokMinimum(),
                    s.getJumlahStok(),
                    0
                });
            }
        }
    }
    
    private void prosesSimpan() {
        if (tblBarang.getCellEditor() != null) {
            tblBarang.getCellEditor().stopCellEditing();
        }
        
        LogStok.TipeTransaksi tipe = (LogStok.TipeTransaksi) cmbTipeTransaksi.getSelectedItem();
        String keterangan = txtKeterangan.getText().trim();
        if (keterangan.isEmpty()) keterangan = "Edit Stok Manual";
        
        int confirm = JOptionPane.showConfirmDialog(this, 
                "Simpan transaksi " + tipe + " ini?", 
                "Konfirmasi", JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                for (int i = 0; i < tableModel.getRowCount(); i++) {
                    int idStok = (int) tableModel.getValueAt(i, 0);
                    int idBarang = (int) tableModel.getValueAt(i, 1);
                    int stokMinInput = (int) tableModel.getValueAt(i, 4);
                    int jumlahInput = (int) tableModel.getValueAt(i, 6); 
                    
                    // 1. Update Stok Minimum
                    Optional<Stok> sAsli = stokRepo.getById(idStok);
                    if (sAsli.isPresent() && sAsli.get().getStokMinimum() != stokMinInput) {
                        stokRepo.updateStokMinimum(null, idStok, stokMinInput);
                    }
                    
                    // 2. Proses Transaksi
                    if (jumlahInput > 0 || tipe == LogStok.TipeTransaksi.REKONSILIASI) {
                        
                        if (tipe == LogStok.TipeTransaksi.KELUAR) {
                            int stokSaatIni = (int) tableModel.getValueAt(i, 5);
                            if (jumlahInput > stokSaatIni) {
                                throw new Exception("Stok tidak cukup untuk ID Barang: " + idBarang);
                            }
                            inventoryService.kurangiStok(idBarang, idGudangUser, jumlahInput, keterangan);
                        } 
                        else if (tipe == LogStok.TipeTransaksi.MASUK) {
                            inventoryService.tambahStok(idBarang, idGudangUser, jumlahInput, keterangan);
                        } 
                        else if (tipe == LogStok.TipeTransaksi.REKONSILIASI) {
                            inventoryService.rekonsiliasiStok(idBarang, idGudangUser, jumlahInput, keterangan);
                        }
                    }
                    
                    if (jumlahInput > 0 || tipe == LogStok.TipeTransaksi.REKONSILIASI) {
                        
                        if (tipe == LogStok.TipeTransaksi.MASUK) {
                            inventoryService.tambahStok(idBarang, idGudangUser, jumlahInput, keterangan);
                        } 
                        else if (tipe == LogStok.TipeTransaksi.KONTAINER) {
                            inventoryService.tambahStokKontainer(idBarang, idGudangUser, jumlahInput, keterangan);
                        }
                        else if (tipe == LogStok.TipeTransaksi.KELUAR) {
                            int stokSaatIni = (int) tableModel.getValueAt(i, 5);
                            if (jumlahInput > stokSaatIni) {
                                throw new Exception("Stok tidak cukup untuk ID Barang: " + idBarang);
                            }
                            inventoryService.kurangiStok(idBarang, idGudangUser, jumlahInput, keterangan);
                        } 
                        else if (tipe == LogStok.TipeTransaksi.REKONSILIASI) {
                            inventoryService.rekonsiliasiStok(idBarang, idGudangUser, jumlahInput, keterangan);
                        }
                    }
                }
                
                JOptionPane.showMessageDialog(this, "Berhasil disimpan!");
                dispose(); 
                
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), "Gagal", JOptionPane.ERROR_MESSAGE);
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
        java.awt.GridBagConstraints gridBagConstraints;

        jPanel2 = new javax.swing.JPanel();
        pnlWadahTabel = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblBarang = new javax.swing.JTable();
        pnlLogStok = new javax.swing.JPanel();
        lblNamaGudangUser = new javax.swing.JLabel();
        cmbTipeTransaksi = new javax.swing.JComboBox<>();
        lblPilihTipeTransaksi = new javax.swing.JLabel();
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

        pnlLogStok.setLayout(new java.awt.GridBagLayout());

        lblNamaGudangUser.setFont(new java.awt.Font("Arial", 1, 12)); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.ipadx = 114;
        gridBagConstraints.ipady = 30;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(20, 0, 0, 0);
        pnlLogStok.add(lblNamaGudangUser, gridBagConstraints);

        cmbTipeTransaksi.setFont(new java.awt.Font("Arial", 1, 12)); // NOI18N
        cmbTipeTransaksi.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(23, 6, 0, 0);
        pnlLogStok.add(cmbTipeTransaksi, gridBagConstraints);

        lblPilihTipeTransaksi.setFont(new java.awt.Font("Arial", 1, 12)); // NOI18N
        lblPilihTipeTransaksi.setText("Tipe Transaksi");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.ipadx = 30;
        gridBagConstraints.ipady = 16;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(18, 0, 0, 0);
        pnlLogStok.add(lblPilihTipeTransaksi, gridBagConstraints);

        lblKeterangan.setFont(new java.awt.Font("Arial", 1, 12)); // NOI18N
        lblKeterangan.setText("Keterangan");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.ipadx = 48;
        gridBagConstraints.ipady = 16;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 0, 0);
        pnlLogStok.add(lblKeterangan, gridBagConstraints);

        txtKeterangan.setColumns(20);
        txtKeterangan.setRows(5);
        jScrollPane2.setViewportView(txtKeterangan);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.ipadx = 308;
        gridBagConstraints.ipady = 121;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 0, 83);
        pnlLogStok.add(jScrollPane2, gridBagConstraints);

        btnSimpan.setText("Simpan");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(18, 6, 398, 0);
        pnlLogStok.add(btnSimpan, gridBagConstraints);

        btnBatal.setText("Batal");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(18, 18, 398, 0);
        pnlLogStok.add(btnBatal, gridBagConstraints);

        jPanel2.add(pnlLogStok);

        getContentPane().add(jPanel2, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnBatal;
    private javax.swing.JButton btnSimpan;
    private javax.swing.JComboBox<Object> cmbTipeTransaksi;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JLabel lblKeterangan;
    private javax.swing.JLabel lblNamaGudangUser;
    private javax.swing.JLabel lblPilihTipeTransaksi;
    private javax.swing.JPanel pnlLogStok;
    private javax.swing.JPanel pnlWadahTabel;
    private javax.swing.JTable tblBarang;
    private javax.swing.JTextArea txtKeterangan;
    // End of variables declaration//GEN-END:variables
}
