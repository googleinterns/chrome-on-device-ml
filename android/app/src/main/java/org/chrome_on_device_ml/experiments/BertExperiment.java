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
package org.chrome_on_device_ml.experiments;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import java.util.ArrayList;
import java.util.List;

import org.chrome_on_device_ml.ml.LoadDatasetClient;
import org.chrome_on_device_ml.ml.QaAnswer;
import org.chrome_on_device_ml.ml.QaClient;

/** Bert model question/answer experiment */
public class BertExperiment{
  private static final String TAG = "CDML_MobileBert";
  private final Context context;
  private Handler handler;
  private Handler activityHandler;

  private LoadDatasetClient datasetClient;
  private QaClient qaClient;
  private ArrayList<Double> timing;

  public BertExperiment(Context context, Handler handler) {
    this.context = context;
    this.activityHandler = handler;

    this.datasetClient = new LoadDatasetClient(this.context);

    HandlerThread handlerThread = new HandlerThread("BertExp");
    handlerThread.start();

    this.handler = new Handler(handlerThread.getLooper());
    this.qaClient = new QaClient(this.context);
    this.timing = new ArrayList<Double>();
  }

  public void initialize() {
    handler.post(
      () -> {
        qaClient.loadModel();
        qaClient.loadDictionary();
      });
  }

  public void close() {
    handler.post(
      () -> {
        qaClient.close();
      });
  }

  // Evaluates Bert model with contents and questions
  public void evaluate(int numberOfContents) {
    timing.clear();
    int contentsRun = Math.min(numberOfContents, datasetClient.getNumberOfContents());
    handler.post(
      () -> {
        for (int i = 0; i < contentsRun; i++) {
          // fetch a content
          final String content = datasetClient.getContent(i);
          String[] question_set = datasetClient.getQuestions(i);

          for (int j = 0; j < question_set.length; j++) {
            // fetch a question
            String question = question_set[j];

            // Add question mark to match with the dataset
            if (!question.endsWith("?")) {
              question += '?';
            }

            // Run model and store timing
            final String questionToAsk = question;
            long beforeTime = System.currentTimeMillis();
            final List<QaAnswer> answers = qaClient.predict(questionToAsk, content);
            long afterTime = System.currentTimeMillis();
            Double contentTime = new Double((afterTime - beforeTime) / 1000.0);
            timing.add(contentTime);
          }
        }
        // Send message to UI thread
        Message doneMsg = new Message();
        doneMsg.what = 0;
        doneMsg.obj = "Evaluation Finished";
        this.activityHandler.sendMessage(new Message());
      }
    );
  }

  public double getTime() {
    double time = 0;
    for (Double item : timing) {
      time += item;
    }
    return time/timing.size();
  }
}
