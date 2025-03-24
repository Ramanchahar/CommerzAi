package com.example.commerzai;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class SavedFragment extends Fragment {
    private RecyclerView savedItemsRecyclerView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_saved, container, false);

        savedItemsRecyclerView = view.findViewById(R.id.savedItemsRecyclerView);
        savedItemsRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));

        // TODO: Set up adapter and populate with saved items data

        return view;
    }
}