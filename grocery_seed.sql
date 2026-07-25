-- ============================================================
-- GROCERY SEED — Prime Spot Grocery
-- Run this AFTER the main schema + seed data already exist.
-- Safe to run multiple times (uses explicit IDs).
-- ============================================================

-- ── 1. CATEGORIES (Zepto-style granular grocery splits) ──────
--
-- Zepto splits dairy / staples / beverages more granularly than
-- a single "Grocery" bucket so the user can drill straight into
-- the aisle they want. We mirror that with 6 focused categories.
--
-- Existing IDs 1–5 are Electronics/Mobiles/Laptops/Appliances/Fashion.
-- Grocery categories start at 6.

INSERT INTO categories (id, name, emoji, color_hex, image_url) VALUES
(6,  'Dairy & Eggs',          '🥛', '#2563EB',
     'https://picsum.photos/seed/cat-dairy/200/200'),
(7,  'Atta, Rice & Staples',  '🌾', '#D97706',
     'https://picsum.photos/seed/cat-staples/200/200'),
(8,  'Beverages',             '🥤', '#DC2626',
     'https://picsum.photos/seed/cat-beverages/200/200'),
(9,  'Snacks & Munchies',     '🍿', '#EA580C',
     'https://picsum.photos/seed/cat-snacks/200/200'),
(10, 'Cleaning Essentials',   '🧹', '#059669',
     'https://picsum.photos/seed/cat-cleaning/200/200'),
(11, 'Personal Care',         '🧴', '#0891B2',
     'https://picsum.photos/seed/cat-personalcare/200/200')
ON CONFLICT (id) DO NOTHING;

SELECT setval('categories_id_seq', (SELECT MAX(id) FROM categories));


-- ── 2. BRANDS ────────────────────────────────────────────────
--
-- Consumer-facing brand names — the same ones customers type in search.
-- Tata covers both Tata Salt and Tata Tea (same parent brand).
-- Lay's & Kurkure are kept separate even though both are PepsiCo,
-- because customers search and filter by product-brand name.
--
-- Existing IDs 1–35 are tech brands. Grocery brands start at 36.

INSERT INTO brands (id, name) VALUES
(36, 'Amul'),
(37, 'Nestlé'),
(38, 'India Gate'),
(39, 'Aashirvaad'),
(40, 'Tata'),
(41, 'Fortune'),
(42, 'Coca-Cola'),
(43, 'Tropicana'),
(44, 'Bisleri'),
(45, 'Lay''s'),
(46, 'Cadbury'),
(47, 'Parle'),
(48, 'Kurkure'),
(49, 'Vim'),
(50, 'Surf Excel'),
(51, 'Harpic'),
(52, 'Dettol')
ON CONFLICT (id) DO NOTHING;

SELECT setval('brands_id_seq', (SELECT MAX(id) FROM brands));


-- ── 3. SHOP — Prime Spot Grocery ─────────────────────────────
--
-- New grocery shop (ID 6). Existing shops 1–5 are electronics/general.
-- is_official defaults to false (added in Phase 3 ALTER).

INSERT INTO shops (id, name, owner_name, phone, city, upi_id,
                   logo_url, tagline, rating, total_sales, approved)
VALUES (6, 'Prime Spot Grocery', 'Senthil Kumar', '9876543220',
        'Chennai', 'primegrocery@upi',
        'https://picsum.photos/seed/shop-grocery/100/100',
        'Fresh groceries delivered in minutes', 4.7, 32000, true)
ON CONFLICT (id) DO NOTHING;

SELECT setval('shops_id_seq', (SELECT MAX(id) FROM shops));


-- ── 4. PRODUCTS (IDs 51–70) ───────────────────────────────────
--
-- image_url uses placeholder seeds for now.
-- Replace with real S3/CloudFront URLs when images are ready.
--
-- Category mapping (Zepto-style reasoning):
--   Milk / Butter / Cheese / Dahi   → Dairy & Eggs        (cat 6)
--   Rice / Atta / Salt / Sugar       → Atta, Rice & Staples(cat 7)
--   Coke / OJ / Water / Tea          → Beverages           (cat 8)
--   Lay's / Dairy Milk / Parle-G / Kurkure → Snacks        (cat 9)
--   Vim / Surf Excel / Harpic        → Cleaning Essentials (cat 10)
--   Dettol Handwash                  → Personal Care       (cat 11)
--     (Dettol is hygiene/personal care in Zepto, not cleaning)

-- ── Dairy & Eggs (category 6) ──
INSERT INTO products (id, name, slug, brand_id, category_id,
                      description, specifications, image_url,
                      featured, new_arrival, top_selling)
VALUES
(51, 'Amul Taaza Milk 1L',
     'amul-taaza-milk-1l', 36, 6,
     'Fresh toned milk from Amul, packed with essential nutrients and vitamins. Delivered chilled.',
     '{"Volume":"1 Litre","Type":"Toned Milk","Fat":"3%","SNF":"8.5%","Shelf Life":"3 days refrigerated"}',
     'https://picsum.photos/seed/g-amul-milk/400/400',
     true, false, true),

(52, 'Amul Butter 100g',
     'amul-butter-100g', 36, 6,
     'Rich and creamy Amul salted butter — perfect for spreading on toast or cooking.',
     '{"Weight":"100g","Type":"Salted Butter","Fat":"80%","Shelf Life":"90 days refrigerated","Pack":"Cube"}',
     'https://picsum.photos/seed/g-amul-butter/400/400',
     true, false, false),

(53, 'Amul Cheese Slices 200g',
     'amul-cheese-slices-200g', 36, 6,
     'Processed cheese slices from Amul — ideal for sandwiches, burgers, and grilled dishes.',
     '{"Weight":"200g","Slices":"10","Type":"Processed Cheese","Fat":"25%","Shelf Life":"6 months"}',
     'https://picsum.photos/seed/g-amul-cheese/400/400',
     false, false, false),

(54, 'Nestlé Dahi 400g',
     'nestle-dahi-400g', 37, 6,
     'Thick and creamy set curd by Nestlé. Made from fresh milk — great with meals or as raita.',
     '{"Weight":"400g","Type":"Set Curd","Fat":"3.1%","Protein":"4.5g per 100g","Shelf Life":"10 days refrigerated"}',
     'https://picsum.photos/seed/g-nestle-dahi/400/400',
     false, false, false)
ON CONFLICT (id) DO NOTHING;

-- ── Atta, Rice & Staples (category 7) ──
INSERT INTO products (id, name, slug, brand_id, category_id,
                      description, specifications, image_url,
                      featured, new_arrival, top_selling)
VALUES
(55, 'India Gate Basmati Rice 1kg',
     'india-gate-basmati-rice-1kg', 38, 7,
     'Premium aged Basmati rice with extra-long grains and a rich natural aroma.',
     '{"Weight":"1kg","Variety":"Classic Basmati","Grain":"Extra Long","Aged":"Yes","Cooking Time":"15 min"}',
     'https://picsum.photos/seed/g-indiagate-rice/400/400',
     true, false, true),

(56, 'Aashirvaad Atta 2kg',
     'aashirvaad-atta-2kg', 39, 7,
     '100% whole wheat atta from Aashirvaad. Soft, nutritious rotis every single time.',
     '{"Weight":"2kg","Type":"Whole Wheat Atta","Protein":"10.5g per 100g","Fiber":"High","Origin":"India"}',
     'https://picsum.photos/seed/g-aashirvaad-atta/400/400',
     true, false, false),

(57, 'Tata Salt 1kg',
     'tata-salt-1kg', 40, 7,
     'India''s most trusted iodised salt — pure, clean, and free-flowing.',
     '{"Weight":"1kg","Type":"Iodised Salt","Iodine":"15ppm","Sodium":"38.7g per 100g"}',
     'https://picsum.photos/seed/g-tata-salt/400/400',
     false, false, true),

(58, 'Fortune Sugar 1kg',
     'fortune-sugar-1kg', 41, 7,
     'Fine grain refined white sugar by Fortune — pure and consistent quality.',
     '{"Weight":"1kg","Type":"Refined Sugar","Grade":"M-30","Moisture":"< 0.5%","Colour":"White"}',
     'https://picsum.photos/seed/g-fortune-sugar/400/400',
     false, false, false)
ON CONFLICT (id) DO NOTHING;

-- ── Beverages (category 8) ──
--
-- Zepto note: Bisleri goes in Beverages (packaged water is in the same
-- aisle as drinks). Tata Tea goes here too — Zepto has "Tea, Coffee & More"
-- as a sub-section under the broader Beverages aisle.
INSERT INTO products (id, name, slug, brand_id, category_id,
                      description, specifications, image_url,
                      featured, new_arrival, top_selling)
VALUES
(59, 'Coca-Cola 750ml',
     'coca-cola-750ml', 42, 8,
     'The original refreshing Coca-Cola — a classic carbonated soft drink enjoyed worldwide.',
     '{"Volume":"750ml","Type":"Carbonated Soft Drink","Calories":"90 per 250ml","Caffeine":"Yes","Packaging":"PET Bottle"}',
     'https://picsum.photos/seed/g-cocacola/400/400',
     true, false, true),

(60, 'Tropicana Orange Juice 1L',
     'tropicana-orange-juice-1l', 43, 8,
     '100% pure squeezed orange juice — no added sugar, no preservatives, no water.',
     '{"Volume":"1 Litre","Type":"Orange Juice","Added Sugar":"No","Vitamins":"C, A","Pulp":"With Pulp"}',
     'https://picsum.photos/seed/g-tropicana-oj/400/400',
     false, false, false),

(61, 'Bisleri Water 1L',
     'bisleri-water-1l', 44, 8,
     'Pure and safe packaged drinking water from Bisleri. 7-stage purification process.',
     '{"Volume":"1 Litre","Type":"Packaged Drinking Water","TDS":"< 150 ppm","pH":"6.5 – 8.5","Packaging":"PET Bottle"}',
     'https://picsum.photos/seed/g-bisleri/400/400',
     false, false, false),

(62, 'Tata Tea Premium 250g',
     'tata-tea-premium-250g', 40, 8,
     'Tata Tea Premium — strong and aromatic CTC leaf tea for a perfect morning brew.',
     '{"Weight":"250g","Type":"CTC Leaf Tea","Origin":"Assam & Darjeeling","Caffeine":"Yes","Brew":"Hot"}',
     'https://picsum.photos/seed/g-tata-tea/400/400',
     false, false, false)
ON CONFLICT (id) DO NOTHING;

-- ── Snacks & Munchies (category 9) ──
INSERT INTO products (id, name, slug, brand_id, category_id,
                      description, specifications, image_url,
                      featured, new_arrival, top_selling)
VALUES
(63, 'Lay''s Cream & Onion Small Pack',
     'lays-cream-onion-small', 45, 9,
     'Lay''s American Style Cream & Onion potato chips — light, crunchy, and irresistible.',
     '{"Weight":"26g","Flavour":"Cream & Onion","Type":"Potato Chips","Pack":"Small"}',
     'https://picsum.photos/seed/g-lays/400/400',
     false, false, true),

(64, 'Cadbury Dairy Milk 25g',
     'cadbury-dairy-milk-25g', 46, 9,
     'Cadbury Dairy Milk — India''s favourite milk chocolate. Rich, smooth, and irresistibly delicious.',
     '{"Weight":"25g","Type":"Milk Chocolate","Cocoa":"Min 20%","Pack":"Single Bar"}',
     'https://picsum.photos/seed/g-dairymilk/400/400',
     false, false, true),

(65, 'Parle-G Biscuits 100g',
     'parle-g-biscuits-100g', 47, 9,
     'Parle-G — world''s largest selling biscuit brand. Crispy, lightly sweet, and full of energy.',
     '{"Weight":"100g","Type":"Glucose Biscuit","Protein":"6g per 100g","Pack":"Family Pack"}',
     'https://picsum.photos/seed/g-parleg/400/400',
     true, false, true),

(66, 'Kurkure Chilli Chatka Small Pack',
     'kurkure-chilli-chatka-small', 48, 9,
     'Kurkure Chilli Chatka — tangy, spicy, and crunchy puffed corn snack. Hard to stop at one!',
     '{"Weight":"22g","Flavour":"Chilli Chatka","Type":"Puffed Corn Snack","Pack":"Small"}',
     'https://picsum.photos/seed/g-kurkure/400/400',
     false, false, false)
ON CONFLICT (id) DO NOTHING;

-- ── Cleaning Essentials (category 10) ──
INSERT INTO products (id, name, slug, brand_id, category_id,
                      description, specifications, image_url,
                      featured, new_arrival, top_selling)
VALUES
(67, 'Vim Dishwash Liquid 250ml',
     'vim-dishwash-liquid-250ml', 49, 10,
     'Vim dishwash liquid with lemon power. Cuts through tough grease 5× faster.',
     '{"Volume":"250ml","Type":"Dishwash Liquid","Fragrance":"Lemon","Formula":"Anti-Bacterial"}',
     'https://picsum.photos/seed/g-vim/400/400',
     false, false, false),

(68, 'Surf Excel Quick Wash 1kg',
     'surf-excel-quick-wash-1kg', 50, 10,
     'Surf Excel Quick Wash — removes 100 tough stains and delivers bright, white results.',
     '{"Weight":"1kg","Type":"Washing Powder","Suitable For":"Machine & Hand Wash","Fragrance":"Yes","Stain Removal":"100 stains"}',
     'https://picsum.photos/seed/g-surfexcel/400/400',
     false, false, false),

(69, 'Harpic Toilet Cleaner 500ml',
     'harpic-toilet-cleaner-500ml', 51, 10,
     'Harpic Power Plus — kills 99.9% germs and removes tough limescale and stains.',
     '{"Volume":"500ml","Type":"Toilet Cleaner","Kill":"99.9% Germs","Fragrance":"Original","Formula":"Thick Liquid"}',
     'https://picsum.photos/seed/g-harpic/400/400',
     false, false, false)
ON CONFLICT (id) DO NOTHING;

-- ── Personal Care (category 11) ──
--
-- Dettol Handwash is Personal Care / Hygiene in Zepto's taxonomy,
-- NOT Cleaning Essentials (which is for home/surface cleaners).
INSERT INTO products (id, name, slug, brand_id, category_id,
                      description, specifications, image_url,
                      featured, new_arrival, top_selling)
VALUES
(70, 'Dettol Original Handwash 200ml',
     'dettol-original-handwash-200ml', 52, 11,
     'Dettol liquid handwash — protects against 100 illness-causing germs. Safe for the whole family.',
     '{"Volume":"200ml","Type":"Liquid Handwash","Protection":"100 Germs","Fragrance":"Original"}',
     'https://picsum.photos/seed/g-dettol/400/400',
     false, false, false)
ON CONFLICT (id) DO NOTHING;

SELECT setval('products_id_seq', (SELECT MAX(id) FROM products));


-- ── 5. SHOP_PRODUCTS — Prime Spot Grocery pricing ─────────────
--
-- Rules applied:
--   • delivery_days = 1 (Zepto / quick-commerce model)
--   • stock is generous (grocery restocks daily)
--   • selling_price gives a real discount vs MRP (not fake)
--   • discount_percent = ROUND((mrp - selling_price) / mrp * 100)
--   • Parle-G is a price-controlled item — selling_price = mrp, discount = 0

INSERT INTO shop_products
  (shop_id, product_id, mrp, selling_price, discount_percent, stock, delivery_days)
VALUES
-- Dairy & Eggs
(6, 51,  60.00,  58.00,  3, 500, 1),   -- Amul Taaza Milk
(6, 52,  58.00,  55.00,  5, 300, 1),   -- Amul Butter
(6, 53, 120.00, 110.00,  8, 200, 1),   -- Amul Cheese Slices
(6, 54,  30.00,  28.00,  7, 400, 1),   -- Nestlé Dahi
-- Atta, Rice & Staples
(6, 55, 120.00, 110.00,  8, 800, 1),   -- India Gate Basmati Rice
(6, 56, 110.00,  99.00, 10, 600, 1),   -- Aashirvaad Atta
(6, 57,  28.00,  25.00, 11,1000, 1),   -- Tata Salt
(6, 58,  55.00,  49.00, 11, 800, 1),   -- Fortune Sugar
-- Beverages
(6, 59,  45.00,  43.00,  4, 500, 1),   -- Coca-Cola
(6, 60, 110.00,  99.00, 10, 300, 1),   -- Tropicana Orange Juice
(6, 61,  20.00,  18.00, 10,1000, 1),   -- Bisleri Water
(6, 62, 110.00,  99.00, 10, 400, 1),   -- Tata Tea Premium
-- Snacks & Munchies
(6, 63,  20.00,  18.00, 10, 800, 1),   -- Lay's Cream & Onion
(6, 64,  20.00,  18.00, 10, 600, 1),   -- Cadbury Dairy Milk
(6, 65,  10.00,  10.00,  0,1000, 1),   -- Parle-G (price-controlled MRP item)
(6, 66,  20.00,  18.00, 10, 700, 1),   -- Kurkure Chilli Chatka
-- Cleaning Essentials
(6, 67,  45.00,  40.00, 11, 500, 1),   -- Vim Dishwash Liquid
(6, 68, 230.00, 205.00, 11, 400, 1),   -- Surf Excel Quick Wash
(6, 69,  85.00,  75.00, 12, 350, 1),   -- Harpic Toilet Cleaner
-- Personal Care
(6, 70,  80.00,  72.00, 10, 450, 1)    -- Dettol Original Handwash
ON CONFLICT (shop_id, product_id) DO NOTHING;


-- ── 6. VERIFICATION QUERY ─────────────────────────────────────
SELECT
  (SELECT COUNT(*) FROM categories WHERE id >= 6)          AS new_categories,
  (SELECT COUNT(*) FROM brands    WHERE id >= 36)          AS new_brands,
  (SELECT COUNT(*) FROM shops     WHERE id  = 6)           AS grocery_shop,
  (SELECT COUNT(*) FROM products  WHERE id BETWEEN 51 AND 70) AS new_products,
  (SELECT COUNT(*) FROM shop_products WHERE shop_id = 6)   AS shop_product_rows;
-- Expected: 6 | 17 | 1 | 20 | 20


-- ── HOW TO UPDATE IMAGE URLs LATER ───────────────────────────
--
-- Once you upload the real product images to S3/CloudFront,
-- run these UPDATE statements (replace the CDN URLs):
--
-- UPDATE products SET image_url = 'https://YOUR_CDN/products/amul-milk.jpg'
-- WHERE id = 51;
--
-- UPDATE products SET image_url = 'https://YOUR_CDN/products/amul-butter.jpg'
-- WHERE id = 52;
--
-- ... repeat for each product ID 51–70.
--
-- To add extra gallery images (product_images table):
-- INSERT INTO product_images (product_id, image_url, sort_order) VALUES
-- (51, 'https://YOUR_CDN/products/amul-milk-back.jpg', 1),
-- (51, 'https://YOUR_CDN/products/amul-milk-nutrition.jpg', 2);
