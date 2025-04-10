package com.example.commerzai.models;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class SerpApiResponse {
    @SerializedName("shopping_results")
    private List<ShoppingResult> shoppingResults;

    public List<ShoppingResult> getShoppingResults() {
        return shoppingResults;
    }
}