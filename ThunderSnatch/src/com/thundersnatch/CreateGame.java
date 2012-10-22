/*
 * Filename: CreateGame.java
 * 
 * Description: This represents the screen in which a user creates a game lobby.
 *              They select the options that they want and then click the "Create
 *              Game" button. This will set up the game. If they chose to manually
 *              set flags, they will then be given the option to do so. Otherwise,
 *              they will be brought to the game lobby where they will await other
 *              players.
 */

package com.thundersnatch;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

public class CreateGame extends Activity {

	private TextView seekValue;
	private SeekBar seekbar;
	private int mapRadius = 100;
	
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);       
        
        // Sets the activity to fullscreen.
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
        		WindowManager.LayoutParams.FLAG_FULLSCREEN);
        
        setContentView(R.layout.activity_create_game);
        
        seekValue = (TextView)findViewById(R.id.textView4);
        
        seekbar = (SeekBar)findViewById(R.id.seekBar1);
        seekbar.setOnSeekBarChangeListener(new OnSeekBarChangeListener(){

			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				
				progress += 50;
				mapRadius = progress;
				char[] num = Integer.toString(progress).toCharArray();
				//Integer.toString(progress);
				seekValue.setText(num, 0, num.length);
				
			}

			public void onStartTrackingTouch(SeekBar arg0) { }

			public void onStopTrackingTouch(SeekBar arg0) { }
        	
        });
        
        // Sets up the "Create Game" button handler.
        Button createGame = (Button)findViewById(R.id.button1);
        createGame.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				
				// Should all previous menus be closed at this point?
				finish();
				
				Intent intent = new Intent(CreateGame.this, GameLobby.class);
	            CreateGame.this.startActivity(intent);
				
			}
		});
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_create_game, menu);
        return true;
    }
}
