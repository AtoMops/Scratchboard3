package com.goregoblin.scratchboard3.model;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Environment;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;


public class DrawView extends View {

    public static int BRUSH_SIZE_DEFAULT = 20;
    public static final int DEFAULT_COLOR = Color.BLUE;
    public static final int DEFAULT_BG_COLOR = Color.WHITE;
    private static final float TOUCH_TOLERANCE = 4;
    private float mX, mY;
    private int height, width;
    private Path mPath;
    private Paint mPaint;
    private ArrayList<DrawPath> paths = new ArrayList<>();
    public int currentColor;
    private int backgroundColor = DEFAULT_BG_COLOR;
    private float strokeWidth;
    private Bitmap mBitmap;
    private Bitmap mBitmap2;
    public Canvas mCanvas;
    private Paint mBitmapPaint = new Paint(Paint.DITHER_FLAG);

    private boolean loaded = false;


    private boolean penSizeIsCustom;

    // 1. Constructor
    /*
    public DrawView(Context context) {
        this(context, null);
    }

     */

    // 2. Constructor
    public DrawView(Context context, AttributeSet attrs) {
        super(context, attrs);
        definePaintSettings();
    }

    private void definePaintSettings(){

        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
        mPaint.setColor(DEFAULT_COLOR);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setXfermode(null);
        mPaint.setAlpha(0xff);

    }


    public void init(DisplayMetrics metrics) {
        height = metrics.heightPixels;
        width = metrics.widthPixels;

        mBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        mBitmap2 = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        mCanvas = new Canvas(mBitmap);

        currentColor = DEFAULT_COLOR;

        if (!penSizeIsCustom){
            strokeWidth = BRUSH_SIZE_DEFAULT;
        }
    }

    public void clear() {
        backgroundColor = DEFAULT_BG_COLOR;
        paths.clear();

        mBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        mBitmap2 = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

        mCanvas.setBitmap(mBitmap);

        this.setDrawingCacheEnabled(false);
        this.draw(mCanvas);

        invalidate();

    }

    public void setPenSize(float penSize){
        strokeWidth = penSize;
        penSizeIsCustom = true;
    }

    public void exportBMPOld() {
        /*
            wir versuchen erstmal mit external storage
            das ist wie ein Export
         */
        View content = this; // DrawView IST eine View
        content.setDrawingCacheEnabled(true);
        content.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
        Bitmap bitmap = content.getDrawingCache();


        String path = Environment.getExternalStorageDirectory().getAbsolutePath(); // geht aber ist nicht sehr hilfreich
        System.out.println("path is: " + path);
        File file = new File(path + "/myImage.png");
        FileOutputStream ostream;
        try {
            file.createNewFile();
            ostream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, ostream);

            System.out.println("File is: " + file.toString());
            ostream.flush();
            ostream.close();
//            Toast.makeText(mainActivity.getApplicationContext(), "image saved", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
            //          Toast.makeText(mainActivity.getApplicationContext(), "error", Toast.LENGTH_SHORT).show();
        }

    }

        public void exportBMP(String savName, String savPath, int type, int imgQual){
        /*
           hier Export (entweder JPEG oder PNG)
         */
            View currentView = this; // DrawView IST eine View
            currentView.setDrawingCacheEnabled(true);
            currentView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
            Bitmap bitmapToExport = currentView.getDrawingCache();


           // String path = Environment.getExternalStorageDirectory().getAbsolutePath(); // geht aber ist nicht sehr hilfreich
            System.out.println("path is: " + savPath);

            if (type == 0) {
                savName = savName+".jpeg";
            }

            if (type == 1) {
                savName = savName+".png";
            }

            File file = new File(savPath + savName);

            FileOutputStream ostream;
            try {
                if (type == 0) {
                    file.createNewFile();
                    ostream = new FileOutputStream(file);
                    bitmapToExport.compress(Bitmap.CompressFormat.JPEG, imgQual, ostream);

                    System.out.println("File is: " + file.toString());
                    ostream.flush();
                    ostream.close();
                }
                if (type == 1){
                    file.createNewFile();
                    ostream = new FileOutputStream(file);
                    bitmapToExport.compress(Bitmap.CompressFormat.PNG, imgQual, ostream);

                    System.out.println("File is: " + file.toString());
                    ostream.flush();
                    ostream.close();
                }
            // Toast.makeText(mainActivity.getApplicationContext(), "image saved", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                e.printStackTrace();
                //          Toast.makeText(mainActivity.getApplicationContext(), "error", Toast.LENGTH_SHORT).show();
            }

    }

    // Method to return Serializeable Bitmap-Object
    public BitmapDataObject getmBitmapSav(){

        Bitmap bitmapToSav = saveBitmap();

        // set Bitmap to BitmapDataObject by using constructor (do NOT try cast! will not work ^^')
        BitmapDataObject bitMapToSav = new BitmapDataObject(bitmapToSav);

        return bitMapToSav;
    }

    // Methode um Bitmap für speichern vorzubereiten (serialisiertes Objekt)
    private Bitmap saveBitmap(){

        invalidate(); // !! die View vor dem speichern updaten!! (Hölle ^^')
        View content = this; // DrawView IST eine View
        content.setDrawingCacheEnabled(true); // !!
        content.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
        Bitmap bitmap = content.getDrawingCache();

        return bitmap;
    }


    // Method to add loaded BitmapDataObject to Canvas
    public void setmBitmapLoad(BitmapDataObject bitmapDataObject){
        loaded = true;
         mBitmap2 = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
         mBitmap2 = bitmapDataObject.getCurrentImage();
    }

    // overidden methods of "View"
    @Override
    protected void onDraw(Canvas canvas) {

        /* das hier sieht umständlich aus aber so können wir auf einer
            geladenen Bitmap wieder weiterzeichnen
         */
        if (loaded) { // bei load Bitmap kopieren
            mBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            mBitmap = Bitmap.createBitmap(mBitmap2);
            canvas.drawBitmap(mBitmap, 0, 0, mBitmapPaint);
            loaded = false;
        } else { // sonst auf der vorhandenen Bitmap zeichnen
            canvas.drawBitmap(mBitmap, 0, 0, mBitmapPaint); // das MUSS sonst löscht du die alte Bitmap
            for (DrawPath fp : paths) {
                mPaint.setColor(fp.color);
                mPaint.setStrokeWidth(fp.strokeWidth * 1.75f);
                mPaint.setMaskFilter(null);
                canvas.drawPath(fp.path, mPaint);
            }
            canvas.save();
        }

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        switch(event.getAction()) {
            case MotionEvent.ACTION_DOWN :
                touchStart(x, y);
                invalidate();
                break;
            case MotionEvent.ACTION_MOVE :
                touchMove(x, y);
                invalidate();
                break;
            case MotionEvent.ACTION_UP :
                touchUp();
                invalidate();
                break;
        }

        return true;
    }


    // custom methods
    // https://stackoverflow.com/questions/7007429/how-to-draw-a-triangle-a-star-a-square-or-a-heart-on-the-canvas#14676589

    private void touchStart(float x, float y) {
        mPath = new Path();
        DrawPath fp = new DrawPath(currentColor, (int)strokeWidth, mPath);
        paths.add(fp);

        mPath.reset();
        mPath.moveTo(x, y);
        mX = x;
        mY = y;
    }

    private void touchMove(float x, float y) {
        float dx = Math.abs(x - mX);
        float dy = Math.abs(y - mY);

        if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
            mPath.quadTo(mX, mY, (x + mX) / 2, (y + mY) / 2);
            mX = x;
            mY = y;
        }
    }

    private void touchUp() {
        mPath.lineTo(mX, mY);
    }


}