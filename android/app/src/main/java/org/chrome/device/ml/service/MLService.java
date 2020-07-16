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
import android.os.Looper;
import android.os.Message;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.util.Log;

import androidx.annotation.Nullable;

import android.os.Process;

import java.util.ArrayList;

import org.chrome.device.ml.experiments.Experiment;
import org.chrome.device.ml.experiments.MobileBertExperiment;
import org.chrome.device.ml.service.RemoteService;
import org.chrome.device.ml.service.RemoteServiceCallback;

/**
 * Runs ML task on a service
 */
public class MLService extends Service {
    private final static String TAG = "MLService";
    private static final int MSG_REPORT = 1;
    private static final int MODELS_SIZE = 1;

    private Handler expHandler;
    private ArrayList experiments;
    private double expTime;
    private int modelSelection;

    // This is a list of callbacks that have been registered with the service.
    private final RemoteCallbackList<RemoteServiceCallback> mCallbacks
            = new RemoteCallbackList<RemoteServiceCallback>();

    private final RemoteService.Stub mbinder = new RemoteService.Stub() {
        @Override
        public int getPid() {
            return Process.myPid();
        }

        @Override
        public void basicTypes(int anInt, long aLong, boolean aBoolean,
                float aFloat, double aDouble, String aString) {
        }

        @Override
        public void taskStart() throws RemoteException {
            experimentRun();
        }

        @Override
        public void registerCallback(RemoteServiceCallback cb) throws RemoteException {
            Log.v(TAG, "Service callback register.");
            if (cb != null) mCallbacks.register(cb);
        }

        @Override
        public void unregisterCallback(RemoteServiceCallback cb) throws RemoteException {
            Log.v(TAG, "Serivce callback unregister.");
            if (cb != null) mCallbacks.unregister(cb);
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        modelSelection = 0;
        expHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                experimentMessageHandler(msg);
            }
        };

        experiments = new ArrayList();
        experiments.add(new MobileBertExperiment(getApplicationContext(), expHandler));
        for (int i = 0; i < MODELS_SIZE; i++) {
            ((Experiment) experiments.get(i)).initialize();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "Received start id " + startId + ": " + intent);
        experimentRun();
        return Service.START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mbinder;
    }

    @Override
    public void onDestroy() {
        for (int i = 0; i < MODELS_SIZE; i++) {
            ((Experiment) experiments.get(i)).close();
        }

        // Remove the next pending message to increment the counter, stoppin the increment loop
        mHandler.removeMessages(MSG_REPORT);
    }

    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            Log.v(TAG, "msg.what: " + msg.what);
            switch (msg.what) {
                case MSG_REPORT: {
                    // Broadcast to all clients the new value
                    final int N = mCallbacks.beginBroadcast();
                    for (int i = 0; i < N; i++) {
                        try {
                            mCallbacks.getBroadcastItem(i).timeChanged(expTime);
                        } catch (RemoteException e) {
                            Log.e(TAG, e.getStackTrace().toString());
                        }
                    }
                    mCallbacks.finishBroadcast();
                }
                break;
                default:
                    super.handleMessage(msg);
            }
        }
    };

    // Handle messages from experiments
    private void experimentMessageHandler(Message msg) {
        expTime = ((Experiment) experiments.get(modelSelection)).getTime();
        Log.v(TAG, "Time: + " + expTime);
        mHandler.sendEmptyMessage(MSG_REPORT);
    }

    // Run experiment
    private void experimentRun() {
        int contents = 1;
        String texttoShow = "Running contents: " + contents + "\n";
        texttoShow += "...\n";
        Log.v(TAG, texttoShow);

        expHandler.post(
                () -> {
                    ((Experiment) experiments.get(modelSelection)).evaluate(contents);
                }
        );
    }
}
