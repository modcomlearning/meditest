package com.modcom.meditest;



import android.animation.ValueAnimator;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.JointType;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.RoundCap;
import com.google.android.gms.maps.model.SquareCap;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.scalars.ScalarsConverterFactory;
import retrofit_api.RecyclerInterface;
import utils.MapUtils;

import static com.google.android.gms.maps.model.JointType.ROUND;
import static utils.MapUtils.getBearing;


/**
 * A demonstration about car movement on google map
 by @Shihab Uddin
 TO RUN -> GIVE YOUR GOOGLE API KEY to >  google_maps_api.xml file
 -> GIVE YOUR SERVER URL TO FETCH LOCATION UPDATE
 */

public class Track_Phlebo extends AppCompatActivity implements OnMapReadyCallback {
    private static final long DELAY = 4500;
    private static final long ANIMATION_TIME_PER_ROUTE = 3000;
    String polyLine = "q`epCakwfP_@EMvBEv@iSmBq@GeGg@}C]mBS{@KTiDRyCiBS";
    GoogleMap googleMap;
    private PolylineOptions polylineOptions;
    private Polyline greyPolyLine;
    private SupportMapFragment mapFragment;
    private Handler handler;
    private Marker carMarker;
    private int index;
    private int next;
    private LatLng startPosition;
    private LatLng endPosition;
    private float v;
    Button button2;
    List<LatLng> polyLineList;
    private double lat, lng;
    // banani
    double latitude = -1.274164;
    double longitude = 36.8201789;
    private String TAG = "HomeActivity";
    SharedPreferences shared;

    // Give your Server URL here >> where you get car location update
    public static final String URL_DRIVER_LOCATION_ON_RIDE = "*******";
    private boolean isFirstPosition = true;
    private Double startLatitude;
    private Double startLongitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_track__phlebo);
        shared = getSharedPreferences("mediprefs", MODE_PRIVATE);
        //Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        handler = new Handler();
        startGettingOnlineDataFromCar();
    }

    void staticPolyLine() {
        googleMap.clear();

        polyLineList = MapUtils.decodePoly(polyLine);

        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (LatLng latLng : polyLineList) {
            builder.include(latLng);
        }
        LatLngBounds bounds = builder.build();
        CameraUpdate mCameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, 2);
        googleMap.animateCamera(mCameraUpdate);

        polylineOptions = new PolylineOptions();
        polylineOptions.color(Color.BLACK);
        polylineOptions.width(5);
        polylineOptions.startCap(new SquareCap());
        polylineOptions.endCap(new SquareCap());
        polylineOptions.jointType(ROUND);
        polylineOptions.addAll(polyLineList);
        greyPolyLine = googleMap.addPolyline(polylineOptions);
        startCarAnimation(latitude, longitude);
    }

    Runnable staticCarRunnable = new Runnable() {
        @Override
        public void run() {
            Log.i(TAG, "staticCarRunnable handler called...");
            if (index < (polyLineList.size() - 1)) {
                index++;
                next = index + 1;
            } else {
                index = -1;
                next = 1;
                stopRepeatingTask();
                return;
            }

            if (index < (polyLineList.size() - 1)) {
//                startPosition = polyLineList.get(index);
                startPosition = carMarker.getPosition();
                endPosition = polyLineList.get(next);
            }

            ValueAnimator valueAnimator = ValueAnimator.ofFloat(0, 1);
            valueAnimator.setDuration(3000);
            valueAnimator.setInterpolator(new LinearInterpolator());
            valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
//                    Log.i(TAG, "Car Animation Started...");
                    v = valueAnimator.getAnimatedFraction();
                    lng = v * endPosition.longitude + (1 - v)
                            * startPosition.longitude;
                    lat = v * endPosition.latitude + (1 - v)
                            * startPosition.latitude;
                    LatLng newPos = new LatLng(lat, lng);
                    carMarker.setPosition(newPos);
                    carMarker.setAnchor(0.5f, 0.5f);
                    carMarker.setRotation(getBearing(startPosition, newPos));
                    googleMap.moveCamera(CameraUpdateFactory
                            .newCameraPosition
                                    (new CameraPosition.Builder()
                                            .target(newPos)
                                            .zoom(15.5f)
                                            .build()));


                }
            });
            valueAnimator.start();
            handler.postDelayed(this, 5000);

        }
    };

    private void startCarAnimation(Double latitude, Double longitude) {
        LatLng latLng = new LatLng(latitude, longitude);

        carMarker = googleMap.addMarker(new MarkerOptions().position(latLng).
                flat(true).icon(BitmapDescriptorFactory.fromResource(R.drawable.car)));


        index = -1;
        next = 1;
        handler.postDelayed(staticCarRunnable, 3000);
    }

    void stopRepeatingTask() {

        if (staticCarRunnable != null) {
            handler.removeCallbacks(staticCarRunnable);
        }
    }
    @Override
    protected void onPause() {
        super.onPause();
        stopRepeatingTask();
        handler.removeCallbacks(mStatusChecker);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
        googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        googleMap.setTrafficEnabled(false);
        googleMap.setIndoorEnabled(false);
        googleMap.setBuildingsEnabled(false);

        //googleMap.getUiSettings().setZoomControlsEnabled(true);

    }


    // [START maps_poly_activity_style_polyline]
    private static final int COLOR_BLACK_ARGB = 0xff000000;
    private static final int POLYLINE_STROKE_WIDTH_PX = 12;
    /**
     * Styles the polyline, based on type.
     * @param polyline The polyline object that needs styling.
     */

    private void stylePolyline(Polyline polyline) {
        // Get the data object stored with the polyline.
                // Use a round cap at the start of the line.
                polyline.setStartCap(new RoundCap());

        polyline.setEndCap(new RoundCap());
        polyline.setWidth(POLYLINE_STROKE_WIDTH_PX);
        polyline.setColor(COLOR_BLACK_ARGB);
        polyline.setJointType(JointType.ROUND);
    }


//    private void getDriverLocationUpdate() {
//
//        StringRequest request = new StringRequest(Request.Method.POST, URL_DRIVER_LOCATION_ON_RIDE, new Response.Listener<String>() {
//
//            @Override
//            public void onResponse(String response) {
//
//                Log.d("PartnerInfoRes::", response);
//                JSONObject jObj;
//                try {
//                    jObj = new JSONObject(response);
//                    String ApiSuccess = jObj.getString("success");
//                    if (ApiSuccess.trim().equals("true")) {
//
//                        JSONObject jObj2 = new JSONObject(jObj.getString("data"));
//                        JSONObject jObj3 = new JSONObject(jObj2.getString("driver"));
//
//                        startLatitude = Double.valueOf(jObj3.getString("lat"));
//                        startLongitude = Double.valueOf(jObj3.getString("lng"));
//
//                        Log.d(TAG, startLatitude + "--" + startLongitude);
//
//                        if (isFirstPosition) {
//                            startPosition = new LatLng(startLatitude, startLongitude);
//
//                            carMarker = googleMap.addMarker(new MarkerOptions().position(startPosition).
//                                    flat(true).icon(BitmapDescriptorFactory.fromResource(R.drawable.car)));
//                            carMarker.setAnchor(0.5f, 0.5f);
//
//                            googleMap.moveCamera(CameraUpdateFactory
//                                    .newCameraPosition
//                                            (new CameraPosition.Builder()
//                                                    .target(startPosition)
//                                                    .zoom(15.5f)
//                                                    .build()));
//
//                            isFirstPosition = false;
//
//                        } else {
//                            endPosition = new LatLng(startLatitude, startLongitude);
//
//                            Log.d(TAG, startPosition.latitude + "--" + endPosition.latitude + "--Check --" + startPosition.longitude + "--" + endPosition.longitude);
//
//                            if ((startPosition.latitude != endPosition.latitude) || (startPosition.longitude != endPosition.longitude)) {
//
//                                Log.e(TAG, "NOT SAME");
//                                startBikeAnimation(startPosition, endPosition);
//
//                            } else {
//
//                                Log.e(TAG, "SAMME");
//                            }
//                        }
//
//                    }
//                    if (jObj.getString("message").trim().equals("Unauthorized")) {
//
//                        Log.e(TAG, "--- Unauthorized ---");
//
//                    }
//
//                } catch (Exception e) {
//                    Log.d("jsonError::", e + "");
//                }
//
//
//            }
//        }, new Response.ErrorListener() {
//            @Override
//            public void onErrorResponse(VolleyError error) {
//
//                Log.e(TAG, error.getMessage());
//            }
//        }) {
//            @Override
//            protected Map<String, String> getParams() {
//                Map<String, String> params = new HashMap<>();
//                //params.put("driver_id", driverId);
//                return params;
//            }
//
//            @Override
//            public Map<String, String> getHeaders() throws AuthFailureError {
//                Map<String, String> params = new HashMap<String, String>();
//                //Log.d("acc::", ClientAccToken);
//                //params.put("authorization", "ClientAccToken");
//
//                return params;
//            }
//
//        };
//
//        App.getAppInstance().addToRequestQueue(request, TAG);
//    }


    private void startBikeAnimation(final LatLng start, final LatLng end) {
        Log.i(TAG, "startBikeAnimation called...");
        ValueAnimator valueAnimator = ValueAnimator.ofFloat(0, 1);
        valueAnimator.setDuration(ANIMATION_TIME_PER_ROUTE);
        valueAnimator.setInterpolator(new LinearInterpolator());
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                //LogMe.i(TAG, "Car Animation Started...");
                v = valueAnimator.getAnimatedFraction();
                lng = v * end.longitude + (1 - v)
                        * start.longitude;
                lat = v * end.latitude + (1 - v)
                        * start.latitude;

                LatLng newPos = new LatLng(lat, lng);
                carMarker.setPosition(newPos);
                carMarker.setAnchor(0.5f, 0.5f);
                carMarker.setRotation(getBearing(start, end));

                // todo : Shihab > i can delay here
                googleMap.moveCamera(CameraUpdateFactory
                        .newCameraPosition
                                (new CameraPosition.Builder()
                                        .target(newPos)
                                        .zoom(15.5f)
                                        .build()));

                startPosition = carMarker.getPosition();

            }

        });
        valueAnimator.start();
    }

    Runnable mStatusChecker = new Runnable() {
        @Override
        public void run() {
            try {

                getDriverLocationUpdate();


            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
            }

            handler.postDelayed(mStatusChecker, DELAY);

        }
    };
    void startGettingOnlineDataFromCar() {
        handler.post(mStatusChecker);
    }

    void CreatePolyLineOnly() {

        googleMap.clear();

        polyLineList = MapUtils.decodePoly(polyLine);

        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (LatLng latLng : polyLineList) {
            builder.include(latLng);
        }
        LatLngBounds bounds = builder.build();
        CameraUpdate mCameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, 2);
        googleMap.animateCamera(mCameraUpdate);

        polylineOptions = new PolylineOptions();
        polylineOptions.color(Color.BLACK);
        polylineOptions.width(5);
        polylineOptions.startCap(new SquareCap());
        polylineOptions.endCap(new SquareCap());
        polylineOptions.jointType(ROUND);
        polylineOptions.addAll(polyLineList);
        greyPolyLine = googleMap.addPolyline(polylineOptions);


    }


    // private Marker marker;
    public void getDriverLocationUpdate() {

        try {
            // String phlebo_id = shared.getString("phlebo_id","");
            // ==========
            OkHttpClient client = new OkHttpClient.Builder().addInterceptor(new Interceptor() {
                @NotNull
                @Override
                public okhttp3.Response intercept(@NotNull Chain chain) throws IOException {

                    String token = shared.getString("token", "");
                    Request newRequest = chain.request().newBuilder()
                            .addHeader("Authorization", "Bearer " + token)
                            //  .addHeader("Authorization", "Bearer 107|4fRJslzbfcbPyu9hlhKnTeWHRgY7MHK5PVpkFsUL")
                            .build();
                    return chain.proceed(newRequest);
                }
            }).connectTimeout(30, TimeUnit.SECONDS)
                    .callTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .writeTimeout(30, TimeUnit.SECONDS)
                    .build();

            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(RecyclerInterface.JSONURL)
                    .addConverterFactory(ScalarsConverterFactory.create())
                    .client(client)
                    .build();

            String phlebo_id = shared.getString("phlebo_id", "");
            if (phlebo_id.equalsIgnoreCase("")) {
                startActivity(new Intent(getApplicationContext(), UserLogin.class));
                finish();
            }
          else
             {
            RecyclerInterface api = retrofit.create(RecyclerInterface.class);
            Call<String> call = api.getStringSinglePhleboLocation(Integer.parseInt(phlebo_id));
            call.enqueue(new Callback<String>() {

                @Override
                public void onResponse(Call<String> call, retrofit2.Response<String> response) {
                    try {
                        if (response.isSuccessful()) {
                            if (response.body() != null) {

                        JSONObject jObj = new JSONObject(response.body().toString());

                        if (jObj.isNull("lat") || jObj.isNull("lon")) {
                            Toast.makeText(Track_Phlebo.this, "Please wait for representative's location updates, retry in a few seconds", Toast.LENGTH_LONG).show();
                            handler.removeCallbacks(staticCarRunnable);
                            handler.removeCallbacks(mStatusChecker);
                            startActivity(new Intent(getApplicationContext(), PhleboDetails.class));
                            finish();
                        } else {
                            startLatitude = Double.valueOf(jObj.getString("lat"));
                            startLongitude = Double.valueOf(jObj.getString("lon"));


                            //else {


                            Log.d(TAG, startLatitude + "--" + startLongitude);

                            if (isFirstPosition) {
                                startPosition = new LatLng(startLatitude, startLongitude);

                                carMarker = googleMap.addMarker(new MarkerOptions().position(startPosition).
                                        flat(true).icon(BitmapDescriptorFactory.fromResource(R.drawable.car)));
                                carMarker.setAnchor(0.5f, 0.5f);

                                googleMap.moveCamera(CameraUpdateFactory
                                        .newCameraPosition
                                                (new CameraPosition.Builder()
                                                        .target(startPosition)
                                                        .zoom(15.5f)
                                                        .build()));

                                isFirstPosition = false;

                            } else {
                                endPosition = new LatLng(startLatitude, startLongitude);

                                Log.d(TAG, startPosition.latitude + "--" + endPosition.latitude + "--Check --" + startPosition.longitude + "--" + endPosition.longitude);

                                if ((startPosition.latitude != endPosition.latitude) || (startPosition.longitude != endPosition.longitude)) {

                                    Log.e(TAG, "NOT SAME");
                                    startBikeAnimation(startPosition, endPosition);
                                    //startCarAnimation(latitude, longitude);
                                } else {

                                    Log.e(TAG, "SAMME");
                                }
                            }
                        }
                            //here
                        } else {

                            Toast.makeText(Track_Phlebo.this, "Server Error!, Try again", Toast.LENGTH_SHORT).show();
                            // Log.i("onEmptyResponse", "Returned empty response");//Toast.makeText(getContext(),"Nothing returned",Toast.LENGTH_LONG).show();
                            //startActivity(new Intent(getApplicationContext(), NewMainPage.class));
                            //finish();

                        }
                }
                        else {

                    Toast.makeText(Track_Phlebo.this, "No Server Response, Try Again", Toast.LENGTH_SHORT).show();
                    //startActivity(new Intent(getApplicationContext(), NewMainPage.class));
                    //finish();
                }

                    } catch (JSONException e) {
                        e.printStackTrace();
                        stopRepeatingTask();
                        handler.removeCallbacks(mStatusChecker);

                    }

                }

                @Override
                public void onFailure(Call<String> call, Throwable t) {
                    Toast.makeText(Track_Phlebo.this, "There was a server error, try again", Toast.LENGTH_SHORT).show();
                    handler.removeCallbacks(staticCarRunnable);
                    handler.removeCallbacks(mStatusChecker);
//                startActivity(new Intent(getApplicationContext(), SingleBooking.class));
//                finish();
                }
            });
        }

        }
        catch (Exception e){
            Toast.makeText(this, "Server Error!, try again", Toast.LENGTH_SHORT).show();
        }

    }


}

