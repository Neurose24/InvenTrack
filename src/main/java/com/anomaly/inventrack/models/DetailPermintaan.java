package com.anomaly.inventrack.models;

import java.io.Serializable;

public class DetailPermintaan implements Serializable {
    private Integer idDetailPermintaan;
    private Integer idPermintaan;
    private Integer idBarang;
    private Integer jumlahDiminta;
    private Integer jumlahDisetujui;
    private String catatanAdmin;

    public DetailPermintaan() {
    }

    public DetailPermintaan(Integer idDetailPermintaan, Integer idPermintaan, Integer idBarang, Integer jumlahDiminta, Integer jumlahDisetujui, String catatanAdmin) {
        this.idDetailPermintaan = idDetailPermintaan;
        this.idPermintaan = idPermintaan;
        this.idBarang = idBarang;
        this.jumlahDiminta = jumlahDiminta;
        this.jumlahDisetujui = jumlahDisetujui;
        this.catatanAdmin = catatanAdmin;
    }

    public Integer getIdDetailPermintaan() { return idDetailPermintaan; }
    public void setIdDetailPermintaan(Integer idDetailPermintaan) { this.idDetailPermintaan = idDetailPermintaan; }

    public Integer getIdPermintaan() { return idPermintaan; }
    public void setIdPermintaan(Integer idPermintaan) { this.idPermintaan = idPermintaan; }

    public Integer getIdBarang() { return idBarang; }
    public void setIdBarang(Integer idBarang) { this.idBarang = idBarang; }

    public Integer getJumlahDiminta() { return jumlahDiminta; }
    public void setJumlahDiminta(Integer jumlahDiminta) { this.jumlahDiminta = jumlahDiminta; }

    public Integer getJumlahDisetujui() { return jumlahDisetujui; }
    public void setJumlahDisetujui(Integer jumlahDisetujui) { this.jumlahDisetujui = jumlahDisetujui; }

    public String getCatatanAdmin() { return catatanAdmin; }
    public void setCatatanAdmin(String catatanAdmin) { this.catatanAdmin = catatanAdmin; }

    @Override
    public String toString() {
        return "DetailPermintaan{" +
                "idDetailPermintaan=" + idDetailPermintaan +
                ", idPermintaan=" + idPermintaan +
                ", idBarang=" + idBarang +
                ", jumlahDiminta=" + jumlahDiminta +
                ", jumlahDisetujui=" + jumlahDisetujui +
                ", catatanAdmin='" + catatanAdmin + '\'' +
                '}';
    }
}
