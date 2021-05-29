package com.modcom.meditest;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;

import com.google.gson.JsonObject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.View;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

import helpers.StoreDatabase;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit_api.IRetrofit;
import services.ServiceGenerator;

import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.content.DialogInterface;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.app.AlertDialog;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

public class Cart extends AppCompatActivity {
    ArrayList<HashMap<String, String>> userList;
    BaseAdapter adapter;
    TextView ids;


    private StoreDatabase dbHelper;
    ListView listView;
    SharedPreferences shared;
    String email_pref;
    String password_pref;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        shared = getSharedPreferences("mediprefs", MODE_PRIVATE);

        email_pref = shared.getString("email","");
        password_pref = shared.getString("password","");

        dbHelper = new StoreDatabase(getApplicationContext());
        try {
            dbHelper.open();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        int total = dbHelper.getTotalItemsCount();
        int num = dbHelper.getCartItemsRowCount(true);


        int amount = dbHelper.getAmount();
        BigDecimal priceVal;
//        if (total == num){
//            double tAmount = amount - (0.2 * amount);
//            priceVal = BigDecimal.valueOf((long) tAmount, 2);
//        } else {
//            priceVal = BigDecimal.valueOf(amount, 2);
//        }

        TextView numItemsBought = (TextView)findViewById(R.id.cart);
        numItemsBought.setText(num+" items");

        TextView totalAmount = (TextView)findViewById(R.id.total);
        totalAmount.setText("Total Amount: KES "+amount);

//
//        DBHelper helper = new DBHelper(getApplicationContext());
//        ArrayList<HashMap<String, String>> aa = helper.GetUsers();
//        //Log.d("data2", aa.toString());
//        Button total = findViewById(R.id.proceed);
//        Button next = findViewById(R.id.next);
//        next.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                startActivity(new Intent(getApplicationContext(), UserrReg.class));
//            }
//        });
//       // total.setText("Your Cart is Empty");
//
//
//        db = new DBHelper(this);
//        userList = db.GetUsers();
//
//        int res  = userList.size();
//        if(res == 1){
//            Toast.makeText(this, "Your Cart is Empty, Please Select More Services", Toast.LENGTH_SHORT).show();
//            lv = (ListView) findViewById(R.id.user_list);
//            adapter = new SimpleAdapter(this, userList, R.layout.list_row, new String[]{"id", "test_name", "test_price"}, new int[]{R.id.name, R.id.designation, R.id.price});
//            lv.setAdapter(adapter);
//            lv.setOnItemLongClickListener(myClickListener);
//            total.setText(R.string.message);
//
//        }
//
//        else {
//            //Toast.makeText(this, "Ok"+userList.size(), Toast.LENGTH_SHORT).show();
//            lv = (ListView) findViewById(R.id.user_list);
//            adapter = new SimpleAdapter(this, userList, R.layout.list_row, new String[]{"id", "test_name", "test_price"}, new int[]{R.id.name, R.id.designation, R.id.price});
//            lv.setAdapter(adapter);
//            lv.setOnItemLongClickListener(myClickListener);
//            total.setText("TOTAL KES 3856/=");
//        }


        AppCompatButton checkout = findViewById(R.id.checkout);
        checkout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
              if(email_pref.equals("") && password_pref.equals("")) {
                  startActivity(new Intent(getApplicationContext(), UserLogin.class));

              }

              else{
                 onPostClicked(email_pref, password_pref, Cart.this);
              }
            }
        });


        LinearLayout cart = (LinearLayout)findViewById(R.id.linearLayout);
        assert cart != null;
        displayListView();
    }

    SimpleCursorAdapter dataAdapter;
    Cursor cursor;
    private void displayListView() {
        cursor = dbHelper.fetchAllItems(); // 1 is used to denote an item in the shopping cart
        // Display name of item to be bought
        String[] columns = new String[] {
                StoreDatabase.KEY_NAME, StoreDatabase.KEY_PRICE
        };

        // the XML defined view which the data will be bound to
        int[] to = new int[] {
                R.id.name,R.id.price
        };

        // create the adapter using the cursor pointing to the desired data
        //as well as the layout information
        dataAdapter = new SimpleCursorAdapter(
                this, R.layout.item_layout,
                cursor,
                columns,
                to,
                0);

        listView = (ListView) findViewById(R.id.listView);
        ids = findViewById(R.id.id);
        // Assign adapter to ListView
        assert listView != null;
        listView.setAdapter(dataAdapter);
        listView.setOnItemLongClickListener(myClickListener);
    }

    /**
     * On a long click delete the selected item
     */
    public OnItemLongClickListener myClickListener = new OnItemLongClickListener() {
        @Override
        public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
                                       int arg2, long arg3) {
            // Creating a new alert dialog to confirm the delete
            AlertDialog alert = new AlertDialog.Builder(Cart.this)
                    .setTitle("Delete Record?")
                    .setPositiveButton("Ok",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,
                                                    int whichButton) {
                                    // Deleting it from the ArrayList<string>
                                    // property which is linked to our adapter
                                    int idSpeaker = cursor.getInt(cursor.getColumnIndex("_id"));
                                  //  Toast.makeText(Cart.this, ""+idSpeaker, Toast.LENGTH_SHORT).show();
                                    dbHelper.deleteContact(String.valueOf(idSpeaker));
                                    //lv.setAdapter(adapter);
                                    listView.invalidate();
                                    dataAdapter.notifyDataSetChanged();
                                    startActivity(new Intent(getApplicationContext(), Cart.class));
                                   // dialog.dismiss();
                                    finish();

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

    @Override
    protected void onResume() {
        super.onResume();
        shared = getSharedPreferences("mediprefs", MODE_PRIVATE);
        email_pref = shared.getString("email","");
        password_pref = shared.getString("password","");


        dbHelper = new StoreDatabase(getApplicationContext());
        try {
            dbHelper.open();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        int total = dbHelper.getTotalItemsCount();
        int num = dbHelper.getCartItemsRowCount(true);

        int amount = dbHelper.getAmount();


        TextView numItemsBought = (TextView)findViewById(R.id.cart);
        numItemsBought.setText(num+" items");

        TextView totalAmount = (TextView)findViewById(R.id.total);
        totalAmount.setText("Total Amount: KES "+amount);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_cart, menu);
        // Associate searchable configuration with the SearchView
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if (item.getItemId() == android.R.id.home){
            Intent intent = new Intent(getApplicationContext(), ServicesPage.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        }



        if (item.getItemId() == R.id.action_clear){

            dbHelper.deleteAllItems();
            Intent intent = new Intent(getApplicationContext(), ServicesPage.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        }


        return super.onOptionsItemSelected(item);
    }


    public void onPostClicked(String email_input, String password_input, Activity context) {


        try{
        if (email_input.equals("") || password_input.equals("")) {
            startActivity(new Intent(getApplicationContext(), UserLogin.class));
            finish();
        } else {

            ProgressDialog dialog = new ProgressDialog(Cart.this);
            dialog.setTitle("Logging in..");
            dialog.setMessage("Please wait..");
            dialog.setMax(100);
            dialog.setCancelable(false);
            dialog.setCanceledOnTouchOutside(false);
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
                                    Toast.makeText(Cart.this, "Email Error!", Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(getApplicationContext(), UserLogin.class));
                                    finish();
                                }
                                if (dataArray.has("password")) {
                                    Toast.makeText(Cart.this, "Password Error!", Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(getApplicationContext(), UserLogin.class));
                                    finish();
                                }

                            }

                        } else if (code == 500) {

                            Toast.makeText(Cart.this, obj.getString("message"), Toast.LENGTH_SHORT).show();
                        } else if (code == 200) {
                            startActivity(new Intent(getApplicationContext(), NewHome.class));
                            finish();
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

                            //Toast.makeText(Cart.this, "Login  Successfully", Toast.LENGTH_SHORT).show();


                        } else {
                            Toast.makeText(Cart.this, "Error Occured! try Again", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(getApplicationContext(), UserLogin.class));
                            finish();

                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }

                @Override
                public void onFailure(Call<JsonObject> call, Throwable t) {
                    Log.d("response-failure", call.toString());
                    Toast.makeText(Cart.this, "Check Your internet Connection and try again", Toast.LENGTH_LONG).show();
                    startActivity(new Intent(getApplicationContext(), UserLogin.class));
                    finish();
                    dialog.dismiss();
                }
            });
        }

        }
        catch (Exception e){
            Toast.makeText(this, "Server Error!, try again", Toast.LENGTH_SHORT).show();
        }
    }
}