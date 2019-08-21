package com.tryz.io.nrruboard;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

import de.hdodenhof.circleimageview.CircleImageView;

public class ReadStoryActivity extends AppCompatActivity {

    private Toolbar toolbarRead;
    private FirebaseFirestore firebaseFirestore;
    private String displayname = "Unified";
    private Uri profileUrl = null;
    private CircleImageView toolbar_read_img_header;
    private TextView toolbar_read_txtusername,toolbar_read_txtdate;
    private ImageView toolbar_read_bookmarks;
    private String postid = null,uid = null;
    private WebView webview;
    private String content,date,selfid;
    private FloatingActionButton likefloatingactionbutton;
    private FirebaseFirestore mFirebaseFirestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_read_story);
        toolbarRead = findViewById(R.id.toolbar_read);
        setSupportActionBar(toolbarRead);
        toolbarRead.setNavigationIcon(R.drawable.ic_chevron_left_black_24dp);
        toolbarRead.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToHome();
            }
        });
        selfid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        postid = getIntent().getExtras().getString("postid");
        uid = getIntent().getExtras().getString("userid");
        date = getIntent().getExtras().getString("date");
        content = getIntent().getExtras().getString("content");

        webview = (WebView) findViewById(R.id.webview);
        webview.getSettings().setLoadWithOverviewMode(true);
        webview.getSettings().setUseWideViewPort(false);
        webview.getSettings().setDisplayZoomControls(true);
        webview.getSettings().setJavaScriptEnabled(false);
        likefloatingactionbutton = findViewById(R.id.likefloatingactionbutton);
        //handle on click like .
        likefloatingactionbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Animation animZoomIn = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.zoom_out);
                likefloatingactionbutton.startAnimation(animZoomIn);
                firebaseFirestore.collection("user_story/"+postid+"/like").document(selfid).get()
                        .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                if(!task.getResult().exists()){
                                    Map<String,Object> likemap = new HashMap<>();
                                    likemap.put("timestamp", FieldValue.serverTimestamp());
                                    firebaseFirestore.collection("user_story/"+postid+"/like/").document(selfid).set(likemap);
                                    Toast.makeText(ReadStoryActivity.this,"Like it.",Toast.LENGTH_LONG).show();
                                }else{
                                    firebaseFirestore.collection("user_story/"+postid+"/like/").document(selfid).delete();
                                }
                            }
                        });


            }
        });

        toolbar_read_img_header = toolbarRead.findViewById(R.id.toolbar_read_img_header);
        toolbar_read_txtusername = toolbarRead.findViewById(R.id.toolbar_read_txtusername);
        toolbar_read_txtdate = toolbarRead.findViewById(R.id.toolbar_read_txtdate);
        toolbar_read_bookmarks = toolbarRead.findViewById(R.id.toolbar_read_bookmarks);
        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseFirestore.collection("user_data").document(uid).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    if(task.getResult().exists()){
                        displayname = task.getResult().getString("user_displayname");
                        toolbar_read_txtusername.setText(displayname);
                        toolbar_read_txtdate.setText(date);
                        profileUrl = Uri.parse(task.getResult().getString("user_profileuri"));
                        RequestOptions requestOptions = new RequestOptions();
                        requestOptions.placeholder(R.mipmap.img_broken);
                        Glide.with(getApplicationContext()).setDefaultRequestOptions(requestOptions).load(profileUrl).into(toolbar_read_img_header);
                        webview.loadData(content,"text/html; charset=utf-8","utf-8");
                    }
                }
            }
        });
    }

    private void goToHome() {
        Intent homeTntent = new Intent(ReadStoryActivity.this, MainActivity.class);
        startActivity(homeTntent);
        finish();
    }

    @Override
    protected void onStart() {
        super.onStart();
        //get icon for like or unlike
        firebaseFirestore.collection("user_story/"+postid+"/like").document(selfid)
                .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                        if(documentSnapshot.exists()){
                            likefloatingactionbutton.setBackgroundResource(R.color.colorTextPurpore);
                        }else{
                            likefloatingactionbutton.setBackgroundResource(R.color.colorPrimary);
                        }
                    }
                });
    }
}
