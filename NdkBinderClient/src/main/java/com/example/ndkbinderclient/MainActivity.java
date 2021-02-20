package com.example.ndkbinderclient;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.ConditionVariable;
import android.os.IBinder;
import android.util.Log;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.Constants;

public class MainActivity extends AppCompatActivity implements ServiceConnection
{
    // Used to load the 'native-lib' library on application startup.
    static
    {
        System.loadLibrary("native-lib");
    }

    private volatile boolean mIsServiceConnected = false;
    private final ConditionVariable mServiceConnectionWaitLock = new ConditionVariable();
    private TextView mTV = null;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTV = findViewById(R.id.sample_text);
    }

    @Override
    protected void onResume()
    {
        super.onResume();

        Intent intent = new Intent();
        intent.setClassName("com.example.javabinderservice",
                "com.example.javabinderservice.MyService");

        Log.d(Constants.LOG_TAG, "[App] [java] bindService");

        bindService(intent, this, BIND_AUTO_CREATE);

        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                runOnUiThread(new SetTextRunnable("Waiting to talk to IMyService..."));

                // Not connected to service yet?
                while(!mIsServiceConnected)
                {
                    mServiceConnectionWaitLock.block(); // waits for service connection
                }

                String returnedString = talkToService();

                runOnUiThread(new SetTextRunnable("Talked to IMyService. Returned : " + returnedString));
            }
        }).start();
    }

    @Override
    protected void onPause()
    {
        unbindService(this);

        mIsServiceConnected = false;

        onServiceDisconnected();

        Log.d(Constants.LOG_TAG, "[App] [java] unbindService");

        super.onPause();
    }

    @Override
    public void onServiceConnected(ComponentName componentName, IBinder iBinder)
    {
        Log.d(Constants.LOG_TAG, "[App] [java] onServiceConnected");

        onServiceConnected(iBinder);

        mIsServiceConnected = true;

        mServiceConnectionWaitLock.open(); // breaks service connection waits
    }

    @Override
    public void onServiceDisconnected(ComponentName componentName)
    {
        mIsServiceConnected = false;

        onServiceDisconnected();

        Log.d(Constants.LOG_TAG, "[App] [java] onServiceDisconnected");
    }

    private class SetTextRunnable implements Runnable
    {
        final String mText;

        SetTextRunnable(String text)
        {
            mText = text;
        }

        @Override
        public void run()
        {
            mTV.setText(mText);
        }
    }

    /**
     * A native methods that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native void onServiceConnected(IBinder binder);
    public native void onServiceDisconnected();
    public native String talkToService();
}
