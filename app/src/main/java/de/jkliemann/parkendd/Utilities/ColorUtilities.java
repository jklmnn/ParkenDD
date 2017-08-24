package de.jkliemann.parkendd.Utilities;

import android.animation.ArgbEvaluator;
import android.content.res.ColorStateList;
import android.graphics.Color;

/**
 * Created by kasam on 18/08/2017.
 */

public class ColorUtilities {

    public static int mixBetweenColors(float percentage, Object firstColor, Object secondColor) {
        ArgbEvaluator colorEvaluator = new ArgbEvaluator();

        return (int)colorEvaluator.evaluate(percentage, firstColor, secondColor);
    }

    public static int darkenColor(int color, float percentage) {
        float factor = (100-percentage)/100;

        int alpha = Color.alpha(color);

        int red =  Math.round(Color.red(color) * factor);
        int green = Math.round(Color.green(color) * factor);
        int blue = Math.round(Color.blue(color) * factor);

        return Color.argb(alpha, red, green, blue);
    }
}
