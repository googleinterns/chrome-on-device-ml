/* Copyright 2020. All Rights Reserved.
Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at
    http://www.apache.org/licenses/LICENSE-2.0
Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
==============================================================================*/
package org.chrome.device.ml.service;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.util.Log;
import androidx.annotation.Nullable;
import android.os.Process;

import org.chrome.device.ml.service.RemoteService;
import org.chrome.device.ml.service.RemoteServiceCallback;

public class MLService extends Service {
  private final static String TAG = "MLService";
  int mValue = 0;
  /**
   * This is a list of callbacks that have been registered with the
   * service.  Note that this is package scoped (instead of private) so
   * that it can be accessed more efficiently from inner classes.
   */
  final RemoteCallbackList<RemoteServiceCallback> mCallbacks
          = new RemoteCallbackList<RemoteServiceCallback>();

  private final RemoteService.Stub mbinder = new RemoteService.Stub() {
    @Override
    public int getPid(){
      Log.v(TAG, "getPid");
      return Process.myPid();
    }

    @Override
    public void basicTypes(int anInt, long aLong, boolean aBoolean,
                           float aFloat, double aDouble, String aString) {
      Log.v(TAG, "basicType");
    }

    @Override
    public void registerCallback(RemoteServiceCallback cb) throws RemoteException {
      Log.v(TAG, "registerCallback");
      if (cb != null) mCallbacks.register(cb);
    }

    @Override
    public void unregisterCallback(RemoteServiceCallback cb) throws RemoteException {
      Log.v(TAG, "unregisterCallback");
      if (cb != null) mCallbacks.unregister(cb);
    }
  };

  @Override
  public void onCreate() {
    super.onCreate();
    Log.v(TAG, "onCreate");
  }

  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {
    //TODO do something useful
    Log.i(TAG, "Received start id " + startId + ": " + intent);
    /**
     While this service is running, it will continually increment a
     number.  Send the first message that is used to perform the
     increment.
    */
    mHandler.sendEmptyMessage(REPORT_MSG);
    return Service.START_NOT_STICKY;
  }

  @Nullable
  @Override
  public IBinder onBind(Intent intent) {
    Log.v(TAG, "onBind");
    return mbinder;
  }

  @Override
  public void onDestroy() {
    Log.v(TAG, "onDestroy");
    /** Remove the next pending message to increment the counter, stopping
     * the increment loop.
     */
    mHandler.removeMessages(REPORT_MSG);
  }

  private static final int REPORT_MSG = 1;
  private final Handler mHandler = new Handler() {
    @Override public void handleMessage(Message msg) {
      switch (msg.what) {
        /* It is time to bump the value! */
        case REPORT_MSG: {
          // Up it goes.
          int value = ++mValue;

          /* Broadcast to all clients the new value. */
          final int N = mCallbacks.beginBroadcast();
          for (int i=0; i<N; i++) {
            try {
              mCallbacks.getBroadcastItem(i).valueChanged(value);
            } catch (RemoteException e) {
              Log.e(TAG, "Error handle message");
              /*
               The RemoteCallbackList will take care of removing
               the dead object for us.
              */
            }
          }
          mCallbacks.finishBroadcast();

          /* Repeat every 1 second. */
          sendMessageDelayed(obtainMessage(REPORT_MSG), 1*1000);
        } break;
        default:
          super.handleMessage(msg);
      }
    }
  };
}
