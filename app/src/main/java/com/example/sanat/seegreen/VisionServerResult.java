package com.example.sanat.seegreen;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.example.sanat.seegreen.util.NetworkUtils;

import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class VisionServerResult extends AppCompatActivity {




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vision_server_result);

        Intent intent = getIntent();
        String server_string = intent.getExtras().getString("vision_result");

        Log.v("Server string", server_string);

        APIInterface apiService = APIClient.getClient().create(APIInterface.class);

        Call<AnalyzeResponse> call = apiService.getAnalytics(server_string);
        call.enqueue(new Callback<AnalyzeResponse>() {
            @Override
            public void onResponse(Call<AnalyzeResponse> call, Response<AnalyzeResponse> response) {
                Log.v("Results", "Name = " + response.body().getName());
                Log.v("Results", "Value = " + response.body().getValue());
            }

            @Override
            public void onFailure(Call<AnalyzeResponse> call, Throwable t) {
                Log.e("Results", t.toString());
            }
        });

/*        try {

            Log.v("JSON", "Created Json string?");
            Log.v("URI", uri.toString());

            URL url = new URL(uri.toString());
            URLConnection urlcon = url.openConnection();
            HttpURLConnection urlConnection = (HttpURLConnection) urlcon;
            try {
                Log.v("Before result", String.valueOf(urlConnection.getResponseCode()));
                InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                Log.v("Result", in.toString());
                //readStream(in);
            } catch (Exception e){
                Log.e("Error", e.getMessage());
            }
            finally {
                urlConnection.disconnect();
            }


        } catch (Exception e) {
            Log.e("My App", "Could not parse malformed JSON: \"" + server_string + "\"");
        }*/

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_vision_server_result, menu);
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
}
