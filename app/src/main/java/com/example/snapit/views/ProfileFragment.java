package com.example.snapit.views;


import android.content.Context;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.snapit.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;


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
        final ImageView profileImg = view.findViewById(R.id.profile_img);
        ImageView editImage = view.findViewById(R.id.edt_profile_img);
        final TextView userName = view.findViewById(R.id.tv_user_name);
        final TextView tvEmail = view.findViewById(R.id.tv_email);
        final TextView subjectCount = view.findViewById(R.id.tv_subject_count);
        final TextView documentCount = view.findViewById(R.id.tv_document_count);

        if (currentUser != null){

            editImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            });

            mUserData.child(currentUser.getUid()).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    String name = dataSnapshot.child("name").getValue().toString();
                    String email = dataSnapshot.child("email").getValue().toString();

                    userName.setText(name);
                    tvEmail.setText(email);
                    profileImg.setImageResource(R.drawable.ic_android);

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

}
