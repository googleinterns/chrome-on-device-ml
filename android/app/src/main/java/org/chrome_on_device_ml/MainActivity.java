package org.chrome_on_device_ml;

import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Handler;
import android.util.Log;
import android.view.View;

import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;

import org.chrome_on_device_ml.MobileBertClassification.Result;

import java.util.List;

public class MainActivity extends AppCompatActivity {
	private static final String TAG = "ChromeOnDeviceML";
	private MobileBertClassification client;

	private EditText inputEditText;
	private Button classifyButton;
	private Handler handler;
	private TextView resultTextView;
	private ScrollView scrollView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		Log.v(TAG, "onCreate");

		Toolbar toolbar = findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

		client = new MobileBertClassification(getApplicationContext());
		handler = new Handler();

		inputEditText = findViewById(R.id.input_text);
		classifyButton = findViewById(R.id.button);
		classifyButton.setOnClickListener(
						(View v) -> {
							classify(inputEditText.getText().toString());
						});
		scrollView = findViewById(R.id.scroll_view);
		resultTextView = findViewById(R.id.result_text_view);
	}

	@Override
	protected void onStart() {
		super.onStart();
		Log.v(TAG, "onStart");
		handler.post(
						() -> {
							client.load();
						});
	}

	@Override
	protected void onStop() {
		super.onStop();
		Log.v(TAG, "onStop");
		handler.post(
						() -> {
							client.unload();
						});
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
//							Log.v(TAG, textToShow);

							// Clear the input text.
							inputEditText.getText().clear();

							// Scroll to the bottom to show latest entry's classification result.
							scrollView.post(() -> scrollView.fullScroll(View.FOCUS_DOWN));
						});
	}
}