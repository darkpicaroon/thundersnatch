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

import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

public class LoginScreen extends Activity {

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
				
				// PARSE JSON REQUEST TO DETERMINE IF CREDENTIALS WERE CORRECT
				// Could be done in verifyCredentials method. In that case,
				// we could make it return a boolean.
				
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
    	//send info to database
    	if(!username.equals("") != !password.equals("")){//xor
    		//admin debugger should always be allowed in
        	if(username.equals("admin") && password.equals("admin")){
        		return true;
        	}
        	else{
        		//send info to database to check validity
        		
        		
        		
        		
        		
        		if(false){
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
    	//will return an error message if nothing is entered
    	else{
    		errorMsg.setText("Please enter your username and password: admin/admin");
    		return false;
    	}
    }
}
