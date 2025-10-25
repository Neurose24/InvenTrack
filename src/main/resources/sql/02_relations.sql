-- Indeks untuk tabel `barang`
--
ALTER TABLE `barang`
  ADD PRIMARY KEY (`id_barang`);

--
-- Indeks untuk tabel `detail_pengiriman`
--
ALTER TABLE `detail_pengiriman`
  ADD PRIMARY KEY (`id_detail_pengiriman`),
  ADD KEY `id_pengiriman` (`id_pengiriman`),
  ADD KEY `id_barang` (`id_barang`);

--
-- Indeks untuk tabel `detail_permintaan`
--
ALTER TABLE `detail_permintaan`
  ADD PRIMARY KEY (`id_detail_permintaan`),
  ADD KEY `id_permintaan` (`id_permintaan`),
  ADD KEY `id_barang` (`id_barang`);

--
-- Indeks untuk tabel `gudang`
--
ALTER TABLE `gudang`
  ADD PRIMARY KEY (`id_gudang`);

--
-- Indeks untuk tabel `log_stok`
--
ALTER TABLE `log_stok`
  ADD PRIMARY KEY (`id_log`),
  ADD KEY `id_gudang` (`id_gudang`),
  ADD KEY `id_barang` (`id_barang`);

--
-- Indeks untuk tabel `pengguna`
--
ALTER TABLE `pengguna`
  ADD PRIMARY KEY (`id_pengguna`),
  ADD UNIQUE KEY `username` (`username`),
  ADD KEY `id_gudang` (`id_gudang`);

--
-- Indeks untuk tabel `pengiriman`
--
ALTER TABLE `pengiriman`
  ADD PRIMARY KEY (`id_pengiriman`),
  ADD KEY `id_permintaan` (`id_permintaan`),
  ADD KEY `id_pengguna_pengirim` (`id_pengguna_pengirim`),
  ADD KEY `id_pengguna_penerima` (`id_pengguna_penerima`),
  ADD KEY `id_supir` (`id_supir`);

--
-- Indeks untuk tabel `permintaan`
--
ALTER TABLE `permintaan`
  ADD PRIMARY KEY (`id_permintaan`),
  ADD KEY `id_pengguna_peminta` (`id_pengguna_peminta`);

--
-- Indeks untuk tabel `stok`
--
ALTER TABLE `stok`
  ADD PRIMARY KEY (`id_stok`),
  ADD KEY `id_gudang` (`id_gudang`),
  ADD KEY `id_barang` (`id_barang`);

--
-- Indeks untuk tabel `supir`
--
ALTER TABLE `supir`
  ADD PRIMARY KEY (`id_supir`);

--
-- AUTO_INCREMENT untuk tabel yang dibuang
--

--
-- AUTO_INCREMENT untuk tabel `barang`
--
ALTER TABLE `barang`
  MODIFY `id_barang` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT untuk tabel `detail_pengiriman`
--
ALTER TABLE `detail_pengiriman`
  MODIFY `id_detail_pengiriman` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT untuk tabel `detail_permintaan`
--
ALTER TABLE `detail_permintaan`
  MODIFY `id_detail_permintaan` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT untuk tabel `gudang`
--
ALTER TABLE `gudang`
  MODIFY `id_gudang` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT untuk tabel `log_stok`
--
ALTER TABLE `log_stok`
  MODIFY `id_log` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT untuk tabel `pengguna`
--
ALTER TABLE `pengguna`
  MODIFY `id_pengguna` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT untuk tabel `pengiriman`
--
ALTER TABLE `pengiriman`
  MODIFY `id_pengiriman` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT untuk tabel `permintaan`
--
ALTER TABLE `permintaan`
  MODIFY `id_permintaan` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT untuk tabel `stok`
--
ALTER TABLE `stok`
  MODIFY `id_stok` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT untuk tabel `supir`
--
ALTER TABLE `supir`
  MODIFY `id_supir` int(11) NOT NULL AUTO_INCREMENT;

--
-- Ketidakleluasaan untuk tabel pelimpahan (Dumped Tables)
--

--
-- Ketidakleluasaan untuk tabel `detail_pengiriman`
--
ALTER TABLE `detail_pengiriman`
  ADD CONSTRAINT `detail_pengiriman_ibfk_1` FOREIGN KEY (`id_pengiriman`) REFERENCES `pengiriman` (`id_pengiriman`),
  ADD CONSTRAINT `detail_pengiriman_ibfk_2` FOREIGN KEY (`id_barang`) REFERENCES `barang` (`id_barang`);

--
-- Ketidakleluasaan untuk tabel `detail_permintaan`
--
ALTER TABLE `detail_permintaan`
  ADD CONSTRAINT `detail_permintaan_ibfk_1` FOREIGN KEY (`id_permintaan`) REFERENCES `permintaan` (`id_permintaan`),
  ADD CONSTRAINT `detail_permintaan_ibfk_2` FOREIGN KEY (`id_barang`) REFERENCES `barang` (`id_barang`);

--
-- Ketidakleluasaan untuk tabel `log_stok`
--
ALTER TABLE `log_stok`
  ADD CONSTRAINT `log_stok_ibfk_1` FOREIGN KEY (`id_gudang`) REFERENCES `gudang` (`id_gudang`),
  ADD CONSTRAINT `log_stok_ibfk_2` FOREIGN KEY (`id_barang`) REFERENCES `barang` (`id_barang`);

--
-- Ketidakleluasaan untuk tabel `pengguna`
--
ALTER TABLE `pengguna`
  ADD CONSTRAINT `pengguna_ibfk_1` FOREIGN KEY (`id_gudang`) REFERENCES `gudang` (`id_gudang`);

--
-- Ketidakleluasaan untuk tabel `pengiriman`
--
ALTER TABLE `pengiriman`
  ADD CONSTRAINT `pengiriman_ibfk_1` FOREIGN KEY (`id_permintaan`) REFERENCES `permintaan` (`id_permintaan`),
  ADD CONSTRAINT `pengiriman_ibfk_2` FOREIGN KEY (`id_pengguna_pengirim`) REFERENCES `pengguna` (`id_pengguna`),
  ADD CONSTRAINT `pengiriman_ibfk_3` FOREIGN KEY (`id_pengguna_penerima`) REFERENCES `pengguna` (`id_pengguna`),
  ADD CONSTRAINT `pengiriman_ibfk_4` FOREIGN KEY (`id_supir`) REFERENCES `supir` (`id_supir`);

--
-- Ketidakleluasaan untuk tabel `permintaan`
--
ALTER TABLE `permintaan`
  ADD CONSTRAINT `permintaan_ibfk_1` FOREIGN KEY (`id_pengguna_peminta`) REFERENCES `pengguna` (`id_pengguna`);

--
-- Ketidakleluasaan untuk tabel `stok`
--
ALTER TABLE `stok`
  ADD CONSTRAINT `stok_ibfk_1` FOREIGN KEY (`id_gudang`) REFERENCES `gudang` (`id_gudang`),
  ADD CONSTRAINT `stok_ibfk_2` FOREIGN KEY (`id_barang`) REFERENCES `barang` (`id_barang`);