package com.example.snapit.views;


import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.snapit.R;
import com.example.snapit.models.Bean_User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Objects;

import static android.app.Activity.RESULT_OK;
import static android.widget.Toast.LENGTH_SHORT;


/**
 * A simple {@link Fragment} subclass.
 */
public class ProfileFragment extends Fragment {

    private static final String TAG = ProfileFragment.class.getSimpleName();
    private View view;
    private Context context;
    private FirebaseUser currentUser;
    private DatabaseReference mUserData;
    private DatabaseReference mSubjectData;
    private DatabaseReference mDocumentData;
    private static final int GALLERY_PICK = 101;
    private ProgressDialog progressDialog;
    private ImageView profileImage;
    public StorageReference fileStorage;


    public ProfileFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_profile, container, false);
        context = inflater.getContext();

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        mUserData = firebaseDatabase.getReference().child("Users");
        mUserData.keepSynced(true);

        mSubjectData = firebaseDatabase.getReference().child("Subjects");
        mSubjectData.keepSynced(true);

        mDocumentData = firebaseDatabase.getReference().child("Documents");
        mDocumentData.keepSynced(true);

        loadData();

        return view;
    }

    private void loadData() {
         profileImage = view.findViewById(R.id.profile_img);
        ImageView editImage = view.findViewById(R.id.edt_profile_img);
        final TextView userName = view.findViewById(R.id.tv_user_name);
        final TextView tvEmail = view.findViewById(R.id.tv_email);
        final TextView subjectCount = view.findViewById(R.id.tv_subject_count);
        final TextView documentCount = view.findViewById(R.id.tv_document_count);

        if (currentUser != null){

            editImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (Build.VERSION.SDK_INT >= 22) {

                        checkAndRequestForPermission();


                    }
                    else
                    {
                        openGallery();
                    }
                }
            });

            mUserData.child(currentUser.getUid()).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    String name = dataSnapshot.child("name").getValue().toString();
                    String email = dataSnapshot.child("email").getValue().toString();
                    String profileuri=dataSnapshot.child("profileUrl").getValue().toString();
                    userName.setText(name);
                    tvEmail.setText(email);
                    Picasso.get().load(profileuri).placeholder(R.drawable.ic_android).into(profileImage);

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });


            mSubjectData.child(currentUser.getUid()).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    Log.e(TAG, "onDataChange: "+ dataSnapshot.getChildrenCount() );
                    String count = String.valueOf(dataSnapshot.getChildrenCount());
                    int subjectIntCount = Integer.parseInt(count);

                    if (subjectIntCount > 10){
                        subjectCount.setText(String.valueOf(subjectIntCount));
                    }else {
                        subjectCount.setText("0"+ subjectIntCount);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });


            mDocumentData.child(currentUser.getUid()).addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                    String childCount = String.valueOf(dataSnapshot.getChildrenCount());
                    int docCount = Integer.parseInt(childCount);

                    if (docCount > 10){
                        documentCount.setText(String.valueOf(docCount));
                    }else {
                        documentCount.setText("0"+ docCount);
                    }

                }

                @Override
                public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                }

                @Override
                public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

                }

                @Override
                public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

        }

    }



    private void openGallery() {
        //TODO: open gallery intent and wait for user to pick an image !

        Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent,GALLERY_PICK);
    }


    private void checkAndRequestForPermission() {


        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE)) {

                Toast.makeText(getActivity(),"Please accept for required permission", LENGTH_SHORT).show();

            }

            else
            {
                ActivityCompat.requestPermissions(getActivity(),
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        GALLERY_PICK);
            }

        }
        else
            openGallery();

    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == GALLERY_PICK && data != null ) {

            // the user has successfully picked an image
            // we need to save its reference to a Uri variable
            final Uri imageUri = data.getData();
            profileUpdate(imageUri);
         //   profileImage.setImageURI(imageUri);
        }
        else {
            Toast.makeText(getActivity(),"Please select file", LENGTH_SHORT).show();
        }

    }

    private void profileUpdate(final Uri imageuri) {

        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setTitle("Uploading Image...");
        progressDialog.setMessage("Please Wait while we upload and process the image.");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();
        fileStorage = FirebaseStorage.getInstance().getReference();

        final StorageReference filepath = fileStorage.child("Image_profile").child(imageuri.getLastPathSegment());
        filepath.putFile(imageuri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                filepath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Uri download= uri;
                        mUserData.child(currentUser.getUid()).child("profileUrl").setValue(download.toString())
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {

                                if (task.isSuccessful()) {
                                } else {

                                    Toast.makeText(getActivity(), "File not Successfully Uploaded", LENGTH_SHORT).show();
                                }

                            }


                        });
                        progressDialog.dismiss();


                    }
                });
            }
        });







    }

}
