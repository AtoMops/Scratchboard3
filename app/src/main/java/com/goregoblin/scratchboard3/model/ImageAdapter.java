package com.goregoblin.scratchboard3.model;


import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.goregoblin.scratchboard3.R;

import java.util.List;

public class ImageAdapter extends BaseAdapter {

    // https://abhiandroid.com/ui/baseadapter-tutorial-example.html

    Context context;
    private List<BitmapDataObject> lstImgView;
    LayoutInflater layoutInflater;

    public ImageAdapter(Context appContext, List<BitmapDataObject> lstImgViewIn) {

        this.context = appContext;
        this.lstImgView = lstImgViewIn;
        layoutInflater = (LayoutInflater.from(appContext));

    }

    public int getCount() {
        return lstImgView.size();
    }

    public Object getItem(int position) {
        return lstImgView.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    // Create a new ImageView for each item referenced by the Adapter
    public View getView(int position, View convertView, ViewGroup parent) {
        convertView = layoutInflater.inflate(R.layout.activity_icon, null);//set layout for displaying items
        ImageView icon = (ImageView) convertView.findViewById(R.id.icon);//get id for image view


        if (!lstImgView.isEmpty()) {
            Bitmap bitmap = lstImgView.get(position).getCurrentImage();
            icon.setImageBitmap(bitmap);
        }

        return convertView;
    }
}
