CREATE TABLE categories (
  id          BIGSERIAL PRIMARY KEY,
  name        VARCHAR(100) NOT NULL,
  emoji       VARCHAR(10),
  color_hex   VARCHAR(7),
  image_url   TEXT,
  active      BOOLEAN DEFAULT true,
  created_at  TIMESTAMPTZ DEFAULT NOW()
);

CREATE TABLE brands (
  id         BIGSERIAL PRIMARY KEY,
  name       VARCHAR(100) NOT NULL UNIQUE,
  logo_url   TEXT,
  created_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE TABLE shops (
  id           BIGSERIAL PRIMARY KEY,
  name         VARCHAR(200) NOT NULL,
  owner_name   VARCHAR(200),
  phone        VARCHAR(20),
  email        VARCHAR(200),
  address      TEXT,
  city         VARCHAR(100),
  bank_account VARCHAR(50),
  upi_id       VARCHAR(100),
  logo_url     TEXT,
  tagline      VARCHAR(300),
  rating       DECIMAL(3,1) DEFAULT 4.0,
  total_sales  BIGINT       DEFAULT 0,
  approved     BOOLEAN      DEFAULT true,
  created_at   TIMESTAMPTZ  DEFAULT NOW()
);

CREATE TABLE products (
  id             BIGSERIAL PRIMARY KEY,
  name           VARCHAR(300) NOT NULL,
  slug           VARCHAR(300),
  brand_id       BIGINT REFERENCES brands(id),
  category_id    BIGINT REFERENCES categories(id),
  description    TEXT,
  specifications JSONB,          -- {"RAM":"8GB","Storage":"128GB"}
  image_url      TEXT,           -- primary thumbnail
  active         BOOLEAN DEFAULT true,
  featured       BOOLEAN DEFAULT false,
  new_arrival    BOOLEAN DEFAULT false,
  top_selling    BOOLEAN DEFAULT false,
  created_at     TIMESTAMPTZ DEFAULT NOW()
);

CREATE TABLE product_images (
  id         BIGSERIAL PRIMARY KEY,
  product_id BIGINT NOT NULL REFERENCES products(id) ON DELETE CASCADE,
  image_url  TEXT NOT NULL,
  sort_order INT  DEFAULT 0
);

CREATE TABLE shop_products (
  id               BIGSERIAL PRIMARY KEY,
  shop_id          BIGINT NOT NULL REFERENCES shops(id),
  product_id       BIGINT NOT NULL REFERENCES products(id),
  mrp              DECIMAL(10,2) NOT NULL,
  selling_price    DECIMAL(10,2) NOT NULL,
  discount_percent INT     DEFAULT 0,
  stock            INT     DEFAULT 100,
  delivery_days    INT     DEFAULT 3,
  available        BOOLEAN DEFAULT true,
  created_at       TIMESTAMPTZ DEFAULT NOW(),
  UNIQUE(shop_id, product_id)
);


CREATE TABLE user_profiles (
  id         UUID PRIMARY KEY REFERENCES auth.users(id) ON DELETE CASCADE,
  name       VARCHAR(200),
  phone      VARCHAR(20),
  avatar_url TEXT,
  role       VARCHAR(20) DEFAULT 'CUSTOMER', -- CUSTOMER | SELLER | ADMIN
  created_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE TABLE addresses (
  id            BIGSERIAL PRIMARY KEY,
  user_id       UUID REFERENCES auth.users(id) ON DELETE CASCADE,
  name          VARCHAR(200) NOT NULL,
  phone         VARCHAR(20)  NOT NULL,
  address_line1 TEXT         NOT NULL,
  address_line2 TEXT,
  city          VARCHAR(100) NOT NULL,
  state         VARCHAR(100) NOT NULL,
  pincode       VARCHAR(10)  NOT NULL,
  is_default    BOOLEAN DEFAULT false,
  type          VARCHAR(20) DEFAULT 'HOME',  -- HOME | WORK | OTHER
  created_at    TIMESTAMPTZ DEFAULT NOW()
);

CREATE TABLE wishlist (
  id         BIGSERIAL PRIMARY KEY,
  user_id    UUID   REFERENCES auth.users(id) ON DELETE CASCADE,
  product_id BIGINT REFERENCES products(id)  ON DELETE CASCADE,
  created_at TIMESTAMPTZ DEFAULT NOW(),
  UNIQUE(user_id, product_id)
);

CREATE TABLE cart (
  id              BIGSERIAL PRIMARY KEY,
  user_id         UUID   REFERENCES auth.users(id)       ON DELETE CASCADE,
  shop_product_id BIGINT REFERENCES shop_products(id)    ON DELETE CASCADE,
  quantity        INT DEFAULT 1,
  created_at      TIMESTAMPTZ DEFAULT NOW(),
  UNIQUE(user_id, shop_product_id)
);

CREATE TABLE orders (
  id               BIGSERIAL PRIMARY KEY,
  order_number     VARCHAR(50) UNIQUE,
  user_id          UUID REFERENCES auth.users(id),
  total_amount     DECIMAL(10,2) NOT NULL,
  delivery_charge  DECIMAL(10,2) DEFAULT 0,
  discount_amount  DECIMAL(10,2) DEFAULT 0,
  status           VARCHAR(50)  DEFAULT 'PLACED',
  -- PLACED | CONFIRMED | SHIPPED | OUT_FOR_DELIVERY | DELIVERED | CANCELLED
  payment_method   VARCHAR(50),  -- ONLINE | COD
  payment_status   VARCHAR(50)  DEFAULT 'PENDING',
  shipping_address JSONB,
  created_at       TIMESTAMPTZ DEFAULT NOW(),
  updated_at       TIMESTAMPTZ DEFAULT NOW()
);
CREATE TABLE order_items (
  id              BIGSERIAL PRIMARY KEY,
  order_id        BIGINT REFERENCES orders(id) ON DELETE CASCADE,
  shop_product_id BIGINT REFERENCES shop_products(id),
  product_id      BIGINT REFERENCES products(id),
  shop_id         BIGINT REFERENCES shops(id),
  quantity        INT           NOT NULL,
  price           DECIMAL(10,2) NOT NULL
);
create table banners (
  id bigserial not null,
  title character varying(200) null,
  subtitle character varying(300) null,
  image_url text null,
  bg_color_hex character varying(7) null,
  redirect_type character varying(50) null,
  redirect_id bigint null,
  sort_order integer null default 0,
  active boolean null default true,
  constraint banners_pkey primary key (id)
);

CREATE INDEX idx_products_category    ON products(category_id);
CREATE INDEX idx_products_brand       ON products(brand_id);
CREATE INDEX idx_products_featured    ON products(featured) WHERE featured = true;
CREATE INDEX idx_products_new_arrival ON products(new_arrival) WHERE new_arrival = true;
CREATE INDEX idx_shop_products_product ON shop_products(product_id);
CREATE INDEX idx_shop_products_shop    ON shop_products(shop_id);
CREATE INDEX idx_product_images_product ON product_images(product_id, sort_order);
CREATE INDEX idx_cart_user             ON cart(user_id);
CREATE INDEX idx_wishlist_user         ON wishlist(user_id);
CREATE INDEX idx_orders_user           ON orders(user_id);
CREATE INDEX idx_addresses_user        ON addresses(user_id);

CREATE INDEX idx_products_fts ON products
  USING GIN (to_tsvector('english', name || ' ' || COALESCE(description, '')));

CREATE OR REPLACE FUNCTION public.handle_new_user()
RETURNS TRIGGER LANGUAGE plpgsql SECURITY DEFINER AS $$
BEGIN
  INSERT INTO public.user_profiles (id, name, phone)
  VALUES (
    NEW.id,
    NEW.raw_user_meta_data->>'name',
    NEW.raw_user_meta_data->>'phone'
  );
  RETURN NEW;
END;
$$;

CREATE TRIGGER on_auth_user_created
  AFTER INSERT ON auth.users
  FOR EACH ROW EXECUTE PROCEDURE public.handle_new_user();


-- ============================================================
-- SEED DATA — run AFTER schema
-- ============================================================

INSERT INTO categories (id, name, emoji, color_hex, image_url) VALUES
(1, 'Electronics',  '🎧', '#3B82F6', 'https://picsum.photos/seed/cat-electronics/200/200'),
(2, 'Mobiles',      '📱', '#8B5CF6', 'https://picsum.photos/seed/cat-mobiles/200/200'),
(3, 'Laptops',      '💻', '#10B981', 'https://picsum.photos/seed/cat-laptops/200/200'),
(4, 'Appliances',   '🏠', '#F59E0B', 'https://picsum.photos/seed/cat-appliances/200/200'),
(5, 'Fashion',      '👗', '#EF4444', 'https://picsum.photos/seed/cat-fashion/200/200');

SELECT setval('categories_id_seq', (SELECT MAX(id) FROM categories));

-- ── BRANDS ───────────────────────────────────────────────────
INSERT INTO brands (id, name) VALUES
(1,  'boAt'),
(2,  'JBL'),
(3,  'Sony'),
(4,  'Samsung'),
(5,  'Apple'),
(6,  'Redmi'),
(7,  'Realme'),
(8,  'Noise'),
(9,  'OnePlus'),
(10, 'Nothing'),
(11, 'POCO'),
(12, 'Vivo'),
(13, 'OPPO'),
(14, 'Dell'),
(15, 'HP'),
(16, 'Lenovo'),
(17, 'ASUS'),
(18, 'Acer'),
(19, 'MSI'),
(20, 'Google'),
(21, 'Amazon'),
(22, 'LG'),
(23, 'Whirlpool'),
(24, 'IFB'),
(25, 'Prestige'),
(26, 'Philips'),
(27, 'Kent'),
(28, 'Havells'),
(29, 'Bajaj'),
(30, 'Crompton'),
(31, 'Levi''s'),
(32, 'Puma'),
(33, 'Adidas'),
(34, 'Nike'),
(35, 'Allen Solly');

SELECT setval('brands_id_seq', (SELECT MAX(id) FROM brands));

-- ── SHOPS ────────────────────────────────────────────────────
INSERT INTO shops (id, name, owner_name, phone, city, upi_id, logo_url, tagline, rating, total_sales) VALUES
(1, 'TechZone',    'Ravi Kumar',   '9876543210', 'Chennai', 'techzone@upi',   'https://picsum.photos/seed/shop1/100/100', 'Best tech at best prices',      4.6, 12400),
(2, 'SmartMart',   'Priya Sharma', '9876543211', 'Chennai', 'smartmart@upi',  'https://picsum.photos/seed/shop2/100/100', 'Smart deals every day',         4.3,  8900),
(3, 'DealHub',     'Suresh Babu',  '9876543212', 'Chennai', 'dealhub@upi',    'https://picsum.photos/seed/shop3/100/100', 'Unbeatable deals daily',        4.1,  5600),
(4, 'MegaStore',   'Anitha Raj',   '9876543213', 'Chennai', 'megastore@upi',  'https://picsum.photos/seed/shop4/100/100', 'Everything under one roof',     4.5, 19200);

SELECT setval('shops_id_seq', (SELECT MAX(id) FROM shops));

-- ── BANNERS ──────────────────────────────────────────────────
INSERT INTO banners (title, subtitle, image_url, bg_color_hex, sort_order) VALUES
('Up to 60% Off Electronics',  'Limited time deal — grab fast!',          'https://picsum.photos/seed/banner1/800/300', '#1E3A5F', 1),
('New Mobiles Arrived!',       'Latest smartphones at the best price',     'https://picsum.photos/seed/banner2/800/300', '#4C1D95', 2),
('Fashion Sale — 50% Off',     'Top brands, unbeatable discounts',         'https://picsum.photos/seed/banner3/800/300', '#7F1D1D', 3),
('Free Delivery on ₹499+',     'Shop from multiple sellers, one delivery', 'https://picsum.photos/seed/banner4/800/300', '#064E3B', 4);


-- ── PRODUCTS ─────────────────────────────────────────────────
-- ELECTRONICS (15)
INSERT INTO products (id, name, slug, brand_id, category_id, description, specifications, image_url, featured, new_arrival, top_selling) VALUES
(1,  'boAt Airdopes 141',        'boat-airdopes-141',        1,  1, 'True wireless earbuds with 42H total playback and IPX4 water resistance.',
     '{"Driver":"8mm","Battery":"42H total","Bluetooth":"5.1","Water Resistance":"IPX4","Mic":"Yes","Weight":"4.2g each"}',
     'https://picsum.photos/seed/p-airdopes141/400/400',    true,  false, true),

(2,  'boAt Rockerz 450',         'boat-rockerz-450',         1,  1, 'On-ear wireless headphones with 15H playtime and powerful bass.',
     '{"Driver":"40mm","Battery":"15H","Bluetooth":"5.0","Mic":"Yes","Weight":"220g","Type":"On-Ear"}',
     'https://picsum.photos/seed/p-rockerz450/400/400',     true,  false, false),

(3,  'JBL Tune 760NC',           'jbl-tune-760nc',           2,  1, 'Over-ear wireless headphones with active noise cancellation and 35H battery.',
     '{"Driver":"40mm","Battery":"35H","ANC":"Yes","Bluetooth":"5.0","Foldable":"Yes","Type":"Over-Ear"}',
     'https://picsum.photos/seed/p-jbltune760/400/400',     true,  false, true),

(4,  'Sony WH-CH520',            'sony-wh-ch520',            3,  1, 'Lightweight wireless headphones with 50H battery life and multipoint connection.',
     '{"Battery":"50H","Bluetooth":"5.2","Driver":"30mm","Weight":"147g","Quick Charge":"10 min = 1.5H","Multipoint":"Yes"}',
     'https://picsum.photos/seed/p-sonywh520/400/400',      false, true,  false),

(5,  'Samsung Galaxy Buds FE',   'samsung-galaxy-buds-fe',   4,  1, 'Galaxy Buds FE with ANC, 6H battery, and Active Noise Cancellation.',
     '{"Battery":"6H + 21H case","ANC":"Yes","Driver":"11mm","Water Resistance":"IPX2","Bluetooth":"5.2"}',
     'https://picsum.photos/seed/p-galaxybudsfe/400/400',   false, true,  false),

(6,  'Apple AirPods 3rd Gen',    'apple-airpods-3rd-gen',    5,  1, 'Apple AirPods with Spatial Audio, Adaptive EQ, and MagSafe charging case.',
     '{"Battery":"30H with case","Spatial Audio":"Yes","Water Resistance":"IPX4","Bluetooth":"5.0","Quick Charge":"5 min = 1H"}',
     'https://picsum.photos/seed/p-airpods3/400/400',       true,  false, true),

(7,  'Redmi Buds 5',             'redmi-buds-5',             6,  1, 'TWS earbuds with 46dB ANC, 12.4mm driver, and 40H total battery.',
     '{"ANC":"46dB","Battery":"40H total","Driver":"12.4mm","Bluetooth":"5.3","Water Resistance":"IP54"}',
     'https://picsum.photos/seed/p-redmibuds5/400/400',     false, true,  false),

(8,  'Realme Buds Air 6',        'realme-buds-air-6',        7,  1, 'True wireless with 50dB ANC, 40H playback, and 55ms low latency.',
     '{"ANC":"50dB","Battery":"40H","Driver":"12.4mm","Bluetooth":"5.3","Water Resistance":"IP55","Latency":"55ms"}',
     'https://picsum.photos/seed/p-realmebuds6/400/400',    false, true,  false),

(9,  'Noise Buds VS104',         'noise-buds-vs104',         8,  1, 'Budget TWS with quad mic, 30H total battery, and IPX5 rating.',
     '{"Battery":"30H total","Driver":"13mm","Bluetooth":"5.3","Water Resistance":"IPX5","Mic":"Quad Mic"}',
     'https://picsum.photos/seed/p-noisebuds/400/400',      false, false, false),

(10, 'OnePlus Nord Buds 2',      'oneplus-nord-buds-2',      9,  1, 'TWS with 25dB ANC, 36H battery, and Dynaudio-tuned sound.',
     '{"Battery":"36H","Driver":"12.4mm","ANC":"25dB","Bluetooth":"5.3","Water Resistance":"IP55"}',
     'https://picsum.photos/seed/p-opnordbuds/400/400',     true,  false, false),

(11, 'boAt Stone 620',           'boat-stone-620',           1,  1, 'Portable Bluetooth speaker with 12H battery and IPX5 water resistance.',
     '{"Battery":"12H","Water Resistance":"IPX5","Bluetooth":"5.0","Output":"10W","Weight":"350g"}',
     'https://picsum.photos/seed/p-boatstone620/400/400',   false, true,  false),

(12, 'JBL Go 3',                 'jbl-go-3',                 2,  1, 'Ultra-portable IP67 waterproof & dustproof Bluetooth speaker.',
     '{"Battery":"5H","Water Resistance":"IP67","Output":"4.2W","Weight":"209g","Bluetooth":"5.1"}',
     'https://picsum.photos/seed/p-jblgo3/400/400',         false, false, false),

(13, 'Sony SRS-XB13',            'sony-srs-xb13',            3,  1, 'Compact Extra Bass speaker with 16H battery and IP67 waterproof.',
     '{"Battery":"16H","Water Resistance":"IP67","Weight":"253g","Bluetooth":"5.0","Extra Bass":"Yes"}',
     'https://picsum.photos/seed/p-sonysrs/400/400',        false, true,  false),

(14, 'Amazon Fire TV Stick 4K',  'amazon-fire-tv-stick-4k',  21, 1, 'Stream 4K Ultra HD with Dolby Vision and HDR10+ on any TV.',
     '{"Resolution":"4K Ultra HD","HDR":"Dolby Vision, HDR10+","WiFi":"Wi-Fi 6","Port":"HDMI","Voice":"Alexa built-in"}',
     'https://picsum.photos/seed/p-firetv4k/400/400',       true,  false, true),

(15, 'Google Chromecast HD',     'google-chromecast-hd',     20, 1, 'Stream your favourite shows in HD with Google TV built in.',
     '{"Resolution":"1080p HD","HDR":"Yes","WiFi":"Wi-Fi 5","Port":"HDMI","Control":"Google Home app"}',
     'https://picsum.photos/seed/p-chromecast/400/400',     false, true,  false);

-- MOBILES (10)
INSERT INTO products (id, name, slug, brand_id, category_id, description, specifications, image_url, featured, new_arrival, top_selling) VALUES
(16, 'Samsung Galaxy A35 5G',    'samsung-galaxy-a35-5g',    4,  2, '50MP OIS camera, 5000mAh battery, 6.6" Super AMOLED display.',
     '{"Display":"6.6\" Super AMOLED 120Hz","Processor":"Exynos 1380","RAM":"8GB","Storage":"128GB","Camera":"50MP+8MP+5MP","Battery":"5000mAh","5G":"Yes"}',
     'https://picsum.photos/seed/p-a35/400/400',            true,  false, true),

(17, 'Samsung Galaxy S24',       'samsung-galaxy-s24',       4,  2, 'Galaxy AI features, 50MP camera, Snapdragon 8 Gen 3 chip.',
     '{"Display":"6.2\" Dynamic AMOLED 2X 120Hz","Processor":"Snapdragon 8 Gen 3","RAM":"8GB","Storage":"256GB","Camera":"50MP+12MP+10MP","Battery":"4000mAh","5G":"Yes"}',
     'https://picsum.photos/seed/p-s24/400/400',            true,  false, true),

(18, 'iPhone 15',                'iphone-15',                5,  2, 'Dynamic Island, 48MP Main camera, USB-C, and A16 Bionic chip.',
     '{"Display":"6.1\" Super Retina XDR","Chip":"A16 Bionic","Storage":"128GB","Camera":"48MP+12MP","Battery":"3349mAh","5G":"Yes","USB":"Type-C"}',
     'https://picsum.photos/seed/p-iphone15/400/400',       true,  false, true),

(19, 'iPhone 16',                'iphone-16',                5,  2, 'A18 chip, Camera Control button, 48MP Fusion camera, USB-C.',
     '{"Display":"6.1\" Super Retina XDR","Chip":"A18","Storage":"128GB","Camera":"48MP+12MP","Battery":"3561mAh","5G":"Yes","USB":"Type-C","Camera Control":"Yes"}',
     'https://picsum.photos/seed/p-iphone16/400/400',       true,  true,  false),

(20, 'OnePlus 12',               'oneplus-12',               9,  2, 'Hasselblad camera, 100W SUPERVOOC, Snapdragon 8 Gen 3.',
     '{"Display":"6.82\" LTPO AMOLED 120Hz","Processor":"Snapdragon 8 Gen 3","RAM":"12GB","Storage":"256GB","Camera":"50MP+64MP+48MP","Battery":"5400mAh","Charging":"100W"}',
     'https://picsum.photos/seed/p-op12/400/400',           false, true,  false),

(21, 'Nothing Phone 2',          'nothing-phone-2',          10, 2, 'Glyph Interface 2.0, Snapdragon 8+ Gen 1, and unique transparent design.',
     '{"Display":"6.7\" LTPO OLED 120Hz","Processor":"Snapdragon 8+ Gen 1","RAM":"12GB","Storage":"256GB","Camera":"50MP+50MP","Battery":"4700mAh","5G":"Yes"}',
     'https://picsum.photos/seed/p-nothing2/400/400',       false, true,  false),

(22, 'Redmi Note 14 Pro',        'redmi-note-14-pro',        6,  2, '50MP OIS camera, 45W fast charging, IP68 water resistance.',
     '{"Display":"6.67\" AMOLED 120Hz","Processor":"Snapdragon 7s Gen 3","RAM":"8GB","Storage":"256GB","Camera":"200MP+8MP+2MP","Battery":"5110mAh","IP":"IP68","Charging":"45W"}',
     'https://picsum.photos/seed/p-note14pro/400/400',      false, false, true),

(23, 'POCO X7 Pro',              'poco-x7-pro',              11, 2, 'MediaTek Dimensity 8400, 6000mAh battery, 45W fast charging.',
     '{"Display":"6.67\" AMOLED 120Hz","Processor":"Dimensity 8400","RAM":"12GB","Storage":"256GB","Camera":"50MP+8MP","Battery":"6000mAh","5G":"Yes","Charging":"45W"}',
     'https://picsum.photos/seed/p-pocox7pro/400/400',      false, true,  false),

(24, 'Vivo V40',                 'vivo-v40',                 12, 2, 'ZEISS optics, 50MP front camera, 80W FlashCharge.',
     '{"Display":"6.78\" AMOLED 120Hz","Processor":"Snapdragon 7 Gen 3","RAM":"8GB","Storage":"256GB","Camera":"50MP+50MP (Front)","Battery":"5500mAh","Charging":"80W"}',
     'https://picsum.photos/seed/p-vivov40/400/400',        false, true,  false),

(25, 'OPPO Reno 13 Pro',         'oppo-reno-13-pro',         13, 2, 'Sony LYT-600 sensor, MediaTek Dimensity 8350, 3D curved OLED.',
     '{"Display":"6.83\" OLED 120Hz","Processor":"Dimensity 8350","RAM":"12GB","Storage":"256GB","Camera":"50MP Sony LYT-600","Battery":"5800mAh","Charging":"80W"}',
     'https://picsum.photos/seed/p-reno13pro/400/400',      false, false, false);

-- LAPTOPS (10)
INSERT INTO products (id, name, slug, brand_id, category_id, description, specifications, image_url, featured, new_arrival, top_selling) VALUES
(26, 'MacBook Air M3',           'macbook-air-m3',           5,  3, 'Apple M3 chip, 18H battery life, 13.6" Liquid Retina display.',
     '{"Chip":"Apple M3","RAM":"8GB Unified","Storage":"256GB SSD","Display":"13.6\" Liquid Retina","Battery":"18H","Weight":"1.24kg","OS":"macOS"}',
     'https://picsum.photos/seed/p-mbairm3/400/400',        true,  false, true),

(27, 'MacBook Pro M4',           'macbook-pro-m4',           5,  3, 'Apple M4 chip, 24H battery, ProMotion XDR display.',
     '{"Chip":"Apple M4","RAM":"16GB Unified","Storage":"512GB SSD","Display":"14.2\" ProMotion XDR","Battery":"24H","Weight":"1.55kg","OS":"macOS"}',
     'https://picsum.photos/seed/p-mbprom4/400/400',        true,  false, true),

(28, 'Dell Inspiron 15',         'dell-inspiron-15',         14, 3, 'Intel Core i5 13th Gen, 16GB RAM, 512GB SSD, FHD display.',
     '{"Processor":"Intel Core i5-1335U","RAM":"16GB DDR4","Storage":"512GB SSD","Display":"15.6\" FHD","Battery":"54Whr","OS":"Windows 11 Home"}',
     'https://picsum.photos/seed/p-dellinsp/400/400',       false, true,  false),

(29, 'HP Pavilion 14',           'hp-pavilion-14',           15, 3, 'AMD Ryzen 5, 16GB RAM, slim IPS display, backlit keyboard.',
     '{"Processor":"AMD Ryzen 5 7530U","RAM":"16GB DDR4","Storage":"512GB SSD","Display":"14\" FHD IPS","Battery":"43Whr","OS":"Windows 11 Home"}',
     'https://picsum.photos/seed/p-hppav14/400/400',        false, true,  false),

(30, 'Lenovo IdeaPad Slim 5',    'lenovo-ideapad-slim-5',    16, 3, 'Intel Core i7, 2.8K OLED display, thin & light design.',
     '{"Processor":"Intel Core i7-1355U","RAM":"16GB DDR5","Storage":"512GB SSD","Display":"14\" 2.8K OLED","Battery":"75Whr","Weight":"1.46kg","OS":"Windows 11"}',
     'https://picsum.photos/seed/p-ideapadslim5/400/400',   false, true,  false),

(31, 'ASUS Vivobook 15',         'asus-vivobook-15',         17, 3, 'Intel Core i5, 15.6" FHD display, full-size keyboard.',
     '{"Processor":"Intel Core i5-1335U","RAM":"8GB DDR4","Storage":"512GB SSD","Display":"15.6\" FHD","Battery":"42Whr","OS":"Windows 11 Home"}',
     'https://picsum.photos/seed/p-vivobook15/400/400',     false, false, false),

(32, 'Acer Aspire 7',            'acer-aspire-7',            18, 3, 'AMD Ryzen 5 + GTX 1650 gaming, 15.6" FHD 144Hz display.',
     '{"Processor":"AMD Ryzen 5 5500U","RAM":"8GB DDR4","Storage":"512GB SSD","GPU":"NVIDIA GTX 1650 4GB","Display":"15.6\" FHD 144Hz","Battery":"48Whr"}',
     'https://picsum.photos/seed/p-aceraspire7/400/400',    false, true,  false),

(33, 'MSI Modern 15',            'msi-modern-15',            19, 3, 'Intel Core i5, slim business ultrabook, 512GB SSD.',
     '{"Processor":"Intel Core i5-1155G7","RAM":"8GB DDR4","Storage":"512GB SSD","Display":"15.6\" FHD IPS","Battery":"52Whr","Weight":"1.6kg","OS":"Windows 11"}',
     'https://picsum.photos/seed/p-msimodern15/400/400',    false, false, false),

(34, 'Lenovo Legion 5 Gen 9',    'lenovo-legion-5-gen-9',    16, 3, 'AMD Ryzen 7 + RTX 4060, 165Hz gaming display.',
     '{"Processor":"AMD Ryzen 7 7745HX","RAM":"16GB DDR5","Storage":"512GB SSD","GPU":"RTX 4060 8GB","Display":"15.6\" FHD 165Hz","Battery":"80Whr"}',
     'https://picsum.photos/seed/p-legion5/400/400',        true,  false, true),

(35, 'ASUS ROG Strix G16',       'asus-rog-strix-g16',       17, 3, 'Intel Core i7 + RTX 4070, 240Hz QHD ROG Nebula display.',
     '{"Processor":"Intel Core i7-13650HX","RAM":"16GB DDR5","Storage":"1TB SSD","GPU":"RTX 4070 8GB","Display":"16\" QHD 240Hz","Battery":"90Whr"}',
     'https://picsum.photos/seed/p-rogstrix/400/400',       true,  false, true);

-- APPLIANCES (10)
INSERT INTO products (id, name, slug, brand_id, category_id, description, specifications, image_url, featured, new_arrival, top_selling) VALUES
(36, 'LG 8Kg Front Load Washer',          'lg-8kg-front-load-washer',      22, 4, 'AI Direct Drive, Steam Wash, 14 wash programs.',
     '{"Capacity":"8kg","Type":"Front Load","RPM":"1400","Energy Rating":"5 Star","Programs":"14","Steam Wash":"Yes","AI Direct Drive":"Yes"}',
     'https://picsum.photos/seed/p-lgwash/400/400',         false, true,  false),

(37, 'Samsung 324L Double Door Fridge',   'samsung-324l-double-door',      4,  4, 'Digital Inverter, Twin Cooling Plus, All-Around Cooling.',
     '{"Capacity":"324L","Type":"Double Door","Energy Rating":"2 Star","Inverter":"Digital","Cooling":"Twin Cooling Plus"}',
     'https://picsum.photos/seed/p-samsungfridge/400/400',  false, true,  false),

(38, 'Whirlpool 265L Triple Door Fridge', 'whirlpool-265l-triple-door',    23, 4, 'Intellifresh technology, Zeolite, Moisture retention.',
     '{"Capacity":"265L","Type":"Triple Door","Energy Rating":"3 Star","Inverter":"Yes","Technology":"Intellifresh + Zeolite"}',
     'https://picsum.photos/seed/p-whirlfridge/400/400',    false, false, false),

(39, 'IFB 6.5Kg Front Load Washer',       'ifb-6-5kg-front-load-washer',   24, 4, '3D Wash System, Ball Valve Technology, 1000 RPM.',
     '{"Capacity":"6.5kg","Type":"Front Load","RPM":"1000","Energy Rating":"4 Star","Programs":"15","3D Wash":"Yes"}',
     'https://picsum.photos/seed/p-ifbwash/400/400',        false, true,  false),

(40, 'Prestige PIC 20 Induction Cooktop', 'prestige-pic-20-induction',     25, 4, '1600W, 8 preset cooking menus, auto-off safety.',
     '{"Power":"1600W","Preset Menus":"8","Auto Off":"Yes","Weight":"1.2kg","Display":"LED","Voltage":"220-240V"}',
     'https://picsum.photos/seed/p-prestige/400/400',       false, false, false),

(41, 'Philips HL7756 Mixer Grinder',      'philips-hl7756-mixer-grinder',  26, 4, '750W motor, 3 jars, Turbo function, 2-year warranty.',
     '{"Motor":"750W","Jars":"3 (1.5L + 1L + 0.4L)","Speed":"3 + Turbo","Warranty":"2 Years"}',
     'https://picsum.photos/seed/p-philipsmixer/400/400',   false, true,  false),

(42, 'Kent Grand Plus RO Purifier',       'kent-grand-plus-ro',            27, 4, 'RO+UV+UF+TDS, 8L storage tank, 20 LPH output.',
     '{"Purification":"RO + UV + UF + TDS","Tank":"8L","Flow Rate":"20 LPH","TDS Control":"Yes","Minerals Retention":"Yes"}',
     'https://picsum.photos/seed/p-kentro/400/400',         false, true,  false),

(43, 'Havells 1200mm BLDC Fan',           'havells-1200mm-bldc-fan',       28, 4, 'BLDC motor, 5-star rated, remote control, 28W power.',
     '{"Sweep":"1200mm","Motor":"BLDC","Power":"28W","Energy Rating":"5 Star","Remote":"Yes","Speed Settings":"5"}',
     'https://picsum.photos/seed/p-havellsfan/400/400',     false, false, false),

(44, 'Bajaj 36L Desert Air Cooler',       'bajaj-36l-desert-air-cooler',   29, 4, '3-speed settings, honeycomb pads, ice chamber, 250 sq.ft coverage.',
     '{"Capacity":"36L","Coverage":"250 sq.ft","Speeds":"3","Pads":"Honeycomb","Ice Chamber":"Yes","Auto-Louvre":"Yes"}',
     'https://picsum.photos/seed/p-bajajcooler/400/400',    false, true,  false),

(45, 'Crompton 15L Storage Geyser',       'crompton-15l-storage-geyser',   30, 4, '5 Star, titanium glass-lined tank, anti-siphon valve.',
     '{"Capacity":"15L","Energy Rating":"5 Star","Tank":"Titanium Glass Lined","Warranty":"5 Years Tank","Anti-Siphon":"Yes"}',
     'https://picsum.photos/seed/p-cromptongeyer/400/400',  false, false, false);

-- FASHION (5)
INSERT INTO products (id, name, slug, brand_id, category_id, description, specifications, image_url, featured, new_arrival, top_selling) VALUES
(46, 'Levi''s 511 Slim Fit Jeans',        'levis-511-slim-fit-jeans',      31, 5, 'Classic slim fit, stretch denim, 5-pocket style.',
     '{"Fit":"Slim","Material":"99% Cotton 1% Elastane","Rise":"Mid-Rise","Closure":"Zip Fly","Origin":"India"}',
     'https://picsum.photos/seed/p-levis511/400/400',       false, true,  false),

(47, 'Puma Softride One4All Shoes',       'puma-softride-one4all',         32, 5, 'Lightweight running shoes with SoftFoam+ cushioning.',
     '{"Upper":"Engineered Mesh","Sole":"SoftFoam+","Closure":"Lace-Up","Occasion":"Running","Weight":"280g"}',
     'https://picsum.photos/seed/p-pumashoes/400/400',      true,  false, true),

(48, 'Adidas Ultraboost 22',              'adidas-ultraboost-22',          33, 5, 'Boost midsole, Primeknit+ upper, Continental rubber outsole.',
     '{"Upper":"Primeknit+","Sole":"Boost + Continental Rubber","Closure":"Lace-Up","Occasion":"Running","Weight":"310g"}',
     'https://picsum.photos/seed/p-ultraboost/400/400',     true,  false, false),

(49, 'Nike Dri-FIT Training T-Shirt',     'nike-dri-fit-training-tshirt',  34, 5, 'Sweat-wicking Dri-FIT fabric, crew neck, relaxed fit.',
     '{"Material":"100% Polyester","Technology":"Dri-FIT","Fit":"Regular","Neck":"Crew","Care":"Machine Wash"}',
     'https://picsum.photos/seed/p-nikeshirt/400/400',      false, true,  false),

(50, 'Allen Solly Slim Fit Formal Shirt', 'allen-solly-slim-formal-shirt', 35, 5, '100% Cotton, slim fit, easy-iron finish, point collar.',
     '{"Material":"100% Cotton","Fit":"Slim","Collar":"Point","Occasion":"Formal","Finish":"Easy Iron"}',
     'https://picsum.photos/seed/p-allensolly/400/400',     false, true,  false);

SELECT setval('products_id_seq', (SELECT MAX(id) FROM products));


-- ── PRODUCT IMAGES (extra gallery images) ────────────────────
INSERT INTO product_images (product_id, image_url, sort_order) VALUES
-- boAt Airdopes 141
(1, 'https://picsum.photos/seed/p-airdopes141-2/400/400', 1),
(1, 'https://picsum.photos/seed/p-airdopes141-3/400/400', 2),
-- JBL Tune 760NC
(3, 'https://picsum.photos/seed/p-jbltune760-2/400/400', 1),
(3, 'https://picsum.photos/seed/p-jbltune760-3/400/400', 2),
-- Apple AirPods 3
(6, 'https://picsum.photos/seed/p-airpods3-2/400/400', 1),
(6, 'https://picsum.photos/seed/p-airpods3-3/400/400', 2),
-- Samsung A35
(16, 'https://picsum.photos/seed/p-a35-2/400/400', 1),
(16, 'https://picsum.photos/seed/p-a35-3/400/400', 2),
-- iPhone 16
(19, 'https://picsum.photos/seed/p-iphone16-2/400/400', 1),
(19, 'https://picsum.photos/seed/p-iphone16-3/400/400', 2),
-- MacBook Air M3
(26, 'https://picsum.photos/seed/p-mbairm3-2/400/400', 1),
(26, 'https://picsum.photos/seed/p-mbairm3-3/400/400', 2),
-- Lenovo Legion 5
(34, 'https://picsum.photos/seed/p-legion5-2/400/400', 1),
(34, 'https://picsum.photos/seed/p-legion5-3/400/400', 2),
-- Puma Shoes
(47, 'https://picsum.photos/seed/p-pumashoes-2/400/400', 1),
(47, 'https://picsum.photos/seed/p-pumashoes-3/400/400', 2);

-- ── SHOP_PRODUCTS (multi-vendor pricing) ─────────────────────
-- Format: (shop_id, product_id, mrp, selling_price, discount_percent, stock, delivery_days)
INSERT INTO shop_products (shop_id, product_id, mrp, selling_price, discount_percent, stock, delivery_days) VALUES
-- ── Electronics ──
-- boAt Airdopes 141
(1, 1,  1299,  999, 23, 80, 2),
(2, 1,  1299, 1050, 19, 60, 3),
(3, 1,  1299,  949, 27, 40, 4),
(4, 1,  1299,  980, 25, 90, 1),
-- boAt Rockerz 450
(1, 2,  1999, 1499, 25, 70, 2),
(2, 2,  1999, 1599, 20, 50, 3),
(4, 2,  1999, 1449, 27, 80, 1),
-- JBL Tune 760NC
(1, 3,  5999, 4499, 25, 55, 2),
(2, 3,  5999, 4699, 22, 40, 3),
(3, 3,  5999, 4350, 27, 30, 4),
-- Sony WH-CH520
(1, 4,  4990, 3499, 30, 65, 3),
(4, 4,  4990, 3650, 27, 70, 2),
-- Samsung Galaxy Buds FE
(1, 5,  4999, 3299, 34, 80, 2),
(2, 5,  4999, 3499, 30, 55, 3),
(4, 5,  4999, 3199, 36, 90, 1),
-- Apple AirPods 3
(1, 6,  19900, 16999, 15, 40, 2),
(2, 6,  19900, 17500, 12, 35, 3),
-- Redmi Buds 5
(1, 7,  1999, 1299, 35, 100, 2),
(3, 7,  1999, 1249, 37,  80, 3),
(4, 7,  1999, 1199, 40, 120, 1),
-- Realme Buds Air 6
(1, 8,  2299, 1699, 26, 75, 2),
(2, 8,  2299, 1799, 22, 60, 3),
-- Noise Buds VS104
(1, 9,  1499,  999, 33, 90, 3),
(3, 9,  1499,  949, 37, 70, 4),
-- OnePlus Nord Buds 2
(1, 10, 2299, 1799, 22, 85, 2),
(4, 10, 2299, 1749, 24, 90, 1),
-- boAt Stone 620
(1, 11, 1999, 1299, 35, 60, 3),
(2, 11, 1999, 1399, 30, 45, 2),
-- JBL Go 3
(1, 12, 3999, 2799, 30, 55, 2),
(3, 12, 3999, 2699, 33, 40, 3),
-- Sony SRS-XB13
(1, 13, 4990, 3499, 30, 50, 2),
(4, 13, 4990, 3299, 34, 60, 3),
-- Amazon Fire TV Stick 4K
(1, 14, 5999, 3999, 33, 80, 2),
(2, 14, 5999, 4199, 30, 60, 2),
(4, 14, 5999, 3799, 37, 90, 1),
-- Google Chromecast HD
(1, 15, 6999, 4999, 28, 45, 2),
(2, 15, 6999, 5199, 26, 35, 3),
-- ── Mobiles ──
-- Samsung Galaxy A35
(1, 16, 29999, 24999, 17, 50, 2),
(2, 16, 29999, 25999, 13, 40, 3),
(4, 16, 29999, 24499, 18, 60, 1),
-- Samsung S24
(1, 17, 74999, 64999, 13, 30, 2),
(2, 17, 74999, 66999, 11, 25, 3),
-- iPhone 15
(1, 18, 79900, 72999,  9, 25, 2),
(2, 18, 79900, 74999,  6, 20, 3),
(4, 18, 79900, 71999, 10, 30, 1),
-- iPhone 16
(1, 19, 89900, 82999,  8, 20, 2),
(4, 19, 89900, 80999, 10, 25, 1),
-- OnePlus 12
(1, 20, 64999, 54999, 15, 35, 2),
(3, 20, 64999, 53999, 17, 28, 3),
-- Nothing Phone 2
(1, 21, 44999, 37999, 16, 40, 2),
(2, 21, 44999, 38999, 13, 32, 3),
-- Redmi Note 14 Pro
(1, 22, 27999, 22999, 18, 55, 2),
(3, 22, 27999, 21999, 21, 45, 3),
(4, 22, 27999, 22499, 20, 65, 1),
-- POCO X7 Pro
(1, 23, 24999, 20999, 16, 60, 2),
(2, 23, 24999, 21999, 12, 50, 3),
-- Vivo V40
(1, 24, 42999, 37999, 12, 38, 2),
(4, 24, 42999, 36999, 14, 42, 1),
-- OPPO Reno 13 Pro
(1, 25, 47999, 41999, 12, 35, 2),
(3, 25, 47999, 40999, 15, 28, 3),
-- ── Laptops ──
-- MacBook Air M3
(1, 26, 114900, 109900, 4, 20, 3),
(4, 26, 114900, 107900, 6, 15, 2),
-- MacBook Pro M4
(1, 27, 169900, 162900, 4, 15, 3),
(4, 27, 169900, 159900, 6, 12, 2),
-- Dell Inspiron 15
(1, 28, 70990, 55999, 21, 30, 3),
(2, 28, 70990, 57999, 18, 25, 4),
(3, 28, 70990, 54999, 22, 20, 3),
-- HP Pavilion 14
(1, 29, 72990, 57999, 21, 28, 3),
(2, 29, 72990, 59999, 18, 22, 4),
-- Lenovo IdeaPad Slim 5
(1, 30, 87990, 69999, 20, 22, 3),
(4, 30, 87990, 67999, 23, 18, 2),
-- ASUS Vivobook 15
(1, 31, 54990, 42999, 22, 32, 3),
(3, 31, 54990, 41999, 24, 25, 4),
-- Acer Aspire 7
(1, 32, 67990, 52999, 22, 28, 3),
(2, 32, 67990, 54999, 19, 22, 4),
-- MSI Modern 15
(1, 33, 61990, 48999, 21, 25, 3),
(3, 33, 61990, 47999, 23, 18, 4),
-- Lenovo Legion 5
(1, 34, 99990, 82999, 17, 18, 2),
(4, 34, 99990, 80999, 19, 15, 1),
-- ASUS ROG Strix G16
(1, 35, 129990, 109999, 15, 12, 2),
(2, 35, 129990, 112999, 13, 10, 3),
-- ── Appliances ──
-- LG Washing Machine
(1, 36, 67990, 54999, 19, 15, 4),
(4, 36, 67990, 52999, 22, 12, 3),
-- Samsung Fridge
(1, 37, 37990, 31999, 16, 18, 5),
(2, 37, 37990, 33999, 11, 14, 6),
-- Whirlpool Fridge
(1, 38, 32990, 27999, 15, 16, 5),
(3, 38, 32990, 26999, 18, 12, 6),
-- IFB Washing Machine
(1, 39, 42990, 35999, 16, 14, 4),
(4, 39, 42990, 34999, 19, 10, 3),
-- Prestige Induction
(1, 40, 2195, 1699, 23, 50, 3),
(2, 40, 2195, 1799, 18, 40, 4),
-- Philips Mixer
(1, 41, 3995, 2999, 25, 45, 3),
(3, 41, 3995, 2849, 29, 35, 4),
-- Kent RO
(1, 42, 16990, 13999, 18, 20, 4),
(4, 42, 16990, 13499, 20, 16, 3),
-- Havells Fan
(1, 43, 4590, 3499, 24, 38, 3),
(2, 43, 4590, 3699, 19, 30, 4),
-- Bajaj Air Cooler
(1, 44, 11990, 9499, 21, 22, 4),
(3, 44, 11990, 8999, 25, 18, 5),
-- Crompton Geyser
(1, 45, 7490, 5999, 20, 25, 4),
(4, 45, 7490, 5799, 23, 20, 3),
-- ── Fashion ──
-- Levi's Jeans
(1, 46, 3999, 2799, 30, 40, 3),
(2, 46, 3999, 2999, 25, 32, 4),
(4, 46, 3999, 2699, 33, 50, 2),
-- Puma Shoes
(1, 47, 5999, 4199, 30, 35, 3),
(3, 47, 5999, 3999, 33, 28, 4),
(4, 47, 5999, 3899, 35, 40, 2),
-- Adidas Ultraboost
(1, 48, 14999, 10999, 27, 22, 3),
(2, 48, 14999, 11499, 23, 18, 4),
-- Nike T-Shirt
(1, 49, 2995, 1999, 33, 55, 3),
(3, 49, 2995, 1849, 38, 44, 4),
-- Allen Solly Shirt
(1, 50, 2999, 2199, 27, 48, 3),
(2, 50, 2999, 2299, 23, 38, 4);


SELECT
  (SELECT COUNT(*) FROM categories)    AS categories,
  (SELECT COUNT(*) FROM brands)        AS brands,
  (SELECT COUNT(*) FROM shops)         AS shops,
  (SELECT COUNT(*) FROM products)      AS products,
  (SELECT COUNT(*) FROM shop_products) AS shop_products,
  (SELECT COUNT(*) FROM banners)       AS banners;

DROP TRIGGER IF EXISTS on_auth_user_created ON auth.users;
DROP FUNCTION IF EXISTS public.handle_new_user();

-- Drop dependent tables (auth.users FK)
DROP TABLE IF EXISTS order_items;
DROP TABLE IF EXISTS orders;
DROP TABLE IF EXISTS cart;
DROP TABLE IF EXISTS wishlist;
DROP TABLE IF EXISTS addresses;
DROP TABLE IF EXISTS user_profiles;

CREATE TABLE users (
  id         BIGSERIAL PRIMARY KEY,
  name       VARCHAR(200) NOT NULL,
  email      VARCHAR(200) NOT NULL UNIQUE,
  phone      VARCHAR(20),
  password   VARCHAR(255) NOT NULL,
  role       VARCHAR(20) DEFAULT 'CUSTOMER',
  avatar_url TEXT,
  created_at TIMESTAMPTZ DEFAULT NOW()
);

-- Recreate dependent tables with FK → users
CREATE TABLE addresses (
  id            BIGSERIAL PRIMARY KEY,
  user_id       BIGINT REFERENCES users(id) ON DELETE CASCADE,
  name          VARCHAR(200) NOT NULL,
  phone         VARCHAR(20)  NOT NULL,
  address_line1 TEXT NOT NULL,
  address_line2 TEXT,
  city          VARCHAR(100) NOT NULL,
  state         VARCHAR(100) NOT NULL,
  pincode       VARCHAR(10)  NOT NULL,
  is_default    BOOLEAN DEFAULT false,
  type          VARCHAR(20)  DEFAULT 'HOME',
  created_at    TIMESTAMPTZ  DEFAULT NOW()
);

CREATE TABLE wishlist (
  id         BIGSERIAL PRIMARY KEY,
  user_id    BIGINT REFERENCES users(id)     ON DELETE CASCADE,
  product_id BIGINT REFERENCES products(id)  ON DELETE CASCADE,
  created_at TIMESTAMPTZ DEFAULT NOW(),
  UNIQUE(user_id, product_id)
);

CREATE TABLE cart (
  id              BIGSERIAL PRIMARY KEY,
  user_id         BIGINT REFERENCES users(id)        ON DELETE CASCADE,
  shop_product_id BIGINT REFERENCES shop_products(id) ON DELETE CASCADE,
  quantity        INT DEFAULT 1,
  created_at      TIMESTAMPTZ DEFAULT NOW(),
  UNIQUE(user_id, shop_product_id)
);

CREATE TABLE orders (
  id               BIGSERIAL PRIMARY KEY,
  order_number     VARCHAR(50) UNIQUE,
  user_id          BIGINT REFERENCES users(id),
  total_amount     DECIMAL(10,2) NOT NULL,
  delivery_charge  DECIMAL(10,2) DEFAULT 0,
  discount_amount  DECIMAL(10,2) DEFAULT 0,
  status           VARCHAR(50)   DEFAULT 'PLACED',
  payment_method   VARCHAR(50),
  payment_status   VARCHAR(50)   DEFAULT 'PENDING',
  shipping_address JSONB,
  created_at       TIMESTAMPTZ   DEFAULT NOW(),
  updated_at       TIMESTAMPTZ   DEFAULT NOW()
);

CREATE TABLE order_items (
  id              BIGSERIAL PRIMARY KEY,
  order_id        BIGINT REFERENCES orders(id)       ON DELETE CASCADE,
  shop_product_id BIGINT REFERENCES shop_products(id),
  product_id      BIGINT REFERENCES products(id),
  shop_id         BIGINT REFERENCES shops(id),
  quantity        INT           NOT NULL,
  price           DECIMAL(10,2) NOT NULL
);

CREATE INDEX idx_users_email    ON users(email);
CREATE INDEX idx_cart_user      ON cart(user_id);
CREATE INDEX idx_wishlist_user  ON wishlist(user_id);
CREATE INDEX idx_orders_user    ON orders(user_id);
CREATE INDEX idx_addresses_user ON addresses(user_id);

-- ============================================================
-- Phase 2 — Reviews & product rating columns
-- Run this block in Supabase after the schema above
-- ============================================================

-- Add rating summary columns to products for fast list display
ALTER TABLE products
  ADD COLUMN IF NOT EXISTS rating_avg   DECIMAL(3,2) DEFAULT 0,
  ADD COLUMN IF NOT EXISTS review_count INT          DEFAULT 0;

-- Reviews table (one review per user per product)
CREATE TABLE IF NOT EXISTS reviews (
  id                BIGSERIAL    PRIMARY KEY,
  product_id        BIGINT       NOT NULL REFERENCES products(id)    ON DELETE CASCADE,
  user_id           BIGINT       NOT NULL REFERENCES users(id)       ON DELETE CASCADE,
  shop_product_id   BIGINT       REFERENCES shop_products(id)        ON DELETE SET NULL,
  rating            SMALLINT     NOT NULL CHECK (rating BETWEEN 1 AND 5),
  title             VARCHAR(200),
  body              TEXT,
  verified_purchase BOOLEAN      DEFAULT false,
  helpful_count     INT          DEFAULT 0,
  created_at        TIMESTAMPTZ  DEFAULT NOW(),
  UNIQUE(user_id, product_id)
);

CREATE INDEX IF NOT EXISTS idx_reviews_product ON reviews(product_id, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_reviews_user    ON reviews(user_id);
CREATE INDEX IF NOT EXISTS idx_reviews_rating  ON reviews(product_id, rating);

-- Trigger: keep rating_avg + review_count in sync automatically
CREATE OR REPLACE FUNCTION update_product_rating()
RETURNS TRIGGER LANGUAGE plpgsql AS $$
BEGIN
  UPDATE products
  SET rating_avg   = (SELECT COALESCE(AVG(rating), 0) FROM reviews WHERE product_id = COALESCE(NEW.product_id, OLD.product_id)),
      review_count = (SELECT COUNT(*)                  FROM reviews WHERE product_id = COALESCE(NEW.product_id, OLD.product_id))
  WHERE id = COALESCE(NEW.product_id, OLD.product_id);
  RETURN NEW;
END;
$$;

DROP TRIGGER IF EXISTS trg_update_product_rating ON reviews;
CREATE TRIGGER trg_update_product_rating
  AFTER INSERT OR UPDATE OR DELETE ON reviews
  FOR EACH ROW EXECUTE FUNCTION update_product_rating();

-- ============================================================
-- Phase 3 — KMR Official Store
-- Run this block in Supabase after Phase 2
-- ============================================================

-- Mark shops that are owned by the platform itself
ALTER TABLE shops ADD COLUMN IF NOT EXISTS is_official BOOLEAN DEFAULT false;

-- Insert KMR Store as the platform's own shop (id = 5)
INSERT INTO shops (id, name, owner_name, city, logo_url, tagline, rating, total_sales, is_official, approved)
VALUES (5, 'KMR Store', 'KMR Marketplace', 'Chennai',
        'https://picsum.photos/seed/shop-kmr/100/100',
        'KMR Official Marketplace Store', 5.0, 100000, true, true)
ON CONFLICT (id) DO UPDATE
  SET name = EXCLUDED.name, is_official = EXCLUDED.is_official,
      tagline = EXCLUDED.tagline, rating = EXCLUDED.rating;

SELECT setval('shops_id_seq', (SELECT MAX(id) FROM shops));

-- KMR Store pricing — competitive with guaranteed 1-day delivery
INSERT INTO shop_products (shop_id, product_id, mrp, selling_price, discount_percent, stock, delivery_days, available) VALUES
-- Electronics
(5,  1, 1299,   929, 29, 500, 1, true),   -- boAt Airdopes 141
(5,  3, 5999,  4299, 28, 200, 1, true),   -- JBL Tune 760NC
(5,  6, 19900, 16799, 16, 100, 1, true),  -- Apple AirPods 3
(5, 10, 2299,  1699, 26, 300, 1, true),   -- OnePlus Nord Buds 2
(5, 14, 5999,  3699, 38, 300, 1, true),   -- Amazon Fire TV Stick 4K
-- Mobiles
(5, 16, 29999, 24299, 19, 200, 1, true),  -- Samsung Galaxy A35 5G
(5, 17, 74999, 63999, 15,  80, 1, true),  -- Samsung Galaxy S24
(5, 18, 79900, 71499, 11,  60, 1, true),  -- iPhone 15
(5, 19, 89900, 80299, 11,  50, 1, true),  -- iPhone 16
(5, 22, 27999, 21499, 23, 150, 1, true),  -- Redmi Note 14 Pro
(5, 23, 24999, 20499, 18, 120, 1, true),  -- POCO X7 Pro
-- Laptops
(5, 26, 114900, 106999, 7,  60, 1, true), -- MacBook Air M3
(5, 28,  70990, 53999, 24,  80, 1, true), -- Dell Inspiron 15
(5, 34,  99990, 80499, 20,  40, 1, true), -- Lenovo Legion 5
(5, 35, 129990, 108999, 16, 30, 1, true), -- ASUS ROG Strix G16
-- Fashion
(5, 47, 5999,  3799, 37, 200, 1, true),   -- Puma Shoes
(5, 48, 14999, 10799, 28, 100, 1, true)   -- Adidas Ultraboost
ON CONFLICT (shop_id, product_id) DO NOTHING;

-- rest


-- ============================================================
-- Phase 4 — Orders & Payments (checkout spine)
-- Applied to Supabase 2026-07-26
-- ============================================================
ALTER TABLE orders      ADD COLUMN IF NOT EXISTS razorpay_order_id   VARCHAR(80);
ALTER TABLE orders      ADD COLUMN IF NOT EXISTS razorpay_payment_id VARCHAR(80);
ALTER TABLE order_items ADD COLUMN IF NOT EXISTS status VARCHAR(40) DEFAULT 'PLACED';
CREATE INDEX IF NOT EXISTS idx_order_items_order ON order_items(order_id);
CREATE INDEX IF NOT EXISTS idx_order_items_shop  ON order_items(shop_id);
CREATE INDEX IF NOT EXISTS idx_orders_status     ON orders(status);
