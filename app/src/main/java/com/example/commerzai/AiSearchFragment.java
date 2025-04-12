package com.example.commerzai;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.commerzai.adapters.ProductAdapter;
import com.example.commerzai.api.RetrofitClient;
import com.example.commerzai.api.SerpApiService;
import com.example.commerzai.models.MockProductData;
import com.example.commerzai.models.ShoppingResult;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AiSearchFragment extends Fragment implements ProductAdapter.OnProductClickListener {
    private static final String TAG = "AiSearchFragment";
    private static final String API_KEY = "4b79b1b4698de4415772004064775e1f1696ab5baf41235284c608f1578f4e19";
    private static final String DEFAULT_LOCATION = "United States";
    private static final String DEFAULT_LANGUAGE = "en";
    private static final String DEFAULT_COUNTRY = "us";

    // Limit results to 10 items max
    private static final int MAX_RESULTS = 10;
    private static final int ITEMS_PER_PAGE = 10;

    // Price range values
    private String currentMinPrice = "";
    private String currentMaxPrice = "";

    private EditText searchInput;
    private ImageButton searchButton;
    private TextInputEditText minPriceInput;
    private TextInputEditText maxPriceInput;
    private Button applyFiltersButton;
    private RecyclerView searchResultsRecyclerView;
    private ProgressBar progressBar;
    private LinearLayout emptyStateLayout;

    private ProductAdapter productAdapter;
    private List<ShoppingResult> productList;
    private SerpApiService serpApiService;
    private Gson gson;
    private String lastQuery = "";
    private ShoppingResult lastClickedProduct = null;
    private long lastClickTime = 0;
    private static final long DOUBLE_CLICK_TIMEOUT = 500; // milliseconds

    private boolean isLoading = false;
    private boolean hasMoreItems = true;
    private int currentPage = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_ai_search, container, false);

        // Initialize views
        searchInput = view.findViewById(R.id.searchInput);
        searchButton = view.findViewById(R.id.searchButton);
        minPriceInput = view.findViewById(R.id.minPriceInput);
        maxPriceInput = view.findViewById(R.id.maxPriceInput);
        applyFiltersButton = view.findViewById(R.id.applyFiltersButton);
        searchResultsRecyclerView = view.findViewById(R.id.searchResultsRecyclerView);
        progressBar = view.findViewById(R.id.progressBar);
        emptyStateLayout = view.findViewById(R.id.emptyStateLayout);

        // Set fixed height for the RecyclerView to ensure scrolling works
        searchResultsRecyclerView.getLayoutParams().height = ViewGroup.LayoutParams.MATCH_PARENT;

        // Initialize RecyclerView with GridLayoutManager
        int spanCount = 2; // Number of columns
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), spanCount);
        searchResultsRecyclerView.setLayoutManager(gridLayoutManager);
        productList = new ArrayList<>();
        productAdapter = new ProductAdapter(getContext(), productList, this);
        productAdapter.setHasStableIds(true); // Helps with smooth recycling
        searchResultsRecyclerView.setAdapter(productAdapter);

        // Initialize SerpAPI service and Gson
        serpApiService = RetrofitClient.getSerpApiService();
        gson = new Gson();

        // Set click listeners
        searchButton.setOnClickListener(v -> {
            String query = searchInput.getText().toString().trim();
            if (!query.isEmpty()) {
                resetSearch();
                performSearch(query);
            } else {
                Toast.makeText(getContext(), "Please enter a product to search", Toast.LENGTH_SHORT).show();
            }
        });

        applyFiltersButton.setOnClickListener(v -> {
            String query = searchInput.getText().toString().trim();
            if (!query.isEmpty()) {
                String minPrice = minPriceInput.getText() != null ? minPriceInput.getText().toString() : "";
                String maxPrice = maxPriceInput.getText() != null ? maxPriceInput.getText().toString() : "";

                resetSearch();
                performSearchWithPriceRange(query, minPrice, maxPrice);
            } else {
                Toast.makeText(getContext(), "Please enter a product to search", Toast.LENGTH_SHORT).show();
            }
        });

        // Start with empty state
        showEmptyState();

        return view;
    }

    private void resetSearch() {
        currentPage = 0;
        hasMoreItems = true;
        productAdapter.clearProducts();
    }

    private void showEmptyState() {
        // Show empty state layout, hide results and progress
        emptyStateLayout.setVisibility(View.VISIBLE);
        searchResultsRecyclerView.setVisibility(View.GONE);
        progressBar.setVisibility(View.GONE);
    }

    private void showResults() {
        // Show results, hide empty state and progress
        emptyStateLayout.setVisibility(View.GONE);
        searchResultsRecyclerView.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.GONE);
    }

    private void showLoading(boolean isLoading) {
        this.isLoading = isLoading;

        if (isLoading) {
            // If it's the first load, show the main progress bar
            if (productList.isEmpty()) {
                progressBar.setVisibility(View.VISIBLE);
                searchResultsRecyclerView.setVisibility(View.GONE);
                emptyStateLayout.setVisibility(View.GONE);
            } else {
                // Otherwise, keep showing the list with a footer loading indicator
                // (could add a footer loading item to the adapter)
            }
        } else {
            if (!productList.isEmpty()) {
                showResults();
            } else {
                showEmptyState();
            }
        }
    }

    private void loadNextPage() {
        // Disable loading more pages since we're limiting to MAX_RESULTS
        // This function is called from the scroll listener but won't do anything
        hasMoreItems = false;
    }

    private void performSearchWithPriceRange(String query, String minPrice, String maxPrice) {
        lastQuery = query;
        currentMinPrice = minPrice;
        currentMaxPrice = maxPrice;

        // We'll now handle price filtering in code for more accuracy,
        // but still add it to the query to help the search API
        StringBuilder searchQuery = new StringBuilder(query);

        // Only add price filter if at least one price is specified
        if (!minPrice.isEmpty() || !maxPrice.isEmpty()) {
            if (!minPrice.isEmpty() && !maxPrice.isEmpty()) {
                // Both min and max specified
                searchQuery.append(" price:").append(minPrice).append("-").append(maxPrice);
            } else if (!minPrice.isEmpty()) {
                // Only min specified
                searchQuery.append(" price>").append(minPrice);
            } else {
                // Only max specified
                searchQuery.append(" price<").append(maxPrice);
            }
        }

        performShoppingSearch(searchQuery.toString(), false);
    }

    private void performSearch(String query) {
        lastQuery = query;
        performShoppingSearch(query, false);
    }

    private void performShoppingSearch(String query, boolean isLoadingMore) {
        showLoading(true);

        // Construct the query with pagination if needed
        String paginatedQuery = query;
        if (currentPage > 0) {
            paginatedQuery += "&start=" + (currentPage * ITEMS_PER_PAGE);
        }

        Log.d(TAG, "Performing shopping search with query: " + paginatedQuery + ", page: " + currentPage);

        // Using the tbm=shop specific endpoint to get only shopping results
        Call<JsonObject> call = RetrofitClient.getSerpApiService().searchShoppingResults(
                paginatedQuery,
                DEFAULT_LOCATION,
                DEFAULT_LANGUAGE,
                DEFAULT_COUNTRY,
                API_KEY);

        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                showLoading(false);

                if (response.isSuccessful() && response.body() != null) {
                    JsonObject jsonResponse = response.body();
                    Log.d(TAG, "Response received: "
                            + jsonResponse.toString().substring(0, Math.min(jsonResponse.toString().length(), 500))
                            + "...");

                    List<ShoppingResult> results = parseShoppingResults(jsonResponse);

                    if (results != null && !results.isEmpty()) {
                        Log.d(TAG, "Found " + results.size() + " shopping results");

                        if (isLoadingMore) {
                            // Append to existing results
                            productAdapter.appendProducts(results);
                        } else {
                            // Replace existing results
                            productAdapter.updateProducts(results, true);
                        }

                        showResults();

                        // If we received fewer items than requested, we've reached the end
                        hasMoreItems = results.size() >= ITEMS_PER_PAGE;
                    } else {
                        if (!isLoadingMore) {
                            // Only show no results for the initial search
                            showNoResults();
                        } else {
                            // For subsequent pages, just mark that we have no more items
                            hasMoreItems = false;
                        }
                    }
                } else {
                    int errorCode = response.code();
                    Log.e(TAG, "API Error: " + errorCode);

                    if (response.errorBody() != null) {
                        try {
                            String errorBody = response.errorBody().string();
                            Log.e(TAG, "Error body: " + errorBody);
                        } catch (Exception e) {
                            Log.e(TAG, "Error reading error body", e);
                        }
                    }

                    showError("Error: " + errorCode);
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Log.e(TAG, "Network error", t);
                showLoading(false);
                showError("Network error: " + t.getMessage());
            }
        });
    }

    private List<ShoppingResult> parseShoppingResults(JsonObject response) {
        try {
            // First try the standard shopping_results key which should be available with
            // tbm=shop
            if (response.has("shopping_results")) {
                JsonArray shoppingResultsArray = response.getAsJsonArray("shopping_results");
                Type listType = new TypeToken<List<ShoppingResult>>() {
                }.getType();
                List<ShoppingResult> results = gson.fromJson(shoppingResultsArray, listType);

                // Apply price filtering
                results = filterByPriceRange(results);

                // Limit to 10 results
                if (results.size() > MAX_RESULTS) {
                    results = results.subList(0, MAX_RESULTS);
                    // Since we're limiting to MAX_RESULTS, we don't need to load more
                    hasMoreItems = false;
                }

                return results;
            }

            // Try alternative keys that might contain product information
            String[] possibleKeys = { "shopping_results", "inline_shopping_results", "product_results", "products" };

            for (String key : possibleKeys) {
                if (response.has(key) && response.get(key).isJsonArray()) {
                    JsonArray resultsArray = response.getAsJsonArray(key);
                    if (resultsArray.size() > 0) {
                        Type listType = new TypeToken<List<ShoppingResult>>() {
                        }.getType();
                        List<ShoppingResult> results = gson.fromJson(resultsArray, listType);

                        if (results != null && !results.isEmpty()) {
                            // Apply price filtering
                            results = filterByPriceRange(results);

                            // Limit to 10 results
                            if (results.size() > MAX_RESULTS) {
                                results = results.subList(0, MAX_RESULTS);
                                hasMoreItems = false;
                            }

                            return results;
                        }
                    }
                }
            }

            // If we can't find any relevant results in standard fields, use contextual mock
            // data
            if (getContext() != null) {
                Log.d(TAG, "No standard result fields found, creating appropriate mock data");
                return getContextualMockResults();
            }

        } catch (Exception e) {
            Log.e(TAG, "Error parsing shopping results", e);
        }

        return Collections.emptyList();
    }

    /**
     * Filter results by price range
     */
    private List<ShoppingResult> filterByPriceRange(List<ShoppingResult> results) {
        if (results == null || results.isEmpty()) {
            return results;
        }

        // If no price range is set, return all results
        if (currentMinPrice.isEmpty() && currentMaxPrice.isEmpty()) {
            return results;
        }

        List<ShoppingResult> filteredResults = new ArrayList<>();
        double minPrice = currentMinPrice.isEmpty() ? Double.MIN_VALUE : Double.parseDouble(currentMinPrice);
        double maxPrice = currentMaxPrice.isEmpty() ? Double.MAX_VALUE : Double.parseDouble(currentMaxPrice);

        for (ShoppingResult result : results) {
            try {
                // Use the extracted price for comparison since it's already a double
                double price = result.getExtractedPrice();

                // If the price is 0, try to parse from the price string (remove $, ,, etc.)
                if (price == 0 && result.getPrice() != null && !result.getPrice().isEmpty()) {
                    String priceStr = result.getPrice().replaceAll("[^0-9.]", "");
                    if (!priceStr.isEmpty()) {
                        price = Double.parseDouble(priceStr);
                    }
                }

                // Add the result if it's within the price range
                if (price >= minPrice && price <= maxPrice) {
                    filteredResults.add(result);
                }
            } catch (NumberFormatException e) {
                // Skip results with unparseable prices
                Log.e(TAG, "Error parsing price: " + result.getPrice(), e);
            }
        }

        return filteredResults;
    }

    private List<ShoppingResult> getContextualMockResults() {
        List<ShoppingResult> mockResults;

        // Check if the query is about mobile phones
        if (lastQuery.toLowerCase().contains("mobile") ||
                lastQuery.toLowerCase().contains("phone") ||
                lastQuery.toLowerCase().contains("smartphone") ||
                lastQuery.toLowerCase().contains("iphone") ||
                lastQuery.toLowerCase().contains("android") ||
                lastQuery.toLowerCase().contains("samsung")) {

            mockResults = MockProductData.getMockMobilePhones();
        } else {
            // Default to gaming mice
            mockResults = MockProductData.getMockGamingMouse();
        }

        // Apply price filtering to mock data too
        mockResults = filterByPriceRange(mockResults);

        // Limit to max 10 results
        if (mockResults.size() > MAX_RESULTS) {
            mockResults = mockResults.subList(0, MAX_RESULTS);
        }

        return mockResults;
    }

    private void showNoResults() {
        if (productList.isEmpty()) {
            // Use contextual mock data instead of showing empty results
            List<ShoppingResult> mockData = getContextualMockResults();
            productAdapter.updateProducts(mockData, true);
            showResults();

            Toast.makeText(getContext(), "No products found. Showing recommended products instead.", Toast.LENGTH_SHORT)
                    .show();
        }
    }

    private void showError(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();

        if (productList.isEmpty()) {
            // Only load mock data on error if we don't already have results
            List<ShoppingResult> mockData = getContextualMockResults();
            productAdapter.updateProducts(mockData, true);
            showResults();
        }
    }

    @Override
    public void onProductClick(ShoppingResult product) {
        // Store the last clicked time and product for double click detection
        if (lastClickedProduct != null && lastClickedProduct.equals(product) &&
                (System.currentTimeMillis() - lastClickTime < DOUBLE_CLICK_TIMEOUT)) {
            // Double click detected - open product link in browser
            if (product.getLink() != null && !product.getLink().isEmpty()) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(product.getLink()));
                startActivity(browserIntent);
            }
        } else {
            // First click - show product details or highlight
            Toast.makeText(getContext(), "Double tap to visit " + product.getSource(), Toast.LENGTH_SHORT).show();
        }

        // Update the last clicked product and time
        lastClickedProduct = product;
        lastClickTime = System.currentTimeMillis();
    }
}