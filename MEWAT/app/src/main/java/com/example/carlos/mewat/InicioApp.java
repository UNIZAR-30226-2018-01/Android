package com.example.carlos.mewat;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.view.View;
import android.widget.Button;
import android.widget.EditText;


public class InicioApp extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inicio_app);

        final EditText txtUsername = this.findViewById(R.id.txtUsername);
        final EditText txtPass = this.findViewById(R.id.txtPassword);
        Button btnLogin = this.findViewById(R.id.loginbutton);
        btnLogin.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                if (validar(txtUsername.getText().toString(), txtPass.getText().toString())){
                    Intent Main = new Intent(getApplicationContext(), SongList.class);
                    startActivity(Main);
                }
            }
        });

    }

    protected boolean validar(String username, String pass){
        return username.equals("1") && pass.equals("2");
    }
}
