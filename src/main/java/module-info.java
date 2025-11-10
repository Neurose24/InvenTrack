module com.anomaly {
    
    // 1. Kebutuhan JavaFX (Wajib untuk GUI)
    // Mengharuskan modul JavaFX yang diunduh dari pom.xml
    requires javafx.controls;
    requires javafx.fxml;
    requires transitive javafx.graphics; // Ditambahkan karena kelas UI Anda memerlukannya

    // 2. Kebutuhan JDBC (Database)
    // Ini menyelesaikan ERROR: "package java.sql is not visible"
    requires java.sql;
    
    // 3. Kebutuhan Library Pihak Ketiga (Unresolved Dependensi)
    // Menyelesaikan ERROR: "package com.zaxxer.hikari is not visible"
    // Karena ini adalah library yang tidak modular, kita harus memberi tahu
    // Java bahwa kita memerlukan akses ke kelas-kelasnya.
    requires com.zaxxer.hikari; 
    
    // Asumsi driver MySQL juga non-modular (seperti versi lama)
    // requires com.mysql.jdbc; // Atau modules lain jika driver modular

    // 4. Membuka Paket (Opens) - Untuk FXML dan Reflection
    // FXML perlu mengakses Controller UI Anda.
    opens com.anomaly.inventrack.ui to javafx.fxml, javafx.base; 
    
    // 5. Ekspor Paket (Exports) - Untuk Akses Lintas Modul
    // Mengekspor kelas utama aplikasi (Entry Point)
    exports com.anomaly.inventrack.ui; 

    // Memberitahu module system bahwa modul ini menyediakan implementasi 
    // dari ServiceLoader untuk javafx.application.Application.
    uses javafx.application.Application;
    
    // Memberitahu Java module system bahwa kelas InvenTrackApp adalah 
    // implementasi dari javafx.application.Application.
    provides javafx.application.Application with com.anomaly.inventrack.ui.InvenTrackApp;
    
    // Mengekspor semua paket yang berisi Model, Controller Backend, dan Service
    // Ini penting agar Reflection dan komponen UI dapat melihat data/metode.
    exports com.anomaly.inventrack.models;
    exports com.anomaly.inventrack.controllers;
    exports com.anomaly.inventrack.services;
    exports com.anomaly.inventrack.repositories; 
    exports com.anomaly.inventrack.utils; // Utilitas Database
    exports com.anomaly.inventrack.services.exceptions; // Custom Exceptions
}