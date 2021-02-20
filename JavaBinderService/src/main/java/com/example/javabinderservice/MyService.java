package com.example.javabinderservice;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import com.example.Constants;
import com.example.ComplexType;
import com.example.IMyService;

public class MyService extends Service
{
    private IBinder mBinder;

    @Override
    public void onCreate()
    {
        super.onCreate();

        mBinder = new MyServiceBinder();
    }

    @Override
    public IBinder onBind(Intent intent)
    {
        Log.d(Constants.LOG_TAG, "[MyService] [java] A client binds the service");

        return mBinder;
    }

    private static class MyServiceBinder extends IMyService.Stub
    {
        @Override
        public void basicTypes(int anInt, long aLong, boolean aBoolean, float aFloat,
                               double aDouble, String aString) throws RemoteException
        {
            StringBuilder str = new StringBuilder();
            str.append("[MyService] [java] basicTypes : ")
                .append("int=").append(anInt)
                .append(", long=").append(aLong)
                .append(", boolean=").append(aBoolean)
                .append(", float=").append(aFloat)
                .append(", double=").append(aDouble)
                .append(", string=").append(aString);
            Log.d(Constants.LOG_TAG, str.toString());
        }

        @Override
        public String complexType(ComplexType aComplexObject) throws RemoteException
        {
            StringBuilder str = new StringBuilder();
            str.append("[MyService] [java] complexType : ")
                    .append("int=").append(aComplexObject.mInt)
                    .append(", long=").append(aComplexObject.mLong)
                    .append(", boolean=").append(aComplexObject.mBoolean)
                    .append(", float=").append(aComplexObject.mFloat)
                    .append(", double=").append(aComplexObject.mDouble)
                    .append(", string=").append(aComplexObject.mString);
            Log.d(Constants.LOG_TAG, str.toString());

            return aComplexObject.toString();
        }

        @Override
        public ComplexType returnComplexType(int anInt, long aLong, boolean aBoolean,
                                                         float aFloat, double aDouble,
                                                         String aString) throws RemoteException
        {
            StringBuilder str = new StringBuilder();
            str.append("[MyService] [java] returnComplexType : ")
                    .append("int=").append(anInt)
                    .append(", long=").append(aLong)
                    .append(", boolean=").append(aBoolean)
                    .append(", float=").append(aFloat)
                    .append(", double=").append(aDouble)
                    .append(", string=").append(aString);
            Log.d(Constants.LOG_TAG, str.toString());

            ComplexType co = new ComplexType(anInt, aLong, aBoolean, aFloat, aDouble, aString);

            return co;
        }
    }
}
