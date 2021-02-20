package com.example.javabinderclient;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.ConditionVariable;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.widget.TextView;

import com.example.IMyService;
import com.example.ComplexType;
import com.example.Constants;

public class MainActivity extends AppCompatActivity implements ServiceConnection
{
    private IMyService mService = null;
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
        intent.setClassName("com.example.ndkbinderservice",
                "com.example.ndkbinderservice.MyService");

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

                try
                {
                    Log.d(Constants.LOG_TAG, "[App] [java] IMyService.basicTypes");

                    mService.basicTypes(2021, 65535000, true, 3.14f,
                            3.141592653589793238, "Hello, World!");
                }
                catch (RemoteException e)
                {
                    Log.e(Constants.LOG_TAG, "[App] [java] Exception when invoking IMyService.basicTypes" +
                            e.getMessage());

                    e.printStackTrace();
                }

                String returnedString = null;

                ComplexType ct = new ComplexType(2021, 65535000, true,
                        3.14f, 3.141592653589793238, "Hello, World!");

                try
                {
                    Log.d(Constants.LOG_TAG, "[App] [java] IMyService.complexType");

                    returnedString = mService.complexType(ct);
                }
                catch (RemoteException e)
                {
                    Log.e(Constants.LOG_TAG, "[App] [java] Exception when invoking IMyService.complexType" +
                            e.getMessage());

                    e.printStackTrace();
                }

                ComplexType returnedObject = null;

                try
                {
                    Log.d(Constants.LOG_TAG, "[App] [java] IMyService.returnComplexType");

                    returnedObject = mService.returnComplexType(2021, 65535000,
                            true, 3.14f, 3.141592653589793238,
                            "Hello, World!");
                }
                catch (RemoteException e)
                {
                    Log.e(Constants.LOG_TAG, "[App] [java] Exception when invoking IMyService.returnComplexType" +
                            e.getMessage());

                    e.printStackTrace();
                }

                runOnUiThread(new SetTextRunnable("Talked to IMyService. Returned : " + returnedObject));
            }
        }).start();
    }

    @Override
    protected void onPause()
    {
        unbindService(this);

        mIsServiceConnected = false;

        mService = null;

        Log.d(Constants.LOG_TAG, "[App] [java] unbindService");

        super.onPause();
    }

    @Override
    public void onServiceConnected(ComponentName componentName, IBinder iBinder)
    {
        Log.d(Constants.LOG_TAG, "[App] [java] onServiceConnected");

        mService = IMyService.Stub.asInterface(iBinder);

        mIsServiceConnected = true;

        mServiceConnectionWaitLock.open(); // breaks service connection waits
    }

    @Override
    public void onServiceDisconnected(ComponentName componentName)
    {
        mIsServiceConnected = false;

        mService = null;

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
}
