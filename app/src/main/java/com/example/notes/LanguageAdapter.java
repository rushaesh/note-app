package com.example.notes;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class LanguageAdapter extends RecyclerView.Adapter<LanguageAdapter.LanguageViewHolder> {
    private Context context;
    private List<LanguageItem> languages;
    private int selectedPosition = 0;

    // Interface for sending language change callback
    public interface OnLanguageChangeListener {
        void onLanguageChanged(String languageCode);
    }

    private OnLanguageChangeListener languageChangeListener;

    public LanguageAdapter(Context context, List<LanguageItem> languages, OnLanguageChangeListener listener) {
        this.context = context;
        this.languages = languages;
        this.languageChangeListener = listener;
    }

    @Override
    public LanguageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.recycler_item_language, parent, false);
        return new LanguageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(LanguageViewHolder holder, @SuppressLint("RecyclerView") int position) {
        LanguageItem languageItem = languages.get(position);
        holder.languageTextView.setText(languageItem.getLanguageName());
        holder.languagee.setText(languageItem.getLanguage());
        if (selectedPosition == position) {
            holder.linearLayout.setBackgroundResource(R.drawable.textviewbackgrounf);
            holder.checkIcon.setVisibility(View.VISIBLE);
        } else {
            holder.linearLayout.setBackgroundColor(Color.parseColor("#071323"));
            holder.checkIcon.setVisibility(View.GONE);
        }


        holder.itemView.setOnClickListener(v -> {
            selectedPosition = position;
            notifyDataSetChanged();

            SharedPreferences sharedPreferences = context.getSharedPreferences("AppSettings", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("SelectedLanguageName", languageItem.getLanguageName());
            editor.putString("SelectedLanguage", languageItem.getLanguage());
            editor.apply();
            languageChangeListener.onLanguageChanged(languageItem.getLanguageCode());
        });

    }

    @Override
    public int getItemCount() {
        return languages.size();
    }

    // ViewHolder for RecyclerView
    public class LanguageViewHolder extends RecyclerView.ViewHolder {
        TextView languageTextView,languagee;
        ImageView checkIcon;
        LinearLayout linearLayout;

        public LanguageViewHolder(View itemView) {
            super(itemView);
            languageTextView = itemView.findViewById(R.id.languageTextView);
            checkIcon = itemView.findViewById(R.id.checkIcon);
            linearLayout = itemView.findViewById(R.id.linner);
            languagee = itemView.findViewById(R.id.language);
        }
    }
}
