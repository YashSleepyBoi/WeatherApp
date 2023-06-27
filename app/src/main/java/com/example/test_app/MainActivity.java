package com.example.test_app;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import org.json.JSONArray;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.squareup.picasso.Picasso;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private static final String API_KEY = "a648e1f72d6c5c0ca77d6f99097db093";
    private static final String API_URL = "http://api.openweathermap.org/data/2.5/weather";

    private LocationManager locationManager;
    private LocationListener locationListener;
    TextView Location;
    TextView Temperature;
    TextView Humidity;
    TextView Windspeed;
    TextView Sunset;
    TextView Sunrise;
    ConstraintLayout main;
    TextView Time;
    TextView Description;
    ImageView WeatherImg;
    private Boolean searchString;
    private ArrayList<WeatherData> weatherList;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Initialize LocationManager and LocationListener
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        //Initializing front end components
        main=findViewById(R.id.main);
        Location = findViewById(R.id.location);
        Temperature = findViewById(R.id.temp_c);
        Humidity = findViewById(R.id.humidityval);
        Windspeed = findViewById(R.id.windspeedval);
        Sunset = findViewById(R.id.sunsetval);
        Sunrise = findViewById(R.id.sunriseval);
        Time = findViewById(R.id.timeval);
        Description = findViewById(R.id.desc);
        WeatherImg=findViewById(R.id.visualrep);
        //getting first data
        reload();



    }

    public void reload() {
        if (!isInternetConnected()) {
            Toast.makeText(MainActivity.this, "No internet connection", Toast.LENGTH_SHORT).show();
            return;
        }
        locationListener = new LocationListener() {

            @Override
            public void onLocationChanged(@NonNull Location location) {
                // Get the latitude and longitude
                double latitude = location.getLatitude();
                double longitude = location.getLongitude();

                // Call the method to retrieve the weather data using the obtained latitude and longitude
                getWeatherData(latitude, longitude);
            }
        };

        weatherList = new ArrayList<>();

        // Check if the app has the necessary permissions
        // Start listening for location updates
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Request the permission if not granted
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        } else {
            startLocationUpdates();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // Check if the permission was granted
        if (requestCode == 1 && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            // Start listening for location updates
            startLocationUpdates();
        }
    }
    private boolean isInternetConnected() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }


    private void getWeatherData(final double latitude, final double longitude) {

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    double latitude=12.972442;
                    double longitude=77.580643;

                    // Construct the API request URL
                    String apiUrl = API_URL + "?lat=" + latitude + "&lon=" + longitude + "&appid=" + API_KEY;

                    // Send HTTP GET request
                    URL url = new URL(apiUrl);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");
                    System.out.println(connection);
                    // Check the response code

                    int responseCode = connection.getResponseCode();
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        // Read the API response
                        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                        StringBuilder response = new StringBuilder();
                        String line;
                        while ((line = reader.readLine()) != null) {
                            response.append(line);
                        }
                        reader.close();

                        // Parse the JSON response
                        JSONObject jsonObject = new JSONObject(response.toString());

                        // Extract the weather information
                        String location = jsonObject.getString("name");
                        JSONArray weatherArray = jsonObject.getJSONArray("weather");
                        String description = weatherArray.getJSONObject(0).getString("main");
                        String weathertype = weatherArray.getJSONObject(0).getString("description");
                        String weathericon = weatherArray.getJSONObject(0).getString("icon");
                        double temperatureKelvin = jsonObject.getJSONObject("main").getDouble("temp");
                        int humidity = jsonObject.getJSONObject("main").getInt("humidity");
                        double windSpeed = jsonObject.getJSONObject("wind").getDouble("speed");
                        long sunriseTimestamp = jsonObject.getJSONObject("sys").getLong("sunrise");
                        long sunsetTimestamp = jsonObject.getJSONObject("sys").getLong("sunset");

                        // Convert temperature from Kelvin to Celsius
                        double temperatureCelsius = temperatureKelvin - 273.15;

                        // Convert sunrise and sunset timestamps to formatted time
                        SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault());
                        String sunriseTime = timeFormat.format(new Date(sunriseTimestamp * 1000));
                        String sunsetTime = timeFormat.format(new Date(sunsetTimestamp * 1000));

                        // Get the current time
                        Date currentTime = new Date();
                        String time = timeFormat.format(currentTime);

                        // Create a WeatherData object and add it to the ArrayList
                        WeatherData weatherData = new WeatherData(location, weathertype, description,weathericon, temperatureCelsius, humidity,
                                windSpeed, sunriseTime, sunsetTime, time);
                        weatherList.add(weatherData);

                        // Store the WeatherData object in SharedPreferences
                        saveWeatherData(weatherData);
                        Log.d("WeatherApp", "Location: " + weatherData.getLocation());
                        Log.d("WeatherApp", "Weather Type: " + weatherData.getWeathertype());
                        Log.d("WeatherApp", "Description: " + weatherData.getDescription());
                        Log.d("WeatherApp", "Temperature: " + weatherData.getTemperature() + " °C");
                        Log.d("WeatherApp", "Humidity: " + weatherData.getHumidity() + "%");
                        Log.d("WeatherApp", "Wind Speed: " + weatherData.getWindSpeed() + " m/s");
                        Log.d("WeatherApp", "Sunrise Time: " + weatherData.getSunriseTime());
                        Log.d("WeatherApp", "Sunset Time: " + weatherData.getSunsetTime());
                        Log.d("WeatherApp", "Time: " + weatherData.getTime());

                    } else {
                        Log.e("WeatherApp", "Error: Response Code - " + responseCode);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(MainActivity.this, "Unable to get request", Toast.LENGTH_SHORT).show();
                            }
                        });

                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }catch (Error e){
                    e.printStackTrace();
                }catch (Throwable e){
                    e.printStackTrace();

                }
            }
        });

        // Start the network request thread
        thread.start();
        try {
            // Wait for the thread to finish
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Check if weather data is available in SharedPreferences
        if (weatherList.isEmpty()) {
            // No weather data found in SharedPreferences, fetch new data
            getWeatherDataFromApi(latitude, longitude);
        } else {
            // Weather data found in SharedPreferences, display it
            WeatherData weatherData = weatherList.get(0);
            try {
                displayWeatherData(weatherData);
            } catch (MalformedURLException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void getWeatherDataFromApi(double latitude, double longitude) {
        // Check if the app has the necessary permissions
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Request the permission if not granted
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        } else {
            // Start listening for location updates
            startLocationUpdates();
        }
    }

    private void startLocationUpdates() {
        try {
            // Request location updates
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                    0, 0, locationListener);
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    private void saveWeatherData(WeatherData weatherData) {
        SharedPreferences sharedPreferences = getSharedPreferences("WeatherData", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("Location", weatherData.getLocation());
        editor.putString("Description", weatherData.getDescription());
        editor.putString("WeatherType", weatherData.getWeathertype());
        editor.putString("WeatherIcon",weatherData.getIcon());
        editor.putFloat("Temperature", (float) weatherData.getTemperature());
        editor.putInt("Humidity", weatherData.getHumidity());
        editor.putFloat("WindSpeed", (float) weatherData.getWindSpeed());
        editor.putString("SunriseTime", weatherData.getSunriseTime());
        editor.putString("SunsetTime", weatherData.getSunsetTime());
        editor.putString("Time", weatherData.getTime());
        editor.apply();
    }

    private void loadWeatherData() {
        SharedPreferences sharedPreferences = getSharedPreferences("WeatherData", MODE_PRIVATE);
        String location = sharedPreferences.getString("Location", "");
        String description = sharedPreferences.getString("Description", "");
        String weathertype = sharedPreferences.getString("WeatherType", "");
        String weathericon=sharedPreferences.getString("WeatherIcon","");
        float temperature = sharedPreferences.getFloat("Temperature", 0);
        int humidity = sharedPreferences.getInt("Humidity", 0);
        float windSpeed = sharedPreferences.getFloat("WindSpeed", 0);
        String sunriseTime = sharedPreferences.getString("SunriseTime", "");
        String sunsetTime = sharedPreferences.getString("SunsetTime", "");
        String time = sharedPreferences.getString("Time", "");

        WeatherData weatherData = new WeatherData(location, weathertype, description,weathericon, temperature, humidity,
                windSpeed, sunriseTime, sunsetTime, time);
        try {
            displayWeatherData(weatherData);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressLint("ResourceAsColor")
    private void displayWeatherData(WeatherData weatherData) throws MalformedURLException {
        //Setting the Location
        Location.setText(weatherData.getLocation());

        //Setting the temperature
        int temperatureCelsius = (int) weatherData.getTemperature();
        Temperature.setText(Integer.toString(temperatureCelsius));

        //setting the humididy
        Humidity.setText(Integer.toString(weatherData.getHumidity()) + "%");

        //Setting the windspeed
        int windSpeed = (int) weatherData.getWindSpeed();
        Windspeed.setText(" " + Integer.toString(windSpeed) + " m/s");

        //setting the sunrise
        Sunrise.setText(weatherData.getSunriseTime());
        Sunset.setText(weatherData.getSunsetTime());
        Time.setText(weatherData.getTime());

        //Setting the description
        Description.setText(weatherData.getDescription());

        //Set the weather image
        String icon=weatherData.getIcon();
        String imgurl="http://openweathermap.org/img/w/"+icon+".png";


        Picasso.get().load(imgurl).into(WeatherImg);

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault());
                try {
                    Date currentTime = timeFormat.parse(weatherData.getTime());
                    Date sunsetTime = timeFormat.parse(weatherData.getSunsetTime());
                    if (currentTime != null && sunsetTime != null) {
                        if (currentTime.after(sunsetTime)) {
                            // Current time is past sunset time
                            // Change the background color to black
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    main.setBackgroundColor(R.color.background2);
                                    getWindow().getDecorView().setBackgroundColor(R.color.background2);
                                }
                            });
                        } else {
                            // Current time is before sunset time
                            // Change the background color to a different color if desired
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {

                                    getWindow().getDecorView().setBackgroundColor(R.color.background);

                                }
                            });
                        }
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
        try {
            // Wait for the thread to finish
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }




    @Override
    protected void onResume() {
        super.onResume();
        // Load weather data from SharedPreferences
        loadWeatherData();
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Clear weather data in SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("WeatherData", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
    }
    private class WeatherData {
        private String location;
        private String icon;
        private String description;
        private double temperature;
        private int humidity;
        private double windSpeed;
        private String sunriseTime;
        private String sunsetTime;
        private String time;

        private String weathertype;

        public WeatherData(String location, String weathertype, String description,String icon, double temperature, int humidity,
                           double windSpeed, String sunriseTime, String sunsetTime, String time) {
            this.location = location;
            this.description = description;
            this.temperature = temperature;
            this.humidity = humidity;
            this.windSpeed = windSpeed;
            this.sunriseTime = sunriseTime;
            this.sunsetTime = sunsetTime;
            this.time = time;
            this.weathertype = weathertype;
            this.icon=icon;
        }

        public String getLocation() {
            return location;
        }

        public String getDescription() {
            return description;
        }

        public double getTemperature() {
            return temperature;
        }

        public int getHumidity() {
            return humidity;
        }

        public double getWindSpeed() {
            return windSpeed;
        }

        public String getSunriseTime() {
            return sunriseTime;
        }

        public String getSunsetTime() {
            return sunsetTime;
        }

        public String getTime() {
            return time;
        }

        public String getWeathertype() {
            return weathertype;
        }


        public String getIcon() {
            return this.icon;
        }
    }
}

