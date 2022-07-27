package com.my.company.instagramclone.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.my.company.instagramclone.R;

import java.util.List;

public class TagAdapter extends RecyclerView.Adapter<TagAdapter.ViewHolder> {

    private Context mContext;
    private List<String> mTags;
    private List<String> mTagCounts;

    public TagAdapter(Context mContext, List<String> mTags, List<String> mTagCounts) {
        this.mContext = mContext;
        this.mTags = mTags;
        this.mTagCounts = mTagCounts;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.tag_item, parent, false);
        return new TagAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.tag.setText("# "+mTags.get(position));
        holder.numOfPosts.setText(mTagCounts.get(position) +"posts");
    }

    @Override
    public int getItemCount() {
        return mTags.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        public TextView tag;
        public TextView numOfPosts;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            tag = itemView.findViewById(R.id.hash_tag);
            numOfPosts = itemView.findViewById(R.id.num_of_posts);
        }
    }

    public void filter(List<String> filterTags, List<String> filterTagsCount){
        this.mTags = filterTags;
        this.mTagCounts = filterTagsCount;
        notifyDataSetChanged();
    }
}
