package com.tryz.io.nrruboard;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.nfc.Tag;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatTextView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    private Button btnLogin;
    private ProgressBar progressBarLogin;
    private TextView txtSignup;
    private EditText txtUsername, txtPassword;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        txtSignup = (TextView) findViewById(R.id.txt_signup);
        txtUsername = (EditText) findViewById(R.id.txtUsername);
        txtPassword = (EditText) findViewById(R.id.txtPassword);
        progressBarLogin = (ProgressBar) findViewById(R.id.progressBarlogin);
        progressBarLogin.setVisibility(View.INVISIBLE);
        btnLogin = (Button) findViewById(R.id.btnLogin);
        findViewById(R.id.txt_signup).setOnClickListener(this);
        findViewById(R.id.btnLogin).setOnClickListener(this);
        mAuth = FirebaseAuth.getInstance();
    }

    private void checkLogin() {
        if (!TextUtils.isEmpty(txtUsername.getText()) && !TextUtils.isEmpty(txtPassword.getText())) {
            String email = txtUsername.getText().toString();
            String password = txtPassword.getText().toString();
            signInWithEmailAndPassword(email, password);
        }
    }


    private void goToSignUp() {
        Intent intentRegister = new Intent(LoginActivity.this, NewRegisterActivity.class);
        startActivity(intentRegister);
    }

    private void goToHome() {
        Intent homeTntent = new Intent(LoginActivity.this, MainActivity.class);
        homeTntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(homeTntent);
    }

    public void signInWithEmailAndPassword(String email, String password) {
        progressBarLogin.setVisibility(View.VISIBLE);
        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                            Log.i("Info", user.getEmail());
                            goToHome();
                            progressBarLogin.setVisibility(View.INVISIBLE);
                        } else {
                            Toast.makeText(LoginActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            progressBarLogin.setVisibility(View.INVISIBLE);
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(LoginActivity.this, "Authentication failed.",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onClick(View view) {
        int i = view.getId();
        if(i == R.id.btnLogin){
            checkLogin();
        }else if(i == R.id.txt_signup){
            goToSignUp();
        }
    }

    //    private boolean checkInternetConnected(){
//        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
//        if(cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isAvailable() && cm.getActiveNetworkInfo().isConnected()){
//            return true;
//        }else{
//            Log.i("ggafilrd error","Internet Connection Not Present");
//            return false;
//        }
//    }
}
