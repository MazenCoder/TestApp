package com.codedroid.testapp;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.codedroid.testapp.model.Category;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class AdapterCategory extends RecyclerView.Adapter<AdapterCategory.MyViewHolder>{

    private ArrayList<Category> arrayList;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;
    private ChildEventListener childEventListener;

    public AdapterCategory() {
        FirebaseUtil.getConnection("category");
        firebaseDatabase = FirebaseUtil.firebaseDatabase;
        databaseReference = FirebaseUtil.databaseReference;
        arrayList = FirebaseUtil.categoryArrayList;
        childEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                // TODO TEST CODE
                boolean hasNext = dataSnapshot.getChildren().iterator().hasNext();
                if (hasNext) {
                    DataSnapshot snapshot = dataSnapshot.getChildren().iterator().next();
                }

                Category category = dataSnapshot.getValue(Category.class);
                category.setId(dataSnapshot.getKey());
                arrayList.add(category);
                notifyItemChanged(arrayList.size() -1);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                notifyItemChanged(arrayList.size() -1);
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        databaseReference.addChildEventListener(childEventListener);
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.card_category, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Category category = arrayList.get(position);
        holder.bind(category);
    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        private TextView title, description;
        private ImageView imageView;

        public MyViewHolder(View itemView) {
            super(itemView);

            imageView   = (ImageView)itemView.findViewById(R.id.image_desplay);
            title       = (TextView)itemView.findViewById(R.id.tv_title);
            description = (TextView)itemView.findViewById(R.id.tv_des);
            itemView.setOnClickListener(this);
        }

        private void bind(Category category) {

            Picasso.with(imageView.getContext())
                    .load(category.getImage_url())
                    .fit().centerInside()
                    .into(imageView);
            title.setText(category.getTitel());
            description.setText(category.getDescription());
        }

        @Override
        public void onClick(View v) {
            int position = getAdapterPosition();
            Category category = arrayList.get(position);
            Intent intent = new Intent(v.getContext(), AddActivity.class);
            intent.putExtra("category", category);
            v.getContext().startActivity(intent);
        }
    }
}
