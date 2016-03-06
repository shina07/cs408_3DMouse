package com.forblind.threedmouse.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by user on 2015-12-09.
 */
public class TDPreferences {

    public static final int MODE_PRIVATE = 0;

    Context context;
    static SharedPreferences pref;

    public TDPreferences(Context context){
        this.context = context;
        if(pref == null){
            pref = context.getSharedPreferences(context.getPackageName(), MODE_PRIVATE);
        }
    }

    public static TDPreferences getInstance(Context context){
        return new TDPreferences(context);
    }

    public static SharedPreferences getPreference(Context context) {
        if( pref == null ) {
            pref = context.getSharedPreferences(context.getPackageName(), MODE_PRIVATE);
        }
        return pref;
    }

    public static int getInt(String key){
        return pref.getInt(key, 0);
    }

    public static Boolean setInt(String key, int value)
    {
        SharedPreferences.Editor editor = pref.edit();
        editor.putInt(key, value);
        return editor.commit();
    }
}
