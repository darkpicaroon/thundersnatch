/*
 * Filename: SplashScreen.java
 * 
 * Description: This file represents a splash screen for our application. When
 *              you first open up the application, this screen will appear on
 *              the screen. After 2 seconds, the splash screen will exit and you
 *              will be brought to the login screen.
 */

package com.thundersnatch;

import android.os.Bundle;
import android.os.Handler;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.Window;
import android.view.WindowManager;

public class SplashScreen extends Activity {

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
        		WindowManager.LayoutParams.FLAG_FULLSCREEN);
        
        setContentView(R.layout.activity_splash_screen);
        
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
 
            public void run() {
 
                finish();
 
                Intent intent = new Intent(SplashScreen.this, LoginScreen.class);
                SplashScreen.this.startActivity(intent);
 
            }
 
        }, 2000);
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_splash_screen, menu);
        return true;
    }
}
