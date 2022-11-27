package com.microsoft.mobile.polymer.mishtu.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class WelcomeViewPagerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private ArrayList<Integer> layouts;

    public WelcomeViewPagerAdapter(ArrayList<Integer> layouts) {
        this.layouts = layouts;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(viewType, parent, false);
        return new SliderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

    }

    @Override
    public int getItemViewType(int position) {
        return layouts.get(position);
    }

    @Override
    public int getItemCount() {
        return layouts.size();
    }

    public static class SliderViewHolder extends RecyclerView.ViewHolder {
        public TextView title, year, genre;

        public SliderViewHolder(View view) {
            super(view);
        }
    }
}
