package com.anomaly.inventrack.models;

import java.io.Serializable;

public class Supir implements Serializable {
    private Integer idSupir;
    private String namaSupir;
    private String noHp;
    private String noKendaraan;

    public Supir() {
    }

    public Supir(Integer idSupir, String namaSupir, String noHp, String noKendaraan) {
        this.idSupir = idSupir;
        this.namaSupir = namaSupir;
        this.noHp = noHp;
        this.noKendaraan = noKendaraan;
    }

    public Integer getIdSupir() { return idSupir; }
    public void setIdSupir(Integer idSupir) { this.idSupir = idSupir; }

    public String getNamaSupir() { return namaSupir; }
    public void setNamaSupir(String namaSupir) { this.namaSupir = namaSupir; }

    public String getNoHp() { return noHp; }
    public void setNoHp(String noHp) { this.noHp = noHp; }

    public String getNoKendaraan() { return noKendaraan; }
    public void setNoKendaraan(String noKendaraan) { this.noKendaraan = noKendaraan; }

    @Override
    public String toString() {
        return "Supir{" +
                "idSupir=" + idSupir +
                ", namaSupir='" + namaSupir + '\'' +
                ", noHp='" + noHp + '\'' +
                ", noKendaraan='" + noKendaraan + '\'' +
                '}';
    }
}
