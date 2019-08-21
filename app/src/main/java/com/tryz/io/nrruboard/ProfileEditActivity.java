package com.tryz.io.nrruboard;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.common.wrappers.PackageManagerWrapper;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileEditActivity extends AppCompatActivity implements View.OnClickListener {

    private Toolbar toolbareditprofile;
    private CircleImageView circleImageView_profile;
    private Uri ImageProfileUri = null;
    private EditText txt_editprofile_displayname,txt_editprofile_memo;
    private StorageReference mStorageRef;
    private FirebaseAuth mAuth;
    private FirebaseFirestore mFirebaseFirestore;
    private ProgressBar progressBarEditProfile;
    private String userid;
    private boolean isChanged;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_edit);
        toolbareditprofile = (Toolbar) findViewById(R.id.toolbareditprofile);
        toolbareditprofile.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToHome();
            }
        });
        setSupportActionBar(toolbareditprofile);
        toolbareditprofile.setNavigationIcon(R.drawable.ic_chevron_left_black_24dp);
        toolbareditprofile.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToHome();
            }
        });
        circleImageView_profile = (CircleImageView) findViewById(R.id.circleImageView_profile);
        circleImageView_profile.setOnClickListener(this);
        txt_editprofile_displayname = (EditText) findViewById(R.id.txt_editprofile_displayname);
        txt_editprofile_memo = (EditText) findViewById(R.id.txt_editprofile_memo);
        mAuth = FirebaseAuth.getInstance();
        mFirebaseFirestore = FirebaseFirestore.getInstance();
        mStorageRef = FirebaseStorage.getInstance().getReference().child("user_image_profile");
        progressBarEditProfile = findViewById(R.id.progressBarEditProfile);
        progressBarEditProfile.setVisibility(View.INVISIBLE);
        userid = mAuth.getCurrentUser().getUid();
        isChanged = false;
    }


    @Override
    protected void onStart() {
        super.onStart();
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if(ContextCompat.checkSelfPermission(ProfileEditActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                Toast.makeText(ProfileEditActivity.this,"Permission Denied",Toast.LENGTH_SHORT).show();
                ActivityCompat.requestPermissions(ProfileEditActivity.this,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},1);
            }
            if(ContextCompat.checkSelfPermission(ProfileEditActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                Toast.makeText(ProfileEditActivity.this,"Permission Denied",Toast.LENGTH_SHORT).show();
                ActivityCompat.requestPermissions(ProfileEditActivity.this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},1);
            }
            if(ContextCompat.checkSelfPermission(ProfileEditActivity.this, Manifest.permission.ACCESS_NETWORK_STATE) != PackageManager.PERMISSION_GRANTED){
                Toast.makeText(ProfileEditActivity.this,"Permission Denied",Toast.LENGTH_SHORT).show();
                ActivityCompat.requestPermissions(ProfileEditActivity.this,new String[]{Manifest.permission.ACCESS_NETWORK_STATE},1);
            }

            GET_DATA_FIREBASE();
        }
    }

    private void GET_DATA_FIREBASE(){
        mFirebaseFirestore.collection("user_data").document(userid).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    if(task.getResult().exists()){
                        //Get name from firebase database
                        String displayname = task.getResult().getString("user_displayname");
                        txt_editprofile_displayname.setText(displayname);

                        //Get memo from firebase database
                        String memo = task.getResult().getString("user_memo");
                        txt_editprofile_memo.setText(memo);

                        //Get image uri from firebase database
                        String profileUri = task.getResult().getString("user_profileuri");
                        ImageProfileUri = Uri.parse(profileUri);
                        //Set image
                        RequestOptions requestOptions = new RequestOptions();
                        requestOptions.placeholder(R.mipmap.profile_defualt);
                        Glide.with(ProfileEditActivity.this).setDefaultRequestOptions(requestOptions).load(ImageProfileUri).into(circleImageView_profile);
                    }
                }else{
                    Toast.makeText(ProfileEditActivity.this,"ERROR : "+task.getException().getMessage(),Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_option_editprofile,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int menuId = item.getItemId();
        if (menuId == com.tryz.io.nrruboard.R.id.action_save) {
            saveProfileDataToFirebaseStorge();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if(i == R.id.circleImageView_profile){
            checkSelfPermission();
        }
    }


    private void checkSelfPermission(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
             if(ContextCompat.checkSelfPermission(ProfileEditActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                    Toast.makeText(ProfileEditActivity.this,"Permission Denied",Toast.LENGTH_SHORT).show();
                    ActivityCompat.requestPermissions(ProfileEditActivity.this,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},1);
                }
            if(ContextCompat.checkSelfPermission(ProfileEditActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                Toast.makeText(ProfileEditActivity.this,"Permission Denied",Toast.LENGTH_SHORT).show();
                ActivityCompat.requestPermissions(ProfileEditActivity.this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},1);
            }
            if(ContextCompat.checkSelfPermission(ProfileEditActivity.this, Manifest.permission.ACCESS_NETWORK_STATE) != PackageManager.PERMISSION_GRANTED){
                Toast.makeText(ProfileEditActivity.this,"Permission Denied",Toast.LENGTH_SHORT).show();
                ActivityCompat.requestPermissions(ProfileEditActivity.this,new String[]{Manifest.permission.ACCESS_NETWORK_STATE},1);
            }
            callCropperImage();
        }else{
            callCropperImage();
        }
    }

    private void callCropperImage(){
        CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .setAspectRatio(1,1)
                .start(ProfileEditActivity.this);
    }

    private void saveProfileDataToFirebaseStorge(){
        progressBarEditProfile.setVisibility(View.VISIBLE);
        final String displayname = txt_editprofile_displayname.getText().toString();
        final String memo = txt_editprofile_memo.getText().toString();
        if(isChanged){
            if(!TextUtils.isEmpty(displayname) && !TextUtils.isEmpty(memo) && ImageProfileUri != null){
                mStorageRef.child(userid+".jpg").delete();
                mStorageRef.child(userid+".jpg").putFile(ImageProfileUri).continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                    @Override
                    public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                        if (!task.isSuccessful()) {
                            throw task.getException();
                        }
                        return mStorageRef.child(userid+".jpg").getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if(task.isSuccessful()){
                            setDataToFireStore(task,displayname,memo);
                            GET_DATA_FIREBASE();
                            progressBarEditProfile.setVisibility(View.INVISIBLE);
                        }else{
                            Toast.makeText(ProfileEditActivity.this,"ERROR : "+task.getException().getMessage(),Toast.LENGTH_LONG).show();
                            progressBarEditProfile.setVisibility(View.INVISIBLE);
                        }
                    }
                });

            }
        }else{
            setDataToFireStore(null,displayname,memo);
            GET_DATA_FIREBASE();
            progressBarEditProfile.setVisibility(View.INVISIBLE);
        }
    }

    private void setDataToFireStore(@NonNull Task<Uri> task,String displayname,String memo) {
        Uri ImageProfileUriFirebaseUpload;
        if(task != null){
            ImageProfileUriFirebaseUpload = task.getResult();
        }else{
            ImageProfileUriFirebaseUpload = ImageProfileUri;
        }
        //Creae dat to add firebase
        Map<String,String> usermap = new HashMap<>();
        usermap.put("user_displayname",displayname);
        usermap.put("user_memo",memo);
        usermap.put("user_profileuri",ImageProfileUriFirebaseUpload.toString());

        mFirebaseFirestore.collection("user_data").document(userid).set(usermap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    Toast.makeText(ProfileEditActivity.this,"Profile is Updated",Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(ProfileEditActivity.this,"ERROR : "+task.getException().getMessage(),Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void goToHome(){
        Intent homeTntent = new Intent(ProfileEditActivity.this,MainActivity.class);
        startActivity(homeTntent);
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                ImageProfileUri = result.getUri();
                circleImageView_profile.setImageURI(ImageProfileUri);
                isChanged = true;
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }
}
