package com.example.sqlitesample;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.example.sqlitesample.databinding.LoginBinding;
import com.example.sqlitesample.sqlite.DbHelper;

import java.util.ArrayList;

public class LoginActivity extends AppCompatActivity {
    private LoginBinding loginActivityBinding;

    DbHelper DB;
    ArrayList<String> ProfessionsList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loginActivityBinding = DataBindingUtil.setContentView(this, R.layout.login);
        DB = new DbHelper(this);

        loginActivityBinding.registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent regIntent = new Intent(LoginActivity.this, Register.class);
                startActivity(regIntent);
            }
        });

        loginActivityBinding.loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = loginActivityBinding.emailEditText.getText().toString();
                String pass = loginActivityBinding.passwordEditText.getText().toString();
              /*  DB.insertProfessionValues(new Data("Web Developer"));
                DB.insertProfessionValues(new Data("Computer Support Specialist"));
                DB.insertProfessionValues(new Data("Computer Hardware Engineer"));
                DB.insertProfessionValues(new Data("Computer & Information Research Scientist"));
                DB.insertProfessionValues(new Data("Big Data Engineer"));
                DB.insertProfessionValues(new Data("Software Systems Developer"));
                DB.insertProfessionValues(new Data("Blockchain Developer"));
                DB.insertProfessionValues(new Data("Software Applications Developer"));
                DB.insertProfessionValues(new Data("Computer Network Architect"));
                DB.insertProfessionValues(new Data("Information Security Analyst"));
                DB.insertProfessionValues(new Data("Computer Systems Analyst"));
                DB.insertProfessionValues(new Data("Database Administrator"));
                DB.insertProfessionValues(new Data("Network & Computer System Administrators"));*/
                if(email.equals("")||pass.equals(""))
                    Toast.makeText(LoginActivity.this,"Fields cannot be empty",Toast.LENGTH_SHORT).show();
                else{
                    Boolean checkemailpass = DB.checkusernamepassword(email, pass);
                    if(checkemailpass==true){
                        Toast.makeText(LoginActivity.this, "Signin successful", Toast.LENGTH_SHORT).show();
                        Intent loginIntent = new Intent(getApplicationContext(),HomeActivity.class);
                        startActivity(loginIntent);
                    }else{
                        Toast.makeText(LoginActivity.this, "Invalid credentials", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

    }
}