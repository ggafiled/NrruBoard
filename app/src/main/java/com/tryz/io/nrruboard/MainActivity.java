package com.tryz.io.nrruboard;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.annotation.Nullable;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private  FirebaseUser currentUser;
    private  FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private Toolbar toolbarHome;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private CircleImageView circleImageView_profile;
    private Uri ImageProfileUri = null;
    private View navigationViewHeader;
    private TextView txt_btn_profile_name_edit,txt_profile_name;
    private String userid;
    private FirebaseFirestore mFirebaseFirestore;
    private RecyclerView recyclerviewmain;
    private List<StoryPost> result;
    private StoryRecyclerAdapter dataAdapter;
    private LinearLayoutManager linearLayoutManager;
    private static String TAG = "MainTAG";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mAuth = FirebaseAuth.getInstance();
        mFirebaseFirestore = FirebaseFirestore.getInstance();
        drawerLayout = findViewById(R.id.drawer_layout);


        recyclerviewmain = (RecyclerView) findViewById(R.id.recyclerviewmain);
        recyclerviewmain.setHasFixedSize(true);
        result = new ArrayList<>();
        linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerviewmain.setLayoutManager(linearLayoutManager);
        dataAdapter = new StoryRecyclerAdapter(result);
        recyclerviewmain.setAdapter(dataAdapter);
        bindingData();

        navigationView = findViewById(R.id.navigation_view);
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        // set item as selected to persist highlight
                        menuItem.setChecked(true);
                        // close drawer when item is tapped
                        drawerLayout.closeDrawers();

                        // Add code here to update the UI based on the item selected
                        // For example, swap UI fragments here
                        return onSelectedItem(menuItem);
                    }
                });
        navigationViewHeader = navigationView.getHeaderView(0);
        txt_btn_profile_name_edit = (TextView) navigationViewHeader.findViewById(R.id.txt_btn_profile_name_edit);
        txt_btn_profile_name_edit.setOnClickListener(this);
        txt_profile_name = (TextView) navigationViewHeader.findViewById(R.id.txt_profile_name);
        circleImageView_profile = (CircleImageView) navigationViewHeader.findViewById(R.id.circleImageView_profile);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionbar = getSupportActionBar();
        actionbar.setDisplayHomeAsUpEnabled(true);
        actionbar.setDisplayShowHomeEnabled(true);
        actionbar.setHomeAsUpIndicator(R.drawable.ic_menu_white_24dp);
    }

    private void bindingData() {
           mFirebaseFirestore.collection("user_story").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
               @Override
               public void onComplete(@NonNull Task<QuerySnapshot> task) {
                   if(task.isSuccessful()){
                       for(DocumentSnapshot doc:task.getResult()){
                           StoryPost storyPost = doc.toObject(StoryPost.class);
                           String postId = doc.getId();
                           storyPost.setPost_id(postId);
                           result.add(storyPost);
                           dataAdapter.notifyDataSetChanged();
                       }
                   }else{
                       Toast.makeText(MainActivity.this,"ERROR : "+task.getException().getMessage(),Toast.LENGTH_LONG).show();

                   }
               }
           })
                   .addOnFailureListener(new OnFailureListener() {
                       @Override
                       public void onFailure(@NonNull Exception e) {
                           Toast.makeText(MainActivity.this,"ERROR : "+e.getMessage(),Toast.LENGTH_LONG).show();
                           Log.i(TAG,e.getMessage());

                       }
                   });
    }

    private boolean onSelectedItem(MenuItem menuItem){
        switch (menuItem.getItemId()) {
            case R.id.nav_signout:
                IS_LOGOUT();
                return true;
            case R.id.nav_newstory:
                GOTO_NEWSTORY();
                return true;
            case R.id.nav_corpus:
                GOTO_SEECORPUS();
                return true;
            case R.id.nav_home:
                goToHome();
                return true;

        }
        menuItem.setChecked(false);
        return true;
    }

    private void goToHome() {
        Intent homeTntent = new Intent(MainActivity.this, MainActivity.class);
        homeTntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(homeTntent);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                drawerLayout.openDrawer(GravityCompat.START);
                return true;
            case R.id.nav_search:
                IS_SEARCHING();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflate = getMenuInflater();
        inflate.inflate(R.menu.toolbar_view_right,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(FirebaseAuth.getInstance().getCurrentUser() != null){
            userid = FirebaseAuth.getInstance().getCurrentUser().getUid();
            CHECK_IS_SETUP();
            GET_DATA_FIREBASE();
        }else{
            IS_LOGIN();
        }
    }

    private void CHECK_IS_SETUP(){
        mFirebaseFirestore.collection("user_data").document(userid).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    if(!task.getResult().exists()){
                        GOTO_SEEPROFILE();
                    }
                }
            }
        });
    }

    private void GET_DATA_FIREBASE(){
        mFirebaseFirestore.collection("user_data").document(userid).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    if(task.getResult().exists()){
                        //Get name from firebase database
                        String displayname = task.getResult().getString("user_displayname");
                        txt_profile_name.setText(displayname);

                        String profileUri = task.getResult().getString("user_profileuri");
                        ImageProfileUri = Uri.parse(profileUri);

                        RequestOptions requestOptions = new RequestOptions();
                        requestOptions.placeholder(R.mipmap.profile_defualt);
                        Glide.with(MainActivity.this).setDefaultRequestOptions(requestOptions).load(ImageProfileUri).into(circleImageView_profile);
                    }
                }else{
                    Toast.makeText(MainActivity.this,"ERROR : "+task.getException().getMessage(),Toast.LENGTH_LONG).show();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(MainActivity.this,"ERROR : "+e.getMessage(),Toast.LENGTH_SHORT).show();
                Log.i(TAG,e.getMessage());
            }
        });
    }

    private void GOTO_SEEPROFILE(){
        Intent seeprofileTntent = new Intent(MainActivity.this,ProfileEditActivity.class);
        seeprofileTntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(seeprofileTntent);
    }

    private void GOTO_SEECORPUS(){
        Intent seecorpusTntent = new Intent(MainActivity.this,CorpusStoryActivity.class);
        startActivity(seecorpusTntent);
    }


    private void GOTO_NEWSTORY(){
        Intent newstoryTntent = new Intent(MainActivity.this,NewstoryActivity.class);
        startActivity(newstoryTntent);
    }

    private void IS_LOGIN(){
        Intent loginTntent = new Intent(MainActivity.this,LoginActivity.class);
        loginTntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(loginTntent);
    }

    private void IS_LOGOUT(){
        FirebaseAuth.getInstance().signOut();
        Toast.makeText(MainActivity.this, "Sign Out .",
                Toast.LENGTH_SHORT).show();
        IS_LOGIN();
    }

    private void IS_SEARCHING(){
        Toast.makeText(MainActivity.this, "Searching",
                Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if(i == R.id.txt_btn_profile_name_edit){
            GOTO_SEEPROFILE();
        }
    }
}
