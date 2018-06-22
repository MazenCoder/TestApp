package com.codedroid.testapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.codedroid.testapp.fragment.ChangePhotoDialog;
import com.codedroid.testapp.model.Category;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;

public class AddActivity extends AppCompatActivity implements sendPhoto {

    @Override
    public void UriPhoto(Uri imageUri) {
        if(!imageUri.toString().equals("")){
            mSelectedImageBitmap = null;
            mSelectedImageUri = imageUri;
            Log.d(TAG, "getImagePath: got the image uri: " + mSelectedImageUri);
            Picasso.with(getApplicationContext())
                    .load(imageUri).fit().centerCrop().placeholder(R.drawable.ic_launcher_background)
                    .into(imageView);

            BackgroundImageResize resize = new BackgroundImageResize(null);
            resize.execute(imageUri);
            //ImageLoader.getInstance().displayImage(imagePath.toString(), mProfileImage);
        }
    }

    @Override
    public void bitmapPhoto(Bitmap imageBitmap) {
        if(imageBitmap != null){
            mSelectedImageUri = null;
            mSelectedImageBitmap = imageBitmap;
            Log.d(TAG, "getImageBitmap: got the image bitmap: " + mSelectedImageBitmap);
            imageView.setImageBitmap(imageBitmap);

            BackgroundImageResize resize = new BackgroundImageResize(imageBitmap);
            Uri uri = null;
            resize.execute(uri);
        }
    }

    //vars
    private Uri mSelectedImageUri;
    private Bitmap mSelectedImageBitmap;
    private byte[] mBytes;
    private double progress;

    private static final int REQUEST_CODE = 1234;
    private static final double MB_THRESHHOLD = 5.0;
    private static final double MB = 1000000.0;

    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;

    private EditText title, description;
    private ImageView imageView;
    private Category categoryData;
    private boolean storagePermissions = false;

    private static final String TAG = "AddActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add);

        FirebaseUtil.getConnection("category");
        firebaseDatabase  = FirebaseUtil.firebaseDatabase;
        databaseReference = FirebaseUtil.databaseReference;

        title       = (EditText)findViewById(R.id.ed_titel);
        description = (EditText)findViewById(R.id.ed_des);
        imageView   = (ImageView)findViewById(R.id.img);

        Intent intent = getIntent();
        Category category = (Category)intent.getSerializableExtra("category");

        if (category == null) {
            categoryData = new Category();
        }else {

            this.categoryData = category;

            title.setText(category.getTitel());
            description.setText(category.getDescription());
            Picasso.with(getApplicationContext())
                    .load(categoryData.getImage_url())
                    .into(imageView);
        }
    }

    public void save(View view) {

        if (categoryData.getId() == null){

            categoryData.setTitel(title.getText().toString());
            categoryData.setDescription(description.getText().toString());

            databaseReference.push().setValue(categoryData);
            title.setText("");
            description.setText("");

        }else {

            categoryData.setTitel(title.getText().toString());
            categoryData.setDescription(description.getText().toString());

            databaseReference.child(categoryData.getId()).setValue(categoryData);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.remove:
                removeData();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void removeData() {
        if (categoryData.getId() == null) {
            Toast.makeText(getApplicationContext(), "please check your data!", Toast.LENGTH_LONG).show();
        }else {
            databaseReference.child(categoryData.getId()).removeValue();
        }

        if (categoryData.getImage_path() != null) {
            FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
            StorageReference storageReference = firebaseStorage.getReference().child(categoryData.getImage_path());
            storageReference.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Toast.makeText(getApplicationContext(), "deleted", Toast.LENGTH_LONG).show();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    public void selectedImg(View view) {

        if (storagePermissions) {
            ChangePhotoDialog dialog = new ChangePhotoDialog();
            dialog.show(getSupportFragmentManager(), "ChangePhotoDialog");
        }else {
            verifyStoragePermission();
        }
    }

    private void verifyStoragePermission() {

        String[] permission = {
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA};

        if (ContextCompat.checkSelfPermission(getApplicationContext(), permission[0])
                        == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(getApplicationContext(), permission[1])
                        == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(getApplicationContext(), permission[2])
                        == PackageManager.PERMISSION_GRANTED) {
            storagePermissions = true;
        }else {
            ActivityCompat.requestPermissions(this, permission, REQUEST_CODE);
        }
    }

    /**
     * 1) doinBackground takes an imageUri and returns the byte array after compression
     * 2) onPostExecute will print the % compression to the log once finished
     */
    public class BackgroundImageResize extends AsyncTask<Uri, Integer, byte[]> {

        Bitmap mBitmap;
        public BackgroundImageResize(Bitmap bm) {
            if(bm != null){
                mBitmap = bm;
            }
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
//            showDialog();
            Toast.makeText(getApplicationContext(), "compressing image", Toast.LENGTH_SHORT).show();
        }

        @Override
        protected byte[] doInBackground(Uri... params ) {
            Log.d(TAG, "doInBackground: started.");

            if(mBitmap == null){

                try {
                    mBitmap = MediaStore.Images.Media.getBitmap(getApplicationContext().getContentResolver(), params[0]);
                    Log.d(TAG, "doInBackground: bitmap size: megabytes: " + mBitmap.getByteCount()/MB + " MB");
                } catch (IOException e) {
                    Log.e(TAG, "doInBackground: IOException: ", e.getCause());
                }
            }

            byte[] bytes = null;
            for (int i = 1; i < 11; i++){
                if(i == 10){
                    Toast.makeText(getApplicationContext(), "That image is too large.", Toast.LENGTH_SHORT).show();
                    break;
                }
                bytes = getBytesFromBitmap(mBitmap,100/i);
                Log.d(TAG, "doInBackground: megabytes: (" + (11-i) + "0%) "  + bytes.length/MB + " MB");
                if(bytes.length/MB  < MB_THRESHHOLD){
                    return bytes;
                }
            }
            return bytes;
        }


        @Override
        protected void onPostExecute(byte[] bytes) {
            super.onPostExecute(bytes);
            //hideDialog();
            mBytes = bytes;
            //execute the upload
            executeUploadTask();
        }
    }

    // convert from bitmap to byte array
    public static byte[] getBytesFromBitmap(Bitmap bitmap, int quality) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, stream);
        return stream.toByteArray();
    }

    private void executeUploadTask(){
        //showDialog();
        //FilePaths filePaths = new FilePaths();
//specify where the photo will be stored
        StorageReference storageReference = FirebaseStorage.getInstance().getReference()
                .child(firebaseDatabase.getReference().push()+ "/profile_image"); //just replace the old image with the new one

        if(mBytes.length/MB < MB_THRESHHOLD) {

            // Create file metadata including the content type
            StorageMetadata metadata = new StorageMetadata.Builder()
                    .setContentType("image/jpg")
                    .setContentLanguage("en") //see nodes below
                    /*
                    Make sure to use proper language code ("English" will cause a crash)
                    I actually submitted this as a bug to the Firebase github page so it might be
                    fixed by the time you watch this video. You can check it out at https://github.com/firebase/quickstart-unity/issues/116
                     */
                    .setCustomMetadata("Mitch's special meta data", "JK nothing special here")
                    .setCustomMetadata("location", "Iceland")
                    .build();
            //if the image size is valid then we can submit to database
            UploadTask uploadTask = null;
            uploadTask = storageReference.putBytes(mBytes, metadata);
            //uploadTask = storageReference.putBytes(mBytes); //without metadata


            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    //Now insert the download url into the firebase database
                    Uri firebaseURL = taskSnapshot.getDownloadUrl();

                    Log.e(TAG, "onSuccess: "+taskSnapshot.getStorage().getPath());

                    Toast.makeText(getApplicationContext(), "Upload Success", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "onSuccess: firebase download url : " + firebaseURL.toString());

//                    FirebaseDatabase.getInstance().getReference()
//                            .child(getString(R.string.dbnode_users))
//                            .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
//                            .child(getString(R.string.field_profile_image))
//                            .setValue(firebaseURL.toString());

                    //hideDialog();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    Toast.makeText(getApplicationContext(), "could not upload photo", Toast.LENGTH_SHORT).show();

                    //hideDialog();

                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                    double currentProgress = (100 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                    if(currentProgress > (progress + 15)){
                        progress = (100 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                        Log.d(TAG, "onProgress: Upload is " + progress + "% done");
                        Toast.makeText(getApplicationContext(), progress + "%", Toast.LENGTH_SHORT).show();
                    }

                }
            })
            ;
        }else{
            Toast.makeText(this, "Image is too Large", Toast.LENGTH_SHORT).show();
        }

    }
}
