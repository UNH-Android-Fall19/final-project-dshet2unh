package com.example.snapit.views;


import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.PopupMenu;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.example.snapit.R;
import com.example.snapit.controllers.adapter.SubjectAdapter;
import com.example.snapit.models.Bean_Subject;
import com.example.snapit.controllers.AppConstant;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
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
import java.util.List;
import java.util.Objects;


/**
 * A simple {@link Fragment} subclass.
 */
public class MyDocumentFragment extends Fragment implements SubjectAdapter.SubjectInterfaceCallback {
    private static final String TAG = MyDocumentFragment.class.getSimpleName();
    private View view;
    private Context context;
    private FirebaseUser currentUser;
    private DatabaseReference mSubjectsData;
    private SubjectAdapter subjectAdapter;
    private List<Bean_Subject> subjectList;

    public MyDocumentFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_my_document, container, false);
        context = inflater.getContext();

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();

        mSubjectsData = firebaseDatabase.getReference().child("Subjects");
        mSubjectsData.keepSynced(true);

        loadData();

        return view;
    }

    private void loadData() {
        final RecyclerView subRecylerView = view.findViewById(R.id.document_recycler_view);
        subRecylerView.setHasFixedSize(true);
        subRecylerView.setLayoutManager(new GridLayoutManager(context, 2));

        if (currentUser != null){

            subjectList = new ArrayList<>();
            subjectAdapter = new SubjectAdapter(context, subjectList);
            subjectAdapter.setCallback(this);

            mSubjectsData.child(currentUser.getUid()).addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                    try {
                        JSONObject jsonObject = new JSONObject(dataSnapshot.getValue().toString());

                        Bean_Subject bean_subject = new Bean_Subject();
                        bean_subject.setName(jsonObject.getString("name"));
                        bean_subject.setSubjectId(dataSnapshot.getKey());

                        subjectList.add(bean_subject);
                        subRecylerView.setAdapter(subjectAdapter);
                        subjectAdapter.notifyDataSetChanged();

                    } catch (JSONException e) {
                        e.printStackTrace();
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

    @Override
    public void showOption(final int position, View view) {
        //Show Popup to Rename or Delete or Edit
        PopupMenu popupMenu = new PopupMenu(context, view);
        popupMenu.getMenuInflater().inflate(R.menu.menu_subject_edit,popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()){
                    case R.id.rename:
                        showRenameDialog(position);
                        break;

                    case R.id.delete:
                        showRemoveDialog(position);
                        break;
                }
                return true;
            }
        });
        popupMenu.show();
    }

    private void showRemoveDialog(final int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Delete Subject");
        builder.setMessage("Do you want to delete subject ?");
        builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                mSubjectsData.child(currentUser.getUid()).child(subjectList.get(position).getSubjectId())
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        mSubjectsData.child(currentUser.getUid()).child(dataSnapshot.getKey()).removeValue();

                        subjectList.remove(position);
                        subjectAdapter.notifyItemRemoved(position);
                        subjectAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.create();
        builder.show();

    }

    private void showRenameDialog(final int position) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);

        final View dialogView = LayoutInflater.from(context).inflate(R.layout.subjects_dialog, null);
        final EditText etSubjectName = dialogView.findViewById(R.id.et_subject_name);

        dialogBuilder.setView(dialogView);
        dialogBuilder.setTitle("Update Subject Name");
        dialogBuilder.setPositiveButton("Update", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(final DialogInterface dialog, int which) {

                if (AppConstant.isInternetConnected(context)){
                    final String subjectName = etSubjectName.getText().toString().trim();

                    mSubjectsData.child(currentUser.getUid()).child(subjectList.get(position).getSubjectId())
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    Bean_Subject bean_subject = new Bean_Subject();
                                    bean_subject.setName(subjectName);
                                    mSubjectsData.child(currentUser.getUid())
                                            .child(Objects.requireNonNull(dataSnapshot.getKey()))
                                            .setValue(bean_subject).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()){
                                                Toast.makeText(context, "Updated Successfully !",
                                                        Toast.LENGTH_SHORT).show();

                                                subjectAdapter.updateName(subjectName, position);

                                            }else {
                                                Toast.makeText(context, "Error While Creating Subject Try Again!",
                                                        Toast.LENGTH_SHORT).show();

                                            }
                                            dialog.dismiss();
                                        }
                                    });

                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });

                }else {
                    Toast.makeText(context, "Check your internet connection", Toast.LENGTH_SHORT).show();
                }

            }
        });

        dialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        dialogBuilder.create();
        dialogBuilder.show();
    }


}
