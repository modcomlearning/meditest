package com.modcom.meditest;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

//this page is accessed when some one logs in
public class NewHome extends AppCompatActivity {
    SharedPreferences shared;
    Button btn_self;
    Button btn_others;
    String email_pref;
    String password_pref;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_home);
        //create/get prefs
        shared = getSharedPreferences("mediprefs", MODE_PRIVATE);

        btn_self  = findViewById(R.id.btn_self);
        btn_others = findViewById(R.id.btn_others);
        btn_self.setOnClickListener(new View.OnClickListener() {
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

        btn_others.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //if others go to others activity
                Intent x = new Intent(getApplicationContext() , Others.class);
                startActivity(x);
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
            startActivity(new Intent(getApplicationContext(), NewMainPage.class));
            finish();
        }

        return super.onOptionsItemSelected(item);
    }
}