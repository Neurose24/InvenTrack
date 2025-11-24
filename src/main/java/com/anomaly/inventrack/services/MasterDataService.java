package com.anomaly.inventrack.services;

import com.anomaly.inventrack.models.*;
import com.anomaly.inventrack.repositories.*;
import com.anomaly.inventrack.services.exceptions.NotFoundException;

import java.util.List;

public class MasterDataService {

    private final BarangRepositories barangRepo;
    private final GudangRepositories gudangRepo;
    private final SupirRepositories supirRepo;
    private final PenggunaRepositories penggunaRepo; 

    public MasterDataService() {
        this.barangRepo = new BarangRepositories();
        this.gudangRepo = new GudangRepositories();
        this.supirRepo = new SupirRepositories();
        this.penggunaRepo = new PenggunaRepositories();
    }
    
    public Barang createBarang(Barang barang) {
        if (barang.getNamaBarang() == null || barang.getNamaBarang().trim().isEmpty()) {
            throw new IllegalArgumentException("Nama Barang wajib diisi.");
        }
        barangRepo.save(barang);
        return barang;
    }

    public List<Barang> getAllBarang() {
        return barangRepo.findAll();
    }
    
    public Barang getBarangById(int idBarang) {
        return barangRepo.findById(idBarang)
                   .orElseThrow(() -> new NotFoundException("Barang dengan ID " + idBarang + " tidak ditemukan."));
    }

    public Gudang createGudang(Gudang gudang) {
        if (gudang.getNamaGudang() == null || gudang.getNamaGudang().trim().isEmpty()) {
            throw new IllegalArgumentException("Nama Gudang wajib diisi.");
        }
        
        gudangRepo.save(gudang); 
        
        return gudang;
    }
    
    public List<Gudang> getAllGudang() {
        return gudangRepo.findAll();
    }

    public Gudang updateGudang(Gudang gudang) {
        if (gudang.getIdGudang() == null) {
            throw new IllegalArgumentException("ID Gudang wajib diisi untuk operasi update.");
        }
        getGudangById(gudang.getIdGudang()); 

        gudangRepo.update(gudang); 

        return gudang;
    }

    public Gudang getGudangById(int idGudang) {
         return gudangRepo.findById(idGudang)
                .orElseThrow(() -> new NotFoundException("Gudang dengan ID " + idGudang + " tidak ditemukan."));
    }

    public Supir createSupir(Supir supir) {
        if (supir.getNamaSupir() == null || supir.getNamaSupir().trim().isEmpty()) {
            throw new IllegalArgumentException("Nama Supir wajib diisi.");
        }
        
        if (supir.getIdGudang() == null) {
             throw new IllegalArgumentException("Supir harus terdaftar di gudang tertentu.");
        }

        supirRepo.saveSupir(supir); 
        return supir;
    }

    public List<Supir> getAllSupir() {
        return supirRepo.findAll();
    }

    public boolean deleteSupir(int idSupir) {
        supirRepo.deleteSupir(idSupir);
        return true;
    }

    public List<Pengguna> getAllPengguna() {
        return penggunaRepo.findAll();
    }
}