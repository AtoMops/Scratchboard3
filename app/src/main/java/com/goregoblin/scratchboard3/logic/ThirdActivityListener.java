package com.goregoblin.scratchboard3.logic;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;

import com.goregoblin.scratchboard3.R;
import com.goregoblin.scratchboard3.gui.SecondActivity;
import com.goregoblin.scratchboard3.gui.ThirdActivity;
import com.goregoblin.scratchboard3.model.BitmapDataObject;
import com.goregoblin.scratchboard3.model.ImageAdapter;

import java.util.ArrayList;

public class ThirdActivityListener implements AdapterView.OnItemClickListener {


    ThirdActivity thirdActivity;
    ArrayList<BitmapDataObject> lstGetBitmaps;
    public ImageAdapter imageAdapter;

    public ThirdActivityListener(ThirdActivity thirdActivity) {
        this.thirdActivity = thirdActivity;

        ActionBar actionBar = thirdActivity.getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        lstGetBitmaps = new ArrayList<BitmapDataObject>();
       // lstGetBitmaps.add(null); // das hier um zu verhindern das die Liste leer ist; das läuft in Android etwas anders als in Java oO'

        //get the bundle
        Bundle bundle = thirdActivity.getIntent().getExtras();

        //getting the arraylist from the key
        if (!bundle.isEmpty()) {
            lstGetBitmaps = (ArrayList<BitmapDataObject>) bundle.getSerializable("myList");
        }

        if (lstGetBitmaps != null) {
            imageAdapter = new ImageAdapter(thirdActivity, lstGetBitmaps);
            thirdActivity.gridViewDel.setAdapter(imageAdapter);
            imageAdapter.notifyDataSetChanged();
        }

    } // end ThirdActivityListener-constructor


    public boolean onCreateOptionsMenu (Menu menu){
        return true;
    }

    // Button oben im Reiter-Menü (oben links der Pfeil im SmartPhone)
    public boolean onOptionsItemSelected (MenuItem item){
        switch (item.getItemId()) {
            case android.R.id.home: // der Pfeil oben links ist der "Home-Button"
                // hier was passieren soll
                thirdActivity.finish(); // Standard ist wir beenden diese AppCompatActivity und gehen zur MainActivity zurück
                return true;
        }
        return false;
    }

    /* diese beiden Methoden sind für das AlertMenu das sich öffnet wenn man auf ein Objekt in der View klickt
     */


    private void deleteImage ( int position){
        Intent intent = new Intent();
        intent.putExtra("imageToDelete", position);

        thirdActivity.setResult(Activity.RESULT_OK, intent); // das hier ändert die extern gespeicherte liste

        lstGetBitmaps.remove(position); // das hier ist lokal (also was auf der gridview zu sehen ist)
        imageAdapter.notifyDataSetChanged();

    }



    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        AlertDialog.Builder builder = new AlertDialog.Builder(thirdActivity);
        builder.setMessage("Delete Image?");

        //   View customLayout = secondActivity.getLayoutInflater().inflate(R.layout.alert_options_menu, null);

        builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // hier löschen
                deleteImage(position);
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
