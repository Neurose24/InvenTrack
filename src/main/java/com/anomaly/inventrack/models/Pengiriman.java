package com.anomaly.inventrack.models;

import java.io.Serializable;
import java.time.LocalDateTime;

public class Pengiriman implements Serializable {
    private Integer idPengiriman;
    private Integer idPermintaan;
    private Integer idPenggunaPengirim;
    private Integer idPenggunaPenerima;
    private Integer idSupir;
    private LocalDateTime tanggalPengiriman;
    private String noKendaraan;
    private StatusPengiriman statusPengiriman;
    private String KeteranganPengiriman;

    public Pengiriman() {
    }

    public enum StatusPengiriman {
        DIKIRIM,
        DITERIMA,
        DIBATALKAN
    }

    public Pengiriman(Integer idPengiriman, Integer idPermintaan, Integer idPenggunaPengirim, Integer idPenggunaPenerima, Integer idSupir, LocalDateTime tanggalPengiriman, String noKendaraan, StatusPengiriman statusPengiriman, String keteranganPengiriman) {
        this.idPengiriman = idPengiriman;
        this.idPermintaan = idPermintaan;
        this.idPenggunaPengirim = idPenggunaPengirim;
        this.idPenggunaPenerima = idPenggunaPenerima;
        this.idSupir = idSupir;
        this.tanggalPengiriman = tanggalPengiriman;
        this.noKendaraan = noKendaraan;
        this.statusPengiriman = statusPengiriman;
        this.KeteranganPengiriman = keteranganPengiriman;
    }

    public Integer getIdPengiriman() { return idPengiriman; }
    public void setIdPengiriman(Integer idPengiriman) { this.idPengiriman = idPengiriman; }

    public Integer getIdPermintaan() { return idPermintaan; }
    public void setIdPermintaan(Integer idPermintaan) { this.idPermintaan = idPermintaan; }

    public Integer getIdPenggunaPengirim() { return idPenggunaPengirim; }
    public void setIdPenggunaPengirim(Integer idPenggunaPengirim) { this.idPenggunaPengirim = idPenggunaPengirim; }

    public Integer getIdPenggunaPenerima() { return idPenggunaPenerima; }
    public void setIdPenggunaPenerima(Integer idPenggunaPenerima) { this.idPenggunaPenerima = idPenggunaPenerima; }

    public Integer getIdSupir() { return idSupir; }
    public void setIdSupir(Integer idSupir) { this.idSupir = idSupir; }

    public LocalDateTime getTanggalPengiriman() { return tanggalPengiriman; }
    public void setTanggalPengiriman(LocalDateTime tanggalPengiriman) { this.tanggalPengiriman = tanggalPengiriman; }

    public String getNoKendaraan() { return noKendaraan; }
    public void setNoKendaraan(String noKendaraan) { this.noKendaraan = noKendaraan; }

    public StatusPengiriman getStatusPengiriman() { return statusPengiriman; }
    public void setStatusPengiriman(StatusPengiriman statusPengiriman) { this.statusPengiriman = statusPengiriman; }

    public String getKeteranganPengiriman() { return KeteranganPengiriman; }
    public void setKeteranganPengiriman(String keteranganPengiriman) { KeteranganPengiriman = keteranganPengiriman; }

    @Override
    public String toString() {
        return "Pengiriman{" +
                "idPengiriman=" + idPengiriman +
                ", idPermintaan=" + idPermintaan +
                ", idPenggunaPengirim=" + idPenggunaPengirim +
                ", idPenggunaPenerima=" + idPenggunaPenerima +
                ", idSupir=" + idSupir +
                ", tanggalPengiriman=" + tanggalPengiriman +
                ", noKendaraan='" + noKendaraan + '\'' +
                ", statusPengiriman=" + statusPengiriman +
                ", KeteranganPengiriman='" + KeteranganPengiriman + '\'' +
                '}';
    }
}
