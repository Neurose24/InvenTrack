package com.anomaly.inventrack.models;

import java.io.Serializable;

public class Supir implements Serializable {
    private Integer idSupir;
    private String namaSupir;
    private String noHp;
    private String noKendaraan;
    private Integer idGudang;

    public Supir() {
    }

    public Supir(Integer idSupir, String namaSupir, String noHp, String noKendaraan, Integer idGudang) {
        this.idSupir = idSupir;
        this.namaSupir = namaSupir;
        this.noHp = noHp;
        this.noKendaraan = noKendaraan;
        this.idGudang = idGudang;
    }

    public Integer getIdSupir() { return idSupir; }
    public void setIdSupir(Integer idSupir) { this.idSupir = idSupir; }

    public String getNamaSupir() { return namaSupir; }
    public void setNamaSupir(String namaSupir) { this.namaSupir = namaSupir; }

    public String getNoHp() { return noHp; }
    public void setNoHp(String noHp) { this.noHp = noHp; }

    public String getNoKendaraan() { return noKendaraan; }
    public void setNoKendaraan(String noKendaraan) { this.noKendaraan = noKendaraan; }

    public Integer getIdGudang() { return idGudang; }
    public void setIdGudang(Integer idGudang) { this.idGudang = idGudang; }

    @Override
    public String toString() {
        return "Supir{" +
                "idSupir=" + idSupir +
                ", namaSupir='" + namaSupir + '\'' +
                ", noHp='" + noHp + '\'' +
                ", noKendaraan='" + noKendaraan + '\'' +
                ", idGudang=" + idGudang +
                '}';
    }
}
