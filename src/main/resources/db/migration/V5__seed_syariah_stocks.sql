-- Snapshot of well-known ISSI / Daftar Efek Syariah (DES) constituents.
-- OJK republishes DES twice a year (effective ~1 Jun and ~1 Dec); review this
-- list against https://ojk.go.id/id/kanal/syariah/data-dan-statistik/daftar-efek-syariah
-- each cycle and add a new migration (e.g. V6__update_syariah_stocks.sql) rather
-- than editing this file.
INSERT INTO stock (symbol, company_name, sector, is_syariah) VALUES
    ('DILD', 'Intiland Development Tbk', 'Property', TRUE),
    ('BSDE', 'Bumi Serpong Damai Tbk', 'Property', TRUE),
    ('CPIN', 'Charoen Pokphand Indonesia Tbk', 'Food', TRUE),
    ('TLKM', 'Telekomunikasi Indonesia Tbk', 'Telecom', TRUE),
    ('ANTM', 'Aneka Tambang Tbk', 'Mining', TRUE),
    ('ICBP', 'Indofood CBP Sukses Makmur Tbk', 'Food', TRUE),
    ('UNVR', 'Unilever Indonesia Tbk', 'Consumer Goods', TRUE),
    ('KLBF', 'Kalbe Farma Tbk', 'Pharmaceuticals', TRUE),
    ('PTBA', 'Bukit Asam Tbk', 'Mining', TRUE),
    ('ADRO', 'Adaro Energy Indonesia Tbk', 'Energy', TRUE),
    ('SMGR', 'Semen Indonesia Tbk', 'Basic Materials', TRUE),
    ('PGAS', 'Perusahaan Gas Negara Tbk', 'Energy', TRUE),
    ('JPFA', 'Japfa Comfeed Indonesia Tbk', 'Food', TRUE),
    ('WIKA', 'Wijaya Karya Tbk', 'Construction', TRUE),
    ('INCO', 'Vale Indonesia Tbk', 'Mining', TRUE),
    ('AKRA', 'AKR Corporindo Tbk', 'Trade', TRUE)
ON CONFLICT (symbol) DO NOTHING;
