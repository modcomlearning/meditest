package com.modcom.meditest;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import com.google.gson.JsonObject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import adapters.GridAdapter;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit_api.IRetrofit;
import services.ServiceGenerator;

public class NewMainPage extends AppCompatActivity {
    GridView list;
    String email_pref;
    String password_pref;
    SharedPreferences shared;
    String[] maintitle ={
            "Laboratory", "Pharmacy", "Consultation", "Home Care",
    };

    Integer[] imgid={
            R.mipmap.doc5, R.mipmap.doc1, R.mipmap.doc2,
            R.mipmap.doc3
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home3);
        shared = getSharedPreferences("mediprefs", MODE_PRIVATE);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        GridAdapter adapter=new GridAdapter(this, maintitle,imgid);
        list=(GridView)findViewById(R.id.grid);
        list.setAdapter(adapter);

        email_pref = shared.getString("email","");
        password_pref = shared.getString("password","");

        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,int position, long id) {
                // TODO Auto-generated method stub
                if(position == 0) {
                    //code specific to first list item
                    //Toast.makeText(getApplicationContext(),"Place Your First Option Code",Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(getApplicationContext(), ServicesPage.class));
                }

                else if(position == 1) {
                    //code specific to 2nd list item
                   // throw new RuntimeException("Test Crash");
                   Toast.makeText(getApplicationContext(),"Coming Soon",Toast.LENGTH_SHORT).show();
                }

                else if(position == 2) {

                    Toast.makeText(getApplicationContext(),"Coming Soon",Toast.LENGTH_SHORT).show();
                }
                else if(position == 3) {

                    Toast.makeText(getApplicationContext(),"Coming Soon",Toast.LENGTH_SHORT).show();
                }


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
                onPostClicked(email_pref, password_pref, NewMainPage.this);
            }
        }

        if (item.getItemId() == R.id.action_logout){
            SharedPreferences.Editor editor = shared.edit();
            //Logout();
            editor.clear();
            editor.apply();
            editor.commit();
            startActivity(new Intent(getApplicationContext(), NewMainPage.class));
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

            ProgressDialog dialog = new ProgressDialog(NewMainPage.this);
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
                                    Toast.makeText(NewMainPage.this, "Email Error!", Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(getApplicationContext(), UserLogin.class));
                                    finish();
                                }
                                if (dataArray.has("password")) {
                                    Toast.makeText(NewMainPage.this, "Password Error!", Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(getApplicationContext(), UserLogin.class));
                                    finish();
                                }

                            }

                        } else if (code == 500) {

                            Toast.makeText(NewMainPage.this, obj.getString("message"), Toast.LENGTH_SHORT).show();
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
                            Toast.makeText(NewMainPage.this, "Error Occured! try Again", Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(NewMainPage.this, "Check Your internet Connection and try again", Toast.LENGTH_LONG).show();
                    startActivity(new Intent(getApplicationContext(), UserLogin.class));
                    finish();
                    dialog.dismiss();
                }
            });
        }
    }

    //logout
    public void Logout(){

        try {

            ProgressDialog dialog = new ProgressDialog(NewMainPage.this);
            dialog.setTitle("Logging out");
            dialog.setMessage("Please wait..");
            dialog.setMax(100);
            dialog.show();


            //use iretrofit to post data to end point
            ServiceGenerator serviceGenerator = new ServiceGenerator(getApplicationContext());
            IRetrofit jsonPostService = serviceGenerator.createService(IRetrofit.class, IRetrofit.BASE_URL);
            Call<JsonObject> call = jsonPostService.postRawLogout();
            call.enqueue(new Callback<JsonObject>() {

                @Override
                public void onResponse(@NotNull Call<JsonObject> call, @NotNull Response<JsonObject> response) {
                    try {
                        dialog.dismiss();
                        //handle JSON response
                        //Toast.makeText(UserrReg.this, "OK"+response.body().toString(), Toast.LENGTH_SHORT).show();
                        JSONObject obj = new JSONObject(response.body().toString());
                        int code = Integer.parseInt((obj.getString("status_code")));

                        if (code == 400) {
                            Toast.makeText(NewMainPage.this, ""+code, Toast.LENGTH_SHORT).show();

                        } else if (code == 200) {
                            Toast.makeText(NewMainPage.this, "Logged out Successfully", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(getApplicationContext(), UserLogin.class));
                            finish();
                        } else {
                            Toast.makeText(NewMainPage.this, "Error Occurred! try Again", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(getApplicationContext(), UserLogin.class));
                            finish();
                        }


                    } catch (JSONException e) {
                        //Toast.makeText(UserLogin.this, "Server Error, Check Your internet Connection and try again", Toast.LENGTH_LONG).show();
                        dialog.dismiss();
                        AlertDialog alert = new AlertDialog.Builder(NewMainPage.this)
                                .setTitle("Server Error!. Try again")
                                .setPositiveButton("Ok",
                                        new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog,
                                                                int whichButton) {
                                                //System.exit(0);
                                            }
                                        }).show();
                        alert.setCanceledOnTouchOutside(false);

                    }

                }

                @Override
                public void onFailure(Call<JsonObject> call, Throwable t) {
                    // Log.d("response-failure", call.toString());
                    //Toast.makeText(UserrReg.this, "Server Error, Check Your internet Connection and try again", Toast.LENGTH_LONG).show();
                    //Toast.makeText(UserLogin.this, "Server Error, Check Your internet Connection and try again", Toast.LENGTH_LONG).show();
                    dialog.dismiss();
                    AlertDialog alert = new AlertDialog.Builder(NewMainPage.this)
                            .setTitle("Server Error!. Please Check Your Network connections.try Again")
                            .setPositiveButton("Ok",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog,
                                                            int whichButton) {
                                            //System.exit(0);
                                        }
                                    }).show();
                    alert.setCanceledOnTouchOutside(false);
                    dialog.dismiss();
                }
            });

        }
        catch (Exception e){
            Toast.makeText(this, "Server Error!, try again", Toast.LENGTH_SHORT).show();
        }
    }


}