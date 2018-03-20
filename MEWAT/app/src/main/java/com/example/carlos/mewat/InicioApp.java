package com.example.carlos.mewat;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import android.support.v4.app.ActivityCompat;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;


public class InicioApp extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        int returnespermission = 0;
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, returnespermission);


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inicio_app);

        final EditText txtUsername = this.findViewById(R.id.txtUsername);
        final EditText txtPass = this.findViewById(R.id.txtPassword);
        Button btnLogin = this.findViewById(R.id.loginbutton);
        btnLogin.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                if (validar(txtUsername.getText().toString(), txtPass.getText().toString())){
                    Intent Main = new Intent(getApplicationContext(), PlayListActivity.class);
                    startActivity(Main);
                }
            }
        });

    }

    protected boolean validar(String username, String pass){
        return username.equals("1") && pass.equals("2");
    }
}
