/* Filename: CreateAccount.java
 * 
 * Description: Is used to allow a user to create an account. It first checks to see
 *              if the passwords match. If they do, it then contacts the server with 
 *              a call to create an account with the given username and password. If
 *              the username doesn't already exist, the account information is stored
 *              and a success is returned. If the username is already taken, or anything
 *              else occurs, an error is returned and parsed to see what happened.
 *              After successful account creation, the user is returned back to the
 *              login screen.
 */

package com.thundersnatch;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

public class CreateAccount extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Sets the activity to fullscreen.
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
        		WindowManager.LayoutParams.FLAG_FULLSCREEN);
        
        setContentView(R.layout.activity_create_account);
        
        // Sets up the "Create Account" button handler.
        Button createAccount = (Button)findViewById(R.id.button1);
        createAccount.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				
				// CREATE ACCOUNT LOGIC
				
				finish();
				
			}
		});
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_create_account, menu);
        return true;
    }
}
