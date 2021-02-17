package com.goregoblin.scratchboard3.model;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.Objects;

public class BitmapDataObject implements Serializable  {

    /*
        code from here:
        https://stackoverflow.com/questions/5871482/serializing-and-de-serializing-android-graphics-bitmap-in-java#5954641
     */

    private Bitmap currentImage;

    public Bitmap getCurrentImage() {
        return currentImage;
    }

    public void setCurrentImage(Bitmap currentImage) {
        this.currentImage = currentImage;
    }

    public BitmapDataObject(Bitmap bitmap)
    {
        currentImage = bitmap.copy(bitmap.getConfig(), true); // !! Kopieren! sonst bekommen wir error: "Can't compress a recycled bitmap"
    }

    private void writeObject(java.io.ObjectOutputStream out) throws IOException {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        currentImage.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] byteArray = stream.toByteArray();
        out.writeInt(byteArray.length);
        out.write(byteArray);
    }

    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        int bufferLength = in.readInt();
        byte[] byteArray = new byte[bufferLength];
        int pos = 0;
        do {
            int read = in.read(byteArray, pos, bufferLength - pos);

            if (read != -1) {
                pos += read;
            } else {
                break;
            }
        } while (pos < bufferLength);
        currentImage = BitmapFactory.decodeByteArray(byteArray, 0, bufferLength);
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BitmapDataObject that = (BitmapDataObject) o;
        return Objects.equals(currentImage, that.currentImage);
    }

    @Override
    public int hashCode() {
        return Objects.hash(currentImage);
    }
}