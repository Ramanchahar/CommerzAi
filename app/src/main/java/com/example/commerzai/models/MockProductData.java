package com.example.commerzai.models;

import java.util.ArrayList;
import java.util.List;

public class MockProductData {

    public static List<ShoppingResult> getMockMobilePhones() {
        List<ShoppingResult> phones = new ArrayList<>();

        // Create mock mobile phone products

        ShoppingResult phone1 = new ShoppingResult();
        phone1.setTitle("iPhone 15 Pro Max - 256GB");
        phone1.setPrice("$1,199.00");
        phone1.setSource("Apple.com");
        phone1.setRating(4.8);
        phone1.setReviewsOriginal("2k+");
        phone1.setLink("https://www.apple.com/shop/buy-iphone/iphone-15-pro");
        phone1.setThumbnail(
                "https://store.storeimages.cdn-apple.com/4982/as-images.apple.com/is/iphone-15-pro-finish-select-202309-6-7inch-naturaltitanium?wid=5120&hei=2880&fmt=p-jpg&qlt=80&.v=1693009284541");
        phones.add(phone1);

        ShoppingResult phone2 = new ShoppingResult();
        phone2.setTitle("Samsung Galaxy S23 Ultra");
        phone2.setPrice("$949.99");
        phone2.setSource("Samsung.com");
        phone2.setRating(4.7);
        phone2.setReviewsOriginal("3k+");
        phone2.setLink("https://www.samsung.com/us/smartphones/galaxy-s23-ultra/");
        phone2.setThumbnail(
                "https://image-us.samsung.com/us/smartphones/galaxy-s23-ultra/images/gallery/cream/01-DM3-Cream-PDP-1600x1200.jpg");
        phones.add(phone2);

        ShoppingResult phone3 = new ShoppingResult();
        phone3.setTitle("Google Pixel 8 Pro - 128GB");
        phone3.setPrice("$799.00");
        phone3.setSource("Google Store");
        phone3.setRating(4.6);
        phone3.setReviewsOriginal("1k+");
        phone3.setLink("https://store.google.com/product/pixel_8");
        phone3.setThumbnail(
                "https://lh3.googleusercontent.com/oBOJKmLIxHmGnJ0CnzawkbzFGP-d_0bDt2Cp-9FOvQOj5_LrVj-Mjvt2sS9loXafXLdq2Qi0zK5RLlrH37vcHJTKsUZ9trHe3A=rw-e365-w1200");
        phones.add(phone3);

        ShoppingResult phone4 = new ShoppingResult();
        phone4.setTitle("OnePlus 12 - 5G 256GB");
        phone4.setPrice("$799.99");
        phone4.setSource("OnePlus.com");
        phone4.setRating(4.5);
        phone4.setReviewsOriginal("800+");
        phone4.setLink("https://www.oneplus.com/oneplus-12");
        phone4.setThumbnail("https://image01.oneplus.net/ebp/202401/04/1-m00-51-00-cpgm7wtk6emaqkvcaaldqouvyks391.png");
        phones.add(phone4);

        ShoppingResult phone5 = new ShoppingResult();
        phone5.setTitle("Motorola Edge 40 Pro");
        phone5.setPrice("$649.99");
        phone5.setSource("Amazon.com");
        phone5.setRating(4.3);
        phone5.setReviewsOriginal("456");
        phone5.setLink("https://www.amazon.com/motorola-Unlocked-256GB-Storage-Battery/dp/B0BXPGCN22");
        phone5.setThumbnail("https://motorolaau.vtexassets.com/arquivos/ids/155587-800-auto");
        phones.add(phone5);

        ShoppingResult phone6 = new ShoppingResult();
        phone6.setTitle("Xiaomi 14 Ultra");
        phone6.setPrice("$999.00");
        phone6.setSource("mi.com");
        phone6.setRating(4.6);
        phone6.setReviewsOriginal("1.2k+");
        phone6.setLink("https://www.mi.com/global/product/xiaomi-14-ultra/");
        phone6.setThumbnail(
                "https://i02.appmifile.com/564_operator_sg/28/02/2024/e22bbc71fd07352f7d99cd63f5ee05ce.png");
        phones.add(phone6);

        ShoppingResult phone7 = new ShoppingResult();
        phone7.setTitle("Nothing Phone (2) - 256GB");
        phone7.setPrice("$549.00");
        phone7.setSource("Nothing.tech");
        phone7.setRating(4.4);
        phone7.setReviewsOriginal("720");
        phone7.setLink("https://us.nothing.tech/products/phone-2");
        phone7.setThumbnail("https://us.nothing.tech/cdn/shop/files/Phone_2_PDP_Images_4.png");
        phones.add(phone7);

        ShoppingResult phone8 = new ShoppingResult();
        phone8.setTitle("Nokia G42 - 5G 6GB RAM");
        phone8.setPrice("$199.99");
        phone8.setSource("Nokia.com");
        phone8.setRating(4.1);
        phone8.setReviewsOriginal("325");
        phone8.setLink("https://www.nokia.com/phones/en_int/nokia-g-42");
        phone8.setThumbnail(
                "https://www.nokia.com/sites/default/files/styles/scale_1440_x_1080_/public/2023-06/nokia-g42-5g-grey-back-grey-front.png");
        phones.add(phone8);

        return phones;
    }

    public static List<ShoppingResult> getMockGamingMouse() {
        List<ShoppingResult> products = new ArrayList<>();

        // Add gaming mouse products similar to the previous screenshot
        String[] titles = {
                "Redragon - For Gaming - M9",
                "Logitech G - For Gaming",
                "Lenovo Legion M200 RGB",
                "Razer Basilisk X Hyperspeed",
                "Alienware Wired/wireless",
                "Mad Catz - For Gaming",
                "HyperX - For Gaming",
                "Logitech G - For Gaming"
        };

        String[] prices = { "$21.99", "$102.44", "$15.95", "$39.99", "$69.99", "$68.85", "$39.99", "$39.99" };
        String[] sources = { "Amazon.com", "Amazon.com", "Amazon.com", "Target", "Dell", "Amazon.com", "Best Buy",
                "Amazon.com" };
        String[] reviews = { "18 Button", "11 Button", "5 Button", "(3k+)", "(2k+)", "8 Button", "(409)", "11 Button" };

        for (int i = 0; i < titles.length; i++) {
            ShoppingResult product = new ShoppingResult();
            product.setTitle(titles[i]);
            product.setPrice(prices[i]);
            product.setSource(sources[i]);
            product.setRating(4.5);
            product.setReviewsOriginal(reviews[i]);
            product.setLink("https://www.example.com");
            products.add(product);
        }

        return products;
    }
}