package com.example.climawiz;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;

//import android.support.v7.app.AppCompatActivity;


public class WeatherController extends AppCompatActivity {

    // Constants:
    final int REQUEST_CODE = 123;
    final String WEATHER_URL = "http://api.openweathermap.org/data/2.5/weather";
    // App ID to use OpenWeather data
    final String APP_ID = "01f4af06ab8584c4f0fe83c3d0073de7";
    // Time between location updates (5000 milliseconds or 5 seconds)
    final long MIN_TIME = 5000;
    // Distance between location updates (1000m or 1km)
    final float MIN_DISTANCE = 1000;

    // TODO: Set LOCATION_PROVIDER here:

    final String LOCATION_PROVIDER = LocationManager.GPS_PROVIDER;


    // Member Variables:
    TextView mCityLabel;
    ImageView mWeatherImage;
    TextView mTemperatureLabel;

    // TODO: Declare a LocationManager and a LocationListener here:

    LocationManager mLocationManager;
    LocationListener mLocationListener;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.weather_controller_layout);

        // Linking the elements in the layout to Java code
        mCityLabel = (TextView) findViewById(R.id.locationTV);
        mWeatherImage = (ImageView) findViewById(R.id.weatherSymbolIV);
        mTemperatureLabel = (TextView) findViewById(R.id.tempTV);
        ImageButton changeCityButton = (ImageButton) findViewById(R.id.changeCityButton);


        // TODO: Add an OnClickListener to the changeCityButton here:

        changeCityButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent myIntent = new Intent(WeatherController.this, ChangeCityController.class);

                // Using startActivityForResult since we just get back the city name.
                // Providing an arbitrary request code to check against later.
                //startActivityForResult(myIntent, NEW_CITY_CODE);
                startActivity(myIntent);

            }
        });



    }


    // TODO: Add onResume() here:
    @Override
    protected void onResume() {
        super.onResume();
        Log.d("Clima Wiz", "onResume() called");

        Intent myIntent = getIntent();
        String city = myIntent.getStringExtra("City");

        if (city != null) {

            getWeatherForNewCity(city);
        } else {
            Log.d("Clima Wiz", "Getting weather for current location");
            getWeatherForCurrentLocation();
        }



    }


    // TODO: Add getWeatherForNewCity(String city) here:

    private void getWeatherForNewCity (String city) {

        Log.d("Clima Wiz", "Getting weather for new city");
        RequestParams params = new RequestParams();
        params.put("q", city);
        params.put("appid", APP_ID);

        letsDoSomeNetworking(params);

    }


    // TODO: Add getWeatherForCurrentLocation() here:

    private void getWeatherForCurrentLocation() {


        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        mLocationListener = new LocationListener() {
            @Override
            public void onLocationChanged(@NonNull Location location) {

                Log.d("Clima Wiz", "OnLocationChanged() callback received");
                String longitude = String.valueOf(location.getLongitude());
                String latitude = String.valueOf(location.getLatitude());

                Log.d("Clima Wiz", "Longitude is: "+ longitude);
                Log.d("Clima Wiz", "Latitude is: "+ latitude);


                RequestParams params = new RequestParams();
                params.put("lat", latitude);
                params.put("lon", longitude);
                params.put("appid", APP_ID);
                letsDoSomeNetworking(params);



            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(@NonNull String provider) {

            }

            @Override
            public void onProviderDisabled(@NonNull String provider) {

                Log.d("Clima Wiz", "OnProviderDisabled() callback received");


            }
        };

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.

            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE);
            
            return;
        }
        mLocationManager.requestLocationUpdates(LOCATION_PROVIDER, MIN_TIME, MIN_DISTANCE, mLocationListener);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_CODE){

            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){

                Log.d("Clima Wiz", "onRequestPermissionsResult(): Permission granted!");

                getWeatherForCurrentLocation();

            } else {
                Log.d("Clima Wiz", "Permission denied =( ");
            }
        }
    }


    private void letsDoSomeNetworking(RequestParams params) {

        AsyncHttpClient client = new AsyncHttpClient();

        client.get(WEATHER_URL, params, new JsonHttpResponseHandler() {

            @Override

            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {

                Log.d("Clima Wiz", "Success! JSON: " + response.toString());
                WeatherDataModel weatherData = WeatherDataModel.fromJson(response);
                updateUI(weatherData);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable e, JSONObject response) {

                Log.e("Clima Wiz", "Fail " + e.toString());
                Toast.makeText(WeatherController.this, "Request Failed", Toast.LENGTH_SHORT).show();

                Log.d("Clima Wiz", "Status code " + statusCode);
               Log.d("Clima Wiz", "Here's what we got instead " + response.toString());
            }

        });


    }





    private void updateUI(WeatherDataModel weather) {
        mTemperatureLabel.setText(weather.getTemperature());
        mCityLabel.setText(weather.getCity());

        // Updating the icon based on the resource id of the image in the drawable folder.
        int resourceID = getResources().getIdentifier(weather.getIconName(), "drawable", getPackageName());
        mWeatherImage.setImageResource(resourceID);

    }





    @Override
    protected void onPause() {
        super.onPause();

        if (mLocationManager != null) mLocationManager.removeUpdates(mLocationListener);
    }





}
