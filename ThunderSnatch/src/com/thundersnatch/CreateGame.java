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

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

public class CreateGame extends Activity {

	private TextView maxRadiusText;
	private SeekBar radiusSeek;
	private int mapRadius = 100;
	
	private TextView maxPlayersText;
	private SeekBar playersSeek;
	private int maxPlayers = 5;
	
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);       
        
        // Sets the activity to fullscreen.
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
        		WindowManager.LayoutParams.FLAG_FULLSCREEN);
        
        setContentView(R.layout.activity_create_game);
        
        // Sets up the max radius' changing TextView and its
        // corresponding SeekBar.
        maxRadiusText = (TextView)findViewById(R.id.textView4);
        
        radiusSeek = (SeekBar)findViewById(R.id.seekBar1);
        radiusSeek.setOnSeekBarChangeListener(new OnSeekBarChangeListener(){

			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				
				progress += 50;
				mapRadius = progress;
				char[] num = Integer.toString(progress).toCharArray();
				maxRadiusText.setText(num, 0, num.length);
				
			}

			public void onStartTrackingTouch(SeekBar arg0) { }

			public void onStopTrackingTouch(SeekBar arg0) { }
        	
        });
        
        // Sets up the max players' changing TextView and its
        // corresponding SeekBar.
        maxPlayersText = (TextView)findViewById(R.id.textView6);
        
        playersSeek = (SeekBar)findViewById(R.id.seekBar2);
        playersSeek.setOnSeekBarChangeListener(new OnSeekBarChangeListener(){

			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				
				progress += 1;
				maxPlayers = progress;
				char[] num = Integer.toString(progress).toCharArray();
				maxPlayersText.setText(num, 0, num.length);
				
			}

			public void onStartTrackingTouch(SeekBar arg0) { }

			public void onStopTrackingTouch(SeekBar arg0) { }
        	
        });
        
        // Sets up the "Create Game" button handler.
        Button createGame = (Button)findViewById(R.id.button1);
        createGame.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				
				// Should all previous menus be closed at this point?
				
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
