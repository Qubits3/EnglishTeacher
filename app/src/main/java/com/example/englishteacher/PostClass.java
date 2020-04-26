package com.example.englishteacher;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;

public class PostClass extends ArrayAdapter<String> {

    private ArrayList<String> words;
    private final Activity context;

    PostClass(ArrayList<String> words, @NonNull Activity context) {
        super(context, R.layout.custom_view, words);
        this.words = words;
        this.context = context;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        LayoutInflater inflater = context.getLayoutInflater();
        @SuppressLint({"ViewHolder", "InflateParams"}) View customView = inflater.inflate(R.layout.custom_view, null, false);

        TextView textView = customView.findViewById(R.id.textView);
        textView.setText(words.get(position));

        return customView;
    }

}
