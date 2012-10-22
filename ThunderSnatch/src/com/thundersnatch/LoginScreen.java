package com.thundersnatch;

import android.os.Bundle;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

public class LoginScreen extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Sets the activity to fullscreen.
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
        		WindowManager.LayoutParams.FLAG_FULLSCREEN);
        
        setContentView(R.layout.activity_login_screen);
        
        // Makes the "Login" button clickable.
        Button login = (Button)findViewById(R.id.button1);
        login.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				
				// VERIFY CREDENTIALS HURR
				
				finish();
				
				Intent intent = new Intent(LoginScreen.this, MainMenu.class);
	            LoginScreen.this.startActivity(intent);
				
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
}
