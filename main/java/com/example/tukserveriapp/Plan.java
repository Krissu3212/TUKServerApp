package com.example.tukserveriapp;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;

public class Plan extends AppCompatActivity {

    TextView dayE;
    TextView dayT;
    TextView dayK;
    TextView dayN;
    TextView dayR;
    TextView dayL;
    TextView dayP;
    TextView lastUpdate;
    Button retryPlanBtn;
    TextView spacer;

    String statusText;
    String playerCountText;
    String playerListText;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_plan);

        // App notification functions for future updates
        //createNotificationChannel();
        //createIntentAndBuilder();

        dayE = findViewById(R.id.dayE);
        dayT = findViewById(R.id.dayT);
        dayK = findViewById(R.id.dayK);
        dayN = findViewById(R.id.dayN);
        dayR = findViewById(R.id.dayR);
        dayL = findViewById(R.id.dayL);
        dayP = findViewById(R.id.dayP);
        lastUpdate = findViewById(R.id.lastUpdate);
        retryPlanBtn = findViewById(R.id.retryPlanBtn);
        spacer = findViewById(R.id.spacer);
        retryPlanBtn.setVisibility(View.INVISIBLE);
        retryPlanBtn.setEnabled(false);

        try {
            Bundle extras = getIntent().getExtras();
            statusText = extras.getString("status");
            playerCountText = extras.getString("playerCount");
            playerListText = extras.getString("playerList");
        } catch (Exception e) {
            // Means that no extras were sent (activity was not opened from Status activity)
        }

        PlanRequest request = new PlanRequest("http://80.235.83.219:2005/static/training_plan.json");
        new Thread(request).start();

    }

    public void failedPlanRequest() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                retryPlanBtn.setVisibility(View.VISIBLE);
                spacer.setVisibility(View.INVISIBLE);
                retryPlanBtn.setEnabled(true);
            }
        });
    }

    public void retryPlanRequest(View v) {
        retryPlanBtn.setVisibility(View.INVISIBLE);
        spacer.setVisibility(View.VISIBLE);
        retryPlanBtn.setEnabled(false);
        PlanRequest request = new PlanRequest("http://80.235.83.219:2005/static/training_plan.json");
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

    public class PlanRequest extends AppCompatActivity implements Runnable {

        String url;

        PlanRequest(String url) {
            this.url = url;
        }

        @Override
        public void run() {
            try {
                JSONObject json = new JSONObject(IOUtils.toString(new URL(url), Charset.forName("UTF-8")));
                JSONObject lastUpdateObj = (JSONObject) json.get("last_update");
                String lastUpdateStr = lastUpdateObj.getString("text");

                JSONArray planArray = (JSONArray) json.get("plan");
                Log.d("info", "here1");
                JSONObject dayEObj = (JSONObject) planArray.get(0);
                JSONObject dayTObj = (JSONObject) planArray.get(1);
                JSONObject dayKObj = (JSONObject) planArray.get(2);
                JSONObject dayNObj = (JSONObject) planArray.get(3);
                JSONObject dayRObj = (JSONObject) planArray.get(4);
                JSONObject dayLObj = (JSONObject) planArray.get(5);
                JSONObject dayPObj = (JSONObject) planArray.get(6);
                Log.d("info", "here2");

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Log.d("info", "here3");
                            dayE.setText(dayEObj.getString("text"));
                            dayT.setText(dayTObj.getString("text"));
                            dayK.setText(dayKObj.getString("text"));
                            dayN.setText(dayNObj.getString("text"));
                            dayR.setText(dayRObj.getString("text"));
                            dayL.setText(dayLObj.getString("text"));
                            dayP.setText(dayPObj.getString("text"));
                            lastUpdate.setText("Kava veebiserveris viimati värskendatud: " + lastUpdateStr);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
            } catch (JSONException e) {
                e.printStackTrace();
                Log.d("error", "JSON error");
                failedPlanRequest();
            } catch (IOException e) {
                e.printStackTrace();
                Log.d("error", "IO error");
                failedPlanRequest();
            } catch (Exception e) {
                e.printStackTrace();
                Log.d("error", "Failed to get data");
                failedPlanRequest();
            }
        }
    }

    public void createIntentAndBuilder() {
        Intent intent = new Intent(this, Chat.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "200")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("My notification")
                .setContentText("Hello World!")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);

        notificationManager.notify(0, builder.build());
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("200", name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
}
