package com.thundersnatch;

import java.util.ArrayList;
import java.util.HashMap;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;

public class GameLobby extends Activity {
	
	private int UserID;
	private double xPos;
	private double yPos;
	private int teamSize;
	private int mapRadius;
	
	private boolean host;
	
	private static final int ADD_ITEM_ID = 1;
	
	private ArrayList<HashMap<String,String>> redTeam = new ArrayList<HashMap<String,String>>();
	private SimpleAdapter redTeamAdapter;
	
	private ArrayList<HashMap<String,String>> blueTeam = new ArrayList<HashMap<String,String>>();
	private SimpleAdapter blueTeamAdapter;
	
	private ListView redListView;
	private ListView blueListView;
	
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
        
        if (extras.getInt("Host") == 1)
        {
        	teamSize = extras.getInt("TeamSize");
        	mapRadius = extras.getInt("Radius");
        	host = true;
        }
        else
        	host = false;
        
        redListView = (ListView)findViewById(R.id.listView1);
        blueListView = (ListView)findViewById(R.id.listView2);
        		
        redTeamAdapter = new SimpleAdapter( 
				this, 
				redTeam,
				R.layout.red_team_item,
				new String[] { "line1" },
				new int[] { R.id.text1 }  );
        
        blueTeamAdapter = new SimpleAdapter( 
				this, 
				blueTeam,
				R.layout.blue_team_item,
				new String[] { "line1" },
				new int[] { R.id.text1 }  );
        
        redListView.setAdapter(redTeamAdapter);
        blueListView.setAdapter(blueTeamAdapter);
        
        addRedPlayer("Scotty2Hotty");
        addRedPlayer("Scotty2Hotty");
        addRedPlayer("Scotty2Hotty");
        addRedPlayer("Scotty2Hotty");
        addRedPlayer("Scotty2Hotty");
        addRedPlayer("Scotty2Hotty");
        addRedPlayer("Scotty2Hotty");
        addRedPlayer("Scotty2Hotty");
        addBluePlayer("Faggot");  
        addBluePlayer("Faggot");  
        addBluePlayer("Faggot");  
        addBluePlayer("Faggot");  
        addBluePlayer("Faggot");  
        addBluePlayer("Faggot");  
        addBluePlayer("Faggot");  
        addBluePlayer("Faggot");
    }
    
    @Override
    public void onStop() {
        super.onStop();
    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.activity_game_lobby, menu);
//        return true;
//    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
      boolean result = super.onCreateOptionsMenu(menu);
      menu.add(0, ADD_ITEM_ID, Menu.NONE, R.string.add_item );
      return result;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch ( item.getItemId() ) {
          case ADD_ITEM_ID:
				addRedPlayer("fuck");
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void addRedPlayer(String player) {
  	  HashMap<String,String> item = new HashMap<String,String>();
  	  item.put("line1", player);
  	  redTeam.add(item);
        redTeamAdapter.notifyDataSetChanged();
  	}
    
    private void addBluePlayer(String player) {
  	  HashMap<String,String> item = new HashMap<String,String>();
  	  item.put("line1", player);
  	  blueTeam.add(item);
      blueTeamAdapter.notifyDataSetChanged();
  	}
}
