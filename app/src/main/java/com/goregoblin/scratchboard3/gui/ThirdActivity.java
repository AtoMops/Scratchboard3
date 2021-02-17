package com.goregoblin.scratchboard3.gui;

import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.GridView;

import androidx.appcompat.app.AppCompatActivity;

import com.goregoblin.scratchboard3.R;
import com.goregoblin.scratchboard3.logic.ThirdActivityListener;


public class ThirdActivity extends AppCompatActivity  {


    // Felder und Klassen
    ThirdActivityListener thirdActivityListener;
    public GridView gridViewDel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // hier der content (eine activity_myStuff.xml)
        setContentView(R.layout.activity_loadlist);

        // Felder definieren
        gridViewDel = (GridView) findViewById(R.id.gridviewImages);

        // den Listener initialisieren; --> Vorsicht!! ERST findViewById(..); DANN Listener initialisieren!
        thirdActivityListener = new ThirdActivityListener(this);

        // für normalen klick-event an der gridview
        gridViewDel.setOnItemClickListener(thirdActivityListener);

        // das ist wichtig!! damit sagen wir das das unsre View ist auf der methoden wie "oncreatecontextmenu" laufen sollen
        registerForContextMenu(gridViewDel);

    }

    // diese beiden Methoden für die Verbindung der Activities über den Button im Reiter oben links
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return thirdActivityListener.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return thirdActivityListener.onOptionsItemSelected(item);
    }


}
