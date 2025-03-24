package com.example.commerzai;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class HomeFragment extends Fragment {
    private RecyclerView forYouRecyclerView;
    private RecyclerView flashSaleRecyclerView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // Initialize RecyclerViews
        forYouRecyclerView = view.findViewById(R.id.forYouRecyclerView);
        flashSaleRecyclerView = view.findViewById(R.id.flashSaleRecyclerView);

        // Set up horizontal layout for "For You" section
        LinearLayoutManager forYouLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL,
                false);
        forYouRecyclerView.setLayoutManager(forYouLayoutManager);

        // Set up grid layout for Flash Sale section
        LinearLayoutManager flashSaleLayoutManager = new LinearLayoutManager(getContext());
        flashSaleRecyclerView.setLayoutManager(flashSaleLayoutManager);

        // TODO: Set up adapters and populate with data
        // This would be implemented when connecting to the backend

        return view;
    }
}