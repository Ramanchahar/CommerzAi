package com.example.commerzai;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.commerzai.adapters.ProductAdapter;
import com.example.commerzai.api.RetrofitClient;
import com.example.commerzai.api.SerpApiService;
import com.example.commerzai.models.SavedProduct;
import com.example.commerzai.models.SerpApiResponse;
import com.example.commerzai.models.ShoppingResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.util.*;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeFragment extends Fragment {
    private static final String TAG = "HomeFragment";
    private RecyclerView forYouRecyclerView;
    private RecyclerView flashSaleRecyclerView;

    private DatabaseReference savedRef, allProductsRef;
    private FirebaseAuth mAuth;

    private final List<ShoppingResult> savedList = new ArrayList<>();
    private final List<ShoppingResult> productPool = new ArrayList<>();

    private final int MAX_RECOMMENDED = 5;
    private final int TOTAL_ITEMS = 12;

    private final String API_KEY = "4b79b1b4698de4415772004064775e1f1696ab5baf41235284c608f1578f4e19";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        forYouRecyclerView = view.findViewById(R.id.forYouRecyclerView);
        flashSaleRecyclerView = view.findViewById(R.id.flashSaleRecyclerView);

        forYouRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        flashSaleRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        mAuth = FirebaseAuth.getInstance();
        savedRef = FirebaseDatabase.getInstance().getReference("saved_products");
        allProductsRef = FirebaseDatabase.getInstance().getReference("products");

        loadLocalDataThenQueryAPI();

        return view;
    }

    private void loadLocalDataThenQueryAPI() {
        fetchAllProducts(pool -> {
            productPool.clear();
            productPool.addAll(pool);
            Log.d(TAG, "Loaded product pool: " + pool.size());

            fetchSavedProducts(saved -> {
                savedList.clear();
                savedList.addAll(saved);
                Log.d(TAG, "Loaded saved products: " + saved.size());

                fetchFromApiAndDisplay();
            });
        });
    }

    private void fetchFromApiAndDisplay() {
        SerpApiService api = RetrofitClient.getSerpApiService();

        api.searchShoppingResults("popular electronics", "United States", "en", "us", API_KEY)
                .enqueue(new Callback<JsonObject>() {
                    @Override
                    public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            try {
                                SerpApiResponse parsed = new Gson().fromJson(response.body(), SerpApiResponse.class);
                                List<ShoppingResult> apiResults = parsed.getShoppingResults();
                                Log.d(TAG, "API products fetched: " + apiResults.size());

                                for (ShoppingResult result : apiResults) {
                                    String key = allProductsRef.push().getKey();
                                    allProductsRef.child(key).setValue(result);
                                }

                                productPool.addAll(apiResults);
                            } catch (Exception e) {
                                Log.e(TAG, "Parsing error", e);
                            }
                        } else {
                            Log.w(TAG, "API response not successful");
                        }

                        displayFinalRecommendation();
                    }

                    @Override
                    public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                        Log.e(TAG, "API error: " + t.getMessage());
                        displayFinalRecommendation();
                    }
                });
    }

    private void displayFinalRecommendation() {
        Map<String, Integer> keywordFreq = extractKeywordsFromSavedTitles(savedList);
        Log.d(TAG, "Extracted keywords from saved:");
        for (String k : keywordFreq.keySet()) {
            Log.d(TAG, k + " -> " + keywordFreq.get(k));
        }

        List<ShoppingResult> recommended = selectProductsByTitleKeywords(productPool, keywordFreq, MAX_RECOMMENDED);

        Set<String> usedTitles = new HashSet<>();
        for (ShoppingResult p : recommended) usedTitles.add(p.getTitle());

        Collections.shuffle(productPool);
        for (ShoppingResult p : productPool) {
            if (!usedTitles.contains(p.getTitle())) {
                recommended.add(p);
                usedTitles.add(p.getTitle());
            }
            if (recommended.size() >= TOTAL_ITEMS) break;
        }

        Log.d(TAG, "Final For You list size: " + recommended.size());
        forYouRecyclerView.setAdapter(new ProductAdapter(getContext(), recommended, product -> {}));
        flashSaleRecyclerView.setAdapter(new ProductAdapter(getContext(), getRandomProducts(productPool, 10), product -> {}));
    }

    private Map<String, Integer> extractKeywordsFromSavedTitles(List<ShoppingResult> savedList) {
        Map<String, Integer> keywordCount = new HashMap<>();
        for (ShoppingResult product : savedList) {
            String title = product.getTitle().toLowerCase();
            String[] words = title.split("\\s+|,|\\.|-|\\(|\\)|:");
            for (String word : words) {
                if (word.length() >= 3) {
                    keywordCount.put(word, keywordCount.getOrDefault(word, 0) + 1);
                }
            }
        }
        return keywordCount;
    }

    private List<ShoppingResult> selectProductsByTitleKeywords(
            List<ShoppingResult> pool,
            Map<String, Integer> keywordCount,
            int maxResults) {

        List<ShoppingResult> scoredList = new ArrayList<>();

        for (ShoppingResult product : pool) {
            String title = product.getTitle().toLowerCase();
            int score = 0;

            for (String keyword : keywordCount.keySet()) {
                if (title.contains(keyword)) {
                    score += keywordCount.get(keyword);
                }
            }

            if (score > 0) {
                product.setReviews(score);
                scoredList.add(product);
            }
        }

        scoredList.sort((a, b) -> b.getReviews() - a.getReviews());

        return scoredList.subList(0, Math.min(scoredList.size(), maxResults));
    }

    private List<ShoppingResult> getRandomProducts(List<ShoppingResult> pool, int count) {
        Collections.shuffle(pool);
        return new ArrayList<>(pool.subList(0, Math.min(pool.size(), count)));
    }

    private void fetchAllProducts(OnProductsFetchedListener listener) {
        allProductsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<ShoppingResult> all = new ArrayList<>();
                for (DataSnapshot data : snapshot.getChildren()) {
                    ShoppingResult p = data.getValue(ShoppingResult.class);
                    if (p != null) all.add(p);
                }
                listener.onFetched(all);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Failed to load product pool: " + error.getMessage());
                listener.onFetched(new ArrayList<>());
            }
        });
    }

    private void fetchSavedProducts(OnProductsFetchedListener listener) {
        if (mAuth.getCurrentUser() == null) {
            listener.onFetched(new ArrayList<>());
            return;
        }

        String uid = mAuth.getCurrentUser().getUid();
        savedRef.orderByChild("userId").equalTo(uid)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        List<ShoppingResult> saved = new ArrayList<>();
                        for (DataSnapshot data : snapshot.getChildren()) {
                            SavedProduct sp = data.getValue(SavedProduct.class);
                            if (sp != null) {
                                ShoppingResult p = new ShoppingResult();
                                p.setTitle(sp.getTitle());
                                p.setPrice(sp.getPrice());
                                p.setSource(sp.getSource());
                                p.setThumbnail(sp.getThumbnail());
                                p.setLink(sp.getLink());
                                p.setRating(sp.getRating());
                                p.setReviews(sp.getReviews());
                                saved.add(p);
                            }
                        }
                        listener.onFetched(saved);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        listener.onFetched(new ArrayList<>());
                    }
                });
    }

    interface OnProductsFetchedListener {
        void onFetched(List<ShoppingResult> products);
    }
}
