package com.goregoblin.scratchboard3.gui;

import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.GridView;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;

import com.goregoblin.scratchboard3.R;
import com.goregoblin.scratchboard3.logic.SecondActivityListener;

import java.util.zip.Inflater;

public class SecondActivity extends AppCompatActivity  {


    // Felder und Klassen
    SecondActivityListener secondActivityListener;
    public GridView gridViewLoad;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // hier der content (eine activity_myStuff.xml)
        setContentView(R.layout.activity_loadlist);

        // Felder definieren
        gridViewLoad = (GridView) findViewById(R.id.gridviewImages);

        // den Listener initialisieren; --> Vorsicht!! ERST findViewById(..); DANN Listener initialisieren!
        secondActivityListener = new SecondActivityListener(this);

        // für normalen klick-event an der gridview
        gridViewLoad.setOnItemClickListener(secondActivityListener);

        // das ist wichtig!! damit sagen wir das das unsre View ist auf der methoden wie "oncreatecontextmenu" laufen sollen
        registerForContextMenu(gridViewLoad);

    }

    // diese beiden Methoden für die Verbindung der Activities über den Button im Reiter oben links
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return secondActivityListener.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return secondActivityListener.onOptionsItemSelected(item);
    }


}
