package com.example.snapit;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.snapit.beans.Bean_User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Objects;

public class RegisterUserActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_user);

        mAuth = FirebaseAuth.getInstance();

        progressDialog = new ProgressDialog(this);

        loadData();
    }

    private void loadData() {
        final EditText etName = findViewById(R.id.et_name);
        final EditText etEmail = findViewById(R.id.et_email);
        final EditText etPassword = findViewById(R.id.et_password);
        Button btnRegister = findViewById(R.id.btn_register);
        TextView tvSignIn = findViewById(R.id.tv_sign_in);

        tvSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerUser(etName, etEmail, etPassword);
            }
        });

    }

    private void registerUser(EditText etName, EditText etEmail, EditText etPassword) {
        final String email = etEmail.getText().toString().trim();
        String password  = etPassword.getText().toString().trim();
        final String name = etName.getText().toString().trim();

        //checking if email and passwords are empty
        if (TextUtils.isEmpty(name)){
            etName.setError("Enter your name");
            return;
        }

        if(TextUtils.isEmpty(email)){
            etEmail.setError("Enter your email");
            return;
        }

        if(TextUtils.isEmpty(password)){
            etPassword.setError("Enter your password");
            return;
        }

        if (password.length() < 6){
            etPassword.setError("Password length must be at least 6 characters");
            return;
        }

        progressDialog.setMessage("Please Wait...");
        progressDialog.show();

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()){

                            Bean_User user = new Bean_User();
                            user.setName(name);
                            user.setEmail(email);

                            // Write a message to the database
                            FirebaseDatabase database = FirebaseDatabase.getInstance();
                            DatabaseReference myRef = database.getReference("Users")
                                    .child(Objects.requireNonNull(mAuth.getCurrentUser()).getUid());
                            myRef.setValue(user);

                            Toast.makeText(RegisterUserActivity.this, "Register Successfully", Toast.LENGTH_SHORT).show();
                            progressDialog.dismiss();

                            Intent intent = new Intent(RegisterUserActivity.this, LoginActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent);
                            finish();

                        }else {

                            progressDialog.dismiss();
                            Toast.makeText(RegisterUserActivity.this, "Error Occurred, Try Again!", Toast.LENGTH_SHORT).show();

                        }
                    }
                });

    }

    public void onBackBtnClicked(View view) {
        onBackPressed();
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(RegisterUserActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }
}
