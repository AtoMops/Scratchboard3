package com.goregoblin.scratchboard3.logic;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;

import com.goregoblin.scratchboard3.R;
import com.goregoblin.scratchboard3.gui.SecondActivity;
import com.goregoblin.scratchboard3.model.BitmapDataObject;
import com.goregoblin.scratchboard3.model.ImageAdapter;

import java.util.ArrayList;


public class SecondActivityListener implements AdapterView.OnItemClickListener {

        SecondActivity secondActivity;
        ArrayList<BitmapDataObject> lstGetBitmaps;


    public SecondActivityListener(SecondActivity secondActivity) {
        this.secondActivity = secondActivity;

        ActionBar actionBar = secondActivity.getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        lstGetBitmaps = new ArrayList<BitmapDataObject>();
        lstGetBitmaps.add(null); // das hier um zu verhindern das die Liste leer ist; das läuft in Android etwas anders als in Java oO'

        //get the bundle
        Bundle bundle = secondActivity.getIntent().getExtras();

        //getting the arraylist from the key
        if (!bundle.isEmpty()) {
            lstGetBitmaps = (ArrayList<BitmapDataObject>) bundle.getSerializable("myList");
        }

        if (lstGetBitmaps != null) {
            ImageAdapter imageAdapter = new ImageAdapter(secondActivity, lstGetBitmaps);
            secondActivity.gridViewLoad.setAdapter(imageAdapter);
            imageAdapter.notifyDataSetChanged();
        }

    } // end MainActivityListener-constructor


    /* das hier um zu sagen "wir wollen ein Menu erstellen"
        man könnte auch ein spezifisches Menu ansteuern..
     */
        public boolean onCreateOptionsMenu (Menu menu){
        return true;
    }

        // Button oben im Reiter-Menü (oben links der Pfeil im SmartPhone)
        public boolean onOptionsItemSelected (MenuItem item){
        switch (item.getItemId()) {
            case android.R.id.home: // der Pfeil oben links ist der "Home-Button"
                // hier was passieren soll
                secondActivity.finish(); // Standard ist wir beenden diese AppCompatActivity und gehen zur MainActivity zurück
                return true;
        }
        return false;
    }


        private void loadImage ( int position){
            Intent intent = new Intent();
            intent.putExtra("imageToLoad", position);

            secondActivity.setResult(Activity.RESULT_OK, intent);
            secondActivity.finish();

        }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        AlertDialog.Builder builder = new AlertDialog.Builder(secondActivity);
        builder.setMessage("Load Image?");

        //   View customLayout = secondActivity.getLayoutInflater().inflate(R.layout.alert_options_menu, null);

        builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // hier laden
                loadImage(position);
            }
        });

        builder.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // hier nichts bzw zurück
            }
        });

        // builder.setView(customLayout);
        builder.create().show();

    }


}



