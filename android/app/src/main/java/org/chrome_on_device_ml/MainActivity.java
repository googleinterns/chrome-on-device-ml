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
import org.chrome_on_device_ml.MobileBertClassification;


public class MainActivity extends AppCompatActivity {
	private static final String TAG = "ChromeOnDeviceML";
	private MobileBertClassification client;

	private EditText inputEditText;
	private Button classifyButton;
	private Handler handler;

//    private handler

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
							classify("test");
						});

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
	}
}