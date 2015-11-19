package de.jkliemann.parkendd;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

/**
 * Created by jkliemann on 19.11.15.
 */
public class SlotPopup extends Drawable {

    private String text;

    public SlotPopup(String text){
        this.text = text;
    }

    private Bitmap textAsBitmap(String text) {
        Paint paint = new Paint();
        paint.setTextSize(24);
        paint.setColor(Color.BLACK);
        paint.setTextAlign(Paint.Align.LEFT);
        float baseline = -paint.ascent(); // ascent() is negative
        int width = (int) (paint.measureText(text) + 0.5f); // round
        int height = (int) (baseline + paint.descent() + 0.5f);
        Bitmap image = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(image);
        canvas.drawText(text, 0, baseline, paint);
        return image;
    }

    @Override
    public void draw(Canvas canvas){
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
        Bitmap bmp = textAsBitmap(this.text);
        BitmapDrawable bmpd =  new BitmapDrawable(context.getResources(), bmp);
        return bmpd;
    }
}
