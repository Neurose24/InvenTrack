package com.anomaly.inventrack.models;

import java.io.Serializable;

public class Gudang implements Serializable {
    private Integer idGudang;
    private String namaGudang;
    private String lokasi;
    private String kontakAdmin;

    public Gudang() {
    }

    public Gudang(Integer idGudang, String namaGudang, String lokasi, String kontakAdmin) {
        this.idGudang = idGudang;
        this.namaGudang = namaGudang;
        this.lokasi = lokasi;
        this.kontakAdmin = kontakAdmin;
    }

    public Integer getIdGudang() { return idGudang; }
    public void setIdGudang(Integer idGudang) { this.idGudang = idGudang; }

    public String getNamaGudang() { return namaGudang; }
    public void setNamaGudang(String namaGudang) { this.namaGudang = namaGudang; }

    public String getLokasi() { return lokasi; }
    public void setLokasi(String lokasi) { this.lokasi = lokasi; }

    public String getKontakAdmin() { return kontakAdmin; }
    public void setKontakAdmin(String kontakAdmin) { this.kontakAdmin = kontakAdmin; }

    @Override
    public String toString() {
        return "Gudang{" +
                "idGudang=" + idGudang +
                ", namaGudang='" + namaGudang + '\'' +
                ", lokasi='" + lokasi + '\'' +
                ", kontakAdmin='" + kontakAdmin + '\'' +
                '}';
    }
}
