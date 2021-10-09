package com.example.tukserveriapp;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;

public class Chat extends AppCompatActivity {

    String statusText;
    String playerCountText;
    String playerListText;

    TextView noMessages;
    Button reloadChatBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Receive data from Status activity to display them again once user returns from Chat to Status activity (so status data doesn't need to be requested from webserver again)
        try {
            Bundle extras = getIntent().getExtras();
            statusText = extras.getString("status");
            playerCountText = extras.getString("playerCount");
            playerListText = extras.getString("playerList");
        } catch (Exception e) {
            // Means that no extras were sent (activity was not opened from Status activity)
        }

        setContentView(R.layout.activity_chat);
        noMessages = findViewById(R.id.noMessages);
        noMessages.setVisibility(View.INVISIBLE);
        reloadChatBtn = findViewById(R.id.reloadChatBtn);
        reloadChatBtn.setVisibility(View.INVISIBLE);
        reloadChatBtn.setEnabled(false);

        runChatRequest();
    }

    public void runChatRequest() {
        ChatRequest request = new ChatRequest("http://80.235.83.219:2005/static/server_chat.json");
        new Thread(request).start();
    }

    public void back(View v) {
        Intent intent = new Intent(this, Status.class);
        // Send data back to Status activity
        intent.putExtra("status", statusText);
        intent.putExtra("playerCount", playerCountText);
        intent.putExtra("playerList", playerListText);
        startActivity(intent);
    }

    public void runChatAdapter(ArrayList messages, ArrayList authors, ArrayList times) {
        ChatAdapter adapter = new ChatAdapter(this, messages, authors, times);
        ListView chatList = findViewById(R.id.chat_list);
        chatList.setAdapter(adapter);
    }

    public void noMessagesYet() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                noMessages.setVisibility(View.VISIBLE);
            }
        });
    }

    public void failedChatRequest() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                reloadChatBtn.setVisibility(View.VISIBLE);
                reloadChatBtn.setEnabled(true);
            }
        });
    }

    public void retryChatRequest(View v) {
        reloadChatBtn.setVisibility(View.INVISIBLE);
        reloadChatBtn.setEnabled(false);
        ChatRequest request = new ChatRequest("http://80.235.83.219:2005/static/server_chat.json");
        new Thread(request).start();
    }

    public class ChatRequest extends AppCompatActivity implements Runnable {

        String url;
        ArrayList messages = new ArrayList();
        ArrayList authors = new ArrayList();
        ArrayList times = new ArrayList();

        ChatRequest(String url) {
            this.url = url;
        }

        @Override
        public void run() {
            try {
                JSONObject json = new JSONObject(IOUtils.toString(new URL(url), Charset.forName("UTF-8")));
                JSONArray messagesArray = (JSONArray) json.get("messages");

                if (messagesArray.length() == 0) {
                    // Check if messages list is empty
                    noMessagesYet();
                    return;
                }

                for(int i = 0; i < messagesArray.length(); i++) {
                    JSONObject currentMessage = (JSONObject) messagesArray.get(i);
                    messages.add(currentMessage.get("message"));
                    authors.add(currentMessage.get("author"));
                    times.add(currentMessage.get("time"));
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        runChatAdapter(messages, authors, times);
                    }
                });
            } catch (JSONException e) {
                e.printStackTrace();
                Log.d("error", "JSON error");
                failedChatRequest();
            } catch (IOException e) {
                e.printStackTrace();
                Log.d("error", "IO error");
                failedChatRequest();
            } catch (Exception e) {
                e.printStackTrace();
                Log.d("error", "Failed to get data");
                failedChatRequest();
            }
        }
    }

    public class ChatAdapter extends ArrayAdapter<String> {

        Activity context;
        ArrayList messages;
        ArrayList authors;
        ArrayList times;

        public ChatAdapter(Activity context, ArrayList messages, ArrayList authors, ArrayList times) {
            super(context, R.layout.chat_list_item, messages);
            this.context = context;
            this.messages = messages;
            this.authors = authors;
            this.times = times;
        }

        public View getView(int position, View view, ViewGroup parent) {
            LayoutInflater inflater=context.getLayoutInflater();
            View rowView=inflater.inflate(R.layout.chat_list_item, null,true);

            TextView message = (TextView) rowView.findViewById(R.id.list_message);
            TextView author = (TextView) rowView.findViewById(R.id.list_author);
            TextView time = (TextView) rowView.findViewById(R.id.list_time);

            String getMessage = (String) messages.get(position);
            String getAuthor = (String) authors.get(position);
            String getTime = (String) times.get(position);

            try {
                message.setText(getMessage);
                author.setText(getAuthor);
                time.setText(getTime);
            } catch (Exception e) {
                e.printStackTrace();
                Log.d("error", "Failed to get message data from JSONObject");
            }
            return rowView;
        };
    }
}