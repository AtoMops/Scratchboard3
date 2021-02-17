package com.goregoblin.scratchboard3.gui;

import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.goregoblin.scratchboard3.R;
import com.goregoblin.scratchboard3.logic.MainActivityListener;
import com.goregoblin.scratchboard3.logic.PenSizeSubMenu;
import com.goregoblin.scratchboard3.model.DrawView;


public class MainActivity extends AppCompatActivity {

    /*
            wir schreiben hier in Java aber KOTLIN ist halt die "Future" in Android

            man kann aber von Java zu Kotlin konvertieren:
        https://www.tutorialkart.com/kotlin-android/convert-java-files-in-android-application-to-kotlin-files-or-classes/

         beim nächsten mal einfach direkt mit Kotlin starten
         bei iOS ist es jetzt "Swift" (nicht mehr c#; ähnliche Sache wie bei Java)

            -> Java ist aber noch ok also tippsen wir das Ding hier in Java noch zuende
            nur eben nicht wunder wenn viele Tutorials etc für Android eben Kotlin sind
            Kotlin ist nicht "für dummies" oder so sondern einfach die "neue offielle Android-Sprache"
     */

    private static final String LOG_CAT = MainActivity.class.getSimpleName();
    MainActivityListener mainActivityListener;
    public DrawView drawView;
    public PenSizeSubMenu penSizeSubMenu;
    public static DisplayMetrics metrics;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.i(LOG_CAT, "Verbinde Layout");
        setContentView(R.layout.activity_main);

        drawView = findViewById(R.id.drawView);
        penSizeSubMenu = new PenSizeSubMenu(this);

        Log.i(LOG_CAT, "Verbinde MainActivityListener");
        mainActivityListener = new MainActivityListener(this);

        drawView.setOnClickListener(mainActivityListener);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        return mainActivityListener.onCreateOptionsmenu(menu);
    }

    /* hier METHODE zur Ansteurung der ITEMS im MENU; LOGIC im MainActivityListener
        der MainActivityListener entspricht hier "super"-Aufruf
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return mainActivityListener.onOptionsItemSelected(item);
    }

    // Methoden für Datenaustausch zwischen MainActivity und SecondActivity
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // check if the request code is same as what is passed  here it is 1
        if (requestCode == 1) { // code 1 for loading from list
            // Make sure the request was successful (..setResult(Activity.RESULT_OK,intent) in SecondActivityListener)
            if (resultCode == RESULT_OK) {
                int imageToLoad = data.getIntExtra("imageToLoad",0);

                // Use the data - in this case display it in a Toast.
                Toast.makeText(this, "image is: " + imageToLoad, Toast.LENGTH_LONG).show();

                //  hier load-Methode aufrufen
                mainActivityListener.loadImageFromList(imageToLoad);
            }
        }

        //
        if (requestCode == 2) { // code  for delete from list
            // Make sure the request was successful (..setResult(Activity.RESULT_OK,intent) in SecondActivityListener)
            if (resultCode == RESULT_OK) {
                int imageToDelete = data.getIntExtra("imageToDelete",0);

                // Use the data - in this case display it in a Toast.
                Toast.makeText(this, "image is: " + imageToDelete, Toast.LENGTH_LONG).show();

                //  hier delete-Methode aufrufen
                mainActivityListener.deleteImageFromList(imageToDelete);
            }
        }

    }

    @Override
    public void startActivityForResult(Intent intent, int requestCode) {
        super.startActivityForResult(intent, requestCode);
    }




}