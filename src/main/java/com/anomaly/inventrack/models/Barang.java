package com.anomaly.inventrack.models;

import java.io.Serializable;

public class Barang implements Serializable{
    private Integer idBarang;
    private String namaBarang;
    private String kategori;
    private String satuan;
    private String deskripsi;

    public Barang() {
    }

    public Barang(Integer idBarang, String namaBarang, String kategori, String satuan, String deskripsi) {
        this.idBarang = idBarang;
        this.namaBarang = namaBarang;
        this.kategori = kategori;
        this.satuan = satuan;
        this.deskripsi = deskripsi;
    }

    public Integer getIdBarang() { return idBarang; }
    public void setIdBarang(Integer idBarang) { this.idBarang = idBarang; }

    public String getNamaBarang() { return namaBarang; }
    public void setNamaBarang(String namaBarang) { this.namaBarang = namaBarang; }

    public String getKategori() { return kategori; }
    public void setKategori(String kategori) { this.kategori = kategori; }

    public String getSatuan() { return satuan; }
    public void setSatuan(String satuan) { this.satuan = satuan; }

    public String getDeskripsi() { return deskripsi; }
    public void setDeskripsi(String deskripsi) { this.deskripsi = deskripsi; }

    @Override
    public String toString() {
        return "Barang{" +
                "idBarang=" + idBarang +
                ", namaBarang='" + namaBarang + '\'' +
                ", kategori='" + kategori + '\'' +
                ", satuan='" + satuan + '\'' +
                ", deskripsi='" + deskripsi + '\'' +
                '}';
    }
}
