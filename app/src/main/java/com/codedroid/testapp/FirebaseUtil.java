package com.codedroid.testapp;

import com.codedroid.testapp.model.Category;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

public class FirebaseUtil {

    public static FirebaseDatabase firebaseDatabase;
    public static DatabaseReference databaseReference;
    public static FirebaseUtil firebaseUtil;
    public static ArrayList<Category> categoryArrayList;

    public static FirebaseStorage firebaseStorage;
    public static StorageReference storageReference;

    public static void getConnection(String ref) {

        if (firebaseUtil == null) {
            firebaseUtil = new FirebaseUtil();
            firebaseDatabase = FirebaseDatabase.getInstance();

        }
//        categoryArrayList = new ArrayList<Category>();
        categoryArrayList = new ArrayList<Category>();
        databaseReference = firebaseDatabase.getReference().child(ref);


    }

    public static void connectedStorage(String name) {
        firebaseStorage = FirebaseStorage.getInstance();
        storageReference = firebaseStorage.getReference().child("images/").child(name);
    }

}
