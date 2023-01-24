package com.example.sqlitesample;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.example.sqlitesample.databinding.ActivityRegisterBinding;
import com.example.sqlitesample.sqlite.DbHelper;

public class Register extends AppCompatActivity {

    private ActivityRegisterBinding activityRegisterBinding;

    DbHelper DB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityRegisterBinding = DataBindingUtil.setContentView(this, R.layout.activity_register);
        DB = new DbHelper(this);
        activityRegisterBinding.registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Bundle bundle = new Bundle();
                String user = activityRegisterBinding.emailEditText.getText().toString();
                String pass = activityRegisterBinding.passwordEditText.getText().toString();
                String reenterpass = activityRegisterBinding.reenterPasswordEditText.getText().toString();
                String name = activityRegisterBinding.nameEditText.getText().toString();
                String phone = activityRegisterBinding.phoneEditText.getText().toString();
                //bundle.putAll(bundle);


                if(user.equals("")||pass.equals("")||reenterpass.equals("")||name.equals("")||phone.equals(""))
                    Toast.makeText(Register.this, "Please enter all the fields", Toast.LENGTH_SHORT).show();
                else{
                    if(pass.equals(reenterpass)){
                        Boolean checkuser = DB.checkusername(user);
                        if(checkuser==false){
                            Boolean insert = DB.insertData(user, pass, name, phone);
                            if(insert==true){
                                Toast.makeText(Register.this, "Registered successfully", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(getApplicationContext(),LoginActivity.class);
                                startActivity(intent);
                            }else{
                                Toast.makeText(Register.this, "Registration failed", Toast.LENGTH_SHORT).show();
                            }
                        }
                        else{
                            Toast.makeText(Register.this, "User already exists! please sign in", Toast.LENGTH_SHORT).show();
                        }
                    }else{
                        Toast.makeText(Register.this, "Passwords not matching", Toast.LENGTH_SHORT).show();
                    }
                } }
        });

    }
}