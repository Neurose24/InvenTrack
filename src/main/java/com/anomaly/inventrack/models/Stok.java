package com.anomaly.inventrack.models;

import java.io.Serializable;
import java.time.LocalDateTime;

public class Stok implements Serializable {
    private Integer idStok;
    private Integer idGudang;
    private Integer idBarang;
    private Integer jumlahStok;
    private Integer stokMinimum;
    private LocalDateTime tanggalUpdate;

    public Stok() {
    }

    public Stok(Integer idStok, Integer idGudang, Integer idBarang, Integer jumlahStok, Integer stokMinimum, LocalDateTime tanggalUpdate) {
        this.idStok = idStok;
        this.idGudang = idGudang;
        this.idBarang = idBarang;
        this.jumlahStok = jumlahStok;
        this.stokMinimum = stokMinimum;
        this.tanggalUpdate = tanggalUpdate;
    }

    public Integer getIdStok() { return idStok; }
    public void setIdStok(Integer idStok) { this.idStok = idStok; }

    public Integer getIdGudang() { return idGudang; }
    public void setIdGudang(Integer idGudang) { this.idGudang = idGudang; }

    public Integer getIdBarang() { return idBarang; }
    public void setIdBarang(Integer idBarang) { this.idBarang = idBarang; }

    public Integer getJumlahStok() { return jumlahStok; }
    public void setJumlahStok(Integer jumlahStok) { this.jumlahStok = jumlahStok; }

    public Integer getStokMinimum() { return stokMinimum; }
    public void setStokMinimum(Integer stokMinimum) { this.stokMinimum = stokMinimum; }

    public LocalDateTime getTanggalUpdate() { return tanggalUpdate; }
    public void setTanggalUpdate(LocalDateTime tanggalUpdate) { this.tanggalUpdate = tanggalUpdate; }

    @Override
    public String toString() {
        return "Stok{" +
                "idStok=" + idStok +
                ", idGudang=" + idGudang +
                ", idBarang=" + idBarang +
                ", jumlahStok=" + jumlahStok +
                ", stokMinimum=" + stokMinimum +
                ", tanggalUpdate=" + tanggalUpdate +
                '}';
    }
}
