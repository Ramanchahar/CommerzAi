package com.example.commerzai.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;
import com.example.commerzai.R;
import com.example.commerzai.models.SavedProduct;
import com.example.commerzai.models.ShoppingResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {
    private List<ShoppingResult> productList;
    private Context context;
    private OnProductClickListener listener;
    private DatabaseReference savedProductsRef;
    private FirebaseAuth mAuth;
    private Map<String, String> savedProductIds; // Map to store thumbnail -> productId

    public interface OnProductClickListener {
        void onProductClick(ShoppingResult product);
    }

    public ProductAdapter(Context context, List<ShoppingResult> productList, OnProductClickListener listener) {
        this.context = context;
        this.productList = productList != null ? productList : new ArrayList<>();
        this.listener = listener;
        this.mAuth = FirebaseAuth.getInstance();
        this.savedProductsRef = FirebaseDatabase.getInstance().getReference("saved_products");
        this.savedProductIds = new HashMap<>();
        loadSavedProducts();
    }

    private void loadSavedProducts() {
        if (mAuth.getCurrentUser() != null) {
            String userId = mAuth.getCurrentUser().getUid();
            savedProductsRef.orderByChild("userId").equalTo(userId)
                    .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            savedProductIds.clear();
                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                SavedProduct savedProduct = snapshot.getValue(SavedProduct.class);
                                if (savedProduct != null && savedProduct.getThumbnail() != null) {
                                    savedProductIds.put(savedProduct.getThumbnail(), snapshot.getKey());
                                }
                            }
                            notifyDataSetChanged();
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            Toast.makeText(context, "Failed to load saved products", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_product, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        ShoppingResult product = productList.get(position);

        holder.titleTextView.setText(product.getTitle());
        holder.priceTextView.setText(product.getPrice());
        holder.sourceTextView.setText("From: " + product.getSource());

        if (product.getRating() > 0) {
            holder.ratingBar.setVisibility(View.VISIBLE);
            holder.reviewsTextView.setVisibility(View.VISIBLE);
            holder.ratingBar.setRating((float) product.getRating());
            holder.reviewsTextView.setText(product.getReviewsOriginal() != null
                    ? product.getReviewsOriginal()
                    : String.valueOf(product.getReviews()));
        } else {
            holder.ratingBar.setVisibility(View.GONE);
            holder.reviewsTextView.setVisibility(View.GONE);
        }

        // Clear previous image to avoid flickering during recycling
        holder.imageView.setImageResource(R.drawable.placeholder_image);

        // Load product image using Glide with optimizations
        if (product.getThumbnail() != null && !product.getThumbnail().isEmpty()) {
            RequestOptions options = new RequestOptions()
                    .placeholder(R.drawable.placeholder_image)
                    .error(R.drawable.error_image);

            Glide.with(context)
                    .load(product.getThumbnail())
                    .apply(options)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .into(holder.imageView);
        }

        // Set click listener for the entire item
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onProductClick(product);
            }
        });

        // Update like button state based on whether product is saved
        boolean isSaved = savedProductIds.containsKey(product.getThumbnail());
        holder.likeButton.setImageResource(isSaved ? R.drawable.ic_favorite : R.drawable.ic_favorite_border);

        // Set click listener for the like button
        holder.likeButton.setOnClickListener(v -> {
            if (mAuth.getCurrentUser() != null) {
                String userId = mAuth.getCurrentUser().getUid();
                String productId = savedProductIds.get(product.getThumbnail());

                if (productId != null) {
                    // Product is already saved, remove it
                    savedProductsRef.child(productId).removeValue()
                            .addOnSuccessListener(aVoid -> {
                                holder.likeButton.setImageResource(R.drawable.ic_favorite_border);
                                Toast.makeText(context, "Product removed from saved items", Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(context, "Failed to remove product", Toast.LENGTH_SHORT).show();
                            });
                } else {
                    // Product is not saved, save it
                    SavedProduct savedProduct = new SavedProduct(product, userId);
                    String newProductId = savedProductsRef.push().getKey();
                    savedProduct.setId(newProductId);

                    savedProductsRef.child(newProductId).setValue(savedProduct)
                            .addOnSuccessListener(aVoid -> {
                                holder.likeButton.setImageResource(R.drawable.ic_favorite);
                                Toast.makeText(context, "Product saved!", Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(context, "Failed to save product", Toast.LENGTH_SHORT).show();
                            });
                }
            } else {
                Toast.makeText(context, "Please sign in to save products", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    @Override
    public long getItemId(int position) {
        // Stable IDs help with recycling
        return productList.get(position).hashCode();
    }

    /**
     * Replace all products with new ones
     * @param newProducts The new products to display
     * @param isSearchResults Whether this is for search results (true) or saved products (false)
     */
    public void updateProducts(List<ShoppingResult> newProducts, boolean isSearchResults) {
        if (isSearchResults) {
            // For search results, clear existing items and add new ones
            productList.clear();
            if (newProducts != null) {
                productList.addAll(newProducts);
            }
        } else {
            // For saved products, just add new items without clearing
            if (newProducts == null) {
                productList.addAll(newProducts);
            }
        }
        notifyDataSetChanged();
    }


    /**
     * Add more products to the existing list (for pagination/infinite scrolling)
     */
    public void appendProducts(List<ShoppingResult> additionalProducts) {
        if (additionalProducts == null || additionalProducts.isEmpty()) {
            return;
        }

        int startPosition = productList.size();
        this.productList.addAll(additionalProducts);
        notifyItemRangeInserted(startPosition, additionalProducts.size());
    }

    /**
     * Clear all products from the adapter
     */
    public void clearProducts() {
        int count = productList.size();
        productList.clear();
        notifyItemRangeRemoved(0, count);
    }

    static class ProductViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView titleTextView;
        TextView priceTextView;
        TextView sourceTextView;
        TextView reviewsTextView;
        RatingBar ratingBar;
        ImageButton likeButton;

        ProductViewHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.productImageView);
            titleTextView = itemView.findViewById(R.id.productTitleTextView);
            priceTextView = itemView.findViewById(R.id.productPriceTextView);
            sourceTextView = itemView.findViewById(R.id.productSourceTextView);
            reviewsTextView = itemView.findViewById(R.id.productReviewsTextView);
            ratingBar = itemView.findViewById(R.id.productRatingBar);
            likeButton = itemView.findViewById(R.id.likeButton);
        }
    }
}