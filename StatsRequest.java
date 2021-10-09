package com.example.tukserveriapp;

import android.graphics.Color;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.Charset;

public class StatsRequest extends AppCompatActivity implements Runnable {

    String url;
    TextView status;
    TextView statusVar;
    TextView playerCount;
    TextView playerList;
    ProgressBar progressBar;
    Button refresh;
    TextView refreshError;
    Button serverTurnOn;

    StatsRequest(String url, TextView status, TextView statusVar, TextView playerCount, TextView playerList, ProgressBar progressBar, Button refresh, TextView refreshError, Button serverTurnOn) {
        this.url = url;
        this.status = status;
        this.statusVar = statusVar;
        this.progressBar = progressBar;
        this.refresh = refresh;
        this.refreshError = refreshError;
        this.playerCount = playerCount;
        this.playerList = playerList;
        this.serverTurnOn = serverTurnOn;
    }
    @Override
    public void run() {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        try {
            JSONObject json = new JSONObject(IOUtils.toString(new URL(url), Charset.forName("UTF-8")));
            // Take fields from json
            String statusFromJson = json.getString("mc_server_status");
            String playerCountJson;
            String playerListJson;
            try {
                playerCountJson = json.getString("players_online");
            } catch (JSONException e){
                playerCountJson = "0";
            }
            try {
                playerListJson = json.getString("player_list");
            } catch (JSONException e) {
                playerListJson = "";
            }


            String finalPlayerCountJson = playerCountJson;
            String finalPlayerListJson = playerListJson;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    statusVar.setText(statusFromJson.toUpperCase());
                    playerCount.setText("Mängijate arv: " + finalPlayerCountJson);
                    if (finalPlayerListJson.length() != 0) {
                        playerList.setText("Mängijad: " + finalPlayerListJson);
                        playerList.setVisibility(View.VISIBLE);
                    } else {
                        playerList.setVisibility(View.INVISIBLE);
                    }

                    if (statusFromJson.equals("offline")) {
                        statusVar.setTextColor(Color.RED);
                        serverTurnOn.setVisibility(View.VISIBLE);
                    } else {
                        Log.d("info", statusFromJson);
                        statusVar.setTextColor(Color.GREEN);
                        serverTurnOn.setVisibility(View.INVISIBLE);
                    }
                    status.setVisibility(View.VISIBLE);
                    statusVar.setVisibility(View.VISIBLE);
                    playerCount.setVisibility(View.VISIBLE);
                    progressBar.setVisibility(View.INVISIBLE);
                    refresh.setEnabled(true);
                    Log.d("info", "Refreshed server status");
                    // End thread
                    Thread.currentThread().interrupt();
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
            Log.d("error", "JSON error");
        } catch (IOException e) {
            e.printStackTrace();
            Log.d("error", "IO error");
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    refreshError.setVisibility(View.VISIBLE);
                    refreshError.setTextColor(Color.RED);
                    progressBar.setVisibility(View.INVISIBLE);
                    refresh.setEnabled(true);
                    serverTurnOn.setVisibility(View.INVISIBLE);
                    // End thread
                    Thread.currentThread().interrupt();
                }
            });
        } catch (Exception e) {
            Log.d("error", "Requested webserver not online");
        }
    }
}
