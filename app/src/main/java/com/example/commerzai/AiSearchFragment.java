package com.example.commerzai;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class AiSearchFragment extends Fragment {
    private EditText searchInput;
    private ImageButton searchButton;
    private RecyclerView searchResultsRecyclerView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_ai_search, container, false);

        searchInput = view.findViewById(R.id.searchInput);
        searchButton = view.findViewById(R.id.searchButton);
        searchResultsRecyclerView = view.findViewById(R.id.searchResultsRecyclerView);

        searchResultsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        searchButton.setOnClickListener(v -> {
            String query = searchInput.getText().toString().trim();
            if (!query.isEmpty()) {
                // TODO: Implement AI search functionality
                performAiSearch(query);
            }
        });

        return view;
    }

    private void performAiSearch(String query) {
        // TODO: Implement the AI search logic using Perplexity or other AI model
        // This will be implemented when connecting to the backend
    }
}