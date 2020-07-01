/* Copyright 2019 The TensorFlow Authors. All Rights Reserved.
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
package org.chrome_on_device_ml;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import java.text.DecimalFormat;
import java.util.List;

import org.chrome_on_device_ml.ml.TextClassification;
import org.chrome_on_device_ml.ml.TextClassification.Result;
import org.chrome_on_device_ml.experiments.BertExperiment;

public class ChromeActivity extends AppCompatActivity {
	private static final String TAG = "ChromeOnDeviceML";
	private TextClassification client;

	private EditText inputEditText;
	private Button classifyButton;
	private Handler handler;
	public TextView resultTextView;
	private ScrollView scrollView;
	private BertExperiment bert;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		Log.v(TAG, "onCreate");

		Toolbar toolbar = findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

		client = new TextClassification(getApplicationContext());
		handler = new Handler(Looper.getMainLooper()) {
			@Override
			public void handleMessage(Message msg) {
				messageHandler(msg);
			}
		};

		inputEditText = findViewById(R.id.input_text);
		classifyButton = findViewById(R.id.button);
		classifyButton.setOnClickListener(
			(View v) -> {
				buttonHandler();
			});
		scrollView = findViewById(R.id.scroll_view);
		resultTextView = findViewById(R.id.result_text_view);

		bert = new BertExperiment(getApplicationContext(), handler);
	}

	@Override
	protected void onStart() {
		super.onStart();
		bert.initialize();
	}

	@Override
	protected void onStop() {
		super.onStop();
		bert.close();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu_main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();

		//noinspection SimplifiableIfStatement
		if (id == R.id.action_settings) {
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	/** Hanldles messages from handler **/
	private void messageHandler(Message msg) {
		double time = bert.getTime();
		showExperimentResult(time, 1);
	}

	/** Handles button actions **/
	private void buttonHandler() {
		int contents = 1;
		String texttoShow = "Running contents: " + contents + "\n";
		texttoShow += "...\n";
		resultTextView.append(texttoShow);
		handler.post(
			() -> {
				bert.evaluate(contents);
			});
	}

	/** Show experiment result in textbox **/
	private void showExperimentResult(double time, int numberOfContents) {
		runOnUiThread(
			() -> {
				DecimalFormat df2 = new DecimalFormat("##.##");
				String textToShow = "Time: " + df2.format(time) + "\n";
				resultTextView.append(textToShow);
				scrollView.post(() -> scrollView.fullScroll(View.FOCUS_DOWN));
			}
		);
	}

	/** Send input text to TextClassificationClass and show the classify messages **/
	private void classify(final String text) {
		Log.d(TAG, "classify run");

		handler.post(
			() -> {
				// Run text classification with TF Lite.
				List<Result> results = client.classify(text);

				// Show classification result on screen
				showResult(text, results);
			});
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

				// Append the result to the UI.
				resultTextView.append(textToShow);

				// Clear the input text.
				inputEditText.getText().clear();

				// Scroll to the bottom to show latest entry's classification result.
				scrollView.post(() -> scrollView.fullScroll(View.FOCUS_DOWN));
			});
	}
}