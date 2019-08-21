package com.tryz.io.nrruboard;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.DocumentsContract;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.chinalwb.are.AREditText;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.util.Date;
import java.util.List;

import javax.annotation.Nullable;

public class StoryRecyclerAdapter extends RecyclerView.Adapter<StoryRecyclerAdapter.DataViewHolder> {

    final public List<StoryPost> storyPostList;
    private FirebaseFirestore firebaseFirestore;
    private String displayname = "Unified";
    private Context context;
    private Uri profileUrl = null;
    StoryPost dataModel;
    public StoryRecyclerAdapter(List<StoryPost> result){
        this.storyPostList = result;
    }

    @NonNull
    @Override
    public DataViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        context = parent.getContext();
        return new DataViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.story_list_item,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull final StoryRecyclerAdapter.DataViewHolder viewHolder, int position) {
        viewHolder.setIsRecyclable(false);

        dataModel = storyPostList.get(position);
        String cleanHtml = Html.fromHtml(dataModel.getContent()).toString();
        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseFirestore.collection("user_data").document(dataModel.getUser_id()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    if(task.getResult().exists()){
                        displayname = task.getResult().getString("user_displayname");
                        viewHolder.txtstoryusername.setText(displayname);

                        profileUrl = Uri.parse(task.getResult().getString("user_profileuri"));
                        RequestOptions requestOptions = new RequestOptions();
                        requestOptions.placeholder(R.mipmap.img_broken);
                        requestOptions.error(R.mipmap.img_broken);
                        Glide.with(context).setDefaultRequestOptions(requestOptions).load(profileUrl).into(viewHolder.imageViewTitle);
                    }
                }
            }
        });
        //count like
        firebaseFirestore.collection("user_story/"+dataModel.getPost_id()+"/like")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                        if(!documentSnapshot.isEmpty()){
                            int count = documentSnapshot.size();
                            viewHolder.ratingBar.setRating(count);
                        }else{
                            viewHolder.ratingBar.setRating(0);
                        }
                    }
                });
        viewHolder.txtstorytitle.setText(dataModel.getTitle());

        long milliseconds = dataModel.getTimestamp().getTime();
        String dateString = DateFormat.format("dd/MM/yyyy",new Date(milliseconds)).toString();
        viewHolder.txtstorydatetime.setText(dateString);

        viewHolder.setItemClickListener(new ItemClickListener() {
            @Override
            public void OnClick(View view, int position, boolean isLongClick) {
                StoryPost dataModelTemp = storyPostList.get(position);
                long milliseconds = dataModelTemp.getTimestamp().getTime();
                String dateString = DateFormat.format("dd/MM/yyyy",new Date(milliseconds)).toString();
                goToRead(dataModelTemp.getPost_id(),dataModelTemp.getUser_id(),dateString,setTitleSize(dataModelTemp.getTitle(),dataModelTemp.getContent()));
            }
        });
    }

    private String setTitleSize(String title,String content){
        return "<html><head><style type='text/css'> @font-face { font-family: 'webfont'; src: url('file:///android_res/fonts/thsarabun.ttf') } body{font-family: 'webfont', sans-serif;}</style></head>"
                +"<p><h2>"+title+"</h2></p>"+"<font size='4'>"+content+"</font></html>";
    }

    private void goToRead(String postid,String uid,String date,String content){
        Intent read = new Intent(context,ReadStoryActivity.class);
        read.putExtra("postid",postid);
        read.putExtra("userid",uid);
        read.putExtra("date",date);
        read.putExtra("content",content);
        context.startActivity(read);
    }

    private Uri findImageTag(String html){
        Document doc = Jsoup.parse(html);
        Element img = doc.select("img[src]").first();
        String imgsrc = img.data();
        return Uri.parse(imgsrc);
    }

    @Override
    public int getItemCount() {
        return storyPostList.size();
    }



    public class DataViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener,View.OnLongClickListener{

        private TextView txtstorytitle,txtstoryusername,txtstorydatetime;
        private View mView;
        private ImageView imageViewTitle;
        private ItemClickListener itemClickListener;
        private RatingBar ratingBar;

        public DataViewHolder(@NonNull View itemView) {
            super(itemView);
            mView = itemView;
            txtstorytitle = mView.findViewById(R.id.txtstorytitle);
            txtstoryusername = mView.findViewById(R.id.txtstoryusername);
            imageViewTitle = mView.findViewById(R.id.imageViewTitle);
            txtstorydatetime = mView.findViewById(R.id.txtstorydatetime);
            ratingBar = mView.findViewById(R.id.ratingBar);

            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
        }

        public void setItemClickListener(ItemClickListener _itemClickListener){
            this.itemClickListener = _itemClickListener;
        }

        @Override
        public void onClick(View v) {
            itemClickListener.OnClick(v,getAdapterPosition(),false);
        }

        @Override
        public boolean onLongClick(View v) {
            itemClickListener.OnClick(v,getAdapterPosition(),true);
            return true;
        }
    }
}
