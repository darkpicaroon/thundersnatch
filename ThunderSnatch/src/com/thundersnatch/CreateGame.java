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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.URI;
import java.util.ArrayList;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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

	private String newGameURL = "http://www.rkaneda.com/StartNewGame.php";

	private TextView maxRadiusText;
	private SeekBar radiusSeek;
	private int mapRadius = 100;

	private TextView maxPlayersText;
	private SeekBar playersSeek;
	private int maxPlayers = 5;

	private SharedPreferences settings;
	private SharedPreferences.Editor editor;

	Bundle extras;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		extras = this.getIntent().getExtras();

		// Sets the activity to fullscreen.
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);

		setContentView(R.layout.activity_create_game);

		settings = getApplicationContext().getSharedPreferences(
				"com.thundersnatch", Context.MODE_PRIVATE);
		editor = settings.edit();

		// Sets up the max radius' changing TextView and its
		// corresponding SeekBar.
		maxRadiusText = (TextView) findViewById(R.id.textView4);

		radiusSeek = (SeekBar) findViewById(R.id.seekBar1);
		radiusSeek.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {

				progress += 5;
				mapRadius = progress;
				char[] num = Integer.toString(progress * 10).toCharArray();
				maxRadiusText.setText(num, 0, num.length);

			}

			public void onStartTrackingTouch(SeekBar arg0) {
			}

			public void onStopTrackingTouch(SeekBar arg0) {
			}

		});

		// Sets up the max players' changing TextView and its
		// corresponding SeekBar.
		maxPlayersText = (TextView) findViewById(R.id.textView6);

		playersSeek = (SeekBar) findViewById(R.id.seekBar2);
		playersSeek.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {

				progress += 1;
				maxPlayers = progress;
				char[] num = Integer.toString(progress).toCharArray();
				maxPlayersText.setText(num, 0, num.length);

			}

			public void onStartTrackingTouch(SeekBar arg0) {
			}

			public void onStopTrackingTouch(SeekBar arg0) {
			}

		});

		// Sets up the "Create Game" button handler.
		Button createGame = (Button) findViewById(R.id.button1);
		createGame.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {

				// Should all previous menus be closed at this point?
				JSONObject response = serverShit();
				int userGameID;
				int userTeamID;
				int gameID;

				finish();

				Intent intent = new Intent(CreateGame.this, GameLobby.class);
				try {
					gameID = response.getInt("gameID");
					editor.putInt("GameID", gameID);
					userGameID = response.getInt("userGameID");
					editor.putInt("UserGameID", userGameID);
					userTeamID = response.getInt("userTeamID");
					editor.putInt("TeamID", userTeamID);
					int teamIndex = response.getInt("teamIndex");
					editor.putInt("TeamIndex", teamIndex);

					if (teamIndex == 0)
						editor.putString("TeamColor", "blue");
					else
						editor.putString("TeamColor", "red");

					editor.commit();

				} catch (Exception e) {
					e.printStackTrace();
				}
				CreateGame.this.startActivity(intent);

			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_create_game, menu);
		return true;
	}

	private JSONObject serverShit() {

		// Create a HTTPClient as the form container
		HttpClient httpclient = new DefaultHttpClient();

		// Create an array list for the input data to be sent
		ArrayList<NameValuePair> nameValuePairs;

		// Create a HTTP Response and HTTP Entity
		HttpResponse response;
		HttpEntity entity;

		int userID = settings.getInt("userID", -1);
		if (userID == -1)
			System.out.println("sp error");
		float lng = settings.getFloat("Longitude", -1);
		float lat = settings.getFloat("Latitude", -1);

		// run http methods
		try {

			// Use HTTP POST method
			URI uri = new URI(newGameURL);
			HttpPost httppost = new HttpPost(uri);// this is where the address
													// to the php file goes

			// place credentials in the array list
			nameValuePairs = new ArrayList<NameValuePair>();

			nameValuePairs.add(new BasicNameValuePair("userID", "" + userID));
			nameValuePairs.add(new BasicNameValuePair("gameType", "" + 0));
			nameValuePairs.add(new BasicNameValuePair("duration", "" + 10));
			nameValuePairs.add(new BasicNameValuePair("maxPlayers", ""
					+ (maxPlayers) * 2));
			double dub = ((double) mapRadius) / 364320.0;
			BigDecimal mapR = new BigDecimal(dub);
			nameValuePairs.add(new BasicNameValuePair("gameRadius", "" + mapR));
			System.out.println("" + mapR);
			nameValuePairs.add(new BasicNameValuePair("xPos", "" + lng));
			nameValuePairs.add(new BasicNameValuePair("yPos", "" + lat));
			nameValuePairs.add(new BasicNameValuePair("gamestatus", "" + 0));

			// Add array list to http post
			httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

			// assign executed form container to response
			response = httpclient.execute(httppost);

			// check status code, need to check status code 200
			if (response.getStatusLine().getStatusCode() == 200) {

				// assign response entity to http entity
				entity = response.getEntity();

				// check if entity is not null
				if (entity != null) {
					// Create new input stream with received data assigned
					InputStream instream = entity.getContent();

					// Create new JSON Object. assign converted data as
					// parameter.
					JSONObject jsonResponse = new JSONObject(
							convertStreamToString(instream));
					return jsonResponse;

				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	private static String convertStreamToString(InputStream is) {
		/*
		 * To convert the InputStream to String we use the
		 * BufferedReader.readLine() method. We iterate until the BufferedReader
		 * return null which means there's no more data to read. Each line will
		 * appended to a StringBuilder and returned as String.
		 */
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		StringBuilder sb = new StringBuilder();

		String line = null;
		try {
			while ((line = reader.readLine()) != null) {
				sb.append(line + "\n");
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				is.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return sb.toString();
	}
}
