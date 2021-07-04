package com.modcom.meditest;

import android.app.Activity;
import android.app.ProgressDialog;
import android.app.Service;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.os.Bundle;

import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.circularreveal.cardview.CircularRevealCardView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.play.core.appupdate.AppUpdateInfo;
import com.google.android.play.core.appupdate.AppUpdateManager;
import com.google.android.play.core.appupdate.AppUpdateManagerFactory;
import com.google.android.play.core.install.model.AppUpdateType;
import com.google.android.play.core.install.model.UpdateAvailability;
import com.google.android.play.core.tasks.Task;
import com.google.gson.JsonObject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterViewFlipper;
import android.widget.Toast;

import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

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
import retrofit_api.IRetrofit;
import services.ServiceGenerator;

import static com.google.android.play.core.install.model.AppUpdateType.IMMEDIATE;

public class Updated extends AppCompatActivity {
CircularRevealCardView lab, pharmacy, consultation, homecare;
    String email_pref;
    String password_pref;
    SharedPreferences shared;
    private static final int IMMEDIATE_APP_UPDATE_REQ_CODE = 124;
    //the base url
    public static final String BASE_URL = "https://modcom.co.ke/meditest/";

    //adapterviewflipper object
    private AdapterViewFlipper adapterViewFlipper;
    private AppUpdateManager appUpdateManager;


    private void checkUpdate() {

        Task<AppUpdateInfo> appUpdateInfoTask = appUpdateManager.getAppUpdateInfo();

        appUpdateInfoTask.addOnSuccessListener(appUpdateInfo -> {
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                    && appUpdateInfo.isUpdateTypeAllowed(IMMEDIATE)) {
                //Toast.makeText(this, "Update available", Toast.LENGTH_SHORT).show();
               startUpdateFlow(appUpdateInfo);
            } else if (appUpdateInfo.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS){
                startUpdateFlow(appUpdateInfo);
                //Toast.makeText(this, "Update available", Toast.LENGTH_SHORT).show();
                //Toast.makeText(this, "okay", Toast.LENGTH_SHORT).show();
            }


        });
    }


    private void startUpdateFlow(AppUpdateInfo appUpdateInfo) {
        try {
            appUpdateManager.startUpdateFlowForResult(appUpdateInfo, IMMEDIATE, this,
                    IMMEDIATE_APP_UPDATE_REQ_CODE);
        } catch (IntentSender.SendIntentException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == IMMEDIATE_APP_UPDATE_REQ_CODE) {
            if (resultCode == RESULT_CANCELED) {
                Toast.makeText(getApplicationContext(), "Update canceled by user! Result Code: " + resultCode, Toast.LENGTH_LONG).show();
            } else if (resultCode == RESULT_OK) {
                Toast.makeText(getApplicationContext(), "Update success! Result Code: " + resultCode, Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(getApplicationContext(), "Update Failed! Result Code: " + resultCode, Toast.LENGTH_LONG).show();
                checkUpdate();
            }
        }
    }


    // Checks that the update is not stalled during 'onResume()'.
// However, you should execute this check at all entry points into the app.


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_updated);


        appUpdateManager = AppUpdateManagerFactory.create(getApplicationContext());
        checkUpdate();
        shared = getSharedPreferences("mediprefs", MODE_PRIVATE);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        CollapsingToolbarLayout toolBarLayout = (CollapsingToolbarLayout) findViewById(R.id.toolbar_layout);
        toolBarLayout.setTitle(getTitle());
//Snackbar.make(layoutpharmacy,"Coming soon",Snackbar.LENGTH_LONG).setTextColor(resources.getColor(R.color.white)).show()
        lab = findViewById(R.id.layoutlab);
        lab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), ServicesPage.class));
            }
        });

        pharmacy = findViewById(R.id.layoutpharmacy);
        pharmacy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar snackbar = Snackbar
                        .make(view, "Coming Soon", Snackbar.LENGTH_LONG);
                snackbar.show();
            }
        });

        consultation = findViewById(R.id.layoutconsulttion);
        consultation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar snackbar = Snackbar
                        .make(view, "Coming Soon", Snackbar.LENGTH_LONG);
                snackbar.show();
            }
        });


        homecare = findViewById(R.id.layouthome);
        homecare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar snackbar = Snackbar
                        .make(view, "Coming Soon", Snackbar.LENGTH_LONG);
                snackbar.show();
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
                if (response.isSuccessful()) {
                    if (response.body() != null) {
                        //getting list of heroes
                        ArrayList<Hero> heros = response.body().getHeros();

                        //creating adapter object
                        FlipperAdapter adapter = new FlipperAdapter(getApplicationContext(), heros);

                        //adding it to adapterview flipper
                        adapterViewFlipper.setAdapter(adapter);
                        adapterViewFlipper.setFlipInterval(5000);
                        adapterViewFlipper.startFlipping();
                    }
                }}

            @Override
            public void onFailure(Call<Heroes> call, Throwable t) {
                Toast.makeText(getApplicationContext(), "This App Requires Internet Connection", Toast.LENGTH_LONG).show();
            }
        });




    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_new_main_page, menu);
        return true;
    }



    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        // Toast.makeText(this, "Hey"+email_pref, Toast.LENGTH_SHORT).show();
        // Toast.makeText(this, "Hey"+password_pref, Toast.LENGTH_SHORT).show();
        if (item.getItemId() == R.id.view_booking){
            if(email_pref.equalsIgnoreCase("") && password_pref.equalsIgnoreCase("")) {
                startActivity(new Intent(getApplicationContext(), UserLogin.class));
                //  finish();
            }
            else{
                onPostClicked(email_pref, password_pref, Updated.this);
            }
        }

        if (item.getItemId() == R.id.action_logout){
            SharedPreferences.Editor editor = shared.edit();
            //Logout();
            editor.clear();
            editor.apply();
            editor.commit();
            startActivity(new Intent(getApplicationContext(), Updated.class));
            finish();

        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        shared = getSharedPreferences("mediprefs", MODE_PRIVATE);
        email_pref = shared.getString("email","");
        password_pref = shared.getString("password","");
    }

    public void onPostClicked(String email_input, String password_input, Activity context) {

        if (email_input.equals("") || password_input.equals("")) {
            startActivity(new Intent(getApplicationContext(), UserLogin.class));
            finish();
        } else {

            ProgressDialog dialog = new ProgressDialog(Updated.this);
            dialog.setTitle("Logging in..");
            dialog.setMessage("Please wait..");
            dialog.setMax(100);
            dialog.setMax(100);
            dialog.setCancelable(true);
            dialog.setCanceledOnTouchOutside(true);
            dialog.show();

            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("email", email_input);
            jsonObject.addProperty("password", password_input);

            ServiceGenerator serviceGenerator = new ServiceGenerator(getApplicationContext());
            IRetrofit jsonPostService = serviceGenerator.createService(IRetrofit.class, IRetrofit.BASE_URL);
            Call<JsonObject> call = jsonPostService.postRawLogin(jsonObject);
            call.enqueue(new Callback<JsonObject>() {

                @Override
                public void onResponse(@NotNull Call<JsonObject> call, @NotNull Response<JsonObject> response) {
                    try {
                        dialog.dismiss();
                        //Toast.makeText(UserrReg.this, "OK"+response.body().toString(), Toast.LENGTH_SHORT).show();
                        JSONObject obj = new JSONObject(response.body().toString());
                        int code = Integer.parseInt((obj.getString("status_code")));

                        if (code == 400) {
                            //Toast.makeText(UserrReg.this, ""+code, Toast.LENGTH_SHORT).show();
                            JSONObject dataArray = obj.getJSONObject("message");
                            for (int i = 0; i < dataArray.length(); i++) {

                                if (dataArray.has("email")) {
                                    Toast.makeText(Updated.this, "Email Error!", Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(getApplicationContext(), UserLogin.class));
                                    finish();
                                }
                                if (dataArray.has("password")) {
                                    Toast.makeText(Updated.this, "Password Error!", Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(getApplicationContext(), UserLogin.class));
                                    finish();
                                }

                            }

                        } else if (code == 500) {

                            Toast.makeText(Updated.this, obj.getString("message"), Toast.LENGTH_SHORT).show();
                        } else if (code == 200) {
                            startActivity(new Intent(getApplicationContext(), MyBookings.class));
                            SharedPreferences.Editor editor = shared.edit();
                            String token = obj.getString("token");

                            JSONObject dataArray1 = obj.getJSONObject("user");
                            for (int i = 0; i < dataArray1.length(); i++) {
                                editor.putString("id", dataArray1.getString("id"));
                                editor.putString("first_name", dataArray1.getString("first_name"));
                                editor.putString("last_name", dataArray1.getString("last_name"));
                                editor.putString("sirname", dataArray1.getString("sirname"));
                                editor.putString("phone", dataArray1.getString("phone"));
                                editor.putString("email", dataArray1.getString("email"));
                                editor.putString("password", password_input);
                                //Toast.makeText(UserLogin.this, "ID" + dataArray1.getString("id"), Toast.LENGTH_SHORT).show();
                                // Toast.makeText(UserLogin.this, "User name" + dataArray1.getString("first_name"), Toast.LENGTH_SHORT).show();
                            }

                            editor.putString("token", token);
                            editor.apply();

                            // Toast.makeText(NewMainPage.this, "Login  Successfully", Toast.LENGTH_SHORT).show();


                        } else {
                            Toast.makeText(Updated.this, "Error Occured! try Again", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(getApplicationContext(), UserLogin.class));
                            finish();

                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(context, "Error!, Try again", Toast.LENGTH_SHORT).show();
                    }

                }

                @Override
                public void onFailure(Call<JsonObject> call, Throwable t) {
                    Log.d("response-failure", call.toString());
                    Toast.makeText(Updated.this, "Check Your internet Connection and try again", Toast.LENGTH_LONG).show();
                    startActivity(new Intent(getApplicationContext(), UserLogin.class));
                    finish();
                    dialog.dismiss();
                }
            });
        }
    }
}