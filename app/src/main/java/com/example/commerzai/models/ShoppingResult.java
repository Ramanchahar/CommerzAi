package com.example.commerzai.models;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class ShoppingResult {
    private int position;

    @SerializedName("block_position")
    private String blockPosition;

    private String title;
    private String price;

    @SerializedName("extracted_price")
    private double extractedPrice;

    private String link;
    private String source;
    private String shipping;
    private double rating;
    private int reviews;

    @SerializedName("reviews_original")
    private String reviewsOriginal;

    private String thumbnail;
    private List<String> extensions;

    public int getPosition() {
        return position;
    }

    public String getBlockPosition() {
        return blockPosition;
    }

    public String getTitle() {
        return title;
    }

    public String getPrice() {
        return price;
    }

    public double getExtractedPrice() {
        return extractedPrice;
    }

    public String getLink() {
        return link;
    }

    public String getSource() {
        return source;
    }

    public String getShipping() {
        return shipping;
    }

    public double getRating() {
        return rating;
    }

    public int getReviews() {
        return reviews;
    }

    public String getReviewsOriginal() {
        return reviewsOriginal;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public List<String> getExtensions() {
        return extensions;
    }

    // Setter methods
    public void setPosition(int position) {
        this.position = position;
    }

    public void setBlockPosition(String blockPosition) {
        this.blockPosition = blockPosition;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public void setExtractedPrice(double extractedPrice) {
        this.extractedPrice = extractedPrice;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public void setShipping(String shipping) {
        this.shipping = shipping;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public void setReviews(int reviews) {
        this.reviews = reviews;
    }

    public void setReviewsOriginal(String reviewsOriginal) {
        this.reviewsOriginal = reviewsOriginal;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }

    public void setExtensions(List<String> extensions) {
        this.extensions = extensions;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;

        ShoppingResult that = (ShoppingResult) obj;

        // Two shopping results are considered equal if they have the same link and
        // title
        // This helps with double-click detection
        if (link != null ? !link.equals(that.link) : that.link != null)
            return false;
        return title != null ? title.equals(that.title) : that.title == null;
    }

    @Override
    public int hashCode() {
        int result = link != null ? link.hashCode() : 0;
        result = 31 * result + (title != null ? title.hashCode() : 0);
        return result;
    }
}