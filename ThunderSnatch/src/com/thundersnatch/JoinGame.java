package com.thundersnatch;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.SimpleAdapter;
import android.widget.Toast;

public class JoinGame extends ListActivity {
	private ArrayList<HashMap<String, String>> gameList = new ArrayList<HashMap<String, String>>();
	private ArrayList<Integer> gameIdList = new ArrayList<Integer>();
	private static final int ADD_ITEM_ID = 1;
	private SimpleAdapter notes;

	private int userID;
	private float latitude;
	private float longitude;

	private String gameListURL = "http://www.rkaneda.com/GetGameList.php";
	private String joinGameURL = "http://www.rkaneda.com/JoinGame.php";

	private SharedPreferences settings;
	private SharedPreferences.Editor editor;

	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);

		settings = getApplicationContext().getSharedPreferences(
				"com.thundersnatch", Context.MODE_PRIVATE);
		editor = settings.edit();

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);

		setContentView(R.layout.activity_join_game);

		userID = settings.getInt("userID", 0);
		latitude = settings.getFloat("Latitude", 0);
		longitude = settings.getFloat("Longitude", 0);

		notes = new SimpleAdapter(this, gameList, R.layout.join_game_item2,
				new String[] { "line1", "line2" }, new int[] { R.id.text1,
						R.id.text2 });

		setListAdapter(notes);

		// Set up join game handler.
		this.getListView().setOnItemClickListener(new OnItemClickListener() {

			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				JSONObject response = joinGameServerCall(gameIdList
						.get(position));
				int error = -1;
				try {
					if (response.getBoolean("isValid")) {
						editor.putInt("TeamID", response.getInt("TeamID"));
						editor.putInt("UserGameID",
								response.getInt("UserGameID"));
						int teamIndex = response.getInt("TeamIndex");
						editor.putInt("TeamIndex", teamIndex);
						if (teamIndex == 0)
							editor.putString("TeamColor", "blue");
						else
							editor.putString("TeamColor", "red");
						editor.putInt("GameID", gameIdList.get(position));
						editor.commit();
						error = 0;
					} else
						error = 1;
				} catch (Exception e) {
					e.printStackTrace();
				}

				Context context = getApplicationContext();
				int duration = Toast.LENGTH_SHORT;
				CharSequence text;
				Toast toast;
				if (error == 0) {// enter lobby
					finish();

					Intent intent = new Intent(JoinGame.this, GameLobby.class);
					JoinGame.this.startActivity(intent);
				} else if (error == 1) {// lobby full
					text = "This lobby is full";
					toast = Toast.makeText(context, text, duration);
					toast.show();
				} else {// other error
					text = "Error!";
					toast = Toast.makeText(context, text, duration);
					toast.show();
				}
			}

		});

		getGameList();

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		boolean result = super.onCreateOptionsMenu(menu);
		menu.add(0, ADD_ITEM_ID, Menu.NONE, R.string.add_item);
		return result;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case ADD_ITEM_ID:
			addItem();
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	private void addItem() {
		HashMap<String, String> item = new HashMap<String, String>();
		item.put("line1", "Username");
		item.put("line2", "Distance: 5 ft");
		gameList.add(item);
		notes.notifyDataSetChanged();
	}

	private boolean getGameList() {

		// Create a HTTPClient as the form container
		HttpClient httpclient = new DefaultHttpClient();

		// Create an array list for the input data to be sent
		ArrayList<NameValuePair> nameValuePairs;

		// Create a HTTP Response and HTTP Entity
		HttpResponse response;
		HttpEntity entity;

		// run http methods
		try {

			// Use HTTP POST method
			URI uri = new URI(gameListURL);
			HttpPost httppost = new HttpPost(uri);// this is where the address
													// to the php file goes

			// place credentials in the array list
			nameValuePairs = new ArrayList<NameValuePair>();
			nameValuePairs.add(new BasicNameValuePair("xPos", "" + longitude));
			nameValuePairs.add(new BasicNameValuePair("yPos", "" + latitude));

			System.out.println("lat: " + latitude + "lon: " + longitude);

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

					// assign json responses to local strings
					boolean isValid = jsonResponse.getBoolean("isValid");

					// Response is valid.
					if (isValid) {

						JSONObject list = jsonResponse
								.getJSONObject("GameList");

						for (int i = 0; i < list.length(); i++) {
							JSONObject game = list.getJSONObject("Game" + i);
							HashMap<String, String> temp = new HashMap<String, String>();
							String username = game.getString("userName");
							int distance = (int) (game.getDouble("distance") * 364320);
							temp.put("line1", username);
							temp.put("line2", "Distance: " + distance + " ft");
							gameList.add(temp);
							gameIdList.add((int) game.getInt("gameID"));
						}

						notes.notifyDataSetChanged();
						return true;

					} else {
						// credentials are invalid
						// errorMsg.setText("Invalid login credentials, please try again.");
						return false;
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			// errorMsg.setText("Unable to connect to server.");
			return false;
		}

		return false;
	}

	private JSONObject joinGameServerCall(int gameID) {

		// Create a HTTPClient as the form container
		HttpClient httpclient = new DefaultHttpClient();

		// Create an array list for the input data to be sent
		ArrayList<NameValuePair> nameValuePairs;

		// Create a HTTP Response and HTTP Entity
		HttpResponse response;
		HttpEntity entity;

		// run http methods
		try {

			// Use HTTP POST method
			URI uri = new URI(joinGameURL);
			HttpPost httppost = new HttpPost(uri);// this is where the address
													// to the php file goes

			// place credentials in the array list
			nameValuePairs = new ArrayList<NameValuePair>();
			System.out.println("Join game server: " + userID);
			nameValuePairs.add(new BasicNameValuePair("userId", "" + userID));
			nameValuePairs.add(new BasicNameValuePair("gameId", "" + gameID));
			nameValuePairs.add(new BasicNameValuePair("xPos", ""
					+ settings.getFloat("Longitude", 0)));
			nameValuePairs.add(new BasicNameValuePair("yPos", ""
					+ settings.getFloat("Latitude", 0)));

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
