SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

CREATE TABLE IF NOT EXISTS `barang` (
  `id_barang` int(11) NOT NULL,
  `nama_barang` varchar(100) NOT NULL,
  `kategori` varchar(50) DEFAULT NULL,
  `satuan` varchar(30) DEFAULT NULL,
  `deskripsi` text
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- --------------------------------------------------------

--
-- Struktur dari tabel `detail_pengiriman`
--

CREATE TABLE IF NOT EXISTS `detail_pengiriman` (
  `id_detail_pengiriman` int(11) NOT NULL,
  `id_pengiriman` int(11) DEFAULT NULL,
  `id_barang` int(11) DEFAULT NULL,
  `jumlah_dikirim` int(11) NOT NULL,
  `jumlah_diterima` int(11) DEFAULT '0',
  `status_penerimaan` enum('BELUM DITERIMA','DITERIMA','RUSAK') DEFAULT 'BELUM DITERIMA',
  `catatan_penerimaan` text
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- --------------------------------------------------------

--
-- Struktur dari tabel `detail_permintaan`
--

CREATE TABLE IF NOT EXISTS `detail_permintaan` (
  `id_detail_permintaan` int(11) NOT NULL,
  `id_permintaan` int(11) DEFAULT NULL,
  `id_barang` int(11) DEFAULT NULL,
  `jumlah_diminta` int(11) NOT NULL,
  `jumlah_disetujui` int(11) DEFAULT '0',
  `status_barang` enum('MENUNGGU','DISETUJUI','DITOLAK') DEFAULT 'MENUNGGU',
  `catatan_admin` text
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- --------------------------------------------------------

--
-- Struktur dari tabel `gudang`
--

CREATE TABLE IF NOT EXISTS `gudang` (
  `id_gudang` int(11) NOT NULL,
  `nama_gudang` varchar(100) NOT NULL,
  `lokasi` varchar(150) DEFAULT NULL,
  `kontak_admin` varchar(50) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- --------------------------------------------------------

--
-- Struktur dari tabel `log_stok`
--

CREATE TABLE IF NOT EXISTS `log_stok` (
  `id_log` int(11) NOT NULL,
  `id_gudang` int(11) DEFAULT NULL,
  `id_barang` int(11) DEFAULT NULL,
  `tipe_transaksi` enum('MASUK','KELUAR') NOT NULL,
  `jumlah_perubahan` int(11) NOT NULL,
  `tanggal_log` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `keterangan` text
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- --------------------------------------------------------

--
-- Struktur dari tabel `pengguna`
--

CREATE TABLE IF NOT EXISTS `pengguna` (
  `id_pengguna` int(11) NOT NULL,
  `nama_pengguna` varchar(100) NOT NULL,
  `username` varchar(50) NOT NULL,
  `password` varchar(255) NOT NULL,
  `id_gudang` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- --------------------------------------------------------

--
-- Struktur dari tabel `pengiriman`
--

CREATE TABLE IF NOT EXISTS `pengiriman` (
  `id_pengiriman` int(11) NOT NULL,
  `id_permintaan` int(11) DEFAULT NULL,
  `id_pengguna_pengirim` int(11) DEFAULT NULL,
  `id_pengguna_penerima` int(11) DEFAULT NULL,
  `id_supir` int(11) DEFAULT NULL,
  `tanggal_pengiriman` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `no_kendaraan` varchar(30) DEFAULT NULL,
  `status_pengiriman` enum('DIKIRIM','DITERIMA','DIBATALKAN') DEFAULT 'DIKIRIM',
  `keterangan_pengiriman` text
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- --------------------------------------------------------

--
-- Struktur dari tabel `permintaan`
--

CREATE TABLE IF NOT EXISTS `permintaan` (
  `id_permintaan` int(11) NOT NULL,
  `id_pengguna_peminta` int(11) DEFAULT NULL,
  `id_pengiriman` int(11) DEFAULT NULL,
  `id_penerima` int(11) DEFAULT NULL,
  `tanggal_permintaan` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `status_permintaan` enum('MENUNGGU','DISETUJUI','DITOLAK','SELESAI') DEFAULT 'MENUNGGU',
  `catatan_permintaan` text
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- --------------------------------------------------------

--
-- Struktur dari tabel `stok`
--

CREATE TABLE IF NOT EXISTS `stok` (
  `id_stok` int(11) NOT NULL,
  `id_gudang` int(11) DEFAULT NULL,
  `id_barang` int(11) DEFAULT NULL,
  `jumlah_stok` int(11) DEFAULT '0',
  `stok_minimum` int(11) DEFAULT '0',
  `tanggal_update` datetime DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- --------------------------------------------------------

--
-- Struktur dari tabel `supir`
--

CREATE TABLE IF NOT EXISTS `supir` (
  `id_supir` int(11) NOT NULL,
  `nama_supir` varchar(100) DEFAULT NULL,
  `no_hp` varchar(20) DEFAULT NULL,
  `no_kendaraan` varchar(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

SET FOREIGN_KEY_CHECKS = 1;