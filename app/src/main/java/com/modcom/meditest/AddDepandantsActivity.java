package com.modcom.meditest;


import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;


import com.google.gson.JsonObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;

import retrofit_api.IRetrofit;
import services.ServiceGenerator;

public class AddDepandantsActivity extends AppCompatActivity {

    EditText first_name, sirname, last_name, address, relationship;
    RadioButton rbMale, rbfemale;
    Button btn_dep_create;
    EditText date;
    SharedPreferences shared;
    String user_id;
    String token;
    RadioGroup rg;
    Spinner spinnergender, spinrelationship;
    String array[] = {"male","female"};
    String rel [] = {"child","spouse","parent","extended","sibling","other"};
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_depandants);

        spinnergender = findViewById(R.id.spindepgender);
        ArrayAdapter<String> aa = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, array);
        spinnergender.setAdapter(aa);

        spinrelationship  = findViewById(R.id.relationship);
        ArrayAdapter<String> aaa = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, rel);
        spinrelationship.setAdapter(aaa);


        shared = getSharedPreferences("mediprefs", MODE_PRIVATE);
        user_id = shared.getString("id","");
        token = shared.getString("token","");

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), NewHome.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            }
        });


        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
        Button selectDate = findViewById(R.id.dep_btnDate);
        date = findViewById(R.id.dep_tvSelectedDate);

        selectDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatePickerDialog datePickerDialog = new DatePickerDialog(AddDepandantsActivity.this,android.R.style.Theme_Holo_Dialog,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker datePicker, int year, int monthOftheYear, int day) {
                                //.setText(day + "/" + month + "/" + year);
                                int month = monthOftheYear + 1;
                                String formattedMonth = "" + month;
                                String formattedDayOfMonth = "" + day;

                                if(month < 10){

                                    formattedMonth = "0" + month;
                                }
                                if(day < 10){

                                    formattedDayOfMonth = "0" + day;
                                }


                                date.setText(year + "-" + (formattedMonth) + "-" + formattedDayOfMonth);
                            }
                        }, year, month, dayOfMonth);
                datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
                datePickerDialog.show();
            }
        });

        first_name  = findViewById(R.id.dep_first_name);
        sirname  = findViewById(R.id.dep_sirname);
        last_name  = findViewById(R.id.dep_last_name);
        address  = findViewById(R.id.dep_address);


        btn_dep_create = findViewById(R.id.btn_dep_create);
        btn_dep_create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                onPostClicked();
            }
        });
    }
    RadioButton selectedRadioButton;
    String selectedRbText;

    public void onPostClicked(){
        try {

        String gender = spinnergender.getSelectedItem().toString();
        String relation = spinrelationship.getSelectedItem().toString();


        ProgressDialog dialog = new ProgressDialog(AddDepandantsActivity.this);
        dialog.setTitle("Sending");
        dialog.setMessage("Please wait..");
        dialog.setMax(100);
        dialog.show();


        //creating the json object to send
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("first_name",first_name.getText().toString());
        jsonObject.addProperty("sirname",sirname.getText().toString());
        jsonObject.addProperty("last_name",last_name.getText().toString());
        jsonObject.addProperty("gender",gender);
        jsonObject.addProperty("address",address.getText().toString());
        jsonObject.addProperty("dob",  date.getText().toString());
        jsonObject.addProperty("relationship",  relation);
        jsonObject.addProperty("user_id",  user_id);

        ServiceGenerator serviceGenerator  = new ServiceGenerator(getApplicationContext());
        IRetrofit jsonPostService = serviceGenerator.createService(IRetrofit.class, IRetrofit.BASE_URL);
        Call<JsonObject> call = jsonPostService.postRawAddDependants(jsonObject);
        call.enqueue(new Callback<JsonObject>() {

            @Override
            public void onResponse(@NotNull Call<JsonObject> call, @NotNull Response<JsonObject> response) {
                try{
                    dialog.dismiss();
                    //Toast.makeText(UserrReg.this, "OK"+response.body().toString(), Toast.LENGTH_SHORT).show();
                    //assert response.body() != null;
                    //Log.d("hey", response.body().toString());
                    if (response.isSuccessful()) {
                        if (response.body() != null) {
                    JSONObject obj = new JSONObject(response.body().toString());
                    //Toast.makeText(AddDepandantsActivity.this, response.body().toString(), Toast.LENGTH_SHORT).show();
                    int code   = Integer.parseInt((obj.getString("status_code")));

                    if (code==400) {
                        //Toast.makeText(UserrReg.this, ""+code, Toast.LENGTH_SHORT).show();
                        JSONObject dataArray  = obj.getJSONObject("message");

                        for (int i = 0; i < dataArray.length(); i++) {
                            // JSONArray x = dataArray.getJSONArray("first_name");
                            if (dataArray.has("first_name")) {
                                first_name.setError(dataArray.getJSONArray("first_name").toString());
                            }
                            if (dataArray.has("last_name")) {
                                last_name.setError(dataArray.getJSONArray("last_name").toString());
                            }

                            if (dataArray.has("sirname")) {
                                sirname.setError(dataArray.getJSONArray("sirname").toString());
                            }

                            if (dataArray.has("address")) {
                                address.setError(dataArray.getJSONArray("address").toString());
                            }

                            if (dataArray.has("dob")) {
                                date.setError(dataArray.getJSONArray("dob").toString());
                            }

                            if (dataArray.has("relationship")) {
                                Toast.makeText(AddDepandantsActivity.this, dataArray.getJSONArray("relationship").toString(), Toast.LENGTH_SHORT).show();
                            }

                            if (dataArray.has("gender")) {
                                Toast.makeText(AddDepandantsActivity.this, dataArray.getJSONArray("gender").toString(), Toast.LENGTH_SHORT).show();
                            }


                        }

                    }

                    else if(code==200){
                        Toast.makeText(AddDepandantsActivity.this, "Saved Successfully", Toast.LENGTH_SHORT).show();
                        first_name.setText("");
                        sirname.setText("");
                        last_name.setText("");
                        address.setText("");
                        date.setText("");
                        //relationship.setText("");
                        Intent x = new Intent(getApplicationContext(), Others.class);
                        x.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(x);
                        finish();
                    }

                    else {
                        Toast.makeText(AddDepandantsActivity.this, "Error Occurred! try Again", Toast.LENGTH_SHORT).show();
                        first_name.setText("");
                        sirname.setText("");
                        last_name.setText("");
                        address.setText("");
                        date.setText("");
                        //relationship.setText("");

                    }

                            //here
                        } else {
                            dialog.dismiss();
                            Toast.makeText(AddDepandantsActivity.this, "Server Error!, Try again", Toast.LENGTH_SHORT).show();
                            // Log.i("onEmptyResponse", "Returned empty response");//Toast.makeText(getContext(),"Nothing returned",Toast.LENGTH_LONG).show();
                            //startActivity(new Intent(getApplicationContext(), NewMainPage.class));
                            //finish();

                        }
                    }
                    else {
                        dialog.dismiss();
                        Toast.makeText(AddDepandantsActivity.this, "No Server Response, Try Again", Toast.LENGTH_SHORT).show();
                        //startActivity(new Intent(getApplicationContext(), NewMainPage.class));
                        //finish();
                    }


                }catch (JSONException e){
                    e.printStackTrace();
                    Toast.makeText(AddDepandantsActivity.this, "Check Your internet Connection and try again", Toast.LENGTH_LONG).show();
                }

            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                //Log.d("response-failure", call.toString());
                Toast.makeText(AddDepandantsActivity.this, "Check Your internet Connection and try again", Toast.LENGTH_LONG).show();
                dialog.dismiss();
            }
        });

        }
        catch (Exception e){
            Toast.makeText(this, "Server Error!, try again", Toast.LENGTH_SHORT).show();
        }
    }


}