package com.example.commerzai.models;

public class SavedProduct {
    private String id;
    private String title;
    private String price;
    private String source;
    private String thumbnail;
    private String link;
    private double rating;
    private int reviews;
    private String userId;
    private long timestamp;

    public SavedProduct() {
        // Default constructor required for Firebase
    }

    public SavedProduct(ShoppingResult product, String userId) {
        this.title = product.getTitle();
        this.price = product.getPrice();
        this.source = product.getSource();
        this.thumbnail = product.getThumbnail();
        this.link = product.getLink();
        this.rating = product.getRating();
        this.reviews = product.getReviews();
        this.userId = userId;
        this.timestamp = System.currentTimeMillis();
    }

    // Getters and setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public int getReviews() {
        return reviews;
    }

    public void setReviews(int reviews) {
        this.reviews = reviews;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
} 