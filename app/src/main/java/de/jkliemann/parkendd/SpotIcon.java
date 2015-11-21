package de.jkliemann.parkendd;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.RectF;
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
    private Context context;
    private int size;

    public SpotIcon(ParkingSpot spot, Context context){
        this.spot = spot;
        red = new Paint();
        red.setColor(Color.argb(0xff, 0xef, 0x53, 0x50));
        green = new Paint();
        green.setColor(Color.argb(0xff, 0x66, 0xbb, 0x6a));
        blue = new Paint();
        blue.setColor(Color.argb(0xff, 0x42, 0xa5, 0xf5));
        black = new Paint();
        black.setColor(Color.argb(0xff, 0x00, 0x00, 0x00));
        this.context = context;
        size = (int)context.getResources().getDisplayMetrics().density * 20;
    }

    @Override
    public void draw(Canvas canvas){
        RectF oval = new RectF(0, 0, size, size);
        switch (this.spot.state()) {
            case "closed":
                canvas.drawArc(oval, 0, 360, true, black);
                break;
            case "nodata":
                canvas.drawArc(oval, 0, 360, true, blue);
                break;
            default:
                float free = 360 * ((float)this.spot.free()/(float)this.spot.count());
                if(free > 360){
                    free = 360;
                }
                canvas.drawArc(oval,- 90, free, true, green);
                canvas.drawArc(oval, free - 90, 360 - free, true, red);
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

    public Drawable getBitmapDrawable(){
        Bitmap bmp = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bmp);
        this.setBounds(0, 0, size, size);
        this.draw(canvas);
        SpotIconBitmap bmpd =  new SpotIconBitmap(context.getResources(), bmp);
        bmpd.setSpot(this.spot);
        return bmpd;
    }

    public ParkingSpot getSpot(){
        return this.spot;
    }
}
