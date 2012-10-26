package com.thundersnatch;

import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class CreateAccount extends Activity {
	
	Button createAccount;
    EditText username;
    EditText password;
    EditText retypePassword;
    EditText emailAddress;
    
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Sets the activity to fullscreen.
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
        		WindowManager.LayoutParams.FLAG_FULLSCREEN);
        
        setContentView(R.layout.activity_create_account);
        
        // Initializes pointers to the TextViews.
        username = (EditText)findViewById(R.id.editText1);
        password = (EditText)findViewById(R.id.editText2);
        retypePassword = (EditText)findViewById(R.id.editText3);
        emailAddress = (EditText)findViewById(R.id.editText4);
        
        // Sets up the "Create Account" button handler.
        createAccount = (Button)findViewById(R.id.button1);
        createAccount.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				
				String usernameText = username.getText().toString();
				String passwordText = password.getText().toString();
				String retypeText = retypePassword.getText().toString();
				
				if (passwordText.compareTo(retypeText) == 0 && passwordText.length() >= 6) {
					createAccount(usernameText, passwordText, retypeText);
					
					// SHOULD THEY BE TAKEN TO THE MAIN MENU HERE?
				}
				else {
					
					AlertDialog.Builder builder = new AlertDialog.Builder(CreateAccount.this);
					builder.setCancelable(true);
					
					if (password.length() < 6)
						builder.setTitle("Your password needs to be atleast 6 characters.");
					else
						builder.setTitle("Your passwords don't match.");
					
					builder.setInverseBackgroundForced(true);
					builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
						
						public void onClick(DialogInterface dialog, int which) {
							
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
    
    private void createAccount(String username, String password, String emailAddress)
    {
    	
    }
}
