package com.modcom.meditest;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterViewFlipper;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.material.circularreveal.cardview.CircularRevealCardView;

import java.util.ArrayList;

import offers.APIService;
import offers.FlipperAdapter;
import offers.Hero;
import offers.Heroes;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

//this page is accessed when some one logs in
public class NewHome extends AppCompatActivity {
    SharedPreferences shared;
    Button btn_self;
    Button btn_others;
    String email_pref;
    String password_pref;
    CircularRevealCardView layoutself, layoutothers;

    //the base url
    public static final String BASE_URL = "https://www.simplifiedcoding.net/demos/view-flipper/";

    //adapterviewflipper object
    private AdapterViewFlipper adapterViewFlipper;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_home);
        //create/get prefs
        shared = getSharedPreferences("mediprefs", MODE_PRIVATE);

        layoutself  = findViewById(R.id.layoutself);
        layoutothers = findViewById(R.id.layoutothers);
        layoutself.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // if its self go to confirm booking
                Intent x = new Intent(getApplicationContext() , ConfirmBooking.class);
                SharedPreferences.Editor editor  = shared.edit();
                editor.putBoolean("self", true);
                editor.putString("dependant_id", null);
                editor.putString("relationship", null);
                editor.apply();
                startActivity(x);
            }
        });

        layoutothers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //if others go to others activity
                Intent x = new Intent(getApplicationContext() , Others.class);
                startActivity(x);
            }
        });

        //Flipper
        //getting adapterviewflipper
        adapterViewFlipper = (AdapterViewFlipper) findViewById(R.id.adapterViewFlipper);

        //building retrofit object
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        //Defining retrofit api service
        APIService service = retrofit.create(APIService.class);

        //creating an api call
        Call<Heroes> call =  service.getHeroes();

        //making the call
        call.enqueue(new Callback<Heroes>() {
            @Override
            public void onResponse(Call<Heroes> call, Response<Heroes> response) {

                //getting list of heroes
                ArrayList<Hero> heros = response.body().getHeros();

                //creating adapter object
                FlipperAdapter adapter = new FlipperAdapter(getApplicationContext(), heros);

                //adding it to adapterview flipper
                adapterViewFlipper.setAdapter(adapter);
                adapterViewFlipper.setFlipInterval(5000);
                adapterViewFlipper.startFlipping();
            }

            @Override
            public void onFailure(Call<Heroes> call, Throwable t) {
                Toast.makeText(getApplicationContext(), "This App Requires Internet Connection", Toast.LENGTH_LONG).show();
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.newhome, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.view_booking){
                startActivity(new Intent(getApplicationContext(), MyBookings.class));

        }

        if (item.getItemId() == R.id.action_logout){
            SharedPreferences.Editor editor = shared.edit();
            editor.clear();
            editor.apply();
            editor.commit();
            startActivity(new Intent(getApplicationContext(), Updated.class));
            finish();
        }

        return super.onOptionsItemSelected(item);
    }
}