package com.example.ndkbinderservice;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.example.Constants;

public class MyService extends Service
{
    // Used to load the 'native-lib' library on application startup.
    static
    {
        System.loadLibrary("native-lib");
    }

    private IBinder mBinder;

    @Override
    public void onCreate()
    {
        super.onCreate();

        mBinder = createServiceBinder();

        if(null == mBinder)
        {
            Log.w(Constants.LOG_TAG, "[MyService] [java] Binder is null");
        }
        else
        {
            Log.d(Constants.LOG_TAG, "[MyService] [java] Binder is ready");
        }
    }

    @Override
    public IBinder onBind(Intent intent)
    {
        Log.d(Constants.LOG_TAG, "[MyService] [java] A client binds the service");

        return mBinder;
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native IBinder createServiceBinder();
}
