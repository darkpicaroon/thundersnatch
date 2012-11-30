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

import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class ForgotPassword extends Activity {

	private EditText username;
	private EditText emailAddress;
	private Button submit;
	
	private String forgotPasswordURL = "http://www.rkaneda.com/ForgotPassword.php";
	
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Sets the activity to fullscreen.
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
        		WindowManager.LayoutParams.FLAG_FULLSCREEN);
        
        setContentView(R.layout.activity_forgot_password);
        
        // Initializes pointers to the components.
        username = (EditText)findViewById(R.id.editText1);
        emailAddress = (EditText)findViewById(R.id.editText2);
        submit = (Button)findViewById(R.id.button1);
        
        submit.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				
				String usernameText = username.getText().toString();
				String emailAddressText = emailAddress.getText().toString();
				
				if (forgotPassword(usernameText, emailAddressText))
				{

					finish();
					
					Context context = getApplicationContext();
					CharSequence text = "Email sent successfully.";
					int duration = Toast.LENGTH_SHORT;
					
					Toast toast = Toast.makeText(context, text, duration);
					toast.show();
					
					Intent intent = new Intent(ForgotPassword.this, LoginScreen.class);
					ForgotPassword.this.startActivity(intent);
				}
				else {
					
				}
			}
		});
        
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_forgot_password, menu);
        return true;
    }
    
private boolean forgotPassword(String username, String email) {
        
    	if((!username.equals("") && !email.equals(""))){
    		
        		
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
                    URI uri = new URI(forgotPasswordURL);
                    HttpPost httppost = new HttpPost(uri);//this is where the address to the php file goes
                    
        			//place credentials in the array list
        			nameValuePairs = new ArrayList<NameValuePair>();
                    nameValuePairs.add(new BasicNameValuePair("userName", username));
                    nameValuePairs.add(new BasicNameValuePair("Email", email));
        			
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
	        			     
	        			     // Credentials are valid
	        			     if(isValid) {

								return true;
	        	        	}
        	        		 else {
	        	        			//credentials are invalid
        	        			 	//errorMsg.setText("Invalid login credentials, please try again.");
	        	        		 	return false;
        	        		 }
        			    }
    			   }
    			} 
        		catch(Exception e){
    			   e.printStackTrace();
    			   //errorMsg.setText("Unable to connect to server.");
    			   return false;
    			}
        		//errorMsg.setText("Internal error");
        		return false;
        	}
    	return false;
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
