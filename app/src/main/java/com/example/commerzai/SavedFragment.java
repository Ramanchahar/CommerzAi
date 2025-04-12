package com.example.commerzai;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.example.commerzai.models.SavedProduct;
import com.example.commerzai.models.ShoppingResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class SavedFragment extends Fragment {
    private RecyclerView savedItemsRecyclerView;
    private ProductAdapter productAdapter;
    private final List<ShoppingResult> savedProducts = new ArrayList<>();
    private final List<ShoppingResult> filteredProducts = new ArrayList<>();
    private DatabaseReference savedProductsRef;
    private FirebaseAuth mAuth;
    private ProgressBar progressBar;
    private LinearLayout emptyStateLayout;
    private EditText searchEditText;
    private ImageButton searchButton;

    private static final String TAG = "SavedFragment";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_saved, container, false);

        savedItemsRecyclerView = view.findViewById(R.id.savedItemsRecyclerView);
        progressBar = view.findViewById(R.id.progressBar);
        emptyStateLayout = view.findViewById(R.id.emptyStateLayout);
        searchEditText = view.findViewById(R.id.searchEditText);
        searchButton = view.findViewById(R.id.searchButton);

        savedItemsRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));

        productAdapter = new ProductAdapter(getContext(), filteredProducts, product -> {});
        savedItemsRecyclerView.setAdapter(productAdapter);

        mAuth = FirebaseAuth.getInstance();
        savedProductsRef = FirebaseDatabase.getInstance().getReference("saved_products");

        setupSearch();
        return view;
    }

    private void setupSearch() {
        searchButton.setOnClickListener(v -> {
            String searchQuery = searchEditText.getText().toString().toLowerCase().trim();
            filterProducts(searchQuery);
        });

        // Optional: Add real-time search as user types
        searchEditText.setOnEditorActionListener((v, actionId, event) -> {
            String searchQuery = searchEditText.getText().toString().toLowerCase().trim();
            filterProducts(searchQuery);
            return true;
        });
    }

    private void filterProducts(String searchQuery) {
        filteredProducts.clear();

        if (searchQuery.isEmpty()) {
            filteredProducts.addAll(savedProducts);
        } else {
            for (ShoppingResult product : savedProducts) {
                if (product.getTitle().toLowerCase().contains(searchQuery)) {
                    filteredProducts.add(product);
                }
            }
        }

        productAdapter.updateProducts(filteredProducts, false);
        updateUI();
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume() called");
        loadSavedProducts();
    }

    private void loadSavedProducts() {
        if (mAuth.getCurrentUser() != null) {
            showLoading();
            String userId = mAuth.getCurrentUser().getUid();
            Log.d(TAG, "Current userId: " + userId);

            savedProductsRef.orderByChild("userId").equalTo(userId)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            Log.d(TAG, "Entered onDataChange(), children count: " + dataSnapshot.getChildrenCount());

                            savedProducts.clear();
                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                Log.d(TAG, "Raw snapshot: " + snapshot);
                                SavedProduct savedProduct = snapshot.getValue(SavedProduct.class);
                                if (savedProduct == null) {
                                    Log.w(TAG, "savedProduct is null — possible model mismatch");
                                    continue;
                                }

                                Log.d(TAG, "Parsed title: " + savedProduct.getTitle());

                                ShoppingResult product = new ShoppingResult();
                                product.setTitle(savedProduct.getTitle());
                                product.setPrice(savedProduct.getPrice());
                                product.setSource(savedProduct.getSource());
                                product.setThumbnail(savedProduct.getThumbnail());
                                product.setLink(savedProduct.getLink());
                                product.setRating(savedProduct.getRating());
                                product.setReviews(savedProduct.getReviews());
                                savedProducts.add(product);
                            }

                            Log.d(TAG, "Total products loaded: " + savedProducts.size());
                            //Toast.makeText(getContext(), "Loaded: " + savedProducts.size(), Toast.LENGTH_SHORT).show();

                            // Apply any existing search filter
                            String currentSearch = searchEditText.getText().toString().toLowerCase().trim();
                            filterProducts(currentSearch);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            hideLoading();
                            Log.e(TAG, "Firebase error: " + databaseError.getMessage());
                            Toast.makeText(getContext(), "Failed to load saved products", Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            showEmptyState();
            Toast.makeText(getContext(), "Please sign in to view saved products", Toast.LENGTH_SHORT).show();
        }
    }

    private void showLoading() {
        progressBar.setVisibility(View.VISIBLE);
        savedItemsRecyclerView.setVisibility(View.GONE);
        emptyStateLayout.setVisibility(View.GONE);
        Log.d(TAG, "showLoading() called");
    }

    private void hideLoading() {
        progressBar.setVisibility(View.GONE);
        Log.d(TAG, "hideLoading() called");
    }

    private void showEmptyState() {
        progressBar.setVisibility(View.GONE);
        savedItemsRecyclerView.setVisibility(View.GONE);
        emptyStateLayout.setVisibility(View.VISIBLE);
        Log.d(TAG, "showEmptyState() called");
    }

    private void showResults() {
        progressBar.setVisibility(View.GONE);
        emptyStateLayout.setVisibility(View.GONE);
        savedItemsRecyclerView.setVisibility(View.VISIBLE);
        Log.d(TAG, "showResults() called");
    }

    private void updateUI() {
        hideLoading();
        Log.d(TAG, "savedProducts size in updateUI: " + savedProducts.size());
        if (savedProducts.isEmpty()) {
            showEmptyState();
        } else {
            showResults();
        }
    }
}
