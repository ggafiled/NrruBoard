package com.tryz.io.nrruboard;

import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class CorpusStoryActivity extends AppCompatActivity {

    private Toolbar toolbareditstory;
    private RecyclerView recyclervieweditstory;
    private List<StoryPost> result;
    private LinearLayoutManager linearLayoutManager;
    private FirebaseUser currentUser;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseFirestore mFirebaseFirestore;
    private SelfStoryRecyclerAdapter dataAdapter;
    private String userid;
    private RelativeLayout layout_empty_item;
    private static String TAG = "EditStoryTAG";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_corpus_story);
        mAuth = FirebaseAuth.getInstance();
        layout_empty_item = findViewById(R.id.layout_empty_item);
        mFirebaseFirestore = FirebaseFirestore.getInstance();
        toolbareditstory = (Toolbar) findViewById(R.id.toolbareditstory);
        toolbareditstory.setNavigationIcon(R.drawable.ic_chevron_left_black_24dp);
        toolbareditstory.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToHome();
            }
        });
        recyclervieweditstory = (RecyclerView) findViewById(R.id.recyclervieweditstory);
        recyclervieweditstory.setHasFixedSize(true);
        result = new ArrayList<>();
        linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclervieweditstory.setLayoutManager(linearLayoutManager);
        dataAdapter = new SelfStoryRecyclerAdapter(result);
        recyclervieweditstory.setAdapter(dataAdapter);
        bindingData();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(FirebaseAuth.getInstance().getCurrentUser() != null){
            userid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        }else{
            IS_LOGIN();
        }
    }

    private void IS_LOGIN(){
        Intent loginTntent = new Intent(CorpusStoryActivity.this,LoginActivity.class);
        loginTntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(loginTntent);
    }

    private void goToHome(){
        Intent homeTntent = new Intent(CorpusStoryActivity.this,MainActivity.class);
        startActivity(homeTntent);
        finish();
    }

    private void bindingData() {
        mFirebaseFirestore.collection("user_story").whereEqualTo("user_id",FirebaseAuth.getInstance().getCurrentUser().getUid()).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()){
                    for(DocumentSnapshot doc:task.getResult()){
                        if(doc != null){
                            StoryPost storyPost = doc.toObject(StoryPost.class);
                            String postId = doc.getId();
                            storyPost.setPost_id(postId);
                            result.add(storyPost);
                            dataAdapter.notifyDataSetChanged();
                        }else{
                            recyclervieweditstory.setVisibility(View.INVISIBLE);
                            layout_empty_item.setVisibility(View.VISIBLE);

                        }
                    }
                }else{
                    Toast.makeText(CorpusStoryActivity.this,"ERROR : "+task.getException().getMessage(),Toast.LENGTH_LONG).show();

                }
            }
        })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(CorpusStoryActivity.this,"ERROR : "+e.getMessage(),Toast.LENGTH_LONG).show();
                        Log.i(TAG,e.getMessage());

                    }
                });
    }
}
