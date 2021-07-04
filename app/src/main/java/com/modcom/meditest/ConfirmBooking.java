package com.modcom.meditest;

import android.Manifest;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;

import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.chip.Chip;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.MenuItemCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SimpleCursorAdapter;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import adapters.RetrofitAdapter;
import adapters.RetrofitAdapter1;
import helpers.StoreDatabase;
import models.ModelRecycler;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit_api.IRetrofit;
import services.ServiceGenerator;

//this is the booking activity
public class ConfirmBooking extends AppCompatActivity {
    private RetrofitAdapter1 retrofitAdapter;
    private RecyclerView recyclerView;
    SharedPreferences shared;
    private StoreDatabase dbHelper;
    EditText date, time;
    int mHour, mMinute;
    Chip selectDate, selectTime;
    Button book;
    int[] myNum;
    ArrayList<Integer> arl;
    SharedPreferences.Editor editor;
    EditText book_phone, book_address;
    Chip location, upload;
    TextView test_n, test_p;
    AlertDialog.Builder builder;
    String booking_id;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm_booking);
        recyclerView = findViewById(R.id.recycler);
        upload = findViewById(R.id.btnUploadPres);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[] { Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE }, 0);
        }

        //get shared prefferences
        shared = getSharedPreferences("mediprefs", MODE_PRIVATE);
        //edit preferences
        editor  = shared.edit();

        book_phone = findViewById(R.id.book_phone);
        book_address = findViewById(R.id.book_address);
        location = findViewById(R.id.btnLocation);


//        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);
////        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                //go back on back arrow
//                Intent intent = new Intent(getApplicationContext(), Others.class);
//                //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
//                startActivity(intent);
//                finish();
//            }
//        });

//        CollapsingToolbarLayout toolBarLayout = (CollapsingToolbarLayout) findViewById(R.id.toolbar_layout);
//        toolBarLayout.setTitle(getTitle());

        //access sqlite to allow getting servcies stored in cart
        dbHelper = new StoreDatabase(getApplicationContext());
        try {
            dbHelper.open();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        boolean self = shared.getBoolean("self", false);
        //TextView booking_id = findViewById(R.id.booking_id);
        TextView patient_name = findViewById(R.id.patient_name);
        //TextView patient_relationship = findViewById(R.id.patient_relationship);
        //TextView title_relationship = findViewById(R.id.title_relationship);
        TextView laboratory_tests = findViewById(R.id.laboratory_tests);
        selectDate = findViewById(R.id.btnDatebooking);
        selectTime = findViewById(R.id.btnTimebooking);
//        date = findViewById(R.id.tvSelectedDateBooking);
//        time = findViewById(R.id.tvSelectedTimeBooking);
        book = findViewById(R.id.book);
        book.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            //here we post all data for booking

                String date_select = selectDate.getText().toString();
                String time_select = selectTime.getText().toString();
                String phone_select = book_phone.getText().toString();
                String book_select = book_address.getText().toString();
                String book_address1 = book_address.getText().toString();


                editor.putString("scheduled_date", date_select);
                editor.putString("scheduled_time", time_select);
                editor.putString("phone_select", phone_select);
                editor.putString("book_select", book_select);
                editor.apply();

                String lat = shared.getString("lat","");
                String lon = shared.getString("lon","");
                if (lat.equalsIgnoreCase("") || lon.equalsIgnoreCase("")){
                    Toast.makeText(ConfirmBooking.this, "Please Pin A Location", Toast.LENGTH_SHORT).show();
                }

                else if (date_select.equalsIgnoreCase("")){
                    Toast.makeText(ConfirmBooking.this, "Please Pick A date", Toast.LENGTH_SHORT).show();
                }

                else if (time_select.equalsIgnoreCase("")){
                    Toast.makeText(ConfirmBooking.this, "Please Pick Your Preffered time", Toast.LENGTH_SHORT).show();
                }

                else if (book_address1.equalsIgnoreCase("")){
                    Toast.makeText(ConfirmBooking.this, "Describe Your Current Location in the field provided", Toast.LENGTH_SHORT).show();
                }
                else {
                    onPostClicked();

                }
            }
        });


        String dependant_id = shared.getString("dependant_id","");
        String first_name = shared.getString("dep_first_name","");
        String first_name1 = shared.getString("first_name","");
        String last_name1 = shared.getString("last_name","");

        String relationship = shared.getString("relationship","");

        //disable some textview depending if self/others
        if (self){
            //booking_id.setVisibility(View.GONE);
            //patient_relationship.setVisibility(View.GONE);
            patient_name.setText(first_name1+" "+last_name1);
            //title_relationship.setVisibility(View.GONE);
        }

        else {
            //title_relationship.setVisibility(View.VISIBLE);
            //booking_id.setVisibility(View.VISIBLE);
            //patient_relationship.setVisibility(View.VISIBLE);
            patient_name.setText(first_name);
        }

        //DATA IS BEING STORED PERSISTENTLY ....  FROM THIOS CLASS PROCEED TO PAYMENT ACTIVITY

       // booking_id.setText(dependant_id);

        //patient_relationship.setText(relationship);

        int count = dbHelper.getTotalItemsCount();
        if (count < 1){
            laboratory_tests.setText("No Lab test available, please choose laboratory tests from the services list");
            TextView txtlocset = findViewById(R.id.txtlocset);
            txtlocset.setVisibility(View.GONE);
            selectDate.setVisibility(View.GONE);
            recyclerView.setVisibility(View.GONE);
            selectTime.setVisibility(View.GONE);
//            time.setVisibility(View.GONE);
//            date.setVisibility(View.GONE);
            book.setVisibility(View.GONE);
            location.setVisibility(View.GONE);
            book_phone.setVisibility(View.GONE);
            book_address.setVisibility(View.GONE);
            upload.setVisibility(View.GONE);

        }

        else {
            TextView txtlocset = findViewById(R.id.txtlocset);
            txtlocset.setVisibility(View.VISIBLE);
            book.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.VISIBLE);
            selectDate.setVisibility(View.VISIBLE);
            selectTime.setVisibility(View.VISIBLE);
//            time.setVisibility(View.VISIBLE);
//            date.setVisibility(View.VISIBLE);
            location.setVisibility(View.VISIBLE);
            book_phone.setVisibility(View.VISIBLE);
            book_address.setVisibility(View.VISIBLE);
            upload.setVisibility(View.VISIBLE);
            Cursor cursor = dbHelper.fetchAllItems();


            ArrayList<ModelRecycler> modelRecyclerArrayList = new ArrayList<>();
            int x = 0;
            cursor.moveToPosition(-1);
            while (cursor.moveToNext()) {
              //  laboratory_tests.append(cursor.getString(cursor.getColumnIndex("name")) + "@ KES " + cursor.getString(cursor.getColumnIndex("price")));
               // laboratory_tests.append("\n\n");
                x = x + 1;

                ModelRecycler modelRecycler = new ModelRecycler();
                modelRecycler.setName(x+"."+cursor.getString(cursor.getColumnIndex("name")));
                modelRecycler.setCity(cursor.getString(cursor.getColumnIndex("price")));
                modelRecyclerArrayList.add(modelRecycler);
                arl = new ArrayList<Integer>();
                arl.add(Integer.valueOf(cursor.getString(cursor.getColumnIndex("_id"))));
            }




            retrofitAdapter = new RetrofitAdapter1(this,modelRecyclerArrayList);
            recyclerView.setAdapter(retrofitAdapter);
            recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.VERTICAL, false));


            int amount = dbHelper.getAmount();
            laboratory_tests.append("Total: KES " + amount);
           // displayListView();

            editor.putString("amount", String.valueOf(amount));
            editor.putString("service_id", String.valueOf(arl));



            //Date Picker
            Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);

            selectDate.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    DatePickerDialog datePickerDialog = new DatePickerDialog(ConfirmBooking.this,
                            new DatePickerDialog.OnDateSetListener() {
                                @Override
                                public void onDateSet(DatePicker datePicker, int year, int monthOftheYear, int day) {
                                    //.setText(day + "/" + month + "/" + year);
                                    int month = monthOftheYear + 1;
                                    String formattedMonth = "" + month;
                                    String formattedDayOfMonth = "" + day;

                                    if (month < 10) {

                                        formattedMonth = "0" + month;
                                    }
                                    if (day < 10) {
                                        formattedDayOfMonth = "0" + day;
                                    }

                                    selectDate.setText(year + "-" + (formattedMonth) + "-" + formattedDayOfMonth);
                                }
                            }, year, month, dayOfMonth);
                    datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis());
                    datePickerDialog.show();
                }
            });

            // Get Current Time
            selectTime.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {


                    final Calendar c = Calendar.getInstance();
                    mHour = c.get(Calendar.HOUR_OF_DAY);
                    mMinute = c.get(Calendar.MINUTE);

                    // Launch Time Picker Dialog
                    TimePickerDialog timePickerDialog = new TimePickerDialog(ConfirmBooking.this,
                            new TimePickerDialog.OnTimeSetListener() {

                                @Override
                                public void onTimeSet(TimePicker view, int hourOfDay,
                                                      int minute) {
                                      String newtime =  ""+ minute;
                                      String newhour =  ""+ hourOfDay;
                                      if(minute < 10){
                                          newtime =  "0"+ minute;
                                      }

                                    if(hourOfDay < 10){
                                        newhour =  "0"+ hourOfDay;
                                    }

                                    selectTime.setText(newhour + ":" + newtime);
                                }
                            }, mHour, mMinute, false);

                    timePickerDialog.show();
                }

            });


            location.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    startActivity(new Intent(getApplicationContext(), Maps1Activity.class));
                }
            });
        }



        upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
 selectImage();
            }
        });
    }



    Menu customMenu;
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_test_desc, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        return super.onOptionsItemSelected(item);
    }

    private void selectImage() {
        final CharSequence[] options = { "Take Photo", "Choose from Gallery","Cancel" };
        AlertDialog.Builder builder = new AlertDialog.Builder(ConfirmBooking.this);
        builder.setTitle("Add Photo!");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (options[item].equals("Take Photo"))
                {
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    //File f = new File(android.os.Environment.getExternalStorageDirectory(), "temp.jpg");
                   // intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));
                    startActivityForResult(intent, 1);
                }
                else if (options[item].equals("Choose from Gallery"))
                {
                    Intent intent = new   Intent(Intent.ACTION_PICK,android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(intent, 2);
                }
                else if (options[item].equals("Cancel")) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        // check if the request code is same as what is passed  here it is 2
        if(requestCode==1)
        {
            Toast.makeText(this, "Photo Captured", Toast.LENGTH_LONG).show();

        }

        if(requestCode==2)
        {
            Toast.makeText(this, "Photo Captured", Toast.LENGTH_LONG).show();
        }
    }


    public void onPostClicked(){
        try {
            ProgressDialog dialog = new ProgressDialog(ConfirmBooking.this);
            dialog.setTitle("Sending");
            dialog.setMessage("Please wait..");
            dialog.setCancelable(false);
            dialog.setCanceledOnTouchOutside(false);
            dialog.setMax(100);
            dialog.show();

            //Get location
            //creating the json object to send
            String user_id = shared.getString("id","");
            String dependant_id = shared.getString("dependant_id","");

            boolean paid = false;
            boolean self = shared.getBoolean("self",false);
            String scheduled_date = shared.getString("scheduled_date","");
            String scheduled_time = shared.getString("scheduled_time","");
            // Toast.makeText(Payment.this, "Date: "+scheduled_date, Toast.LENGTH_LONG).show();
            //Toast.makeText(Payment.this, "Time: "+scheduled_time, Toast.LENGTH_LONG).show();
            String total_amount = shared.getString("amount", "");
            String phone = shared.getString("phone", "");
            String lat = shared.getString("lat", "");
            String lon = shared.getString("lon", "");
            String locality = shared.getString("locality", "");
            String admin_area = shared.getString("admin_area", "");
            String sub_admin_area = shared.getString("sub_admin_area", "");
            String book_select = shared.getString("book_select", "");
            String phone_select = shared.getString("phone_select", "");
            dbHelper = new StoreDatabase(getApplicationContext());
            try {
                dbHelper.open();
            } catch (SQLException e) {
                e.printStackTrace();
            }

            Cursor cursor = dbHelper.fetchAllItems();
            JsonArray citiesArray = new JsonArray();
            cursor.moveToPosition(-1);
            while (cursor.moveToNext()) {
                citiesArray.add(Integer.valueOf(cursor.getString(cursor.getColumnIndex("_id"))));

            }

            JsonArray depArray = new JsonArray();
            if(dependant_id.length()!=0) {
                depArray.add(Integer.parseInt(dependant_id));
            }


            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("user_id",user_id);
            jsonObject.add("dependant_id",depArray);
            jsonObject.add("service_id", citiesArray);
            //Log.d("array", citiesArray.toString());
            // Toast.makeText(this, "Array: "+citiesArray.toString(), Toast.LENGTH_SHORT).show();
            jsonObject.addProperty("self",self);
            jsonObject.addProperty("paid",paid);
            jsonObject.addProperty("scheduled_date",scheduled_date);
            jsonObject.addProperty("scheduled_time",scheduled_time);
            jsonObject.addProperty("address_desc",book_select);
            jsonObject.addProperty("phone",phone_select);
            jsonObject.addProperty("total_amount",  total_amount);
            jsonObject.addProperty("phone",  phone);
            jsonObject.addProperty("lat",  lat);
            jsonObject.addProperty("lon",  lon);
            jsonObject.addProperty("locality",  locality);
            jsonObject.addProperty("admin_area",  admin_area);
            jsonObject.addProperty("sub_admin_area",  sub_admin_area);

            ServiceGenerator serviceGenerator  = new ServiceGenerator(getApplicationContext());
            IRetrofit jsonPostService = serviceGenerator.createService(IRetrofit.class, IRetrofit.BASE_URL);
            Call<JsonObject> call = jsonPostService.postRawBook(jsonObject);
            call.enqueue(new Callback<JsonObject>() {

                @Override
                public void onResponse(@NotNull Call<JsonObject> call, @NotNull Response<JsonObject> response) {
                    try {
                        dialog.dismiss();
                        //Toast.makeText(Payment.this, "OK", Toast.LENGTH_SHORT).show();
                        //assert response.body() != null;
                        try {

                            if (response.isSuccessful()) {
                                if (response.body() != null) {

                                    //Log.d("hey", response.message());
                                    JSONObject obj = new JSONObject(response.body().toString());
                                    //Toast.makeText(Payment.this, response.body().toString(), Toast.LENGTH_SHORT).show();
                                    int code = Integer.parseInt((obj.getString("status_code")));
                                    if (code == 400) {
                                        //Toast.makeText(UserrReg.this, ""+code, Toast.LENGTH_SHORT).show();
                                        JSONObject dataArray = obj.getJSONObject("message");

                                        for (int i = 0; i < dataArray.length(); i++) {
                                            // JSONArray x = dataArray.getJSONArray("first_name");
                                            if (dataArray.has("scheduled_date")) {
                                                Toast.makeText(ConfirmBooking.this, dataArray.getJSONArray("scheduled_date").toString(), Toast.LENGTH_LONG).show();
                                            }

                                            if (dataArray.has("scheduled_time")) {
                                                Toast.makeText(ConfirmBooking.this, dataArray.getJSONArray("scheduled_time").toString(), Toast.LENGTH_LONG).show();
                                            }

                                            if (dataArray.has("total_amount")) {
                                                Toast.makeText(ConfirmBooking.this, dataArray.getJSONArray("total_amount").toString(), Toast.LENGTH_LONG).show();
                                                startActivity(new Intent(getApplicationContext(), ConfirmBooking.class));
                                                finish();
                                            }
                                            if (dataArray.has("lat")) {
                                                Toast.makeText(ConfirmBooking.this, dataArray.getJSONArray("lat").toString(), Toast.LENGTH_LONG).show();
                                            }

                                            if (dataArray.has("lon")) {
                                                Toast.makeText(ConfirmBooking.this, dataArray.getJSONArray("lon").toString(), Toast.LENGTH_LONG).show();
                                            }

                                            if (dataArray.has("lon")) {
                                                Toast.makeText(ConfirmBooking.this, dataArray.getJSONArray("lon").toString(), Toast.LENGTH_LONG).show();
                                            }

                                            if (dataArray.has("service_id")) {
                                                Toast.makeText(ConfirmBooking.this, dataArray.getJSONArray("service_id").toString(), Toast.LENGTH_LONG).show();
                                                startActivity(new Intent(getApplicationContext(), ConfirmBooking.class));
                                                finish();
                                            }


                                        }
                                    } else if (code == 200) {

                                        try {
                                            JSONObject dataArray  = obj.getJSONObject("booking");
                                            booking_id = dataArray.getString("id");
                                            SharedPreferences.Editor editor = shared.edit();
                                            editor.putString("booking_id",booking_id);
                                            editor.apply();


                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                            Toast.makeText(ConfirmBooking.this, "There was a server error, try again", Toast.LENGTH_SHORT).show();
                                        }

                                        android.app.AlertDialog alert = new android.app.AlertDialog.Builder(ConfirmBooking.this)
                                                .setTitle("Booking Successful, Press OK")
                                                .setPositiveButton("Ok",
                                                        new DialogInterface.OnClickListener() {
                                                            public void onClick(DialogInterface dialog,
                                                                                int whichButton) {

                                                                dbHelper.deleteAllItems();
                                                                startActivity(new Intent(getApplicationContext(), Payment.class));
                                                                dialog.dismiss();
                                                                finish();
                                                               // Toast.makeText(ConfirmBooking.this, "ID : "+booking_id, Toast.LENGTH_SHORT).show();

                                                            }
                                                        }).show();
                                        alert.setCanceledOnTouchOutside(false);
                                    } else {
                                        //Toast.makeText(Payment.this, "Error Occurred! Check your connection and try Again", Toast.LENGTH_LONG).show();
                                        //Uncomment the below code to Set the message and title from the strings.xml file
                                        builder.setMessage("Booking Status.").setTitle("Error");

                                        //Setting message manually and performing action on button click
                                        builder.setMessage("Error. Booking Failed, check Your Internet Connection")
                                                .setCancelable(false)
                                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                                    public void onClick(DialogInterface dialog, int id) {
                                                        startActivity(new Intent(getApplicationContext(), Updated.class));
                                                    }
                                                });
                                        //Creating dialog box
                                        AlertDialog alert = builder.create();
                                        //Setting the title manually
                                        alert.setTitle("Success");
                                        alert.show();

                                    }
                                    //here
                                } else {
                                    dialog.dismiss();
                                    Toast.makeText(ConfirmBooking.this, "Server Error!, Try again", Toast.LENGTH_SHORT).show();
                                    // Log.i("onEmptyResponse", "Returned empty response");//Toast.makeText(getContext(),"Nothing returned",Toast.LENGTH_LONG).show();
                                    //startActivity(new Intent(getApplicationContext(), NewMainPage.class));
                                    //finish();

                                }
                            }
                            else {
                                dialog.dismiss();
                                Toast.makeText(ConfirmBooking.this, "No Server Response, Try Again", Toast.LENGTH_SHORT).show();
                                //startActivity(new Intent(getApplicationContext(), NewMainPage.class));
                                //finish();
                            }

                        } catch (JSONException e) {
                            Toast.makeText(ConfirmBooking.this, "No Response from server", Toast.LENGTH_SHORT).show();
                        }

                    }
                    catch (NullPointerException e){
                        Toast.makeText(ConfirmBooking.this, "No Response from server, Booking Failed", Toast.LENGTH_SHORT).show();
                    }

                }

                @Override
                public void onFailure(Call<JsonObject> call, Throwable t) {
                    // Log.d("response-failure", call.toString());
                    Toast.makeText(ConfirmBooking.this, "Server Error, Check Your internet Connection and try again", Toast.LENGTH_LONG).show();
                    dialog.dismiss();
                }
            });
        }
        catch (Exception e){
            Toast.makeText(this, "Server Error!, try again", Toast.LENGTH_SHORT).show();
        }
    }


}