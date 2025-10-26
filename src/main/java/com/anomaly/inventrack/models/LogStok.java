package com.anomaly.inventrack.models;

import java.io.Serializable;
import java.time.LocalDateTime;

public class LogStok implements Serializable {
    private Integer idLog;
    private Integer idGudang;
    private Integer idBarang;
    private TipeTransaksi tipeTransaksi;
    private Integer jumlahPerubahan;
    private LocalDateTime tanggalLog;
    private String keterangan;

    public enum TipeTransaksi {
        MASUK,
        KELUAR
    }

    public LogStok() {
    }

    public LogStok(Integer idLog, Integer idGudang, Integer idBarang, TipeTransaksi tipeTransaksi, Integer jumlahPerubahan, LocalDateTime tanggalLog, String keterangan) {
        this.idLog = idLog;
        this.idGudang = idGudang;
        this.idBarang = idBarang;
        this.tipeTransaksi = tipeTransaksi;
        this.jumlahPerubahan = jumlahPerubahan;
        this.tanggalLog = tanggalLog;
        this.keterangan = keterangan;
    }

    public Integer getIdLog() { return idLog; }
    public void setIdLog(Integer idLog) { this.idLog = idLog; }

    public Integer getIdGudang() { return idGudang; }
    public void setIdGudang(Integer idGudang) { this.idGudang = idGudang; }

    public Integer getIdBarang() { return idBarang; }
    public void setIdBarang(Integer idBarang) { this.idBarang = idBarang; }

    public TipeTransaksi getTipeTransaksi() { return tipeTransaksi; }
    public void setTipeTransaksi(TipeTransaksi tipeTransaksi) { this.tipeTransaksi = tipeTransaksi; }

    public Integer getJumlahPerubahan() { return jumlahPerubahan; }
    public void setJumlahPerubahan(Integer jumlahPerubahan) { this.jumlahPerubahan = jumlahPerubahan; }

    public LocalDateTime getTanggalLog() { return tanggalLog; }
    public void setTanggalLog(LocalDateTime tanggalLog) { this.tanggalLog = tanggalLog; }

    public String getKeterangan() { return keterangan; }
    public void setKeterangan(String keterangan) { this.keterangan = keterangan; }

    @Override
    public String toString() {
        return "LogStok{" +
                "idLog=" + idLog +
                ", idGudang=" + idGudang +
                ", idBarang=" + idBarang +
                ", tipeTransaksi=" + tipeTransaksi +
                ", jumlahPerubahan=" + jumlahPerubahan +
                ", tanggalLog=" + tanggalLog +
                ", keterangan='" + keterangan + '\'' +
                '}';
    }
}
