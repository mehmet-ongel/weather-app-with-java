package com.example.weatherappupdate;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.squareup.picasso.Picasso;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private TextView city,temperature,weatherCondition,humidity,maxTemperature,minTemperature,pressure,wind,tvDetails;
    private ImageView imageView;
    private FloatingActionButton buttonSwitch;
    private LinearLayout linearLayout1, linearLayout2;
    private ConstraintLayout constraintLayout;

    AlertDialog dialog;
    AlertDialog.Builder builder;

    LocationManager locationManager;
    LocationListener locationListener;
    double lat,lon;

    private final static String p1 = Manifest.permission.ACCESS_FINE_LOCATION;
    private final static String p2 = Manifest.permission.ACCESS_COARSE_LOCATION;
    private final static int granted = PackageManager.PERMISSION_GRANTED;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        city = findViewById(R.id.textViewCity);
        temperature = findViewById(R.id.textViewTemp);
        weatherCondition = findViewById(R.id.textViewWeatherCondition);
        humidity = findViewById(R.id.textViewHumidity);
        maxTemperature = findViewById(R.id.textViewMaxTemp);
        minTemperature = findViewById(R.id.textViewMinTemp);
        pressure = findViewById(R.id.textViewPressure);
        wind = findViewById(R.id.textViewWind);
        imageView = findViewById(R.id.imageView);
        buttonSwitch = findViewById(R.id.fab);
        tvDetails = findViewById(R.id.textviewDetails);
        linearLayout1 = findViewById(R.id.linearLayout1);
        linearLayout2 = findViewById(R.id.linearLayout2);
        constraintLayout = findViewById(R.id.constraintLayout);

        builder = new AlertDialog.Builder(MainActivity.this);
        builder.setView(R.layout.progress_dialog);
        dialog = builder.create();
        dialog.show();

        buttonSwitch.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this,WeatherCityName.class);
            startActivity(intent);
        });

        locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(@NonNull Location location) {
                lat = location.getLatitude();
                lon = location.getLongitude();

                Log.e("lat", String.valueOf(lat));
                Log.e("lon", String.valueOf(lon));

                getWeatherData(lat,lon);
            }
        };

        if (ContextCompat.checkSelfPermission(this, p1) != granted || ContextCompat.checkSelfPermission(this, p2) != granted)
        {
            ActivityCompat.requestPermissions(this,new String[]{p1,p2},1);
        }
        else
        {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,500,0,locationListener);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 1 && grantResults.length > 0){

            if (ContextCompat.checkSelfPermission(this, p1) != granted && ContextCompat.checkSelfPermission(this, p2) != granted){

                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,500,0,locationListener);
            }
        }
    }

    public void getWeatherData(double lat, double lon)
    {
        WeatherAPI weatherAPI = RetrofitWeather.getClient().create(WeatherAPI.class);
        Call<OpenWeatherMap> call = weatherAPI.getWeatherWithLocation(lat,lon);

        call.enqueue(new Callback<OpenWeatherMap>() {
            @Override
            public void onResponse(Call<OpenWeatherMap> call, Response<OpenWeatherMap> response) {

                if (response.isSuccessful()){

                    city.setText(response.body().getName() +" , "+ response.body().getSys().getCountry());
                    temperature.setText(response.body().getMain().getTemp()+" °C");
                    weatherCondition.setText(response.body().getWeather().get(0).getDescription());
                    humidity.setText(" : "+response.body().getMain().getHumidity()+"%");
                    maxTemperature.setText(" : "+response.body().getMain().getTempMax()+" °C");
                    minTemperature.setText(" : "+response.body().getMain().getTempMin()+" °C");
                    pressure.setText(" : "+response.body().getMain().getPressure());
                    wind.setText(" : "+response.body().getWind().getSpeed());

                    String iconCode = response.body().getWeather().get(0).getIcon();
                    Picasso.get().load("https://openweathermap.org/img/wn/"+iconCode+"@2x.png")
                            .into(imageView);

                    makeVisible();

                    Log.e("city",response.body().getName());
                    Log.e("temp",response.body().getMain().getTemp()+" °C");
                    Log.e("icon",iconCode);
                }else {
                    Toast.makeText(getApplicationContext(),response.message(),Toast.LENGTH_LONG).show();
                }



            }

            @Override
            public void onFailure(Call<OpenWeatherMap> call, Throwable t) {

                    Log.d("weathererror: ", t.getLocalizedMessage());

            }
        });
    }

    public void makeVisible(){

        linearLayout1.setVisibility(View.VISIBLE);
        linearLayout2.setVisibility(View.VISIBLE);
        constraintLayout.setVisibility(View.VISIBLE);
        tvDetails.setVisibility(View.VISIBLE);
        city.setVisibility(View.VISIBLE);

        dialog.dismiss();
    }
}







