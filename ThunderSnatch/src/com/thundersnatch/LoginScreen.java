/*
 * Filename: LoginScreen.java
 * 
 * Description: The login screen is where the user will log in to his/her account.
 *              They will first enter their username and password, if they have one,
 *              and then they will click the "Login" button. If they want the
 *              application to remember their login credentials, they will simply
 *              tick the "Remember Me" checkbox. It will then contact the server to
 *              verify their credentials. If they are valid, they will be sent to the
 *              main menu. If not, they will be presented with an error message.
 *              If the user doesn't have an account, or wants to create a new one,
 *              they can click the link at the bottom to be taken to the account
 *              creation screen.
 */

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
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

public class LoginScreen extends Activity {
	
	private String loginURL = "http://www.rkaneda.com/Login.php";

	
	private TextView errorMsg;
    private EditText username;
    private EditText password;
    private CheckBox rememberMe;
    private Button login;
    
    private SharedPreferences settings;
    private SharedPreferences.Editor editor;
    
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Sets the activity to fullscreen.
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
        		WindowManager.LayoutParams.FLAG_FULLSCREEN);
        
        setContentView(R.layout.activity_login_screen);
        
        username = (EditText)findViewById(R.id.editText1);
        password = (EditText)findViewById(R.id.editText2);
        rememberMe = (CheckBox)findViewById(R.id.checkBox1);
        errorMsg = (TextView)findViewById(R.id.textView5);
        
        settings = getApplicationContext().getSharedPreferences("com.thundersnatch", Context.MODE_PRIVATE);
        editor = settings.edit();
        
        // If the user chose the "Remember Me" option last time,
        // their account information should be loaded.
        if (settings.getBoolean("remember_me", false))
        {
        	username.setText(settings.getString("username", ""));
        	password.setText(settings.getString("password", ""));
        	rememberMe.setChecked(true);
        }
        
        // Makes the "Login" button clickable.
        login = (Button)findViewById(R.id.button1);
        login.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				
				String usernameText = username.getText().toString();
				String passwordText = password.getText().toString();
				
				
				boolean enableLogin = verifyCredentials(usernameText, passwordText);
				
				
				// If the user has the "Remember Me" CheckBox checked,
				// their login information is stored on the phone.
				if (rememberMe.isChecked())
				{
					editor.putBoolean("remember_me", true);
					editor.putString("username", usernameText);
					// Wasn't sure if we just want to store a hashed value
					// of the password or not but we can always fix that
					// later.
					editor.putString("password", passwordText);
					editor.commit();
					
				}
				
				if(enableLogin){
					finish();
					
					Intent intent = new Intent(LoginScreen.this, MainMenu.class);
		            LoginScreen.this.startActivity(intent);
				}
				
			}
		});
        
        // Makes the create account text a link.
        TextView createAccount = (TextView)findViewById(R.id.textView3);
        createAccount.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				
				Intent intent = new Intent(LoginScreen.this, CreateAccount.class);
	            LoginScreen.this.startActivity(intent);
								
			}
		});
    }

    
    
    
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_login_screen, menu);
        return true;
    }
    
    
    
    
    
    private boolean verifyCredentials(String username, String password) {
        
    	if((username.equals("") || password.equals(""))){
    		
    		//admin debugger should always be allowed in
        	if(username.equals("admin") && password.equals("admin")){
        		return true;
        	}
        	
        	//send info to database to check validity
        	else{
        		//Create a HTTPClient as the form container
                HttpClient httpclient = new DefaultHttpClient();
                
                //Create an array list for the input data to be sent
                ArrayList<NameValuePair> nameValuePairs;
                
                //Create a HTTP Response and HTTP Entity
                HttpResponse response;
                HttpEntity entity;
               
        		
                //run http methods
        		try {
        			//Use HTTP POST method
                    URI uri = new URI(loginURL);
                    HttpPost httppost = new HttpPost(uri);//this is where the address to the php file goes
                    
        			//place credentials in the array list
        			nameValuePairs = new ArrayList<NameValuePair>();
                    nameValuePairs.add(new BasicNameValuePair("userName", username));
                    nameValuePairs.add(new BasicNameValuePair("password", password));
        			
    			    //Add array list to http post
    			    httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
    			   
    			    //assign executed form container to response
    			    response = httpclient.execute(httppost);
    			    
    			    //check status code, need to check status code 200
    			    if(response.getStatusLine().getStatusCode()== 200){
        			    
        			   //assign response entity to http entity
        			   entity = response.getEntity();
        			    
        			   //check if entity is not null
        			   if(entity != null){
	        			     //Create new input stream with received data assigned
	        			     InputStream instream = entity.getContent();
	        			     
	        			     //Create new JSON Object. assign converted data as parameter.
	        			     JSONObject jsonResponse = new JSONObject(convertStreamToString(instream));
	        			     
	        			     //assign json responses to local strings
	        			     boolean isValid = jsonResponse.getBoolean("IsValid");
	        			     
	        			     if(isValid){
	        	        			//credentials are valid
	        	        			return true;
	        	        		}
        	        		 else{
	        	        			//credentials are invalid
        	        			 	errorMsg.setText("Invalid login credentials, please try again");
	        	        		 	return false;
        	        		 }
	        			     
        			    }
    			   }
    			} 
        		catch(Exception e){
    			   e.printStackTrace();
    			   System.err.println(e);
    			   errorMsg.setText("Unable to connect to server");
    			   return false;
    			}
        		errorMsg.setText("Internal error");
        		return false;
        	}
    	}
    	//will return an error message if nothing is entered
    	else{
    		errorMsg.setText("Please enter your username and password: admin/admin");
    		return false;
    	}
    }
    
    
    
    private static String convertStreamToString(InputStream is) {
        /*
         * To convert the InputStream to String we use the BufferedReader.readLine()
         * method. We iterate until the BufferedReader return null which means
         * there's no more data to read. Each line will appended to a StringBuilder
         * and returned as String.
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







