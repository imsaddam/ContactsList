package com.example.android.contactslist;

import android.app.Activity;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;

import com.example.android.contactslist.ui.ContactsListActivity;


public class MainSplashScreen extends Activity {

    MediaPlayer oursong;
    @Override
    protected void onCreate(Bundle ankursingh) {
        // TODO Auto-generated method stub
        super.onCreate(ankursingh);
        setContentView(R.layout.main_spalsh_screen);
        oursong = MediaPlayer.create(MainSplashScreen.this, R.raw.song);
        oursong.start();



        new Handler().postDelayed(new Runnable() {

            // Using handler with postDelayed called runnable run method

            @Override
            public void run() {
                Intent i = new Intent(MainSplashScreen.this, ContactsListActivity.class);
                startActivity(i);

                // close this activity
                finish();
            }
        }, 5*1000); // wait for 5 seconds

    }


    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
        oursong.release();
        finish();
    }


}

