package com.anomaly.inventrack.models;

import java.io.Serializable;

public class Pengguna implements Serializable {
    private Integer idPengguna;
    private String namaPengguna;
    private String username;
    private String passwordHash;
    private Integer idGudang;

    public Pengguna() {
    }

    public Pengguna(Integer idPengguna, String namaPengguna, String username, String passwordHash, Integer idGudang) {
        this.idPengguna = idPengguna;
        this.namaPengguna = namaPengguna;
        this.username = username;
        this.passwordHash = passwordHash;
        this.idGudang = idGudang;
    }

    public Integer getIdPengguna() { return idPengguna; }
    public void setIdPengguna(Integer idPengguna) { this.idPengguna = idPengguna; }

    public String getNamaPengguna() { return namaPengguna; }
    public void setNamaPengguna(String namaPengguna) { this.namaPengguna = namaPengguna; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public Integer getIdGudang() { return idGudang; }
    public void setIdGudang(Integer idGudang) { this.idGudang = idGudang; }

    @Override
    public String toString() {
        return "Pengguna{" +
                "idPengguna=" + idPengguna +
                ", namaPengguna='" + namaPengguna + '\'' +
                ", username='" + username + '\'' +
                ", passwordHash='" + passwordHash + '\'' +
                ", idGudang=" + idGudang +
                '}';
    }
}
