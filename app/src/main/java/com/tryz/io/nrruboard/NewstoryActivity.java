package com.tryz.io.nrruboard;

import android.app.ProgressDialog;
import android.content.Intent;
import android.media.Image;
import android.media.ImageReader;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.Toast;

import com.chinalwb.are.AREditText;
import com.chinalwb.are.spans.AreImageSpan;
import com.chinalwb.are.strategies.ImageStrategy;
import com.chinalwb.are.styles.toolbar.ARE_ToolbarDefault;
import com.chinalwb.are.styles.toolbar.IARE_Toolbar;
import com.chinalwb.are.styles.toolitems.ARE_ToolItem_AlignmentCenter;
import com.chinalwb.are.styles.toolitems.ARE_ToolItem_AlignmentLeft;
import com.chinalwb.are.styles.toolitems.ARE_ToolItem_AlignmentRight;
import com.chinalwb.are.styles.toolitems.ARE_ToolItem_At;
import com.chinalwb.are.styles.toolitems.ARE_ToolItem_Bold;
import com.chinalwb.are.styles.toolitems.ARE_ToolItem_Hr;
import com.chinalwb.are.styles.toolitems.ARE_ToolItem_Image;
import com.chinalwb.are.styles.toolitems.ARE_ToolItem_Italic;
import com.chinalwb.are.styles.toolitems.ARE_ToolItem_Link;
import com.chinalwb.are.styles.toolitems.ARE_ToolItem_ListBullet;
import com.chinalwb.are.styles.toolitems.ARE_ToolItem_ListNumber;
import com.chinalwb.are.styles.toolitems.ARE_ToolItem_Quote;
import com.chinalwb.are.styles.toolitems.ARE_ToolItem_Strikethrough;
import com.chinalwb.are.styles.toolitems.ARE_ToolItem_Subscript;
import com.chinalwb.are.styles.toolitems.ARE_ToolItem_Superscript;
import com.chinalwb.are.styles.toolitems.ARE_ToolItem_Underline;
import com.chinalwb.are.styles.toolitems.ARE_ToolItem_Video;
import com.chinalwb.are.styles.toolitems.IARE_ToolItem;
import com.chinalwb.are.styles.toolitems.styles.ARE_Style_Image;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

import static android.os.AsyncTask.THREAD_POOL_EXECUTOR;

public class NewstoryActivity extends AppCompatActivity implements ImageStrategy{

    private IARE_Toolbar mToolbar;
    private AREditText mEditText;
    private AREditText mEditTextTitle;
    private boolean scrollerAtEnd;
    private Toolbar toolbarnewstory;
    private StorageReference mstorageReference;
    private FirebaseFirestore mfirebaseFirestore;
    private FirebaseAuth mAuth;
    private String userid;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_newstory);
        mAuth = FirebaseAuth.getInstance();
        userid = mAuth.getCurrentUser().getUid();
        toolbarnewstory = findViewById(R.id.toolbarnewstory);
        setSupportActionBar(toolbarnewstory);
        toolbarnewstory.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GOTO_MAIN();
            }
        });
        ActionBar actionbar = getSupportActionBar();
        actionbar.setDisplayHomeAsUpEnabled(true);
        actionbar.setDisplayShowHomeEnabled(true);
        actionbar.setHomeAsUpIndicator(R.drawable.ic_chevron_left_black_24dp);
        initToolbar();
        mfirebaseFirestore = FirebaseFirestore.getInstance();
    }

    private void initToolbar() {
        mToolbar = this.findViewById(R.id.areToolbar);
        IARE_ToolItem bold = new ARE_ToolItem_Bold();
        IARE_ToolItem italic = new ARE_ToolItem_Italic();
        IARE_ToolItem underline = new ARE_ToolItem_Underline();
        IARE_ToolItem quote = new ARE_ToolItem_Quote();
        IARE_ToolItem listBullet = new ARE_ToolItem_ListBullet();
        IARE_ToolItem hr = new ARE_ToolItem_Hr();
        IARE_ToolItem link = new ARE_ToolItem_Link();
        IARE_ToolItem left = new ARE_ToolItem_AlignmentLeft();
        IARE_ToolItem center = new ARE_ToolItem_AlignmentCenter();
        IARE_ToolItem right = new ARE_ToolItem_AlignmentRight();
        IARE_ToolItem image = new ARE_ToolItem_Image();
        IARE_ToolItem video = new ARE_ToolItem_Video();
//        IARE_ToolItem at = new ARE_ToolItem_At();
        mToolbar.addToolbarItem(bold);
        mToolbar.addToolbarItem(italic);
        mToolbar.addToolbarItem(underline);
        mToolbar.addToolbarItem(quote);
        mToolbar.addToolbarItem(listBullet);
        mToolbar.addToolbarItem(hr);
        mToolbar.addToolbarItem(link);
        mToolbar.addToolbarItem(left);
        mToolbar.addToolbarItem(center);
        mToolbar.addToolbarItem(right);
        mToolbar.addToolbarItem(image);
        mToolbar.addToolbarItem(video);
//        mToolbar.addToolbarItem(at);

        mEditText = this.findViewById(R.id.arEditText);
        mEditText.setToolbar(mToolbar);

        mEditTextTitle = this.findViewById(R.id.arEditTextTitle);
        mEditTextTitle.setToolbar(mToolbar);

    }

    private void GOTO_MAIN(){
        Intent mainTntent = new Intent(NewstoryActivity.this,MainActivity.class);
        startActivity(mainTntent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_option_newstory, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int menuId = item.getItemId();
        if (menuId == R.id.action_save) {
            String html = this.mEditText.getHtml();
            String title = mEditTextTitle.getText().toString();
            SAVE_STORY_TO_FIREBASE(html,title);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void SAVE_STORY_TO_FIREBASE(String html,String title){
        if(!TextUtils.isEmpty(html) && !TextUtils.isEmpty(title)){
            Map<String,Object> storymap = new HashMap<>();
            storymap.put("title",title);
            storymap.put("content",html);
            storymap.put("user_id",userid);
            storymap.put("timestamp", FieldValue.serverTimestamp());
            
            mfirebaseFirestore.collection("user_story").add(storymap).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                @Override
                public void onComplete(@NonNull Task<DocumentReference> task) {
                    if(task.isSuccessful()){
                        Toast.makeText(NewstoryActivity.this,"Upload story comple",Toast.LENGTH_SHORT).show();

                        GOTO_MAIN();
                    }else{
                        Toast.makeText(NewstoryActivity.this,"ERROR :"+task.getException().getMessage(),Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }else{
            Toast.makeText(NewstoryActivity.this,"The story is Empty Title or Content",Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        mToolbar.onActivityResult(requestCode, resultCode, data);
    }


    @Override
    public void uploadAndInsertImage(Uri uri, ARE_Style_Image areStyleImage) {
        new UploadImageTask(areStyleImage).executeOnExecutor(THREAD_POOL_EXECUTOR, uri);
    }

    private static class UploadImageTask extends AsyncTask<Uri, Integer, String> {

        WeakReference<ARE_Style_Image> areStyleImage;
        private ProgressDialog dialog;

        UploadImageTask(ARE_Style_Image styleImage) {
            this.areStyleImage = new WeakReference<>(styleImage);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (dialog == null) {
                dialog = ProgressDialog.show(
                        areStyleImage.get().getEditText().getContext(),
                        "",
                        "Uploading image. Please wait...",
                        true);
            } else {
                dialog.show();
            }
        }

        @Override
        protected String doInBackground(Uri... uris) {
            if (uris != null && uris.length > 0) {
                try {
                    // do upload here ~
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                // Returns the image url on server here
                return "https://avatars0.githubusercontent.com/u/1758864?s=460&v=4";
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if (dialog != null) {
                dialog.dismiss();
            }
            if (areStyleImage.get() != null) {
                areStyleImage.get().insertImage(s, AreImageSpan.ImageType.URL);
            }
        }
    }
}
