package br.com.nutrisolver.tools;


/*
Utilitario para exibir Toast sobrepondo o anterior (caso haja)
Foi preciso criar isso porque um toast so estava aparecendo quando o anterior acabava
 */

import android.content.Context;
import android.widget.Toast;

public class ToastUtil {

    private static Toast toast = null;

    public static void show(Context context, String text, int duration) {

        if (toast != null)
            toast.cancel();

        toast = Toast.makeText(context.getApplicationContext(), text, duration);
        toast.show();
    }
}
