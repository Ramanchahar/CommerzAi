package com.example.commerzai.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;
import com.example.commerzai.R;
import com.example.commerzai.models.ShoppingResult;

import java.util.ArrayList;
import java.util.List;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {
    private List<ShoppingResult> productList;
    private Context context;
    private OnProductClickListener listener;

    public interface OnProductClickListener {
        void onProductClick(ShoppingResult product);
    }

    public ProductAdapter(Context context, List<ShoppingResult> productList, OnProductClickListener listener) {
        this.context = context;
        this.productList = productList != null ? productList : new ArrayList<>();
        this.listener = listener;
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

        // Set click listener
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onProductClick(product);
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
     */
    public void updateProducts(List<ShoppingResult> newProducts) {
        this.productList.clear();
        if (newProducts != null) {
            this.productList.addAll(newProducts);
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

        ProductViewHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.productImageView);
            titleTextView = itemView.findViewById(R.id.productTitleTextView);
            priceTextView = itemView.findViewById(R.id.productPriceTextView);
            sourceTextView = itemView.findViewById(R.id.productSourceTextView);
            reviewsTextView = itemView.findViewById(R.id.productReviewsTextView);
            ratingBar = itemView.findViewById(R.id.productRatingBar);
        }
    }
}