package com.example.commerzai.api;

import com.example.commerzai.models.SerpApiResponse;
import com.google.gson.JsonObject;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface SerpApiService {
        @GET("search.json")
        Call<SerpApiResponse> searchProducts(
                        @Query("q") String query,
                        @Query("location") String location,
                        @Query("hl") String language,
                        @Query("gl") String country,
                        @Query("api_key") String apiKey);

        @GET("search.json")
        Call<JsonObject> searchRawResponse(
                        @Query("q") String query,
                        @Query("location") String location,
                        @Query("hl") String language,
                        @Query("gl") String country,
                        @Query("api_key") String apiKey);

        @GET("search.json?tbm=shop")
        Call<JsonObject> searchShoppingResults(
                        @Query("q") String query,
                        @Query("location") String location,
                        @Query("hl") String language,
                        @Query("gl") String country,
                        @Query("api_key") String apiKey);
}