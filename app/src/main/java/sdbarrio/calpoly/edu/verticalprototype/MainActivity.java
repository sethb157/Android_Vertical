package sdbarrio.calpoly.edu.verticalprototype;

import android.*;
import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private GoogleApiClient mGoogleApiClient;
    private final String API_KEY = "AIzaSyDnXywKdvqABHJi-Hl3ohTcdRNCoBGaK8Y";
    private final int SEARCH_RADIUS = 1500;
    private final String PLACES_URL
            = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?";
//            "location=%s,%s" +
//            "&radius=%d" +
//            "&type=%s" +
//            "&key=%s";
    public final int LOC_REQ_CODE = 35;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        // Create an instance of GoogleAPIClient.
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
    }

    @Override
    protected void onStart() {
        mGoogleApiClient.connect();
        super.onStart();

    }

    @Override
    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION}, LOC_REQ_CODE);
        }
        Location mlastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        String latitude, longitude;
        if (mlastLocation != null) {
            latitude = String.valueOf(mlastLocation.getLatitude());
            longitude = String.valueOf(mlastLocation.getLongitude());
            Log.d("MAIN_ACTIVITY", latitude + ", " + longitude);
        }
    }

    public void getLocationsForWeather(View view) {
        String type = "library";
        switch (view.getId()) {
            case R.id.rain_button:
                //type = "movie_theater";
                type = "cafe";
                break;
            case R.id.sunny_button:
                type = "park";
                break;
            default:
                break;
        }


        // Build http request
        Location mlastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        OkHttpClient okClient = new OkHttpClient();
        HttpUrl.Builder urlBuilder = HttpUrl.parse(PLACES_URL).newBuilder();
        urlBuilder.addQueryParameter("location", mlastLocation.getLatitude() + "," + mlastLocation.getLongitude());
        urlBuilder.addQueryParameter("radius", Integer.toString(SEARCH_RADIUS));
        urlBuilder.addQueryParameter("type", type);
        urlBuilder.addQueryParameter("key", API_KEY);
        String url = urlBuilder.build().toString();

        final TextView tv = (TextView) MainActivity.this.findViewById(R.id.places_text_view);
        tv.setText("Getting " + type + "s in your area");

        Request request = new Request.Builder().url(url).build();

        // Make request and handle response
        okClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d("MAIN_ACTIVITY", "FAILED");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                final String responseText = response.body().string();
                MainActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tv.setText(responseText, TextView.BufferType.EDITABLE);
                    }
                });
            }
        });
    }


    public void getWeather(View view) {
        String city = "sanluisobispo";
        String query = "select%20*%20from%20weather.forecast%20where%20woeid%20in%20(select%20woeid%20from%20geo.places(1)%20where%20text%3D%22"+ city +"%22)&format=json";

        OkHttpClient okClient = new OkHttpClient();
        Request request = new Request.Builder().url("https://query.yahooapis.com/v1/public/yql?q=" + query).build();

        final TextView weatherText = (TextView) findViewById(R.id.weather_text);

        // Make request and handle response
        okClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d("MAIN_ACTIVITY", "FAILED");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                final String responseText = response.body().string();
                MainActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        weatherText.setText(responseText, TextView.BufferType.EDITABLE);
                    }
                });
            }
        });
    }


    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d("MAIN_ACTIVITY", "connection failed");
    }
}
