package com.anomaly.inventrack.models;

import java.io.Serializable;
import java.time.LocalDateTime;

// Kelas Model Mandiri
public class LogStokDetail extends LogStok implements Serializable { 
    
    // Properti baru
    private String namaBarang;
    private String namaGudang;

    public LogStokDetail() {
        super();
    }

    // Constructor gabungan
    public LogStokDetail(Integer idLog, Integer idGudang, Integer idBarang, TipeTransaksi tipeTransaksi, Integer jumlahPerubahan, LocalDateTime tanggalLog, String keterangan, String namaBarang, String namaGudang) {
        super(idLog, idGudang, idBarang, tipeTransaksi, jumlahPerubahan, tanggalLog, keterangan);
        this.namaBarang = namaBarang;
        this.namaGudang = namaGudang;
    }

    public String getNamaBarang() {
        return namaBarang;
    }

    public void setNamaBarang(String namaBarang) {
        this.namaBarang = namaBarang;
    }

    public String getNamaGudang() {
        return namaGudang;
    }

    public void setNamaGudang(String namaGudang) {
        this.namaGudang = namaGudang;
    }

    @Override
    public String toString() {
        return "LogStokDetail{" +
                "namaBarang='" + namaBarang + '\'' +
                ", namaGudang='" + namaGudang + '\'' +
                "} " + super.toString();
    }
}