package com.example.tukserveriapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.util.Property;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.util.concurrent.ExecutionException;

public class Status extends AppCompatActivity {

    ProgressBar progressBar;
    TextView status;
    TextView statusVar;
    TextView playerCount;
    TextView playerList;
    Button refresh;
    TextView refreshError;
    Button networkTryAgain;
    Button serverTurnOn;

    String statusText;
    String playerCountText;
    String playerListText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        networkTryAgain = findViewById(R.id.tryAgain);
        checkNetwork(networkTryAgain);
    }

    public void checkNetwork(View v) {
        NetworkConnectionCheck check = new NetworkConnectionCheck();
        if (check.checkConnection(this) == false) {
            setContentView(R.layout.no_internet);
        } else {
            setContentView(R.layout.activity_main);

            progressBar = findViewById(R.id.progressBar);
            status = findViewById(R.id.status);
            statusVar = findViewById(R.id.statusVar);
            playerCount = findViewById(R.id.playerCount);
            playerList = findViewById(R.id.playerList);
            refresh = findViewById(R.id.refresh);
            refreshError = findViewById(R.id.refreshError);
            serverTurnOn = findViewById(R.id.serverTurnOn);

            progressBar.setVisibility(View.INVISIBLE);
            refreshError.setVisibility(View.INVISIBLE);

            // Check if extras are from Widget
            try {
                Bundle extras = getIntent().getExtras();
                if (extras.getString("fromWidget").equalsIgnoreCase("true")) {
                    runTurnServerOn(serverTurnOn);
                }
            } catch (Exception e) {}

            // Get data that is returned from Chat activity
            try {
                Bundle extras = getIntent().getExtras();
                if (extras != null) {
                    String statusText = extras.getString("status");
                    String playerCountText = extras.getString("playerCount");
                    String playerListText = extras.getString("playerList");
                    Log.d("info", "Received extras");

                    if (statusText.equalsIgnoreCase("offline")) {
                        statusVar.setText(statusText.toUpperCase());
                        statusVar.setTextColor(Color.RED);

                        playerCount.setText("Mängijate arv: 0");
                        playerList.setVisibility(View.INVISIBLE);
                        serverTurnOn.setVisibility(View.VISIBLE);
                    }
                    if (statusText.equalsIgnoreCase("online")) {
                        statusVar.setText(statusText.toUpperCase());
                        statusVar.setTextColor(Color.GREEN);
                        playerCount.setText(playerCountText);
                        serverTurnOn.setVisibility(View.INVISIBLE);

                        if (playerCountText.equalsIgnoreCase("Mängijate arv: 0")) {
                            playerList.setVisibility(View.INVISIBLE);
                        } else {
                            playerList.setText(playerListText);
                            playerList.setVisibility(View.VISIBLE);
                        }
                    }
                } else {
                    // Request server data when no extras came from other activities (meaning the Status was opened for the first time)
                    serverData(refresh);
                    // Set server turn on button to invisible when no status data about the server is given
                    serverTurnOn.setVisibility(View.INVISIBLE);
                }
            } catch (Exception e) {
                // Means that no extras were sent (activity was not opened from Status activity)
            }
        }
    }

    public void openChat(View v) {

        statusText = (String) statusVar.getText();
        playerCountText = (String) playerCount.getText();
        playerListText = (String) playerList.getText();

        Intent intent = new Intent(this, Chat.class);
        intent.putExtra("status", statusText);
        intent.putExtra("playerCount", playerCountText);
        intent.putExtra("playerList", playerListText);
        startActivity(intent);
    }
    public void openPlan(View v) {
        statusText = (String) statusVar.getText();
        playerCountText = (String) playerCount.getText();
        playerListText = (String) playerList.getText();

        Intent intent = new Intent(this, Plan.class);
        intent.putExtra("status", statusText);
        intent.putExtra("playerCount", playerCountText);
        intent.putExtra("playerList", playerListText);
        startActivity(intent);
    }

    public void serverData(View v) {
        status.setVisibility(View.INVISIBLE);
        statusVar.setVisibility(View.INVISIBLE);
        playerCount.setVisibility(View.INVISIBLE);
        playerList.setVisibility(View.INVISIBLE);
        progressBar.setVisibility(View.VISIBLE);
        refresh.setEnabled(false);
        refreshError.setVisibility(View.INVISIBLE);

        // Check for network
        NetworkConnectionCheck check = new NetworkConnectionCheck();
        if (check.checkConnection(this) == false) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    setContentView(R.layout.no_internet);
                    return;
                }
            });
        }
        StatsRequest request = new StatsRequest("http://80.235.83.219:2005/static/server_status.json", status, statusVar, playerCount, playerList, progressBar, refresh, refreshError, serverTurnOn);
        new Thread(request).start();
    }

    public void runTurnServerOn(View v) {
        TurnServerOn on = new TurnServerOn();
        new Thread(on).start();
    }

    public class TurnServerOn implements Runnable {
        @Override
        public void run() {
            try {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        serverTurnOn.setEnabled(false); // For some reason they need to be run on UI thread
                    }
                });

                URL url = new URL("http://80.235.83.219:2005/run");
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.connect();
                Log.d("error", String.valueOf(urlConnection.getErrorStream())); // URL request won't work without this line

                Thread.sleep(1000);
                serverTurnOn.setVisibility(View.INVISIBLE);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        serverTurnOn.setEnabled(true);
                        serverData(serverTurnOn);
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}