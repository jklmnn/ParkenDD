package de.jkliemann.parkendd;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;

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
