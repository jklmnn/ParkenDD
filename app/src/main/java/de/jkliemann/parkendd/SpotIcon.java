package de.jkliemann.parkendd;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

/**
 * Created by jkliemann on 17.11.15.
 */
public class SpotIcon extends Drawable {

    ParkingSpot spot;
    private final Paint red;
    private final Paint green;
    private final Paint blue;
    private final Paint black;

    public SpotIcon(ParkingSpot spot){
        this.spot = spot;
        red = new Paint();
        red.setColor(Color.argb(0xff, 0xef, 0x53, 0x50));
        green = new Paint();
        green.setColor(Color.argb(0xff, 0x66, 0xbb, 0x6a));
        blue = new Paint();
        blue.setColor(Color.argb(0xff, 0x42, 0xa5, 0xf5));
        black = new Paint();
        black.setColor(Color.argb(0xff, 0x00, 0x00, 0x00));
    }

    @Override
    public void draw(Canvas canvas){
        RectF oval = new RectF(0, 0, 50, 50);
        switch (this.spot.state()) {
            case "closed":
                canvas.drawArc(oval, 0, 360, true, black);
                break;
            case "nodata":
                canvas.drawArc(oval, 0, 360, true, blue);
                break;
            default:
                float free = 360 * ((float)this.spot.free()/(float)this.spot.count());
                canvas.drawArc(oval, (free / 2) * (-1) - 90, free, true, green);
                canvas.drawArc(oval, (free / 2) - 90, 360 - free, true, red);
        }
    }

    @Override
    protected boolean onLevelChange(int level){
        invalidateSelf();
        return true;
    }

    @Override
    public void setAlpha(int alpha){

    }

    @Override
    public void setColorFilter(ColorFilter cf){

    }

    public int getOpacity(){
        return PixelFormat.TRANSLUCENT;
    }

    public Drawable getBitmapDrawable(Context context){
        Bitmap bmp = Bitmap.createBitmap(50, 50, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bmp);
        this.setBounds(0, 0, 50, 50);
        this.draw(canvas);
        SpotIconBitmap bmpd =  new SpotIconBitmap(context.getResources(), bmp);
        bmpd.setSpot(this.spot);
        return bmpd;
    }

    public ParkingSpot getSpot(){
        return this.spot;
    }
}
