package com.modcom.meditest;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import com.google.android.material.appbar.CollapsingToolbarLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.Toolbar;

import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.sql.SQLException;
import java.util.concurrent.TimeUnit;

import helpers.StoreDatabase;
import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.scalars.ScalarsConverterFactory;
import retrofit_api.RecyclerInterface;

public class TestDescActivity extends AppCompatActivity {
TextView name, desc, price, status, constituents, category, prerequisites, report;
LinearLayout linearLayout;
    Menu customMenu;
    private StoreDatabase dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_desc);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        CollapsingToolbarLayout toolBarLayout = (CollapsingToolbarLayout) findViewById(R.id.toolbar_layout);
        toolBarLayout.setTitle(getTitle());

        //String home = b.getString("home");
         name = findViewById(R.id.test_name);
         desc = findViewById(R.id.test_desc);
         price = findViewById(R.id.test_price);
         status = findViewById(R.id.status);
         constituents = findViewById(R.id.test_constituents);
         category = findViewById(R.id.test_category);
         prerequisites = findViewById(R.id.test_prerequisites);
         report = findViewById(R.id.test_report);
         linearLayout = findViewById(R.id.linearLayout);
         linearLayout.setVisibility(View.GONE);


        AppCompatButton x = findViewById(R.id.buy);
        x.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Bundle b = getIntent().getExtras();
                String id = b.getString("id");

                dbHelper = new StoreDatabase(getApplicationContext());
                try {
                    dbHelper.open();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                double amount = Double.parseDouble(price.getText().toString());
                long res =  dbHelper.createItem(id, name.getText().toString(), desc.getText().toString(), amount, "true");
                if (res ==-1) {
                    Toast.makeText(getApplicationContext(), "Record Already Added to Cart", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(getApplicationContext(), ServicesPage.class);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(getApplicationContext(), "Record Added to Cart", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(getApplicationContext(), ServicesPage.class);
                    startActivity(intent);
                    finish();
                }

                //Toast.makeText(TestDescActivity.this, "Record Inserted to Shopping Cart", Toast.LENGTH_SHORT).show();
            }
        });





        fetchJSON();
    }

    private void fetchJSON(){
        try{
        Bundle b = getIntent().getExtras();
        String id = b.getString("id");

        ProgressDialog dialog = new ProgressDialog(TestDescActivity.this);
        dialog.setTitle("Retrieving Test Description..");
        dialog.setMessage("Please wait..");
        dialog.setMax(100);
        dialog.show();


        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .callTimeout(30, TimeUnit.SECONDS)
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();


        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(RecyclerInterface.JSONURL)
                .addConverterFactory(ScalarsConverterFactory.create())
                .client(okHttpClient)
                .build();

        RecyclerInterface api = retrofit.create(RecyclerInterface.class);

        Call<String> call = api.getStringSingle(Integer.parseInt(id));
        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
               // Log.i("Responsestring", response.body().toString());
                //Toast.makeText()
                if (response.isSuccessful()) {
                    if (response.body() != null) {
                        try {
                        dialog.dismiss();
                            linearLayout.setVisibility(View.VISIBLE);
                        //Toast.makeText(TestDescActivity.this, "Success", Toast.LENGTH_SHORT).show();
                      //  Log.i("onSuccess", response.body().toString());

                        String jsonresponse = response.body().toString();

                        JSONObject obj = new JSONObject(jsonresponse);

                        try {
                            JSONObject dataArray  = obj.getJSONObject("data");
                         for (int i = 0; i < dataArray.length(); i++) {
                            name.setText(dataArray.getString("test_name"));
                            desc.setText(dataArray.getString("test_desc"));
                            price.setText(dataArray.getString("test_price"));
                            constituents.setText(dataArray.getString("test_constituents"));
                            category.setText(dataArray.getString("test_category"));
                            prerequisites.setText(dataArray.getString("test_prerequisites"));
                            report.setText(dataArray.getString("test_report_availability"));

                            String x  = dataArray.getString("home");
//                            if (x.equalsIgnoreCase("1")){
//                                status.setText("This sample can be collected at home");
//                            }
//
//                            else {
//                                status.setText("This sample cannot be collected at home");
//                            }

                        }
                        } catch (JSONException e) {
                            AlertDialog alert = new AlertDialog.Builder(TestDescActivity.this)
                                    .setTitle("Server Error! try again.")
                                    .setPositiveButton("Ok",
                                            new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog,
                                                                    int whichButton) {
                                                    //System.exit(0);
                                                }
                                            }).show();
                            alert.setCanceledOnTouchOutside(false);
                        }

                        } catch (JSONException e) {
                            AlertDialog alert = new AlertDialog.Builder(TestDescActivity.this)
                                    .setTitle("Please Check Your Network connections. Try Again")
                                    .setPositiveButton("Ok",
                                            new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog,
                                                                    int whichButton) {
                                                    //System.exit(0);
                                                }
                                            }).show();
                            alert.setCanceledOnTouchOutside(false);
                        }


                    } else {
                        dialog.dismiss();
                        Toast.makeText(TestDescActivity.this, "Error!, Try again", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(getApplicationContext(), ServicesPage.class));
                        //Log.i("onEmptyResponse", "Returned empty response");//Toast.makeText(getContext(),"Nothing returned",Toast.LENGTH_LONG).show();
                    }
                }

                else {
                    dialog.dismiss();
                    Toast.makeText(TestDescActivity.this, "No Response, try again", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(getApplicationContext(), Updated.class));
                    finish();
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                dialog.dismiss();
                Toast.makeText(TestDescActivity.this, "There was a server error, try again", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(getApplicationContext(), ServicesPage.class));
                finish();
            }
        });

       }
         catch (Exception e){
             Toast.makeText(this, "Server Error!, try again", Toast.LENGTH_LONG).show();
         }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_test_desc, menu);
        return true;
    }



    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if (item.getItemId() == android.R.id.home){
            Intent intent = new Intent(getApplicationContext(), ServicesPage.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}