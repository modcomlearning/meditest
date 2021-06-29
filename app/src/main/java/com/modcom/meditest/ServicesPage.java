package com.modcom.meditest;


import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import adapters.RetrofitAdapter;
import helpers.StoreDatabase;
import models.ModelRecycler;
import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import android.widget.EditText;
import retrofit2.converter.scalars.ScalarsConverterFactory;
import retrofit_api.RecyclerInterface;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.appcompat.widget.SearchView;
import android.view.Menu;
import android.view.MenuItem;
import android.app.SearchManager;
import android.content.Context;

public class ServicesPage extends AppCompatActivity {
    private EditText etsearch;
    private RetrofitAdapter retrofitAdapter;
    private RecyclerView recyclerView;
    SearchView searchView;
    SharedPreferences shared;
    TextView number, totals;
    Button btn_view_details;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_services_page);
        recyclerView = findViewById(R.id.recycler);
        number = findViewById(R.id.cart);
        totals = findViewById(R.id.total);
        btn_view_details = findViewById(R.id.btn_view_details);
        btn_view_details.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), Cart.class));
            }
        });

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING);

        dbHelper = new StoreDatabase(getApplicationContext());
        try {
            dbHelper.open();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        int num = dbHelper.getCartItemsRowCount(true);
        number.setText(num+" Item(s)");
        int total = dbHelper.getAmount();
        totals.setText("Total: "+total+" KES");

       // etsearch = (EditText) findViewById(R.id.editText);
        shared = getSharedPreferences("mediprefs", MODE_PRIVATE);
        fetchJSON();
    }

    @Override
    protected void onResume() {
        super.onResume();
        dbHelper = new StoreDatabase(getApplicationContext());
        try {
            dbHelper.open();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        int num = dbHelper.getCartItemsRowCount(true);
        number.setText(num+" Item(s)");
        int total = dbHelper.getAmount();
        totals.setText("Total: "+total+" KES");
    }

    private void fetchJSON(){

        try {
            ProgressDialog dialog = new ProgressDialog(ServicesPage.this);
            dialog.setTitle("Retrieving Services..");
            dialog.setMessage("Please wait..");
            dialog.setMax(100);
            dialog.setCancelable(false);
            dialog.setCanceledOnTouchOutside(true);
            dialog.show();

            OkHttpClient okHttpClient = new OkHttpClient.Builder()
                    .connectTimeout(8, TimeUnit.SECONDS)
                    .callTimeout(8, TimeUnit.SECONDS)
                    .readTimeout(8, TimeUnit.SECONDS)
                    .writeTimeout(8, TimeUnit.SECONDS)
                    .build();

            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(RecyclerInterface.JSONURL)
                    .addConverterFactory(ScalarsConverterFactory.create())
                    .client(okHttpClient)
                    .build();


            RecyclerInterface api = retrofit.create(RecyclerInterface.class);
            Call<String> call = api.getString();
            call.enqueue(new Callback<String>() {
                @Override
                public void onResponse(Call<String> call, Response<String> response) {
                    dialog.dismiss();

                    try {
                        if (response.isSuccessful()) {
                            if (response.body() != null) {
                                dialog.dismiss();
                                // Toast.makeText(ServicesPage.this, "Success", Toast.LENGTH_SHORT).show();
                                //  Log.i("onSuccess", response.body().toString());

                                String jsonresponse = response.body().toString();
                                writeRecycler(jsonresponse);

                            } else {
                                dialog.dismiss();
                                Toast.makeText(ServicesPage.this, "Error!, Try again", Toast.LENGTH_SHORT).show();
                                // Log.i("onEmptyResponse", "Returned empty response");//Toast.makeText(getContext(),"Nothing returned",Toast.LENGTH_LONG).show();
                                startActivity(new Intent(getApplicationContext(), Updated.class));
                                finish();

                            }
                        }
                        else {
                            dialog.dismiss();
                            Toast.makeText(ServicesPage.this, "No Server Response, Try Again", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(getApplicationContext(), Updated.class));
                            finish();
                        }

                    } catch (NullPointerException e) {
                        AlertDialog alert = new AlertDialog.Builder(ServicesPage.this)
                                .setTitle("Server Response Error, check your internet & try Again")
                                .setPositiveButton("Ok",
                                        new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog,
                                                                int whichButton) {
                                                //System.exit(0);
                                            }
                                        }).show();
                        alert.setCanceledOnTouchOutside(false);
                        // Toast.makeText(ServicesPage.this, "Server Response Error, Try again", Toast.LENGTH_SHORT).show();
                    }

                }

                @Override
                public void onFailure(Call<String> call, Throwable t) {
                    dialog.dismiss();
                    //Log.d("error", t.getMessage());
                    Toast.makeText(ServicesPage.this, "There was a server error, check your internet & try again", Toast.LENGTH_SHORT).show();

                    startActivity(new Intent(getApplicationContext(), Updated.class));
                    finish();

                }
            });

        }
        catch (Exception e){
            Toast.makeText(this, "Server error, check your internet & Try again", Toast.LENGTH_SHORT).show();
        }

    }

    private void writeRecycler(String response){

        try {
            //getting the whole json object from the response
            JSONObject obj = new JSONObject(response);
            //if(obj.optString("status").equals("true")){
                ArrayList<ModelRecycler> modelRecyclerArrayList = new ArrayList<>();
                JSONArray dataArray  = obj.getJSONArray("data");
                for (int i = 0; i < dataArray.length(); i++) {
                    ModelRecycler modelRecycler = new ModelRecycler();
                    JSONObject dataobj = dataArray.getJSONObject(i);
                    modelRecycler.setImgURL(dataobj.getString("image_path"));
                    modelRecycler.setName(dataobj.getString("test_name"));
                    modelRecycler.setDesc(dataobj.getString("test_desc"));
                    modelRecycler.setCity(dataobj.getString("test_price"));
                   // modelRecycler.setHome(dataobj.getString("home"));
                    modelRecycler.setId(dataobj.getString("id"));
                    modelRecyclerArrayList.add(modelRecycler);
                }

                retrofitAdapter = new RetrofitAdapter(this,modelRecyclerArrayList);
                recyclerView.setAdapter(retrofitAdapter);
                recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.VERTICAL, false));


//            }else {
//                Toast.makeText(ServicesPage.this, obj.optString("message")+"", Toast.LENGTH_SHORT).show();
//            }

        } catch (JSONException e) {
            AlertDialog alert = new AlertDialog.Builder(ServicesPage.this)
                    .setTitle("Error!, Check your Internet. Try again")
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
    StoreDatabase dbHelper;
    Menu customMenu;
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_home, menu);

        // Associate searchable configuration with the SearchView
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        searchView = (SearchView) menu.findItem(R.id.action_search)
                .getActionView();
        searchView.setSearchableInfo(searchManager
                .getSearchableInfo(getComponentName()));
        searchView.setMaxWidth(Integer.MAX_VALUE);

        // listening to search query text change
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // filter recycler view when query submitted
                //Toast.makeText(ServicesPage.this, "Working", Toast.LENGTH_SHORT).show();
                return false;
            }

            @Override
            public boolean onQueryTextChange(String query) {
                // filter recycler view when text is changed
                retrofitAdapter.getFilter().filter(query);
                retrofitAdapter.notifyDataSetChanged();
                return true;

            }
        });
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {


        if(item.getItemId() ==R.id.action_logout){
            SharedPreferences.Editor editor = shared.edit();
            editor.clear();
            editor.apply();
            startActivity(new Intent(getApplicationContext(), Updated.class));
            finish();
        }


        return super.onOptionsItemSelected(item);
    }
}