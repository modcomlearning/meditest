package com.modcom.meditest;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import adapters.Dep_Adapter;
import models.DepModel;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.scalars.ScalarsConverterFactory;
import retrofit_api.RecyclerInterface;

public class Others extends AppCompatActivity {
    private Dep_Adapter retrofitAdapter;
    private RecyclerView recyclerView;
    SharedPreferences shared;
    SwipeRefreshLayout swipeRefreshLayout;
    TextView dep_status;
    Button btn_add_dependants;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_others);
        shared = getSharedPreferences("mediprefs", MODE_PRIVATE);
        recyclerView = findViewById(R.id.recycler);
        swipeRefreshLayout = (SwipeRefreshLayout)findViewById(R.id.swipeRefreshLayout);

        dep_status   = findViewById(R.id.dep_status);
        dep_status.setVisibility(View.GONE);
        btn_add_dependants   = findViewById(R.id.btn_add_dependants);
        btn_add_dependants.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), AddDepandantsActivity.class));
                //finish();
            }
        });
        fetchJSON();

    }

    private void fetchJSON(){

        try {
        ProgressDialog dialog = new ProgressDialog(Others.this);
        dialog.setTitle("Retrieving Your Dependants..");
        dialog.setMessage("Please wait..");
        dialog.setMax(100);
        dialog.show();


        OkHttpClient client = new OkHttpClient.Builder().addInterceptor(new Interceptor() {
            @NotNull
            @Override
            public okhttp3.Response intercept(@NotNull Chain chain) throws IOException {

                String token = shared.getString("token","");
                Request newRequest  = chain.request().newBuilder()
                        .addHeader("Authorization", "Bearer "+token)
                        //  .addHeader("Authorization", "Bearer 107|4fRJslzbfcbPyu9hlhKnTeWHRgY7MHK5PVpkFsUL")
                        .build();
                return chain.proceed(newRequest);
            }
        }) .callTimeout(7, TimeUnit.SECONDS)
                .connectTimeout(7, TimeUnit.SECONDS)
                .readTimeout(7, TimeUnit.SECONDS)
                .writeTimeout(7, TimeUnit.SECONDS)
                .build();


        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(RecyclerInterface.JSONURL)
                .addConverterFactory(ScalarsConverterFactory.create())
                .client(client)
                .build();



        String user_id = shared.getString("id","");
        //Toast.makeText(getActivity(), "ID "+user_id, Toast.LENGTH_SHORT).show();
        RecyclerInterface api = retrofit.create(RecyclerInterface.class);

        Call<String> call = api.getStringYourDep(Integer.parseInt(user_id));
        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                dialog.dismiss();
                //Log.d("Responsestring", response.body());
                //Toast.makeText()
                // Toast.makeText(getActivity(), "ID: "+response.body(), Toast.LENGTH_SHORT).show();
                if (response.isSuccessful()) {
                    if (!response.body().isEmpty()) {
                        dialog.dismiss();
                        // Toast.makeText(getActivity().getApplicationContext(), "Success", Toast.LENGTH_SHORT).show();
                       // Log.d("onSuccess", response.body().toString());
                        String jsonresponse = response.body().toString();
                        writeRecycler(jsonresponse);

                    } else {
                        dialog.dismiss();
                        Toast.makeText(getApplicationContext(), "Error!, Try again", Toast.LENGTH_SHORT).show();
                        //Log.i("onEmptyResponse", "Returned empty response");//Toast.makeText(getContext(),"Nothing returned",Toast.LENGTH_LONG).show();
                    }
                }

                else {
                    dialog.dismiss();
                    Toast.makeText(Others.this, "No Response, Try Again", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(getApplicationContext(), NewHome.class));
                    finish();
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                dialog.dismiss();
                Toast.makeText(getApplicationContext(), "There was a server error, try again", Toast.LENGTH_SHORT).show();

            }
        });

        }
        catch (Exception e){
            Toast.makeText(this, "Server Error!, try again", Toast.LENGTH_SHORT).show();
        }
    }

    private void writeRecycler(String response){

        try {
            //getting the whole json object from the response
            JSONObject obj = new JSONObject(response);
            //if(obj.optString("status").equals("true")){
            ArrayList<DepModel> modelRecyclerArrayList = new ArrayList<>();
            JSONArray dataArray = obj.getJSONArray("data");

            if (dataArray.isNull(0)) {
                dep_status.setVisibility(View.VISIBLE);
                //Toast.makeText(getActivity(), "Empty", Toast.LENGTH_SHORT).show();
                dep_status.setText(R.string.dep_status);
                swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        swipeRefreshLayout.setRefreshing(false);
                        fetchJSON();
                    }
                });
            } else {
                dep_status.setVisibility(View.VISIBLE);
                dep_status.setText("Pull to Refresh");
                for (int i = 0; i < dataArray.length(); i++) {
                    DepModel modelRecycler = new DepModel();
                    JSONObject dataobj = dataArray.getJSONObject(i);
                    modelRecycler.setId(dataobj.getString("id"));
                    modelRecycler.setFirst_name(dataobj.getString("first_name"));
                    modelRecycler.setRelationship(dataobj.getString("relationship"));
                    modelRecyclerArrayList.add(modelRecycler);
                }

                retrofitAdapter = new Dep_Adapter(getApplicationContext(), modelRecyclerArrayList);
                recyclerView.setAdapter(retrofitAdapter);
                recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.VERTICAL, false));
                // SetOnRefreshListener on SwipeRefreshLayout


                swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        swipeRefreshLayout.setRefreshing(false);
                        fetchJSON();
                    }
                });
//            }else {
//                Toast.makeText(ServicesPage.this, obj.optString("message")+"", Toast.LENGTH_SHORT).show();
//            }
            }
        } catch(JSONException e){
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), "Network Error!, Pull to Refresh", Toast.LENGTH_SHORT).show();
        }

    }

    /**
     * On a long click delete the selected item
     */
    public AdapterView.OnItemLongClickListener myClickListener = new AdapterView.OnItemLongClickListener() {
        @Override
        public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
                                       int arg2, long arg3) {
            // Creating a new alert dialog to confirm the delete
            AlertDialog alert = new AlertDialog.Builder(Others.this)
                    .setTitle("Delete Record?")
                    .setPositiveButton("Ok",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,
                                                    int whichButton) {
                                    // Deleting it from the ArrayList<string>

                                    // dialog.dismiss();
                                }
                            })

                    .setNegativeButton("Cancel",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,
                                                    int whichButton) {
                                    // When you press cancel, just close the
                                    // dialog
//                                    dialog.cancel();
                                }
                            }).show();

            return false;
        }
    };






}