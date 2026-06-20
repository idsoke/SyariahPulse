# SyariahPulse

**AI-Powered Syariah Stock Screener for Indonesia**

Platform screening saham syariah Indonesia (ISSI) yang secara otomatis mengidentifikasi saham terbaik di bawah IDR 300 setiap malam untuk keperluan trading, swing trading, dan momentum trading.

---

## Fitur Utama

- **Scoring Engine** — 5 aturan teknikal dengan skor maksimal 100
- **Top 10 Picks** — ranking otomatis saham syariah terbaik setiap hari
- **Volume Spike Screener** — deteksi saham dengan volume hari ini jauh di atas rata-rata 20 hari
- **Indikator Teknikal** — RSI-14, EMA-20, EMA-50, Average Volume-20 (pure Java, tanpa TA-Lib)
- **Import Data Otomatis** — OHLCV harian diambil dari Yahoo Finance (`.JK`), dengan backfill histori 6 bulan saat data sebuah saham masih kosong
- **Nightly Batch** — proses otomatis setiap 18:00 WIB Senin–Jumat (timezone `Asia/Jakarta`), bisa juga dipicu manual lewat endpoint admin
- **REST API** — endpoint siap pakai untuk integrasi frontend atau bot

---

## Tech Stack

| Layer | Teknologi |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 3.5 |
| Database | PostgreSQL 16 |
| Migration | Flyway |
| ORM | Spring Data JPA |
| Build | Maven |
| Container | Docker, Docker Compose |
| Testing | JUnit 5, Mockito, Testcontainers |

---

## Scoring Rules

| Rule | Kondisi | Poin |
|---|---|---|
| 1 | Harga < IDR 300 | +20 |
| 2 | Volume > 2× Rata-rata Volume 20 hari | +30 |
| 3 | RSI antara 40–70 | +20 |
| 4 | Close > EMA 20 | +15 |
| 5 | EMA 20 > EMA 50 | +15 |

---

## Struktur Project

```
com.syariahpulse
├── common          # Scheduler nightly batch
├── stock           # Domain saham & harga harian
│   ├── domain
│   ├── application
│   ├── infrastructure
│   └── presentation
├── indicator       # Kalkulasi indikator teknikal
├── scoring         # Scoring engine
└── ranking         # Ranking Top 10
```

---

## Menjalankan Project

### Prasyarat

- Java 21
- Maven 3.9+
- Docker & Docker Compose

### Cara 1 — Docker Compose (Recommended)

```bash
mvn clean package -DskipTests
docker compose up --build
```

### Cara 2 — Lokal

```bash
# Jalankan PostgreSQL
docker compose up postgres -d

# Jalankan aplikasi
mvn spring-boot:run
```

Aplikasi berjalan di `http://localhost:8080`

---

## API Endpoints

### GET /api/top-picks

Mengembalikan saham syariah dengan skor tertinggi hari ini.

| Query Param | Default | Keterangan |
|---|---|---|
| `limit` | `10` | Jumlah maksimal saham yang dikembalikan |
| `minScore` | `0` | Skor minimum saham yang ditampilkan |

```bash
curl "http://localhost:8080/api/top-picks?limit=5&minScore=50"
```

```json
[
  {
    "symbol": "DILD",
    "score": 100,
    "price": 200
  }
]
```

### GET /api/volume-spikes

Mengembalikan saham syariah dengan volume hari ini paling tidak `minRatio` kali rata-rata volume 20 hari, diurutkan dari rasio tertinggi.

| Query Param | Default | Keterangan |
|---|---|---|
| `limit` | `10` | Jumlah maksimal saham yang dikembalikan |
| `minRatio` | `2.0` | Rasio minimum volume hari ini terhadap rata-rata 20 hari |

```bash
curl "http://localhost:8080/api/volume-spikes?limit=5&minRatio=3"
```

```json
[
  {
    "symbol": "ANTM",
    "companyName": "Aneka Tambang Tbk",
    "volume": 45000000,
    "avgVolume20": 12000000,
    "volumeRatio": 3.75,
    "price": 1850
  }
]
```

### POST /api/admin/batch/run

Memicu pipeline nightly batch (import → indikator → scoring → ranking) secara manual, di luar jadwal 18:00 WIB. Berguna untuk verifikasi data tanpa menunggu jadwal. **Belum ada autentikasi** — jangan diekspos ke publik tanpa menambahkan access control.

```bash
curl -X POST http://localhost:8080/api/admin/batch/run
```

### GET /api/stocks

Mengembalikan daftar semua saham syariah yang terdaftar.

```bash
curl http://localhost:8080/api/stocks
```

```json
[
  {
    "symbol": "DILD",
    "companyName": "PT Intiland Development Tbk",
    "sector": "Properti"
  }
]
```

### GET /api/stocks/{symbol}

Detail saham beserta alasan scoring.

```bash
curl http://localhost:8080/api/stocks/DILD
```

```json
{
  "symbol": "DILD",
  "price": 200,
  "score": 100,
  "reasons": [
    "Price Below IDR 300",
    "Volume Spike",
    "Healthy RSI",
    "Above EMA20",
    "Bullish Trend EMA20>EMA50"
  ]
}
```

### GET /actuator/health

```bash
curl http://localhost:8080/actuator/health
```

---

## Environment Variables

| Variable | Default | Keterangan |
|---|---|---|
| `POSTGRES_HOST` | `localhost` | Host database |
| `POSTGRES_PORT` | `5432` | Port database |
| `POSTGRES_DB` | `syariahpulse` | Nama database |
| `POSTGRES_USER` | `syariahpulse` | Username database |
| `POSTGRES_PASSWORD` | `syariahpulse` | Password database |

---

## Menjalankan Tests

```bash
mvn clean verify
```

- **Unit Tests** — RSI, EMA, Scoring Engine, Ranking Service
- **Integration Tests** — Testcontainers PostgreSQL (membutuhkan Docker)

---

## Sumber Data

Harga harian (OHLCV) diambil dari endpoint chart Yahoo Finance untuk simbol `.JK` (`YahooFinanceClient`). Ini API publik tidak resmi — tanpa API key, tapi juga tanpa SLA/garansi ketersediaan. Daftar saham syariah saat ini adalah snapshot manual ~16 saham besar yang dikenal masuk Daftar Efek Syariah (lihat `V5__seed_syariah_stocks.sql`), bukan daftar resmi lengkap dari OJK/IDX (yang hanya tersedia dalam format PDF tanpa API/CSV terstruktur).

## Roadmap

- [x] Integrasi data harga harian dari Yahoo Finance (`.JK`)
- [x] Volume Spike Screener
- [x] Trigger batch manual (admin endpoint)
- [ ] Seed data resmi & lengkap ISSI dari OJK/BEI (saat ini masih snapshot manual ~16 saham)
- [ ] Autentikasi untuk endpoint admin
- [ ] Cache Redis untuk response API
- [ ] Notifikasi Telegram Bot setiap malam
- [ ] Modul AI prediksi harga (XGBoost, LSTM)
- [ ] Dashboard web monitoring Top 10

---

## Lisensi

MIT License
