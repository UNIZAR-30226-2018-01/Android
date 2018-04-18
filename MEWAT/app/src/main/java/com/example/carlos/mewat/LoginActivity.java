package com.example.carlos.mewat;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import android.support.v4.app.ActivityCompat;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;


public class LoginActivity extends Activity {

    TextView signup;
    EditText txtPass;
    EditText txtUsername;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        int returnespermission = 0;
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, returnespermission);


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        txtUsername = this.findViewById(R.id.txtUsername);
        txtPass = this.findViewById(R.id.txtPassword);
        signup =this.findViewById(R.id.signup);
        Button btnLogin = this.findViewById(R.id.loginbutton);
        btnLogin.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                if (validar(txtUsername.getText().toString(), txtPass.getText().toString())){
                    Intent PlayListActivity = new Intent(getApplicationContext(), PlayerActivity.class);
                    startActivity(PlayListActivity);
                    finish();
                }
            }
        });

        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent SignUpActivity = new Intent(getApplicationContext(), SignUpActivity.class);
                startActivity(SignUpActivity);
            }
        });


    }

    protected boolean validar(String username, String pass){
        return username.equals("1") && pass.equals("2");
    }
}
