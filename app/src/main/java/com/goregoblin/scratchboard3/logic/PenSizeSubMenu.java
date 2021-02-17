package com.goregoblin.scratchboard3.logic;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.goregoblin.scratchboard3.R;
import com.goregoblin.scratchboard3.gui.MainActivity;


public class PenSizeSubMenu extends View {

    private float circleRadius;
    private float currRad;
    private static float exRad;

    // variablen für Veränderung der PenGröße
    private static final float TOUCH_TOLERANCE = 4; // toleranz-wert um zu sensible reaktion zu vermeiden
    private float moveY;

    // variablen um die Display-metrics zur umrechnung zu verwenden
    private MainActivity mainActivity;
    private static final String LOG_CAT = PenSizeSubMenu.class.getSimpleName();
    private int scaleDPI;
    private float scaleDPIX;
    private float scaleDPIY;

    // Constructors from View
    public PenSizeSubMenu(Context context) {
        super(context);
    }

    public PenSizeSubMenu(Context context, AttributeSet attrs) {
        super(context, attrs);

        // hier context mit Attributen initialisieren
        init(context,attrs); // Methode init(...) siehe unten

        Log.i(LOG_CAT, "Verbindung zur MainActivity hergestellt");
        this.mainActivity = mainActivity;

        // wir brauchen die  "metrics" aber hier ist nicht der Ort um die abzugreifen oO'
        //  DisplayMetrics metrics = new DisplayMetrics();
//        mainActivity.getWindowManager().getDefaultDisplay().getMetrics(metrics);
        // scaleDPI = metrics.densityDpi;

        /*
            hier was zu dpi und wie man die screenn-auflösung x,y bekommt:
            https://stackoverflow.com/questions/3166501/getting-the-screen-density-programmatically-in-android
         */

        //scaleDPI = mainActivity.metrics.densityDpi; // das hier ist nur die auflösungseinstellung "DENSITY_LOW, DENSITY_MEDIUM or DENSITY_HIGH" hier der Wert 440 (der aber nciht mehr verwendet wird)

        /*
        https://stackoverflow.com/questions/12063928/using-dpi-instead-of-pixels-in-canvas-drawbitmap

            unsere auflösung ist:
            1080 X 2220 x 440
            Breite X Höhe X DPI

            das ist hier nicht so simple ^^'
            wir erstellen ja einen canvas in den wir eine Bitmap packen
            wir haben die größe der BITMAP definiert ABER nicht die größe des canvas oO'

            der Canvas wird daher einfach so groß wie die Bitmap

            --> das müssen wir nochmal besser ausknobeln ._.'
         */

        scaleDPIX = mainActivity.metrics.xdpi;
        scaleDPIY = mainActivity.metrics.ydpi;


        //submenuSize = 1000;
        //cBitmap = Bitmap.createBitmap(submenuSize,submenuSize, Bitmap.Config.ARGB_8888);
        //subCanvas = new Canvas(cBitmap);
        // subCanvas.getDensity();
         /*
            https://stackoverflow.com/questions/58394146/how-to-set-a-density-in-emulator-android-studio

            irgendwas stimmt mit der density nicht..
            440dpi wird eigentlich nicht mehr verwendet

            You can only choose between the following DPIs:
            120, 160, 213, 240, 280, 320, 360, 400, 420, 480, 560, 640

            https://stackoverflow.com/questions/58394146/how-to-set-a-density-in-emulator-android-studio


            --> aber alle modelle die wir verwenden haben 440dpi
                wir brauchen mal neue Modelle  --> AVD-Manager

            hier mal die Specificationen des fairphone3
            https://shop.fairphone.com/en/fairphone-3

                18:9 aspect ratio
                2160 x 1080 resolution
                427ppi pixel density (ppi ist NICHT dpi!! ) https://photographylife.com/dpi-vs-ppi
                Displaysize: 5.65 inch

                https://en.99designs.de/blog/tips/ppi-vs-dpi-whats-the-difference/
                dpi und ppi:
                "PPI (pixels per inch) describes the resolution in pixels of a digital image whereas DPI describes the amount of ink dots on a printed image.
                Though PPI largely refers to screen display, it also affects the print size of your design
                and thus the quality of the output.

                 DPI (dots per inch), on the other hand, has nothing to do with anything digital and primarily concerns print."

                confused? you are not the only one ^^'
                see here:
                https://stackoverflow.com/questions/32549719/android-screen-density-dpi-vs-ppi
                and here (!!!): --> das ist von nem Profi.. der beschreibt auch wie man bei iOS oder Android mit Auflösungen umgeht..
                https://www.sebastien-gabriel.com/designers-guide-to-dpi/

          */

        /*
        https://stackoverflow.com/questions/64204020/android-how-does-dp-occupy-different-space-in-devices-with-different-sizes
        "Android defines a baseline dpi of 160 which is used as the reference to compute sizes for all screen densities"

        "The display size is not related to "dp units". The display size is just how big the display canvas is.
         The screen DPI defines how many dots fit in 1 square inch. And a "dp unit" is an abstract unit that,
         depending on the device's DPI, is scaled (up or down) to give "uniform dimensions" on any screen size,
         by using 160 as the baseline reference."

            baseline = 160 dpi
            yourCurrentDPI = densityDPI
            px = dp * (yourCurrentDPI / 160)
         */

        int baselineDPI = 160; // 160 is the baseline used as the reference for all DPIs
        int pxWidth = mainActivity.metrics.widthPixels; // 1080
        int pxHeight = mainActivity.metrics.heightPixels; // 2148
        float density = mainActivity.metrics.density; // 2.75 --> this is all we need because 440/160 = 2.75
        float densityDPI = mainActivity.metrics.densityDpi; // 440



        // bitmap accepts only integer oO'
        //cBitmap = Bitmap.createBitmap(500,500, Bitmap.Config.ARGB_8888);

        // System.out.println("cBitmap.getDensity(): " + cBitmap.getDensity()); // 440

        //cBitmap.setDensity();

        // die größe des canvas orientiert sich an der Bitmap-größe; es ist ok das so zu machen
        // das brauchen wir eigentlich nicht da alles was den canvas betrifft in der Methode "onDraw()" geschieht
        //subCanvas = new Canvas(cBitmap);

    }


    private void setCircleRadius(float circRad){
        circleRadius = circRad;
    }

    // die pensize-methode nimmt nur int deswegen der downcast
    public float getCircleRadius(){
        return exRad;
    }

    // diese Methode ist geerbt von View
    // wird die View erstellt wird erstmal das gezeigt was hier geschrieben wurde
    /*
        hier mal was über canvas:
        https://jimbaca.com/what-is-canvas-in-android/
     */

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        /*
          px = dp * (yourCurrentDPI / 160)
         (440/160) = metrics.density = 2.75
         defined canvas in dp * metrics.density
         300dp * 2.75 = 825 px

          die Bitmap im canvas "bemalen"; müssen wir auch nicht.. die objekte sind AUF dem canvas..
            wir wollen hier ja keine Bitmap exportieren oder speichern sondern nur
            anzeigen wie groß der Pen ist..

            die Größe des Canvas haben wir in "activity_pensize" mit 300dp definiert

         */

        canvas.save();

        // die Frame um den canvas
        drawFrame(300,canvas);

        drawCirlce(canvas,circleRadius);

        // das hier um den radius an den MainActivcityListener zu kommunizieren..das geht evtl noch eleganter oO'
        exRad = circleRadius;

        // update der canvas
        invalidate();

    }


    // .. Android hat eben seine Eigenheiten oO'
    private void drawFrame(int usedDP, Canvas canvas){

        // you need to know what dp-size you used for this in activity_pensize.xml for this
        int definedDPinActivity = usedDP;
        int strokeWidthWant = 20;
        float density = mainActivity.metrics.density;

        int rectLeft = strokeWidthWant/2;
        int rectTop = strokeWidthWant/2;;
        int rectRight = (int)(definedDPinActivity*density); // we get out integer number but still have to take care of type
        int rectBottom = (int)(definedDPinActivity*density);

        // correct for strokeWidth
        rectRight-=rectLeft;
        rectBottom-=rectTop;

        Rect rectangle = new Rect(rectLeft,rectTop,rectRight,rectBottom);

        // the fillPaint
        Paint paintRect = new Paint(Color.BLUE);
        paintRect.setStyle(Paint.Style.STROKE); // for non-fill
        paintRect.setStrokeWidth(strokeWidthWant);

        // die Objekte dem canvas übergeben
        canvas.drawRect(rectangle, paintRect);

    }


    private void drawCirlce(Canvas canvas, float circRadius){

        int height = getHeight();
        int width = getWidth();

        int circleCenterX = width/2;
        float circleCenterY = height/2;

        // fill Color
        Paint paintcircle = new Paint();
        paintcircle.setColor(Color.BLACK);
        paintcircle.setStyle(Paint.Style.FILL);

        // circle
        canvas.drawCircle(circleCenterX, circleCenterY, circRadius, paintcircle);

    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {

        /*  hier Touch-Event für die Kreis-Größe
            wir wollen drag-up für Kreis größer
            und drag down für Kreis kleiner
            bestätigung mit Button
         */
        float y = event.getY();

        switch(event.getAction()) {

            case MotionEvent.ACTION_DOWN:
                moveY = event.getY();
                invalidate();
                break;

            case MotionEvent.ACTION_UP:
                moveY = 0;
                invalidate();
                break;

            case MotionEvent.ACTION_MOVE:
                boolean movingUp = touchMoveDirection(y);

                if (movingUp) {
                    currRad = getCircleRadius();
                    if (currRad < 350) {
                        currRad += 10.0;
                        setCircleRadius(currRad);
                    }else {
                        currRad = 350.0f;
                        setCircleRadius(currRad);
                    }
                } else {
                    /*  hier minus-werte verhindern!
                        sonst landen wir bei -xxx und der User sieht keinen Kreis mehr
                        das minimum so setzen das der Kreis immer noch gerade zu sehen ist
                     */
                    currRad = getCircleRadius();

                    if (currRad > 5){
                        currRad -= 10.0;
                        setCircleRadius(currRad);
                    } else{
                        currRad = 5.0f; // kleiner sieht man eh nicht ^^'
                        setCircleRadius(currRad);
                    }

                }
                invalidate();
                break;
        }
        return true;
    }


    // wir müssen hier testen ob die Bewegung auf der Screen aufwärts oder abwärts geht
    private boolean touchMoveDirection(float y) {
        //float dx = Math.abs(x - moveX); // brauchen wir nicht
        float dy = Math.abs(y - moveY); // oben | unten
        boolean isMovingUp = false;

        if (dy >= TOUCH_TOLERANCE) {

            if (y < moveY){
                isMovingUp = true;
            }else {
                isMovingUp = false;
            }
        }
        return isMovingUp;
    }


    // die datei die wir mit R.stylable ansteuern ist "attributes.xml" in res/values
    public void init(Context context, AttributeSet attributeSet){

        TypedArray typedArray = context.obtainStyledAttributes(attributeSet, R.styleable.PenSizeSubMenu);

        circleRadius = typedArray.getDimension(R.styleable.PenSizeSubMenu_radius, 20);
        int cColor = typedArray.getColor(R.styleable.PenSizeSubMenu_circleColor, Color.BLUE);

        typedArray.recycle();

    }

}
