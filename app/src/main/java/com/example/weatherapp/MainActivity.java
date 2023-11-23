package com.example.weatherapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.JsonIOException;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
//     847890ee01ce6cbaa7ef7fc4e52f36ec
//    https://api.openweathermap.org/data/2.5/weather?q={city name}&appid={API key}

    private RelativeLayout homeRL;
    private ProgressBar loadingPB;
    private TextView cityNameTV, temperatureTV, conditionTV;
    private TextInputEditText cityEdit;
    private ImageView backIV, iconIV, searchIV;
    private RecyclerView weatherRv;
    private ArrayList<WeatherRVModel> weatherRVModelArrayList;
    private WeatherAdapter weatherAdapter;
    private LocationManager locationManager;
    private int PERMISSION_CODE = 1;
    private String cityName;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);


        setContentView(R.layout.activity_main);
        homeRL = findViewById(R.id.rlHome);
        loadingPB = findViewById(R.id.loadingBar);
        cityEdit = findViewById(R.id.EdCity);
        cityNameTV = findViewById(R.id.txt_cityName);
        temperatureTV = findViewById(R.id.TVTemperature);
        conditionTV = findViewById(R.id.TVCondition);
        weatherRv = findViewById(R.id.RVWeather);
        backIV = findViewById(R.id.image_back);
        iconIV = findViewById(R.id.IVIcon);
        searchIV = findViewById(R.id.IVSearch);
        weatherRVModelArrayList = new ArrayList<>();
        weatherAdapter = new WeatherAdapter(this, weatherRVModelArrayList);
        weatherRv.setAdapter(weatherAdapter);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_CODE);
        }


        Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        cityName=getCityName(location.getLongitude(),location.getLatitude());
        getWeatherInfo(cityName);

        searchIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String city=cityEdit.getText().toString();
                if(city.isEmpty()){
                    Toast.makeText(MainActivity.this, "Please Enter The City Name", Toast.LENGTH_SHORT).show();
                }else {
                    cityNameTV.setText(cityName);
                    getWeatherInfo(city);
                }
            }
        });



    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode==PERMISSION_CODE){
            if(grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                Toast.makeText(this, "Permission Granted..", Toast.LENGTH_SHORT).show();
            }
            else {
                Toast.makeText(this, "Please Provide Permission ", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    private String getCityName(double longitude, double latitude) {
        String cityName = "Not Found";
        Geocoder gcd = new Geocoder(getBaseContext(), Locale.getDefault());
        try {
            List<Address> addresses = gcd.getFromLocation(latitude, longitude, 10);
            for (Address adr : addresses) {
                if (adr != null) {
                    String city = adr.getLocality();
                    if (city != null && !city.equals("")) {
                        cityName = city;
                        break;
                    } else {
                        Log.d("TAG", "CITY NOT FOUND");
                        Toast.makeText(this, "User city not found", Toast.LENGTH_SHORT).show();
                    }
                }
            }

        } catch (IOException e) {

        }
        return cityName;
    }

    private void getWeatherInfo(String cityName){
        String url="http://api.weatherapi.com/v1/current.json?key=a262248d28d8424f825175549232211&q=" + cityName + "&aqi=no";
        cityNameTV.setText(cityName);
        RequestQueue requestQueue=Volley.newRequestQueue(MainActivity.this);
        JsonObjectRequest jsonObjectRequest=new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                loadingPB.setVisibility(View.GONE);
                homeRL.setVisibility(View.VISIBLE);
                weatherRVModelArrayList.clear();

                try{
                    String temperature=response.getJSONObject("current").getString("temp_c");
                    temperatureTV.setText(temperature+"°C");
                    int isDay=response.getJSONObject("current").getInt("is_day");
                    String condition=response.getJSONObject("current").getJSONObject("condition").getString("text");
                    String conditionIcon=response.getJSONObject("current").getJSONObject("condition").getString("icon");
                    Picasso.get().load("http:".concat(conditionIcon)).into(iconIV);
                    conditionTV.setText(condition);
                    if(isDay==1){
                        Picasso.get().load("https://img.lovepik.com/background/20211101/medium/lovepik-morning-beauty-mobile-phone-wallpaper-background-image_400492696.jpg").into(backIV);
                    }else{
                        Picasso.get().load("https://e0.pxfuel.com/wallpapers/279/970/desktop-wallpaper-anime-night-city-anime-night-street.jpg").into(backIV);
                    }

                    JSONObject forecastObj=response.getJSONObject("forecast");
                    JSONObject forcast0= forecastObj.getJSONObject("forecastday");
                    JSONArray hourArray=forcast0.getJSONArray("hour");
                    for (int i=0; i<hourArray.length();i++){
                        JSONObject hourObj=hourArray.getJSONObject(i);
                        String time=hourObj.getString("time");
                        String temper=hourObj.getString("temp_c");
                        String img = hourObj.getJSONObject("condition").getString("icon");
                        String wind = hourObj.getString("wind_kph");
                        weatherRVModelArrayList.add(new WeatherRVModel(time,temper,img,wind));
                    }
                    weatherAdapter.notifyDataSetChanged();


                }catch (JSONException e){

                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("Final Error","Response Error:"+error);
                Toast.makeText(MainActivity.this, "Please Enter Valid City Name", Toast.LENGTH_SHORT).show();
            }
        });
        requestQueue.add(jsonObjectRequest);
    }
}