package com.anomaly.inventrack.models;

import java.io.Serializable;

public class DetailPengiriman implements Serializable {
    private Integer idDetailPengiriman;
    private Integer idPengiriman;
    private Integer idBarang;
    private Integer jumlahDikirim;
    private Integer jumlahDiterima;
    private StatusPenerimaan statusPenerimaan;
    private String catatanPenerimaan;

    public enum StatusPenerimaan {
        DITERIMA,
        BELUM_DITERIMA,
        RUSAK
    }

    public DetailPengiriman() {
    }

    public DetailPengiriman(Integer idDetailPengiriman, Integer idPengiriman, Integer idBarang, Integer jumlahDikirim, Integer jumlahDiterima, StatusPenerimaan statusPenerimaan, String catatanPenerimaan) {
        this.idDetailPengiriman = idDetailPengiriman;
        this.idPengiriman = idPengiriman;
        this.idBarang = idBarang;
        this.jumlahDikirim = jumlahDikirim;
        this.jumlahDiterima = jumlahDiterima;
        this.statusPenerimaan = statusPenerimaan;
        this.catatanPenerimaan = catatanPenerimaan;
    }

    public Integer getIdDetailPengiriman() { return idDetailPengiriman; }
    public void setIdDetailPengiriman(Integer idDetailPengiriman) { this.idDetailPengiriman = idDetailPengiriman; }

    public Integer getIdPengiriman() { return idPengiriman; }
    public void setIdPengiriman(Integer idPengiriman) { this.idPengiriman = idPengiriman; }

    public Integer getIdBarang() { return idBarang; }
    public void setIdBarang(Integer idBarang) { this.idBarang = idBarang; }

    public Integer getJumlahDikirim() { return jumlahDikirim; }
    public void setJumlahDikirim(Integer jumlahDikirim) { this.jumlahDikirim = jumlahDikirim; }

    public Integer getJumlahDiterima() { return jumlahDiterima; }
    public void setJumlahDiterima(Integer jumlahDiterima) { this.jumlahDiterima = jumlahDiterima; }

    public StatusPenerimaan getStatusPenerimaan() { return statusPenerimaan; }
    public void setStatusPenerimaan(StatusPenerimaan statusPenerimaan) { this.statusPenerimaan = statusPenerimaan; }

    public String getCatatanPenerimaan() { return catatanPenerimaan; }
    public void setCatatanPenerimaan(String catatanPenerimaan) { this.catatanPenerimaan = catatanPenerimaan; }

    @Override
    public String toString() {
        return "DetailPengiriman{" +
                "idDetailPengiriman=" + idDetailPengiriman +
                ", idPengiriman=" + idPengiriman +
                ", idBarang=" + idBarang +
                ", jumlahDikirim=" + jumlahDikirim +
                ", jumlahDiterima=" + jumlahDiterima +
                ", statusPenerimaan=" + statusPenerimaan +
                ", catatanPenerimaan='" + catatanPenerimaan + '\'' +
                '}';
    }
}
