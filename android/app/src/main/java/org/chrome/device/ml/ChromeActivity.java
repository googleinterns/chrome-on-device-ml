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
package org.chrome.device.ml;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.browser.customtabs.CustomTabsIntent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import org.chrome.device.ml.customTab.CustomTabActivityHelper;
import org.chrome.device.ml.ml.TextClassification;
import org.chrome.device.ml.ml.TextClassification.Result;
import org.chrome.device.ml.service.MLService;
import org.chrome.device.ml.service.RemoteService;
import org.chrome.device.ml.service.RemoteServiceCallback;

public class ChromeActivity extends AppCompatActivity implements ServiceConnection {
  private static final String TAG = "ChromeOnDeviceML";
  private static final String [] MODELS = {"Bert", "MobileBert"};
  private static final int MSG_TIME_UPDATE = 1;

  private Button classifyButton;
  private Handler mhandler;
  private Handler tabHandler;
  public TextView resultTextView;
  private ScrollView scrollView;
  private Spinner modelSpinner;
  private int spinnerSelection;

  private TextClassification client;

  /** ML Service */
  private RemoteService mService;
  private Intent mBindIntent;
  private Handler serviceHandler;
  private RemoteServiceCallback serviceCallback;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    Log.v(TAG, "onCreate");

    Toolbar toolbar = findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);

    client = new TextClassification(getApplicationContext());
    mhandler = new Handler(Looper.getMainLooper());
    tabHandler = new Handler();

    modelSpinner = findViewById(R.id.modelSpinner);
    classifyButton = findViewById(R.id.button);
    classifyButton.setOnClickListener(
            (View v) -> {
              buttonHandler();
            });
    scrollView = findViewById(R.id.scroll_view);
    resultTextView = findViewById(R.id.result_text_view);

    addItemsOnSpinner();
    addListenerOnSpinnerItemSelection();

    mBindIntent = new Intent(ChromeActivity.this, MLService.class);
    mBindIntent.setAction(RemoteService.class.getName());
  }


  @Override
  protected void onStart() {
    super.onStart();

    serviceHandler = new Handler() {
      @Override public void handleMessage(Message msg) {
        switch (msg.what) {
          case MSG_TIME_UPDATE:
            double time = (double) msg.obj;
            textboxAppend("Time: " + time + "\n");
            break;
          default:
            super.handleMessage(msg);
        }
      }
    };

    serviceCallback = new RemoteServiceCallback.Stub() {
      /**
       * This is called by the remote service regularly to tell us about
       * new values.  Note that IPC calls are dispatched through a thread
       * pool running in each process, so the code executing here will
       * NOT be running in our main thread like most other things -- so,
       * to update the UI, we need to use a Handler to hop over there.
       */
      public void timeChanged(double time) {
        Message msg = new Message();
        msg.what = MSG_TIME_UPDATE;
        msg.obj = time;
        serviceHandler.sendMessage(msg);
      }
    };

    bindService(mBindIntent, this, Context.BIND_AUTO_CREATE);
  }

  @Override
  protected void onStop() {
    super.onStop();
    if (mService != null) {
      try {
        mService.unregisterCallback(serviceCallback);
      } catch (RemoteException e) {
        Log.e(TAG, "Error unregister callback");
      }
    }
    unbindService(this);
    stopService(new Intent(ChromeActivity.this, MLService.class));

  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    /** Inflate the menu; this adds items to the action bar if it is present. */
    getMenuInflater().inflate(R.menu.menu_main, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    /**
     Handle action bar item clicks here. The action bar will
     automatically handle clicks on the Home/Up button, so long
     as you specify a parent activity in AndroidManifest.xml.
    */
    int id = item.getItemId();

    /** noinspection SimplifiableIfStatement */
    if (id == R.id.action_settings) {
      return true;
    }

    return super.onOptionsItemSelected(item);
  }

  public void addListenerOnSpinnerItemSelection() {
    modelSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
      @Override
      public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        spinnerSelection = i;
        textboxAppend("Model Selected: " + MODELS[spinnerSelection] + "\n");
      }

      @Override
      public void onNothingSelected(AdapterView<?> adapterView) {
        spinnerSelection = 0;
      }
    });
  }

  private void addItemsOnSpinner() {
    List<String> list = new ArrayList<String>();
    list.add("Bert");
    list.add("MobileBert");
    ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(getApplicationContext(),
      android.R.layout.simple_spinner_item, list);
    dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    modelSpinner.setAdapter(dataAdapter);
  }

  /** Handles button actions */
  private void buttonHandler() {

    textboxAppend("IPC\n");
    Log.v(TAG, "IPC");

    this.startService(mBindIntent);
  }

  @Override
  public void onServiceConnected(ComponentName componentName, IBinder service) {
    Log.v(TAG, "Service connected.");
    mService = RemoteService.Stub.asInterface(service);

    /** Monitor service */
    try{
      mService.registerCallback(serviceCallback);
    } catch (RemoteException e) {
      Log.e(TAG, e.getStackTrace().toString());
    }
  }

  @Override
  public void onServiceDisconnected(ComponentName componentName) {
    Log.e(TAG, "Service has unexpectedly disconnected");
    mService = null;
  }

  /** Show experiment result in textbox */
  private void showExperimentResult(double time, int numberOfContents) {
    runOnUiThread(
      () -> {
        DecimalFormat df2 = new DecimalFormat("##.##");
        String textToShow = "Time: " + df2.format(time) + "\n";
        textboxAppend(textToShow);
      }
    );
  }

  /** Send input text to TextClassificationClass and show the classify messages */
  private void classify(final String text) {
    Log.d(TAG, "classify run");

    //TODO: move this to service
    mhandler.post(
      () -> {
        // Run text classification with TF Lite.
        List<Result> results = client.classify(text);

        // Show classification result on screen
        showResult(text, results);
      }
    );
  }

  /** Show classification result on the screen. */
  private void showResult(final String inputText, final List<Result> results) {
    // Run on UI thread as we'll updating our app UI
    runOnUiThread(
      () -> {
        String textToShow = "Input: " + inputText + "\nOutput:\n";
        for (int i = 0; i < results.size(); i++) {
          Result result = results.get(i);
          textToShow +=
                  String.format("    %s: %s\n", result.getTitle(), result.getConfidence());
        }
        textToShow += "---------\n";
        textboxAppend(textToShow);
      });
  }

  /** Append text to the textbox and scroll down the view. */
  private void textboxAppend(String text) {
    resultTextView.append(text);
    scrollView.post(() -> scrollView.fullScroll(View.FOCUS_DOWN));
  }

  private void openCustomTab(String url) {
    CustomTabsIntent.Builder intentBuilder = new CustomTabsIntent.Builder();
    intentBuilder.setShowTitle(true);

    CustomTabActivityHelper.openCustomTab(
            this, intentBuilder.build(), Uri.parse(url));
  }
}