

/*


NOT USED
older version of ChatRequest class
new version and ChatAdapter class are moved to Chat class


 */
package com.example.tukserveriapp;

import android.app.Activity;
import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.logging.LogRecord;

public class ChatRequest extends AppCompatActivity implements Runnable {

    String url;
    Activity context = this;

    ArrayList messages;
    ArrayList authors;
    ArrayList times;

    ChatRequest(String url) {
        this.url = url;
    }

    @Override
    public void run() {
        try {
            JSONObject json = new JSONObject(IOUtils.toString(new URL(url), Charset.forName("UTF-8")));

            JSONArray messagesArray = (JSONArray) json.get("messages");

            ArrayList messagesR = new ArrayList();
            ArrayList authorsR = new ArrayList();
            ArrayList timesR = new ArrayList();
            Log.d("info", "here1");

            for(int i = messagesArray.length() - 1; i > 0; i--) {
                JSONObject currentMessage = (JSONObject) messagesArray.get(i);
                Log.d("info", "looping");
                messagesR.add(currentMessage.get("message"));
                authorsR.add(currentMessage.get("author"));
                timesR.add(currentMessage.get("time"));
                Log.d("info", "loop done");
            }

            messages = messagesR;
            authors = authorsR;
            times = timesR;
            Log.d("info", "here2");

            /*mainHandler.post(new Runnable() {
                @Override
                public void run() {
                    ChatAdapter adapter = new ChatAdapter(context, messages, authors, times);
                    ListView chatList = findViewById(R.id.chat_list);
                    chatList.setAdapter(adapter);
                }
            });*/
            /*
            android.os.Handler threadHandler = new android.os.Handler(Looper.getMainLooper());
            threadHandler.post(new Runnable() {
                @Override
                public void run() {

                }
            });
            */

            /*
            runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    Log.d("info", "here3");

                    Log.d("info", "here4");
                    // End thread
                    Thread.currentThread().interrupt();
                }
            });*/
        } catch (JSONException e) {
            e.printStackTrace();
            Log.d("error", "json error");
        } catch (IOException e) {
            e.printStackTrace();
            Log.d("error", "IO error");
            runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    // End thread
                    Thread.currentThread().interrupt();
                }
            });
        } catch (Exception e) {
            Log.d("error", "Requested webserver not online");
            e.printStackTrace();
        }
    }

    public JSONArray returnData() {
        JSONArray object = new JSONArray();
        object.put(messages);
        object.put(authors);
        object.put(times);
        return object;
    }
}
