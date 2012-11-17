package com.thundersnatch;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.Window;
import android.view.WindowManager;

public class GameLobby extends Activity {
	
	private int UserID;
	private double xPos;
	private double yPos;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Sets the activity to fullscreen.
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
        		WindowManager.LayoutParams.FLAG_FULLSCREEN);
        
        setContentView(R.layout.activity_game_lobby);
        
        Bundle extras = this.getIntent().getExtras();
        UserID = extras.getInt("UserID");
        xPos = extras.getDouble("xPos");
        yPos = extras.getDouble("yPos");
        
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_game_lobby, menu);
        return true;
    }
}
