package de.jkliemann.parkendd.Utilities;

import android.content.Context;
import android.widget.Toast;


/**
 * Created by jkliemann on 14.12.14.
 */
public class Error {

    public static void showLongErrorToast(Context context, String errorMessage){
        Toast errorToast = Toast.makeText(context, errorMessage, Toast.LENGTH_LONG);
        errorToast.show();
        return;
    }

    public static void showShortErrorToast(Context context, String errorMessage){
        Toast errorToast = Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT);
        errorToast.show();
        return;
    }
}
