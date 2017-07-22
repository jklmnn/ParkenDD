package de.jkliemann.parkendd.Views.Drawables;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;

import de.jkliemann.parkendd.Model.ParkingSpot;

/**
 * Created by jkliemann on 19.11.15.
 */
public class SpotIconBitmap extends BitmapDrawable {
    private ParkingSpot spot;

    public SpotIconBitmap(Resources res, Bitmap bmp){
        super(res, bmp);
    }

    public void setSpot(ParkingSpot spot){
        this.spot = spot;
    }

    public ParkingSpot getSpot(){
        return this.spot;
    }
}
