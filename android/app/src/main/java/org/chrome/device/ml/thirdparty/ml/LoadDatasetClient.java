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
package org.chrome.device.ml.thirdparty.ml;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Interface to load squad dataset. Provide passages for users to choose from & provide questions
 * for autoCompleteTextView.
 */
public class LoadDatasetClient {
    private static final String TAG = "CDML_LoadDatasetClient";
    private static final String JSON_DIR = "bert_qa.json";
    private static final String DIC_DIR = "bert_vocab.txt";
    private final Context context;

    private String[] contents;
    private String[] titles;
    private String[][] questions;
    private int contentLength;

    public LoadDatasetClient(Context context) {
        this.context = context;
        loadJson();
    }

    private void loadJson() {
        try {
            InputStream is = context.getAssets().open(JSON_DIR);
            JsonReader reader = new JsonReader(new InputStreamReader(is));
            HashMap<String, List<List<String>>> map = new Gson().fromJson(reader, HashMap.class);
            List<List<String>> jsonTitles = map.get("titles");
            List<List<String>> jsonContents = map.get("contents");
            List<List<String>> jsonQuestions = map.get("questions");

            titles = listToArray(jsonTitles);
            contents = listToArray(jsonContents);

            questions = new String[jsonQuestions.size()][];
            int index = 0;
            for (List<String> item : jsonQuestions) {
                questions[index++] = item.toArray(new String[item.size()]);
            }
            contentLength = index;

        } catch (IOException ex) {
            Log.e(TAG, ex.toString());
        }
    }

    private static String[] listToArray(List<List<String>> list) {
        String[] answer = new String[list.size()];
        int index = 0;
        for (List<String> item : list) {
            answer[index++] = item.get(0);
        }
        return answer;
    }

    public String[] getTitles() {
        return titles;
    }

    public String getContent(int index) {
        return contents[index];
    }

    public int getNumberOfContents() {
        return contentLength;
    }

    public String[] getQuestions(int index) {
        return questions[index];
    }

    public Map<String, Integer> loadDictionary() {
        Map<String, Integer> dic = new HashMap<>();
        try (InputStream ins = context.getAssets().open(DIC_DIR);
             BufferedReader reader = new BufferedReader(new InputStreamReader(ins))) {
            int index = 0;
            while (reader.ready()) {
                String key = reader.readLine();
                dic.put(key, index++);
            }
        } catch (IOException ex) {
            Log.e(TAG, ex.getMessage());
        }
        return dic;
    }
}
