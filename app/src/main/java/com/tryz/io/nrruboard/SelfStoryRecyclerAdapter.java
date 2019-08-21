package com.tryz.io.nrruboard;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
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

public class SelfStoryRecyclerAdapter extends RecyclerView.Adapter<SelfStoryRecyclerAdapter.DataViewHolder> {

    final public List<StoryPost> storyPostList;
    private FirebaseFirestore firebaseFirestore;
    private String displayname = "Unified";
    private Context context;
    private Uri profileUrl = null;
    StoryPost dataModel;
    public SelfStoryRecyclerAdapter(List<StoryPost> result){
        this.storyPostList = result;
    }

    @NonNull
    @Override
    public DataViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        context = parent.getContext();
        return new DataViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.self_story_list_item,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull final SelfStoryRecyclerAdapter.DataViewHolder viewHolder, final int position) {
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
        viewHolder.treedot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DELETE_POST_FIREBASE(storyPostList.get(position));
            }
        });
        viewHolder.setItemClickListener(new ItemClickListener() {
            @Override
            public void OnClick(View view, final int position, boolean isLongClick) {
                StoryPost dataModelTemp = storyPostList.get(position);
                if(isLongClick){
                    viewHolder.treedot.setVisibility(View.VISIBLE);
                }else{
                    goToStoryEdit(dataModelTemp.getPost_id(),dataModelTemp.getUser_id(),dataModelTemp.getTitle(),dataModelTemp.getContent());
                    if(viewHolder.treedot.getVisibility() == View.VISIBLE){
                        viewHolder.treedot.setVisibility(View.INVISIBLE);
                    }
                }
            }


        });

    }

    private void DELETE_POST_FIREBASE(final StoryPost model) {
        firebaseFirestore.collection("user_story").document(model.getPost_id()).delete().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    Toast.makeText(context,"Delete Post Completed."+model.getTitle(),Toast.LENGTH_LONG).show();
                }else{
                    Toast.makeText(context,"ERROR : "+task.getException().getMessage(),Toast.LENGTH_LONG).show();

                }
            }
        });
    }

    private void goToStoryEdit(String postid,String uid,String date,String content){
        Intent edit = new Intent(context,EditStoryActivity.class);
        edit.putExtra("postid",postid);
        edit.putExtra("userid",uid);
        edit.putExtra("title",date);
        edit.putExtra("content",content);
        context.startActivity(edit);
    }

    @Override
    public int getItemCount() {
        return storyPostList.size();
    }



    public class DataViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener,View.OnLongClickListener{

        private TextView txtstorytitle,txtstoryusername,txtstorydatetime,treedot;
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
            treedot = mView.findViewById(R.id.treedot);
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
