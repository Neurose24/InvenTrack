package com.anomaly.inventrack.models;

import java.io.Serializable;
import java.time.LocalDateTime;

public class Permintaan implements Serializable {
    private Integer idPermintaan;
    private Integer idPenggunaPeminta;
    private LocalDateTime tanggalPermintaan;
    private StatusPermintaan statusPermintaan;
    private String catatanPermintaan;

    public enum StatusPermintaan {
        MENUNGGU,
        DISETUJUI,
        DITOLAK,
        SELESAI
    }

    public Permintaan() {
    }

    public Permintaan(Integer idPermintaan, Integer idPenggunaPeminta, LocalDateTime tanggalPermintaan, StatusPermintaan statusPermintaan, String catatanPermintaan) {
        this.idPermintaan = idPermintaan;
        this.idPenggunaPeminta = idPenggunaPeminta;
        this.tanggalPermintaan = tanggalPermintaan;
        this.statusPermintaan = statusPermintaan;
        this.catatanPermintaan = catatanPermintaan;
    }

    public Integer getIdPermintaan() { return idPermintaan; }
    public void setIdPermintaan(Integer idPermintaan) { this.idPermintaan = idPermintaan; }

    public Integer getIdPenggunaPeminta() { return idPenggunaPeminta; }
    public void setIdPenggunaPeminta(Integer idPenggunaPeminta) { this.idPenggunaPeminta = idPenggunaPeminta; }

    public LocalDateTime getTanggalPermintaan() { return tanggalPermintaan; }
    public void setTanggalPermintaan(LocalDateTime tanggalPermintaan) { this.tanggalPermintaan = tanggalPermintaan; }

    public StatusPermintaan getStatusPermintaan() { return statusPermintaan; }
    public void setStatusPermintaan(StatusPermintaan statusPermintaan) { this.statusPermintaan = statusPermintaan; }

    public String getCatatanPermintaan() { return catatanPermintaan; }
    public void setCatatanPermintaan(String catatanPermintaan) { this.catatanPermintaan = catatanPermintaan; }

    @Override
    public String toString() {
        return "Permintaan{" +
                "idPermintaan=" + idPermintaan +
                ", idPenggunaPeminta=" + idPenggunaPeminta +
                ", tanggalPermintaan=" + tanggalPermintaan +
                ", statusPermintaan=" + statusPermintaan +
                ", catatanPermintaan='" + catatanPermintaan + '\'' +
                '}';
    }
}
