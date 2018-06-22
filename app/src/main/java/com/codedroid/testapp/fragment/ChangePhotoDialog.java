package com.codedroid.testapp.fragment;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.codedroid.testapp.R;
import com.codedroid.testapp.sendPhoto;


/**
 * A simple {@link Fragment} subclass.
 */
public class ChangePhotoDialog extends DialogFragment implements View.OnClickListener {


    public static final int  CAMERA_REQUEST_CODE = 5467;//random number
    public static final int PICKFILE_REQUEST_CODE = 8352;//random number

    private static final String TAG = "ChangePhotoDialog";
    private sendPhoto sendPhoto;

    public ChangePhotoDialog() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_change_photo_dialog, container, false);

        TextView tv_camera = (TextView)view.findViewById(R.id.tv_camera);
        tv_camera.setOnClickListener(this);

        TextView tv_gallery = (TextView)view.findViewById(R.id.tv_gallery);
        tv_gallery.setOnClickListener(this);

        return view;
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.tv_camera:
                takePhoto();
                break;

            case R.id.tv_gallery:
                selectPhoto();
                break;
        }
    }

    private void takePhoto() {

        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(cameraIntent, CAMERA_REQUEST_CODE);
    }

    private void selectPhoto() {

        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
        startActivityForResult(intent, PICKFILE_REQUEST_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICKFILE_REQUEST_CODE && resultCode == Activity.RESULT_OK) {

            Uri selectedImageUri = data.getData();
            sendPhoto.UriPhoto(selectedImageUri);
            getDialog().dismiss();

        }else if(requestCode == CAMERA_REQUEST_CODE && resultCode == Activity.RESULT_OK){

            Bitmap bitmap;
            bitmap = (Bitmap) data.getExtras().get("data");

            sendPhoto.bitmapPhoto(bitmap);
            getDialog().dismiss();
        }

    }

    @Override
    public void onAttach(Context context) {
        try{
            sendPhoto = (sendPhoto) getActivity();
        }catch (ClassCastException e){
            Log.e(TAG, "onAttach: ClassCastException", e.getCause() );
        }
        super.onAttach(context);
    }
}
