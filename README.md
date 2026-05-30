# SyariahPulse

**AI-Powered Syariah Stock Screener for Indonesia**

Platform screening saham syariah Indonesia (ISSI) yang secara otomatis mengidentifikasi saham terbaik di bawah IDR 300 setiap malam untuk keperluan trading, swing trading, dan momentum trading.

---

## Fitur Utama

- **Scoring Engine** — 5 aturan teknikal dengan skor maksimal 100
- **Top 10 Picks** — ranking otomatis saham syariah terbaik setiap hari
- **Indikator Teknikal** — RSI-14, EMA-20, EMA-50, Average Volume-20 (pure Java, tanpa TA-Lib)
- **Nightly Batch** — proses otomatis setiap 18:00 WIB Senin–Jumat
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

Mengembalikan Top 10 saham syariah dengan skor tertinggi hari ini.

```bash
curl http://localhost:8080/api/top-picks
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

## Roadmap

- [ ] Integrasi data harga real-time dari IDX / Yahoo Finance
- [ ] Seed data resmi ISSI dari OJK/BEI
- [ ] Cache Redis untuk response API
- [ ] Notifikasi Telegram Bot setiap malam
- [ ] Modul AI prediksi harga (XGBoost, LSTM)
- [ ] Dashboard web monitoring Top 10

---

## Lisensi

MIT License
