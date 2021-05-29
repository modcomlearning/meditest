package com.modcom.meditest;


import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;
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

//this page is for sign up
public class UserrReg extends AppCompatActivity {

   EditText first_name, sirname, last_name, phone, password, password_confirmation, address, email;
   RadioButton rbMale, rbfemale;
   Button btn_signup;
   EditText date;
   Spinner spinnergender;
   String array[] = {"male","female"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_userr_reg);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //for back arrow
                Intent intent = new Intent(getApplicationContext(), UserLogin.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            }
        });

        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
        Button selectDate = findViewById(R.id.btnDate);
        date = findViewById(R.id.tvSelectedDate);
        spinnergender = findViewById(R.id.spingender);

        ArrayAdapter<String> aa = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, array);
        spinnergender.setAdapter(aa);


       //date fields
        selectDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatePickerDialog datePickerDialog = new DatePickerDialog(UserrReg.this,
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

        first_name  = findViewById(R.id.first_name);
        sirname  = findViewById(R.id.sirname);
        last_name  = findViewById(R.id.last_name);
        phone  = findViewById(R.id.phone);
        address  = findViewById(R.id.address);
        email  = findViewById(R.id.email);
        password  = findViewById(R.id.password);
        password_confirmation = findViewById(R.id.password_confirmation);
        btn_signup = findViewById(R.id.btn_signup);
        btn_signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onPostClicked();
            }
        });

        TextView link_login = findViewById(R.id.link_login);
        link_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), UserLogin.class));
            }
        });

    }
     String  gender;
    //this method is for posting to endpoint
    public void onPostClicked(){

        try {

            ProgressDialog dialog = new ProgressDialog(UserrReg.this);
            dialog.setTitle("Sending");
            dialog.setMessage("Please wait..");
            dialog.setMax(100);
            dialog.show();

            String gender = spinnergender.getSelectedItem().toString();
            //prepare data for posting
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("first_name", first_name.getText().toString());
            jsonObject.addProperty("sirname", sirname.getText().toString());
            jsonObject.addProperty("last_name", last_name.getText().toString());
            jsonObject.addProperty("gender", gender);
            jsonObject.addProperty("email", email.getText().toString());
            jsonObject.addProperty("password", password.getText().toString());
            jsonObject.addProperty("password_confirmation", password_confirmation.getText().toString());
            jsonObject.addProperty("phone", phone.getText().toString());
            jsonObject.addProperty("address", address.getText().toString());
            jsonObject.addProperty("dob", date.getText().toString());

            //use iretrofit to post data to end point
            ServiceGenerator serviceGenerator = new ServiceGenerator(getApplicationContext());
            IRetrofit jsonPostService = serviceGenerator.createService(IRetrofit.class, IRetrofit.BASE_URL);
            Call<JsonObject> call = jsonPostService.postRawJSON(jsonObject);
            call.enqueue(new Callback<JsonObject>() {

                @Override
                public void onResponse(@NotNull Call<JsonObject> call, @NotNull Response<JsonObject> response) {
                    try {
                        dialog.dismiss();
                        //handle JSON response
                        //Toast.makeText(UserrReg.this, "OK"+response.body().toString(), Toast.LENGTH_SHORT).show();
                        if (response.isSuccessful()) {
                            if (response.body() != null) {

                        JSONObject obj = new JSONObject(response.body().toString());
                        int code = Integer.parseInt((obj.getString("status_code")));

                        if (code == 400) {
                            //Toast.makeText(UserrReg.this, ""+code, Toast.LENGTH_SHORT).show();

                            JSONObject dataArray = obj.getJSONObject("message");

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

                                if (dataArray.has("email")) {
                                    email.setError(dataArray.getJSONArray("email").toString());
                                }
                                if (dataArray.has("password")) {
                                    password.setError(dataArray.getJSONArray("password").toString());
                                }
                                if (dataArray.has("phone")) {
                                    phone.setError(dataArray.getJSONArray("phone").toString());
                                }

                                if (dataArray.has("address")) {
                                    address.setError(dataArray.getJSONArray("address").toString());
                                }

                                if (dataArray.has("dob")) {
                                    date.setError(dataArray.getJSONArray("dob").toString());
                                }

                                if (dataArray.has("gender")) {
                                    Toast.makeText(UserrReg.this, dataArray.getJSONArray("gender").toString(), Toast.LENGTH_SHORT).show();
                                }


                            }


                        } else if (code == 200) {
                            Toast.makeText(UserrReg.this, "Saved Successfully", Toast.LENGTH_SHORT).show();
                            first_name.setText("");
                            sirname.setText("");
                            last_name.setText("");
                            email.setText("");
                            phone.setText("");
                            address.setText("");
                            date.setText("");
                            password.setText("");
                            password_confirmation.setText("");
                            startActivity(new Intent(getApplicationContext(), UserLogin.class));
                            finish();
                        } else {
                            Toast.makeText(UserrReg.this, "Error Occurred! try Again", Toast.LENGTH_SHORT).show();
                            first_name.setText("");
                            sirname.setText("");
                            last_name.setText("");
                            email.setText("");
                            phone.setText("");
                            address.setText("");
                            date.setText("");
                            password.setText("");
                            password_confirmation.setText("");
                        }
//here
                                //here
                            } else {
                                dialog.dismiss();
                                Toast.makeText(UserrReg.this, "Server Error!, Try again", Toast.LENGTH_SHORT).show();
                                // Log.i("onEmptyResponse", "Returned empty response");//Toast.makeText(getContext(),"Nothing returned",Toast.LENGTH_LONG).show();
                                //startActivity(new Intent(getApplicationContext(), NewMainPage.class));
                                //finish();

                            }
                        }
                        else {
                            dialog.dismiss();
                            Toast.makeText(UserrReg.this, "No Server Response, Try Again", Toast.LENGTH_SHORT).show();
                            //startActivity(new Intent(getApplicationContext(), NewMainPage.class));
                            //finish();
                        }

                    } catch (JSONException e) {
                        //Toast.makeText(UserLogin.this, "Server Error, Check Your internet Connection and try again", Toast.LENGTH_LONG).show();
                        dialog.dismiss();
                        AlertDialog alert = new AlertDialog.Builder(UserrReg.this)
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
                    AlertDialog alert = new AlertDialog.Builder(UserrReg.this)
                            .setTitle("Please Check Your Network connections. Try Again")
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