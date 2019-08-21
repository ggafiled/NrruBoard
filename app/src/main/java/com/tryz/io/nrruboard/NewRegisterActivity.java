package com.tryz.io.nrruboard;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class NewRegisterActivity extends AppCompatActivity {

    private EditText inputEmail, inputPassword,inputPasswordConfirm;
    private FirebaseAuth auth;
    private Button btnSignUp;
    private ProgressBar progressBar;
    private Toolbar toolbarRegister;



    @Override    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_register);
        progressBar = findViewById(R.id.progressBarlogin);
        toolbarRegister = findViewById(R.id.toolbarRegister);
        setSupportActionBar(toolbarRegister);
        toolbarRegister.setNavigationIcon(R.drawable.ic_chevron_left_black_24dp);
        toolbarRegister.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                IS_LOGIN();
            }
        });
        auth = FirebaseAuth.getInstance();

        if (auth.getCurrentUser() != null) {
            auth.signOut();
        }

        inputEmail = (EditText) findViewById(R.id.email);
        inputPassword = (EditText) findViewById(R.id.password);
        inputPasswordConfirm = (EditText) findViewById(R.id.password_confirm);
        btnSignUp = (Button) findViewById(R.id.sign_up_button);

        btnSignUp.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                    progressBar.setVisibility(View.VISIBLE);
                    final String email = inputEmail.getText().toString();
                    final String password = inputPassword.getText().toString();
                    final String passwordConfirm = inputPasswordConfirm.getText().toString();
                    try {
                        if (password.length() > 0 && email.length() > 0) {
                            if (password.equals(passwordConfirm)) {
                                auth.createUserWithEmailAndPassword(email, password)
                                        .addOnCompleteListener(NewRegisterActivity.this, new OnCompleteListener<AuthResult>() {
                                            @Override
                                            public void onComplete(@NonNull Task<AuthResult> task) {
                                                if (!task.isSuccessful()) {
                                                    Toast.makeText(
                                                            NewRegisterActivity.this,
                                                            "Sign up Failed.",
                                                            Toast.LENGTH_LONG).show();
                                                    Log.v("error", task.getResult().toString());
                                                } else {
                                                    IS_LOGIN();
                                                }
                                                progressBar.setVisibility(View.INVISIBLE);
                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(NewRegisterActivity.this, "Sign up Failed.",
                                                Toast.LENGTH_SHORT).show();
                                    }
                                });
                            } else {
                                progressBar.setVisibility(View.INVISIBLE);
                                Toast.makeText(
                                        NewRegisterActivity.this,
                                        "Password not match",
                                        Toast.LENGTH_LONG).show();
                            }
                        } else {
                            Toast.makeText(
                                    NewRegisterActivity.this,
                                    "Fill All Fields",
                                    Toast.LENGTH_LONG).show();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
            }
        });
    }

    private void IS_LOGIN() {
        Intent intent = new Intent(NewRegisterActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

}