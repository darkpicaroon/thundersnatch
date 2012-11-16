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

public class ForgotPassword extends Activity {

	private EditText username;
	private EditText emailAddress;
	private Button submit;
	
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
				
				// Process forget password
				
				finish();
				
				Intent intent = new Intent(ForgotPassword.this, LoginScreen.class);
				ForgotPassword.this.startActivity(intent);
			}
		});
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_forgot_password, menu);
        return true;
    }
}
