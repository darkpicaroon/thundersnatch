package com.thundersnatch;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class CreateAccount extends Activity {

	private String signupURL = "http://www.rkaneda.com/SignUp.php";

	private Button createAccount;
	private EditText username;
	private EditText password;
	private EditText retypePassword;
	private EditText emailAddress;
	private TextView errorMessage;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Sets the activity to fullscreen.
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);

		setContentView(R.layout.activity_create_account);

		// Initializes pointers to the EditTexts
		username = (EditText) findViewById(R.id.editText1);
		password = (EditText) findViewById(R.id.editText2);
		retypePassword = (EditText) findViewById(R.id.editText3);
		emailAddress = (EditText) findViewById(R.id.editText4);

		// Initialize error message TextView
		errorMessage = (TextView) findViewById(R.id.textView5);

		// Sets up the "Create Account" button handler.
		createAccount = (Button) findViewById(R.id.button1);
		createAccount.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {

				String usernameText = username.getText().toString();
				String passwordText = password.getText().toString();
				String retypeText = retypePassword.getText().toString();
				String emailAddressText = emailAddress.getText().toString();

				if (passwordText.compareTo(retypeText) == 0
						&& passwordText.length() >= 6) {

					boolean success = createAccount(usernameText, passwordText,
							emailAddressText);

					if (success) {
						finish();

						Context context = getApplicationContext();
						CharSequence text = "Account created successfully.";
						int duration = Toast.LENGTH_SHORT;

						Toast toast = Toast.makeText(context, text, duration);
						toast.show();

						Intent intent = new Intent(CreateAccount.this,
								LoginScreen.class);
						CreateAccount.this.startActivity(intent);
					} else {
						Context context = getApplicationContext();
						CharSequence text = "Failed to create account. Please try again.";
						int duration = Toast.LENGTH_SHORT;

						Toast toast = Toast.makeText(context, text, duration);
						toast.show();
					}
				} else {

					AlertDialog.Builder builder = new AlertDialog.Builder(
							CreateAccount.this);
					builder.setCancelable(true);

					if (password.length() < 6)
						builder.setTitle("Your password needs to be atleast 6 characters.");
					else
						builder.setTitle("Your passwords don't match.");

					builder.setInverseBackgroundForced(true);
					builder.setPositiveButton("Ok",
							new DialogInterface.OnClickListener() {

								public void onClick(DialogInterface dialog,
										int which) {

									dialog.dismiss();

								}
							});

					AlertDialog alert = builder.create();
					alert.show();
				}
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_create_account, menu);
		return true;
	}

	private boolean createAccount(String username, String password,
			String emailAddress) {

		if (!username.equals("") && !password.equals("")
				&& !emailAddress.equals("")) {

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
				URI uri = new URI(signupURL);
				HttpPost httppost = new HttpPost(uri);

				// place credentials in the array list
				nameValuePairs = new ArrayList<NameValuePair>();
				nameValuePairs
						.add(new BasicNameValuePair("userName", username));
				nameValuePairs
						.add(new BasicNameValuePair("password", password));
				nameValuePairs.add(new BasicNameValuePair("confirmPassword",
						password));
				nameValuePairs
						.add(new BasicNameValuePair("email", emailAddress));

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
						boolean isValid = jsonResponse.getBoolean("IsValid");

						// Credentials are valid
						if (isValid) {

							String reply = jsonResponse
									.getString("ReturnMessage");

							if (reply
									.compareTo("User account created successfully!") != 0) {
								errorMessage.setText(reply);

								return false;
							}

							return true;

						} else {
							// credentials are invalid
							return false;
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
				// errorMsg.setText("Unable to connect to server.");
				return false;
			}
			// errorMsg.setText("Internal error");
			return false;

		}
		// will return an error message if nothing is entered
		else
			return false;
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
