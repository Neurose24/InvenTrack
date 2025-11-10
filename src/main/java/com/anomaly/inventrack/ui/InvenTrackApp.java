package com.anomaly.inventrack.ui;

import com.anomaly.inventrack.controllers.InventrackController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Kelas utama aplikasi desktop (Entry Point JavaFX).
 */
public class InvenTrackApp extends Application {

    // Simpan objek controller untuk diakses oleh semua tampilan
    private InventrackController controller;

    // --- 1. Metode Wajib JavaFX (Memulai Aplikasi) ---
    @Override
    public void start(Stage primaryStage) throws IOException {
        
        // 1. Inisialisasi Controller Backend
        this.controller = new InventrackController();
        
        // 2. Muat Tampilan Awal (Login)
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
        Parent root = loader.load();
        
        // Opsional: Jika Anda ingin memberikan Controller ke tampilan awal
        // LoginView loginController = loader.getController();
        // loginController.setMainApp(this); // Pola umum untuk menghubungkan scene
        
        // 3. Setup Stage
        primaryStage.setTitle("Inventrack - Sistem Inventaris Multi-Gudang");
        primaryStage.setScene(new Scene(root));
        primaryStage.show();
    }

    // --- 2. Metode main untuk menjalankan aplikasi ---
    public static void main(String[] args) {
        // Metode launch() yang akan memanggil start(Stage) di atas
        launch(args); 
    }

    // --- 3. Getter untuk Controller (Agar dapat diakses di tampilan lain) ---
    public InventrackController getController() {
        return controller;
    }
}