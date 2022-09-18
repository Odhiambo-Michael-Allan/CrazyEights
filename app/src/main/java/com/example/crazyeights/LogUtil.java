package com.example.crazyeights;

import android.util.Log;

/**
 * A logging utility class..
 */
public class LogUtil {

    private static final String TAG = "CrazyEights";

    public static void i( String message, Object... args ) {
        if ( args.length > 0 )
            message = String.format( message, args );
        Log.i( TAG, message );
    }
}
