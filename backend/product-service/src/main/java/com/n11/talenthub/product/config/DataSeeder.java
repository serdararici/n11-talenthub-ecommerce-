package com.n11.talenthub.product.config;

import com.n11.talenthub.product.entity.Product;
import com.n11.talenthub.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final ProductRepository productRepository;

    @Override
    public void run(String... args) {
        if (productRepository.count() > 0) {
            log.info("Database already seeded, skipping.");
            return;
        }

        List<Product> products = List.of(
            Product.builder()
                .name("Apple MacBook Pro 14\" M3 Pro 18GB 512GB")
                .brand("Apple").category("Bilgisayar")
                .price(new BigDecimal("62999")).originalPrice(new BigDecimal("69999"))
                .stockQuantity(15).freeShipping(true).rating(4.8).reviewCount(234)
                .description("Apple M3 Pro çip, 18GB birleşik bellek, 512GB SSD depolama. ProMotion teknolojisi ile 120Hz yenileme hızı. 18 saate kadar pil ömrü.")
                .build(),
            Product.builder()
                .name("Samsung Galaxy Tab S9 FE 10.9\" 128GB Wi-Fi")
                .brand("Samsung").category("Tablet").badge("FIRSATLAR")
                .price(new BigDecimal("8399")).originalPrice(new BigDecimal("10999"))
                .stockQuantity(42).freeShipping(true).rating(4.5).reviewCount(1120)
                .description("10.9\" TFT ekran, Samsung Exynos 1380 işlemci, 6GB RAM, 128GB depolama, Wi-Fi 6 desteği.")
                .build(),
            Product.builder()
                .name("Lenovo IdeaPad Gaming 3 Ryzen 5 16GB 512GB RTX 3050")
                .brand("Lenovo").category("Bilgisayar")
                .price(new BigDecimal("28499")).originalPrice(new BigDecimal("31999"))
                .stockQuantity(8).freeShipping(true).rating(4.6).reviewCount(567)
                .description("AMD Ryzen 5 7535HS, 16GB DDR5 RAM, 512GB NVMe SSD, NVIDIA GeForce RTX 3050, 15.6\" FHD 144Hz ekran.")
                .build(),
            Product.builder()
                .name("Samsung 65\" Neo QLED 4K Smart TV")
                .brand("Samsung").category("Elektronik").badge("ÇOK AL AZ ÖDE")
                .price(new BigDecimal("34999")).originalPrice(new BigDecimal("44999"))
                .stockQuantity(5).freeShipping(true).rating(4.7).reviewCount(89)
                .description("Neo QLED 4K, Quantum HDR, Object Tracking Sound+, Smart TV Tizen OS 7.0.")
                .build(),
            Product.builder()
                .name("iPhone 15 Pro Max 256GB Doğal Titanyum")
                .brand("Apple").category("Telefon")
                .price(new BigDecimal("74999")).originalPrice(new BigDecimal("79999"))
                .stockQuantity(20).freeShipping(true).rating(4.9).reviewCount(2341)
                .description("A17 Pro çip, titanyum tasarım, 48MP ana kamera, Action Button, USB-C bağlantısı.")
                .build(),
            Product.builder()
                .name("Xiaomi Redmi Pad Pro 8GB 256GB 12.1\"")
                .brand("Xiaomi").category("Tablet").badge("KUPONLU ÜRÜN")
                .price(new BigDecimal("7249")).originalPrice(new BigDecimal("8399"))
                .stockQuantity(55).freeShipping(true).rating(4.4).reviewCount(334)
                .description("Snapdragon 7s Gen 2, 12.1\" 2.5K ekran, 10000mAh batarya, 45W hızlı şarj.")
                .build(),
            Product.builder()
                .name("ASUS ROG Strix G16 Intel i9 32GB 1TB RTX 4080")
                .brand("ASUS").category("Bilgisayar")
                .price(new BigDecimal("89999")).originalPrice(new BigDecimal("99999"))
                .stockQuantity(3).freeShipping(true).rating(4.8).reviewCount(156)
                .description("Intel Core i9-14900HX, 32GB DDR5, 1TB PCIe 4.0 SSD, RTX 4080 12GB, 16\" QHD+ 240Hz.")
                .build(),
            Product.builder()
                .name("Sony WH-1000XM5 Kablosuz Kulaklık")
                .brand("Sony").category("Elektronik").badge("ÜCRETSİZ KARGO")
                .price(new BigDecimal("9499")).originalPrice(new BigDecimal("12999"))
                .stockQuantity(30).freeShipping(true).rating(4.9).reviewCount(4523)
                .description("Sektör lideri gürültü engelleme, 30 saate kadar pil ömrü, Multipoint bağlantı.")
                .build(),
            Product.builder()
                .name("Huawei MateBook D16 Intel i5 16GB 512GB")
                .brand("Huawei").category("Bilgisayar")
                .price(new BigDecimal("21999")).originalPrice(new BigDecimal("24999"))
                .stockQuantity(12).freeShipping(false).rating(4.3).reviewCount(211)
                .description("Intel Core i5-12450H, 16GB DDR4, 512GB NVMe SSD, 16\" FHD IPS matris ekran.")
                .build(),
            Product.builder()
                .name("Apple iPad Air 11\" M2 256GB Wi-Fi Mor")
                .brand("Apple").category("Tablet")
                .price(new BigDecimal("24999")).originalPrice(new BigDecimal("27999"))
                .stockQuantity(18).freeShipping(true).rating(4.7).reviewCount(678)
                .description("Apple M2 çip, 11\" Liquid Retina ekran, USB-C, 5G desteği (isteğe bağlı), iPadOS 17.")
                .build(),
            Product.builder()
                .name("Logitech MX Master 3S Kablosuz Mouse")
                .brand("Logitech").category("Elektronik").badge("ÜCRETSİZ KARGO")
                .price(new BigDecimal("2799")).originalPrice(new BigDecimal("3499"))
                .stockQuantity(100).freeShipping(true).rating(4.8).reviewCount(1890)
                .description("8000 DPI sensör, ses yalıtımlı tıklama, MagSpeed tekerlek, 70 gün pil ömrü.")
                .build(),
            Product.builder()
                .name("Samsung 27\" Odyssey G5 165Hz QHD Curved")
                .brand("Samsung").category("Elektronik")
                .price(new BigDecimal("8999")).originalPrice(new BigDecimal("11999"))
                .stockQuantity(22).freeShipping(true).rating(4.6).reviewCount(445)
                .description("1ms GTG, FreeSync Premium, HDR10, 1000R eğri ekran, HDMI 2.0 x2.")
                .build(),
            Product.builder()
                .name("MSI Prestige 14 Evo Intel i7 16GB 512GB")
                .brand("MSI").category("Bilgisayar")
                .price(new BigDecimal("25999")).originalPrice(new BigDecimal("28999"))
                .stockQuantity(7).freeShipping(true).rating(4.5).reviewCount(132)
                .description("Intel Core Ultra 7 155H, Intel Arc grafik, 16GB LPDDR5, 1TB NVMe SSD, 14\" 2.8K OLED.")
                .build(),
            Product.builder()
                .name("Xiaomi 14 Ultra 512GB Siyah")
                .brand("Xiaomi").category("Telefon").badge("HIZLI KARGO")
                .price(new BigDecimal("49999")).originalPrice(new BigDecimal("54999"))
                .stockQuantity(14).freeShipping(true).rating(4.7).reviewCount(891)
                .description("Snapdragon 8 Gen 3, Leica quad kamera, 5000mAh, 90W kablolu + 80W kablosuz şarj.")
                .build(),
            Product.builder()
                .name("TP-Link Archer AX73 Wi-Fi 6 Router")
                .brand("TP-Link").category("Elektronik")
                .price(new BigDecimal("2199")).originalPrice(new BigDecimal("2899"))
                .stockQuantity(45).freeShipping(true).rating(4.5).reviewCount(567)
                .description("AX5400, Wi-Fi 6, 6 anten, OneMesh, 1.5GHz üçlü çekirdek işlemci.")
                .build(),
            Product.builder()
                .name("AMD Ryzen 9 7950X 4.5GHz 16 Çekirdek")
                .brand("AMD").category("Bilgisayar")
                .price(new BigDecimal("15249")).originalPrice(new BigDecimal("17999"))
                .stockQuantity(9).freeShipping(false).rating(4.9).reviewCount(203)
                .description("AM5 soket, 16 çekirdek / 32 iş parçacığı, 80MB toplam önbellek, 170W TDP.")
                .build(),
            Product.builder()
                .name("Mavi Erkek Slim Fit Jean Pantolon")
                .brand("Mavi").category("Moda").badge("ÜCRETSİZ KARGO")
                .price(new BigDecimal("899")).originalPrice(new BigDecimal("1199"))
                .stockQuantity(200).freeShipping(true).rating(4.4).reviewCount(2341)
                .description("%98 pamuk, %2 elastan. Slim fit kesim, orta bel. 5 cep modeli.")
                .build(),
            Product.builder()
                .name("Nike Air Max 270 Erkek Spor Ayakkabı")
                .brand("Nike").category("Moda")
                .price(new BigDecimal("3499")).originalPrice(new BigDecimal("4299"))
                .stockQuantity(60).freeShipping(true).rating(4.6).reviewCount(1123)
                .description("Max Air yastıklama, nefes alabilir örgü üst, dayanıklı lastik taban.")
                .build(),
            Product.builder()
                .name("IKEA MALM Çift Karyola 160x200")
                .brand("IKEA").category("Ev & Yaşam")
                .price(new BigDecimal("4599")).originalPrice(new BigDecimal("5299"))
                .stockQuantity(11).freeShipping(false).rating(4.3).reviewCount(445)
                .description("Boyalı astarlı yüzey, kaplama, yüksek karyola yatağı için uygun. 160x200 cm.")
                .build(),
            Product.builder()
                .name("Tefal Ingenio Preference 13 Parça Tencere Seti")
                .brand("Tefal").category("Ev & Yaşam").badge("FIRSATLAR")
                .price(new BigDecimal("2849")).originalPrice(new BigDecimal("3999"))
                .stockQuantity(33).freeShipping(true).rating(4.7).reviewCount(2109)
                .description("Çıkarılabilir sap teknolojisi, titanyum kaplama, indüksiyonlu ocak uyumlu.")
                .build(),
            Product.builder()
                .name("Sapiens – Yuval Noah Harari (Türkçe)")
                .brand("Kolektif Kitap").category("Kitap & Müzik")
                .price(new BigDecimal("189")).originalPrice(new BigDecimal("249"))
                .stockQuantity(500).freeShipping(true).rating(4.9).reviewCount(8901)
                .description("İnsanlığın kısa tarihi. Dünyada en çok satan kitaplardan biri. 624 sayfa, ciltsiz.")
                .build(),
            Product.builder()
                .name("Adidas Tiro 23 Erkek Antrenman Şortu")
                .brand("Adidas").category("Spor").badge("ÜCRETSİZ KARGO")
                .price(new BigDecimal("649")).originalPrice(new BigDecimal("899"))
                .stockQuantity(150).freeShipping(true).rating(4.5).reviewCount(678)
                .description("Hafif dokuma kumaş, AEROREADY nem yönetimi, elastik bel bandı.")
                .build(),
            Product.builder()
                .name("Philips Airfryer XL 6.2L Yağsız Fritöz")
                .brand("Philips").category("Ev & Yaşam").badge("ÇOK AL AZ ÖDE")
                .price(new BigDecimal("4299")).originalPrice(new BigDecimal("5499"))
                .stockQuantity(28).freeShipping(true).rating(4.8).reviewCount(3211)
                .description("6.2L XL hacim, RapidAir teknolojisi, dijital dokunmatik ekran, 7 program.")
                .build(),
            Product.builder()
                .name("HP LaserJet MFP M234dw Kablosuz Yazıcı")
                .brand("HP").category("Elektronik")
                .price(new BigDecimal("5499")).originalPrice(new BigDecimal("6799"))
                .stockQuantity(17).freeShipping(true).rating(4.4).reviewCount(334)
                .description("Çift taraflı baskı, Wi-Fi+Bluetooth, 30 s/dk baskı hızı, HP Smart uygulaması.")
                .build()
        );

        productRepository.saveAll(products);
        log.info("Seeded {} products into the database.", products.size());
    }
}
