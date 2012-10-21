package com.thundersnatch;

import android.os.Bundle;
import android.os.Handler;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.Window;
import android.view.WindowManager;

public class SplashScreen extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
        		WindowManager.LayoutParams.FLAG_FULLSCREEN);
        
        setContentView(R.layout.activity_splash_screen);
        
        Handler handler = new Handler();
        
        // run a thread after 2 seconds to start the home screen
        handler.postDelayed(new Runnable() {
 
            public void run() {
 
                // make sure we close the splash screen so the user won't come back when it presses back key
 
                finish();
                // start the home screen
 
                Intent intent = new Intent(SplashScreen.this, LoginScreen.class);
                SplashScreen.this.startActivity(intent);
 
            }
 
        }, 2000);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_splash_screen, menu);
        return true;
    }
}
