package org.chrome_on_device_ml;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.util.Log;

import androidx.annotation.WorkerThread;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.tensorflow.lite.Interpreter;

public class MobileBertClassification {
	private static final String TAG = "TextClassificationDemo";
	private static final String MODEL_PATH = "text_classification.tflite";
	private static final String DIC_PATH = "text_classification_vocab.txt";
	private static final String LABEL_PATH = "text_classification_labels.txt";

	private final Context context;
	private final Map<String, Integer> dic = new HashMap<>();
	private final List<String> labels = new ArrayList<>();
	private Interpreter tflite;

	public static class Result  {

	}

	public MobileBertClassification(Context context) {
		this.context = context;
	}

	/** Load the TF Lite model and dictionary so that the client can start classifying text. */
	@WorkerThread
	public void load() {
		loadModel();
		loadDictionary();
		loadLabels();
	}

	/** Free up resources as the client is no longer needed. */
	@WorkerThread
	public synchronized void unload() {
		tflite.close();
		dic.clear();
		labels.clear();
	}

	/** Load TF Lite model. */
	@WorkerThread
	private synchronized void loadModel() {
		try {
			ByteBuffer buffer = loadModelFile(this.context.getAssets());
			tflite = new Interpreter(buffer);
			Log.v(TAG, "TFLite model loaded.");
		} catch (IOException ex) {
			Log.e(TAG, ex.getMessage());
		}
	}

	/** Load words dictionary. */
	@WorkerThread
	private synchronized void loadDictionary() {
		try {
			loadDictionaryFile(this.context.getAssets());
			Log.v(TAG, "Dictionary loaded.");
		} catch (IOException ex) {
			Log.e(TAG, ex.getMessage());
		}
	}

	/** Load labels. */
	@WorkerThread
	private synchronized void loadLabels() {
		try {
			loadLabelFile(this.context.getAssets());
			Log.v(TAG, "Labels loaded.");
		} catch (IOException ex) {
			Log.e(TAG, ex.getMessage());
		}
	}

	/** Load TF Lite model from assets. */
	private static MappedByteBuffer loadModelFile(AssetManager assetManager) throws IOException {
		try (AssetFileDescriptor fileDescriptor = assetManager.openFd(MODEL_PATH);
				 FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor())) {
			FileChannel fileChannel = inputStream.getChannel();
			long startOffset = fileDescriptor.getStartOffset();
			long declaredLength = fileDescriptor.getDeclaredLength();
			return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
		}
	}

	/** Load dictionary from assets. */
	private void loadLabelFile(AssetManager assetManager) throws IOException {
		try (InputStream ins = assetManager.open(LABEL_PATH);
				 BufferedReader reader = new BufferedReader(new InputStreamReader(ins))) {
			// Each line in the label file is a label.
			while (reader.ready()) {
				labels.add(reader.readLine());
			}
		}
	}

	/** Load labels from assets. */
	private void loadDictionaryFile(AssetManager assetManager) throws IOException {
		try (InputStream ins = assetManager.open(DIC_PATH);
				 BufferedReader reader = new BufferedReader(new InputStreamReader(ins))) {
			// Each line in the dictionary has two columns.
			// First column is a word, and the second is the index of this word.
			while (reader.ready()) {
				List<String> line = Arrays.asList(reader.readLine().split(" "));
				if (line.size() < 2) {
					continue;
				}
				dic.put(line.get(0), Integer.parseInt(line.get(1)));
			}
		}
	}
}
