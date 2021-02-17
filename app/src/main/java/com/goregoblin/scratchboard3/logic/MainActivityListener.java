package com.goregoblin.scratchboard3.logic;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.PopupWindow;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.chip.Chip;
import androidx.appcompat.widget.SwitchCompat;
import com.google.android.material.chip.ChipGroup;
import com.goregoblin.scratchboard3.R;
import com.goregoblin.scratchboard3.gui.MainActivity;
import com.goregoblin.scratchboard3.gui.SecondActivity;
import com.goregoblin.scratchboard3.gui.ThirdActivity;
import com.goregoblin.scratchboard3.model.BitmapDataObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;


public class MainActivityListener implements View.OnClickListener {

    private static final String VOLUME_EXTERNAL_PRIMARY = "";
    public MainActivity mainActivity;
    private static final String LOG_CAT = MainActivityListener.class.getSimpleName();
    // für die Permission
    private static final int RW_STORAGE = 123;
    private static final int WE_STORAGE = 123;

    private String savLocation = "";
    private File savFile;
    public ArrayList<BitmapDataObject> lstBitmapObjectSavs = new ArrayList<BitmapDataObject>();
    Intent intentSecondActivity;
    Intent intentThirdActivity;
    private Bundle bundle;

    public MainActivityListener(MainActivity mainActivity) {

        Log.i(LOG_CAT, "Verbindung zur MainActivity hergestellt");
        this.mainActivity = mainActivity;

        // das hier ist für den internal storage (App-specific-storage)
        String savName = "mySavFileList.sav";
        savFile =  setStorageDirAppSpecific(savName);

        /* hier wird DrawView initislisiert (enthält den canvas)
            die Klasse DrawView hat ihre eigene Logic
         */
        initDrawView();

    }


    private void initDrawView(){
        /*
            vorsicht mit den display-metrics:
            https://stackoverflow.com/questions/6533368/draw-rectangle-which-change-size-w-r-t-different-android-screen-size#6534145
            es macht einen unterschied ob wir die "direkt" verwenden oder ob wir "dpi" verwenden
            DPI ist hier NICHT DotsPerInch wie bei Druckern; das scheint ein generelles Problem mit der Benennung zu sein
         */

        mainActivity.metrics = new DisplayMetrics(); // !!!
        mainActivity.getWindowManager().getDefaultDisplay().getMetrics(mainActivity.metrics);
        mainActivity.drawView.init(mainActivity.metrics); // hier ok
    }

    public boolean onCreateOptionsmenu(Menu menu) {
        MenuInflater inflater = mainActivity.getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return true;
    }

    /* hier werden die Items des Menus angesteuert
        die IDs der Items musst du schon definiert haben
     */
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.clear: // ID des Menu-Items das angestuert werden soll
                mainActivity.drawView.clear(); // was immer dann passieren soll
                System.out.println("delete button gedrückt");
                break;
            case R.id.color_options:
                System.out.println("color_options gedrückt");
                callColorPicker();

                break;
            case R.id.size_options:
                System.out.println("size_options gedrückt");
                callPensizePopup();

                break;
            case R.id.save_options:
                System.out.println("save_options gedrückt");

                /* Speichern implementieren (intern! Kein  Folder zum aussuchen wie bei Export)

                    bisher speichern wir nur für den Emulator
                    auf dem Handy raucht das aber immer noch ab..
                    gut wäre ein Ordner im User-Profil also da wo DCIM usw auch Ordner haben
                    dorthin können wir dann auch den Export hinpacken; die lokalen speicher der Liste einfach in einen Subordner e.g. "Data"
                 */


                // testen ob die Liste schon existiert; dann vorhandene Liste laden
                if (!savFile.exists()){
                    if (mainActivity.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                        // wenn keine Berechtigung vorhanden danach fragen
                        mainActivity.requestPermissions(new String[] {Manifest.permission.READ_EXTERNAL_STORAGE}, RW_STORAGE);
                    } else {
                        // wenn  Berechtigung vorhanden Methode starten

                            // String savName = "mySavFileList.sav";
                            // File savFile =  setStorageDirAppSpecific(savName);

                            lstBitmapObjectSavs = loadFromListWFile(savFile);

                    }
                }



                // testen ob RW-Access vorhanden
                System.out.println(Environment.getExternalStorageState());
                if (mainActivity.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                    // wenn keine Berechtigung vorhanden danach fragen
                    mainActivity.requestPermissions(new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE}, WE_STORAGE);
                } else {
                    // wenn  Berechtigung vorhanden Methode starten

                      //  savLocation = setStorageDir();
                     //  String savName = "mySavFileList.sav";
                     //  File savFile =  setStorageDirAppSpecific(savName);

                       // System.out.println("savLocation is: " + savLocation);

                        if (lstBitmapObjectSavs == null){
                            lstBitmapObjectSavs = new ArrayList<BitmapDataObject>();
                        }

                        savListAddWFile(savFile,lstBitmapObjectSavs);

                        Toast.makeText(mainActivity.getApplicationContext(),"Image saved", Toast.LENGTH_SHORT);

                        // alte Methode die String verwendet
                        //savListAddWString(savLocation, savFile, lstBitmapObjectSavs);
                }

                break;
            case R.id.load_options:
                System.out.println("load_options gedrückt");
                //  hier die LoadList (SecondActivity) über Intent aufrufen

                // auch nach lesen fragen
                if (mainActivity.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                    // wenn keine Berechtigung vorhanden danach fragen
                    mainActivity.requestPermissions(new String[] {Manifest.permission.READ_EXTERNAL_STORAGE}, RW_STORAGE);
                } else {
                    // wenn  Berechtigung vorhanden Methode starten

                       // String savName = "mySavFileList.sav";
                       // File savFile =  setStorageDirAppSpecific(savName);

                       lstBitmapObjectSavs = loadFromListWFile(savFile);

                }

                // Ein Bundle für den Datenaustausch zwischen den Activites
                bundle = new Bundle();//
                bundle.putSerializable("myList", (Serializable) lstBitmapObjectSavs);

                intentSecondActivity = new Intent(mainActivity, SecondActivity.class); // Intent erstellen
                intentSecondActivity.putExtras(bundle);

               // mainActivity.startActivity(intentSecondActivity); // das wäre NUR Activity ohne Datenaustausch
                mainActivity.startActivityForResult(intentSecondActivity,1);

                break;

            case  R.id.delete_options:

                /* hier liste zeigen (ThirdActivity) aber nur löschen
                    gut wäre hier was zum markieren damit man auch mehrere Images löschen kann
                    bei "startActivityForResult(...)" können wir einen anderen RequestCode verwenden
                 */


                // auch nach lesen fragen
                if (mainActivity.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                    // wenn keine Berechtigung vorhanden danach fragen
                    mainActivity.requestPermissions(new String[] {Manifest.permission.READ_EXTERNAL_STORAGE}, RW_STORAGE);
                } else {
                    // wenn  Berechtigung vorhanden Methode starten

                      //  String savName = "mySavFileList.sav";
                       // File savFile =  setStorageDirAppSpecific(savName);

                        lstBitmapObjectSavs = loadFromListWFile(savFile);

                }


                // Ein Bundle für den Datenaustausch zwischen den Activites
                bundle = new Bundle();//
                bundle.putSerializable("myList", (Serializable) lstBitmapObjectSavs);

                intentThirdActivity = new Intent(mainActivity, ThirdActivity.class); // Intent erstellen
                intentThirdActivity.putExtras(bundle);

                // mainActivity.startActivity(intentSecondActivity); // das wäre NUR Activity ohne Datenaustausch
                mainActivity.startActivityForResult(intentThirdActivity,2);


                break;

            case R.id.export_options:
                System.out.println("export_options gedrückt");

                /* TODO: Export überarbeiten

                    Probleme: speichert datei immer mit "data.." ab
                              der Hintergrund bei jpeg-Export ist schwarz (wenn nicht übermalt)
                              Hinweis wenn gespeichert und WO gespeichert
                              Speicherort noch nicht ok
                              zurzeit:   /sdcard/
                              oder:      /storage/emulated/0

                              er scheint in beiden Foldern zu speichern
                              wir brauchen ein spezielles für die App im User-Bereich
                              also z.B: /sdcard/Scratchboard/savedImages

                 */

                 callExportPopup();

                // setStorageDirSharedStorage();

                break;
        }
        return false;
    }


    private void exportImage(String savName, int exportType, int imgQuality){

        /*
            exportType: 0 = JPEG; 1 = PNG
            imgQuality: 0 = min; 100 = max
         */
      //  System.out.println(Environment.getExternalStorageState());
        if (mainActivity.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            // wenn keine Berechtigung vorhanden danach fragen
            mainActivity.requestPermissions(new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE}, WE_STORAGE);
        } else {
            // wenn  Berechtigung vorhanden Methode starten

            //savLocation = setStorageDir();
            savLocation = setStorageDirSharedStorage().getAbsolutePath();

            //savFile = savName; // hier brauchen wir noch ein Menu für die Datei-Benennung; erstmal standards wie jpeg;png;gif (pdf ist nicht so einfach und geht nur mit externen Libs)
            System.out.println("savLocation is: " + savLocation);

          //  if (!savLocation.equals("") && !savFile.equals("")){
             //   mainActivity.drawView.exportBMP(savFile,savLocation, exportType,imgQuality);
                mainActivity.drawView.exportBMP("myImage123",savLocation, exportType,imgQuality);
           // };
        }

    }


    // diese Methode wird in der überschriebenen Methode "onActivityResult" in der MainActvitiy aufgerufen
    public void loadImageFromList(int position){

        lstBitmapObjectSavs = new ArrayList<BitmapDataObject>();

        //lstBitmapObjectSavs = loadFromListWString(savLocation,savFile);
        lstBitmapObjectSavs = loadFromListWFile(savFile);
        BitmapDataObject bitmapDataObject = lstBitmapObjectSavs.get(position);
        Toast.makeText(mainActivity.getApplicationContext(), savFile.toString(), Toast.LENGTH_SHORT).show();
        mainActivity.drawView.clear();
        mainActivity.drawView.setmBitmapLoad(bitmapDataObject);
    }

    public void deleteImageFromList(int position){

        // Objekt aus liste entfernen
        lstBitmapObjectSavs.remove(position);

        // dann liste wieder speichern

        savListRemoveWFile(savFile,lstBitmapObjectSavs);

       // savListRemoveWString(savLocation, savFile, lstBitmapObjectSavs);

    }

    // Method to create new Folder and file
    public String getStorageDir(String fileName) {
        //create folder
        // File file = new File(Environment.getExternalStorageDirectory() + "/data/app/com.goregoblin.scratchboard2-1/");
        // Environment.getExternalStorageDirectory() ist hier "/storage/emulated/0/"
        File file = new File(Environment.getExternalStorageDirectory() + "/data/");
        if (!file.mkdirs()) { // wenn folder noch nicht vorhanden --> erstellen
            file.mkdirs();
        }
        String filePath = file.getAbsolutePath() + File.separator + fileName;
        return filePath;
    }


    /*
        TODO: rewrite the way to store data

        the way we implemented saving and export here works within the emulation but not on real phone
        we need:
        --> "app-specific files" (e.g. the ArrayList with Bitmap-Objects)
               see here: https://developer.android.com/training/data-storage/app-specific

        --> "shared storage" for the export of Bitmaps to png and jpeg
               see here: https://developer.android.com/training/data-storage/shared

     */


    // this Method to store saved Image-List internal (no access from outside the App) | We save it external to the SDCard but the file is a *.bin
    public File setStorageDirAppSpecific(String filename){

        // dieser Speicher ist intern aber NUR solange die App läuft
         // Context context = mainActivity.getApplicationContext();
        //File file = new File(context.getFilesDir(), filename);

        /* dieser Speicher ist extern aber innerhalb des Installations-Ordners der app; der User kann darauf zugreifen (wenn er sich auskennt) aber die Liste ist als *.bin gespeichert
            also in Android-schlau: "app-specific directories within external storage"
         */
        Context context = mainActivity.getApplicationContext();
        File folder = new File(context.getExternalFilesDir(null),"");

        System.out.println("Folder is: " + folder.toString());

        // hier den ordern erstellen wenn dieser noch nicht existiert
        if (!folder.mkdirs()) { // wenn folder noch nicht vorhanden --> erstellen
            folder.mkdirs();
        }

        // hier den Pfad mit File
        String folderIs = folder.getAbsolutePath();
        File file = new File(folderIs,filename);

        return file;
    }


    // this Method to Export the images to png or jpeg (public access from outside the App)
    public File setStorageDirSharedStorage(){

        /*  hier mal Media-storage probieren
            https://developer.android.com/training/data-storage/shared/media

         */
        File fileToRoot = null;

        Context context = mainActivity.getApplicationContext();

        //
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) { // Q ist API 29
            // das hier geht erst ab API-level 29
            Set<String> volumeNames = MediaStore.getExternalVolumeNames(context);
            String firstVolumeName = volumeNames.iterator().next();

            fileToRoot = new File(firstVolumeName);

        } else {

            /*
                The VOLUME_EXTERNAL volume provides a view of all shared storage volumes on the device.
                You can read the contents of this synthetic volume, but you cannot modify the contents.

                The VOLUME_EXTERNAL_PRIMARY volume represents the primary shared storage volume on the device.
                You can read and modify the contents of this volume.

             */

            File[] directories =  context.getExternalFilesDirs(null);
            List<File> dirFileList = Arrays.asList(directories);
            for (File file:dirFileList) {
                System.out.println(file.toString());
            }

            File[] directories2 =  mainActivity.getExternalMediaDirs();
            List<File> dirFileList2 = Arrays.asList(directories2);
            for (File file:dirFileList2) {
                System.out.println(file.toString());
            }

            File file = dirFileList2.get(0);

            String pathToAppRoot = file.getAbsolutePath().toString();
            String pathToAppPart = "Android/media/com.goregoblin.scratchboard3";

            String pathToRoot = pathToAppRoot.replace("Android/media/com.goregoblin.scratchboard3", "");

            System.out.println("pathToRoot: " + pathToRoot);

            fileToRoot = new File(pathToRoot);


            Cursor cursor = mainActivity
                            .getContentResolver()
                             //.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, null, null, null);
                             //.query(Uri.parse(MediaStore.VOLUME_EXTERNAL_PRIMARY), null, null, null, null);
                             .query(Uri.parse(MediaStore.VOLUME_EXTERNAL), null, null, null, null);



            //Integer col = cursor.getColumnCount();
            //Integer row =  cursor.getCount();
            //Toast.makeText(mainActivity, "cols: " + col + "; rows: " + row ,Toast.LENGTH_SHORT);

            // In order to get the column names, we use:

            if (cursor != null) {
                String[] columnNames = cursor.getColumnNames();
                List<String> strList = Arrays.asList(columnNames);
                strList.stream().forEach(System.out::println);
            }else {
                System.out.println("cursor is empty!");
            }

        }

        return fileToRoot;

    }


    // Method to create new Folder (old)
    public String setStorageDir() {
        /*
            https://www.tutlane.com/tutorial/android/android-external-storage-with-examples

            https://www.dev2qa.com/android-read-write-external-storage-file-example/

            https://stackoverflow.com/questions/57116335/environment-getexternalstoragedirectory-deprecated-in-api-level-29-java

            das ist hier etwas komplizierter weil sich die methoden seit API level 29 geändert haben
            wir müssen auch die Version checken (hier verwenden wir ja Version 24 zu testen)

            wir sollten das hier mal durchkauen:
            https://developer.android.com/guide/topics/data

         */

        String filePath = "";

        if (mediaMountCheck()) { // checken of auf der SD-Card geschrieben werden kann

            System.out.println("mediaMountCheck ok");
            //create folder
            // File file = new File(Environment.getExternalStorageDirectory() + "/data/app/com.goregoblin.scratchboard2-1/");
            //  Environment.getExternalStorageDirectory() ist hier "/storage/emulated/0/"

            /*
                das hier ist veraltet:
                "Use getExternalFilesDir(), getExternalCacheDir(), or getExternalMediaDirs() (methods on Context) instead of Environment.getExternalStorageDirectory()."
                https://stackoverflow.com/questions/57116335/environment-getexternalstoragedirectory-deprecated-in-api-level-29-java

                auch hier:
                https://developer.android.com/training/data-storage/use-cases
             */

        //    File folder = new File(Environment.getExternalStorageDirectory() + "/data/"); // das hier zum testen
          // File folder = new File(Environment.getExternalStorageDirectory() + "/Scratchboard/data/"); // das hhier zum testen
       //     File folder = Environment.getExternalStoragePublicDirectory(Environment.getExternalStorageState() + "/Scratchboard/data/");


            Context context = mainActivity.getApplicationContext();
            //File folder = new File(context.getExternalCacheDir()  + "/Scratchboard/data/");
            // File folder = new File(context.getExternalFilesDir(Environment.DIRECTORY_DCIM)  + "/Scratchboard/data/");
            //File folder = new File(context.getExternalFilesDir(Environment.DIRECTORY_DCIM).toString());

            // https://developer.android.com/training/data-storage/app-specific#media
            //File folder = new File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), "MyScratchBoardFolder");

            //File folder = new File(mainActivity.getExternalFilesDir(null), "DemoFile.jpg");

            // getExternalStoragePublicDirectory
            //File folder = new File(mainActivity.getExternalFilesDir(null), ""); // das hier ist das folder innerhalb der App (das ist zugänglich unter "Android/data/com.goregoblin..")
            File folder = new File(Environment.getExternalStoragePublicDirectory(null) + "/Scratchboard/", "");

            System.out.println("Folder is: " + folder.toString());

            //  File file = new File("/data/app/com.goregoblin.scratchboard2/"); // das später
            if (!folder.mkdirs()) { // wenn folder noch nicht vorhanden --> erstellen
                folder.mkdirs();
            }
             filePath = folder.getAbsolutePath();
        } else {
            System.out.println("SDCard not mounted or not readable");
            Toast.makeText(mainActivity.getApplicationContext(), "SDCard not mounted or not readable", Toast.LENGTH_SHORT);
        }

        return filePath;
    }



    /*
    void createExternalStoragePrivateFile() {
        // Create a path where we will place our private file on external
        // storage.
        File file = new File(mainActivity.getExternalFilesDir(null), "DemoFile.jpg");

        try {
            // Very simple code to copy a picture from the application's
            // resource into the external file.  Note that this code does
            // no error checking, and assumes the picture is small (does not
            // try to copy it in chunks).  Note that if external storage is
            // not currently mounted this will silently fail.
            InputStream is = mainActivity.getResources().openRawResource(R.drawable.border_images);
            OutputStream os = new FileOutputStream(file);
            byte[] data = new byte[is.available()];
            is.read(data);
            os.write(data);
            is.close();
            os.close();
        } catch (IOException e) {
            // Unable to create file, likely because external storage is
            // not currently mounted.
            Log.w("ExternalStorage", "Error writing " + file, e);
        }
    }

     */


    void deleteExternalStoragePrivateFile() {
        // Get path for the file on external storage.  If external
        // storage is not currently mounted this will fail.
        File file = new File(mainActivity.getExternalFilesDir(null), "DemoFile.jpg");
        file.delete();
    }

    boolean hasExternalStoragePrivateFile() {
        // Get path for the file on external storage.  If external
        // storage is not currently mounted this will fail.
        File file = new File(mainActivity.getExternalFilesDir(null), "DemoFile.jpg");
        return file.exists();
    }


    private boolean mediaMountCheck(){


        boolean Available= false;
        boolean Readable= false;
        String state = Environment.getExternalStorageState();
        if(Environment.MEDIA_MOUNTED.equals(state)){
            // Both Read and write operations available
            Available= true;
            System.out.println("Both Read and write operations available");
        } else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)){
            // Only Read operation available
            System.out.println("Only Read operation available");
            Available= true;
            Readable= true;
        } else {
            // SD card not mounted
            Available = false;
            System.out.println("SD card not mounted");
        }

        return Available;
    }



    // method to save list with BitmapDataObjects using File
    private void serializeListBitmapDataObjectOutWFile(File fileToSav, List<BitmapDataObject> lstBitmapObjectSavs) throws IOException {

        File fileName = fileToSav;
        FileOutputStream fos = new FileOutputStream(fileName);
        ObjectOutputStream oos = new ObjectOutputStream(fos);
        oos.writeObject(lstBitmapObjectSavs);
        oos.flush();
        oos.close();

    }

    // method to save list with BitmapDataObjects using String
    private void serializeListBitmapDataObjectOutWString(List<BitmapDataObject> lstBitmapObjectSavs, String savFileName) throws IOException {

        String fileName = savFileName;
        FileOutputStream fos = new FileOutputStream(fileName);
        ObjectOutputStream oos = new ObjectOutputStream(fos);
        oos.writeObject(lstBitmapObjectSavs);
        oos.flush();
        oos.close();

    }

    // Method to load list with BitmapDataObjects using File
    private ArrayList<BitmapDataObject>  serializeListBitmapDataObjectInWFile(File loadFileName) throws IOException, ClassNotFoundException {

        File fileName = loadFileName;
        FileInputStream fin = new FileInputStream(fileName);
        ObjectInputStream ois = new ObjectInputStream(fin);
        ArrayList<BitmapDataObject>  lstBitmapObjectSavs = (ArrayList<BitmapDataObject> ) ois.readObject();
        ois.close();
        return lstBitmapObjectSavs;
    }



    // Method to load list with BitmapDataObjects using String
    private ArrayList<BitmapDataObject>  serializeListBitmapDataObjectInWString(String loadFileName) throws IOException, ClassNotFoundException {

        String fileName = loadFileName;
        FileInputStream fin = new FileInputStream(fileName);
        ObjectInputStream ois = new ObjectInputStream(fin);
        ArrayList<BitmapDataObject>  lstBitmapObjectSavs = (ArrayList<BitmapDataObject> ) ois.readObject();
        ois.close();
        return lstBitmapObjectSavs;
    }


    private void serializeBitmapDataObjectOut(BitmapDataObject bitmapDataObject, String savFileName) throws IOException {
        String fileName = savFileName;
        FileOutputStream fos = new FileOutputStream(fileName);
        ObjectOutputStream oos = new ObjectOutputStream(fos);
        oos.writeObject(bitmapDataObject);
        oos.flush();
        oos.close();
    }

    private BitmapDataObject serializeBitmapDataObjectIn(String loadFileName) throws IOException, ClassNotFoundException {
        String fileName = loadFileName;
        FileInputStream fin = new FileInputStream(fileName);
        ObjectInputStream ois = new ObjectInputStream(fin);
        BitmapDataObject bitmapDataObject = (BitmapDataObject) ois.readObject();
        ois.close();
        return bitmapDataObject;
    }

    /* hier Aufruf des ColorPickers;
       der hat noch mehr optionen so wie FarbCode-Anzeige abschalten etc
     */
    private void callColorPicker(){

            new ColorPickerSimple.Builder(mainActivity) // MainActivity ist unser "Context"
                    .initialColor(mainActivity.drawView.currentColor) // immer letzte Farbe übergeben; Standard ist blau; NICHT BLACK! weil dann die brightness direkt auf null ist was am Anfang verwirrend seien könnte
                    .enableBrightness(true)
                    .enableAlpha(true)
                    .showValue(false) // first options
                    .build() // then build
                    .show(new ColorPickerSimple.ColorPickerObserver() {
                        @Override
                        public void onColorPicked(int color) {
                            Log.i("picked color: ", Integer.toString(color));
                            mainActivity.drawView.currentColor = color;
                        }
                    });
            System.out.println("color != 0" + mainActivity.drawView.currentColor);

    }


    // Methode für Popup-Window für Image-Export
    private void callExportPopup(){
        LayoutInflater inflaterExport = mainActivity.getLayoutInflater();
        View layoutExport = inflaterExport.inflate(R.layout.activity_img_export, null); // this is our Layout

        // we add our Layout to a popupwindow
        PopupWindow popupWindowExport = new PopupWindow(layoutExport, ViewGroup.LayoutParams.MATCH_PARENT,
                                                                      ViewGroup.LayoutParams.WRAP_CONTENT);


        popupWindowExport.setFocusable(true); // !! wichtig! Sonst bekommen wir kein keyboard; das muss gesetzt sein BEVOR du "showAtLocation" aufrufst!
        // we add some edits
        popupWindowExport.setBackgroundDrawable(new ColorDrawable(Color.WHITE));
        popupWindowExport.setOutsideTouchable(false); // if true the window will close if the parent-view is touched

        // place Popup-Window to middle of screen
        popupWindowExport.showAtLocation(layoutExport, Gravity.CENTER, 0, layoutExport.getHeight()/2);

            //hier Elemente des Popup-Windows ansprechen also deine TextViews; Buttons etc

        EditText txtImageName = (EditText) layoutExport.findViewById(R.id.txtEdit_imageName);
        TextView txtImageQualValue = layoutExport.findViewById(R.id.txtView_qualitiyValue);

        SwitchCompat switchCompatJPG = layoutExport.findViewById(R.id.switch_JPEG);
        SwitchCompat switchCompatPNG = layoutExport.findViewById(R.id.switch_PNG);
        SeekBar seekBar = layoutExport.findViewById(R.id.seekBar_ImageQual);

        Button btnOkExport = (Button) layoutExport.findViewById(R.id.btnOkExport); // "layout" ist unsere aktuelle view
        Button btnBackExport = (Button) layoutExport.findViewById(R.id.btnCancelExport);

        txtImageQualValue.setText("50");
        seekBar.setProgress(50);

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress,boolean fromUser) {
                txtImageQualValue.setText(Integer.toString(progress));
            }
        });

        // wenn der eine SwitchCompat aktiviert ist den anderen deaktieren; geht eleganter aber funzt ^^'
        switchCompatJPG.setOnCheckedChangeListener((e1,e2) -> {
            if (switchCompatJPG.isChecked()) {
                switchCompatPNG.setChecked(false);
            }else {
                switchCompatPNG.setChecked(true);
            }
        });

        switchCompatPNG.setOnCheckedChangeListener((e1,e2) -> {
            if (switchCompatPNG.isChecked()) {
                switchCompatJPG.setChecked(false);
            }else {
                switchCompatJPG.setChecked(true);
            }
        });


        btnOkExport.setOnClickListener(e -> {

            int expType = 10 ;
            if (switchCompatJPG.isChecked()){
                expType = 0;
            } else if (switchCompatPNG.isChecked()){
                expType = 1;
            }

            String seekbarQualVal = txtImageQualValue.getText().toString();
            Integer valImgQual = Integer.parseInt(seekbarQualVal);

            String imgSavName = txtImageName.getText().toString();

            // e.g. exportImage("myImage","0",50)

            /*
                TODO: wir brauchen hier eine "runtime permission"

                wir wollen auf  "/storage/emulated/0" speichern (das ist ein "SymLink" zum Telefonspeicher)
                https://stackoverflow.com/questions/37819550/java-io-filenotfoundexception-storage-emulated-0-new-file-txt-open-failed-ea

             */
            exportImage(imgSavName,expType,valImgQual);

            popupWindowExport.dismiss();

        });


        btnBackExport.setOnClickListener(e -> {
            popupWindowExport.dismiss();  // PopUpWindow schließen
        });
    }


    private void callPensizePopup(){
        LayoutInflater inflaterPen = mainActivity.getLayoutInflater();
        View layoutPen = inflaterPen.inflate(R.layout.activity_pensize, null); // this is our Layout

        // we add our Layout to a popupwindow
        PopupWindow popupWindowPenSize = new PopupWindow(layoutPen, ViewGroup.LayoutParams.WRAP_CONTENT,
                                                                    ViewGroup.LayoutParams.WRAP_CONTENT);

        // we add some edits
        popupWindowPenSize.setBackgroundDrawable(new ColorDrawable(Color.WHITE));
        popupWindowPenSize.setOutsideTouchable(false); // if true the window will close if the parent-view is touched


        popupWindowPenSize.setAnimationStyle(R.style.TopDefaultsViewColorPickerPopupAnimation);
        // if (parent == null) parent = layout; // parent is the same as our layout (maybe skip this part here?!)
        popupWindowPenSize.showAtLocation(layoutPen, Gravity.CENTER, 0, layoutPen.getHeight()/2);

        // elemente im popup-window ansprechen (buttons etc) --> NICHT in der Klasse selbst! --> probiert.. geht schief ^^'
        ImageButton btnOkPen = (ImageButton) layoutPen.findViewById(R.id.btnOkPen); // "layout" ist unsere aktuelle view
        ImageButton btnBackPen = (ImageButton) layoutPen.findViewById(R.id.btnBackPen);


        btnOkPen.setOnClickListener( e -> {
            Log.i("ButtonOK", "pressed BtnOk");

            // hier über statische variable exRad in PenSizeSubMenu; nicht optimal aber funzt erstmal
            mainActivity.drawView.setPenSize(mainActivity.penSizeSubMenu.getCircleRadius());
            popupWindowPenSize.dismiss(); // PopUpWindow schließen
        });

        btnBackPen.setOnClickListener(e -> {
            Log.i("ButtonBack", "pressed BtnBack");
            popupWindowPenSize.dismiss();  // PopUpWindow schließen
        });


    }

    @Override
    public void onClick(View v) {

    }

    // Hier Methode zum speichern der Liste mit neuem Image die File verwendet
    private void savListAddWFile(File fileToSav, ArrayList<BitmapDataObject> lstBitmapObjectSavs){

        // get current Bitmap from Context and convert to serializeable BitmapDataObject
        Bitmap bitmapToSav = mainActivity.drawView.getmBitmapSav().getCurrentImage();
        BitmapDataObject bitMapDO = new BitmapDataObject(bitmapToSav);

        // add BitmapDataObject to List
        lstBitmapObjectSavs.add(bitMapDO);

        try {
            serializeListBitmapDataObjectOutWFile(fileToSav,lstBitmapObjectSavs);
            //Toast.makeText(mainActivity.getApplicationContext(), "Image saved", Toast.LENGTH_SHORT).show();
            Toast.makeText(mainActivity.getApplicationContext(), "length of List is: " + lstBitmapObjectSavs.size(), Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(mainActivity.getApplicationContext(), "Error! Image could not be saved", Toast.LENGTH_SHORT).show();
        }

    }


    // Hier Methode zum speichern der Liste mit neuem Image die String verwendet
    private void savListAddWString(String path, String fileName, ArrayList<BitmapDataObject> lstBitmapObjectSavs){

        //String savPath = "res/raw/";
        String fPath = path;
        String fName = fileName;
        String savName = fPath + File.separator + fName;

        // get current Bitmap from Context and convert to serializeable BitmapDataObject
        Bitmap bitmapToSav = mainActivity.drawView.getmBitmapSav().getCurrentImage();
        BitmapDataObject bitMapDO = new BitmapDataObject(bitmapToSav);

        // add BitmapDataObject to List
        lstBitmapObjectSavs.add(bitMapDO);

        try {
            serializeListBitmapDataObjectOutWString(lstBitmapObjectSavs,savName);
            //Toast.makeText(mainActivity.getApplicationContext(), "Image saved", Toast.LENGTH_SHORT).show();
            Toast.makeText(mainActivity.getApplicationContext(), "length of List is: " + lstBitmapObjectSavs.size(), Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(mainActivity.getApplicationContext(), "Error! Image could not be saved", Toast.LENGTH_SHORT).show();
        }

    }


    // Hier Methode zum speichern der Liste wenn was enfernt wurde (verwendet String)
    private void savListRemoveWFile(File fileName, ArrayList<BitmapDataObject> lstBitmapObjectSavs){

        File savName = fileName;

        try {
            serializeListBitmapDataObjectOutWFile(savName,lstBitmapObjectSavs);
            Toast.makeText(mainActivity.getApplicationContext(), "Image saved", Toast.LENGTH_SHORT).show();
            //Toast.makeText(mainActivity.getApplicationContext(), "length of List is: " + lstBitmapObjectSavs.size(), Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(mainActivity.getApplicationContext(), "Error! Image could not be saved", Toast.LENGTH_SHORT).show();
        }

    }


    // Hier Methode zum speichern der Liste wenn was enfernt wurde (verwendet String)
    private void savListRemoveWString(String path, String fileName, ArrayList<BitmapDataObject> lstBitmapObjectSavs){

        //String savPath = "res/raw/";
        String fPath = path;
        String fName = fileName;
        String savName = fPath + File.separator + fName;

        try {
            serializeListBitmapDataObjectOutWString(lstBitmapObjectSavs,savName);
            //Toast.makeText(mainActivity.getApplicationContext(), "Image saved", Toast.LENGTH_SHORT).show();
            Toast.makeText(mainActivity.getApplicationContext(), "length of List is: " + lstBitmapObjectSavs.size(), Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(mainActivity.getApplicationContext(), "Error! Image could not be saved", Toast.LENGTH_SHORT).show();
        }

    }


    // Methode zum laden der gespeicherten Liste die File verwendet
    private  ArrayList<BitmapDataObject> loadFromListWFile(File fileToLoad){

        File loadFile = fileToLoad;
        ArrayList<BitmapDataObject> lstBitmapDO = null;
        try {
            lstBitmapDO = serializeListBitmapDataObjectInWFile(loadFile);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return lstBitmapDO;
    }




    // Methode zum laden der gespeicherten Liste die String verwendet
    private  ArrayList<BitmapDataObject> loadFromListWString(String path, String file){
        String loadPath = path;
        String fileName = file;
        String loadName = loadPath + File.separator + fileName;

        ArrayList<BitmapDataObject> lstBitmapDO = null;
        try {
            lstBitmapDO = serializeListBitmapDataObjectInWString(loadName);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return lstBitmapDO;
    }


    // Methode um einzelnes serialisierbares Objekt zu laden
    private BitmapDataObject loadMyStuff(String path, String file){
        String loadPath = path;
        String fileName = file;
        String loadName = loadPath + File.separator + fileName;

        BitmapDataObject bitToSav = null;
        try {
            bitToSav = serializeBitmapDataObjectIn(loadName);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return bitToSav;
    }

}
