package de.jkliemann.parkendd;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

/**
 * Created by jkliemann on 19.11.15.
 */
public class SlotPopup extends Drawable {

    private String text;
    private Context context;
    private int density;

    public SlotPopup(String text, Context context){
        this.text = text;
        this.context = context;
        density = (int)this.context.getResources().getDisplayMetrics().density;
    }

    private Bitmap textAsBitmap(String text) {
        Paint paint = new Paint();
        paint.setTextSize(14 * this.density); //24
        paint.setColor(Color.BLACK);
        paint.setTextAlign(Paint.Align.LEFT);
        float baseline = -paint.ascent(); // ascent() is negative
        int width = (int) (paint.measureText(text) + 0.5f + 4 * this.density); // round
        int height = (int) (baseline + paint.descent() + 0.5f + 13 * this.density);
        Bitmap image = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(image);
        Paint rpaint = new Paint();
        rpaint.setColor(Color.WHITE);
        Rect rect = new Rect(0, 0, width, height - 9 * this.density);
        RectF rectf = new RectF(rect);
        canvas.drawRoundRect(rectf, (float)4 * this.density, (float)4 * this.density, rpaint);
        canvas.drawText(text, (float)2 * this.density, baseline + (float)2 * this.density, paint);
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

    public Drawable getBitmapDrawable(){
        Bitmap bmp = textAsBitmap(this.text);
        BitmapDrawable bmpd =  new BitmapDrawable(this.context.getResources(), bmp);
        return bmpd;
    }
}
