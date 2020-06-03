package com.example.mymaps;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class CustomAdapter extends BaseAdapter {
        Context context;
        int images[];
        String[] items;
        LayoutInflater inflater;

        public CustomAdapter(Context applicationContext, int[] images, String[] items) {
            this.context = applicationContext;
            this.images = images;
            this.items = items;
            inflater = (LayoutInflater.from(applicationContext));
        }

        @Override
        public int getCount() {
            return images.length;
        }

        @Override
        public Object getItem(int i) {
            return null;
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            view = inflater.inflate(R.layout.custom_spinner_items, null);
            ImageView icon = (ImageView) view.findViewById(R.id.imageView);
            TextView names = (TextView) view.findViewById(R.id.textView);
            icon.setImageResource(images[i]);
            names.setText(items[i]);
            return view;
        }
    }
