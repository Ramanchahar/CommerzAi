package com.example.commerzai;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
    private List<ShoppingResult> savedProducts;
    private DatabaseReference savedProductsRef;
    private FirebaseAuth mAuth;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_saved, container, false);

        savedItemsRecyclerView = view.findViewById(R.id.savedItemsRecyclerView);
        savedItemsRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));

        savedProducts = new ArrayList<>();
        productAdapter = new ProductAdapter(getContext(), savedProducts, product -> {
            // Handle product click if needed
        });
        savedItemsRecyclerView.setAdapter(productAdapter);

        mAuth = FirebaseAuth.getInstance();
        savedProductsRef = FirebaseDatabase.getInstance().getReference("saved_products");

        loadSavedProducts();

        return view;
    }

    private void loadSavedProducts() {
        if (mAuth.getCurrentUser() != null) {
            String userId = mAuth.getCurrentUser().getUid();
            
            savedProductsRef.orderByChild("userId").equalTo(userId)
                    .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            savedProducts.clear();
                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                SavedProduct savedProduct = snapshot.getValue(SavedProduct.class);
                                if (savedProduct != null) {
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
                            }
                            productAdapter.updateProducts(savedProducts);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            Toast.makeText(getContext(), "Failed to load saved products", Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            Toast.makeText(getContext(), "Please sign in to view saved products", Toast.LENGTH_SHORT).show();
        }
    }
}