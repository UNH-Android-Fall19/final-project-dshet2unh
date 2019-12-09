package com.example.snapit.views;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.snapit.R;
import com.example.snapit.controllers.adapter.DocumentAdapter;
import com.example.snapit.models.Bean_Document;
import com.example.snapit.controllers.AppConstant;
import com.example.snapit.models.Bean_Subject;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

public class DocumentListActivity extends AppCompatActivity implements DocumentAdapter.DocumentInterfaceCallback {

    private static final String TAG = DocumentListActivity.class.getSimpleName();
    private FirebaseUser currentUser;
    private FirebaseDatabase database;
    private StorageReference fileStorage;
    private DatabaseReference userDocumentData;

    private List<Bean_Document> documentList = new ArrayList<>();
    private String subjectId, subjectName;
    private static final int MY_PERMISSION_REQUEST_CODE = 100;
    private static final int GALLERY_PICK = 101;
    private static final int REQUEST_CAMERA = 102;
    private ProgressDialog progressDialog;
    public RecyclerView recyclerView;
    private String uploadType;
    private EditText etSearch;
    private Button btnSearch;
    public DocumentAdapter documentAdapter;
    private String imgPath;
    Uri mImageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_document_list);

        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        currentUser = firebaseAuth.getCurrentUser();
        database = FirebaseDatabase.getInstance();
        fileStorage = FirebaseStorage.getInstance().getReference();

        userDocumentData = database.getReference("Documents");
        userDocumentData.keepSynced(true);

        Intent intent = getIntent();
        subjectId = intent.getStringExtra("subjectId");
        subjectName = intent.getStringExtra("subjectName");

        loadData();

    }

    private void loadData() {
        Toolbar toolbar = findViewById(R.id.doc_toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(subjectName);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        FloatingActionButton fab = findViewById(R.id.upload_file_fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectOptionDialog();

            }
        });

        etSearch = findViewById(R.id.et_search);
        btnSearch = findViewById(R.id.btn_search);
        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                documentList.clear();
                searchQuery();
                documentAdapter.notifyDataSetChanged();
            }
        });
        recyclerView = findViewById(R.id.docs_recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));

        if (currentUser != null) {
            dataFetchFromFirebase();

        }


    }

    private void searchQuery() {

        String edtname = etSearch.getText().toString().trim();
        final Query query = userDocumentData.child(currentUser.getUid()).child(subjectId).orderByChild("name").startAt(edtname).endAt(edtname + "\uf8ff");
        query.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                fetchData(dataSnapshot);
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

    private void dataFetchFromFirebase() {

        userDocumentData.child(currentUser.getUid()).child(subjectId).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                fetchData(dataSnapshot);
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

    private void fetchData(DataSnapshot dataSnapshot) {
        Bean_Document beanDocument = dataSnapshot.getValue(Bean_Document.class);
        if (beanDocument != null) {
            beanDocument.setImageId(dataSnapshot.getKey());
            beanDocument.setDocSubjectName(subjectName);
        }
        documentList.add(beanDocument);
        documentAdapter = new DocumentAdapter(this, documentList);
        documentAdapter.setCallback(this);
        recyclerView.setAdapter(documentAdapter);

    }

    private boolean checkPermission() {
        boolean permission = false;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)== PackageManager.PERMISSION_GRANTED  &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
        {
            permission = true;
        }

        return permission;
    }

    private void selectOptionDialog() {

        if (checkPermission()) {

            if (AppConstant.isInternetConnected(this)) {

                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Choose Option");

                final CharSequence[] items = {"Camera", "Choose Existing Image", "Choose PDF File"};

                builder.setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int item) {
                        switch (item) {
                            case 0:
                                openCamera();
                                break;
                            case 1:
                                chooseFromExistingFile();
                                break;
                            case 2:
                                choosePDFFile();
                                break;
                        }
                    }
                });
                builder.show();

            } else {
                Toast.makeText(this, "Check your internet connection", Toast.LENGTH_SHORT).show();
            }

        } else {
            //request run time permission
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.CAMERA,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE,

            }, MY_PERMISSION_REQUEST_CODE);

        }

    }

    private void choosePDFFile() {
//        Toast.makeText(this, "updating soon", Toast.LENGTH_SHORT).show();
        Intent galleryIntent = new Intent();
        galleryIntent.setType("application/pdf/*");
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(galleryIntent, "SELECT FILE"), GALLERY_PICK);
        uploadType = "Pdf";
    }

    private void chooseFromExistingFile() {
        Intent galleryIntent = new Intent();
        galleryIntent.setType("image/*");
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(galleryIntent, "SELECT IMAGE"), GALLERY_PICK);
        uploadType = "Gallery";
    }

    private void openCamera() {

        takeimage(REQUEST_CAMERA);
        uploadType = "Gallery";

    }


    private void takeimage(int requestCode) {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, setImageUri());
        startActivityForResult(cameraIntent, requestCode);
    }

    public Uri setImageUri() {
        // Store image in dcim
        File file = new File(Environment.getExternalStorageDirectory() + "/DCIM/", "image" + new Date().getTime() + ".png");
        mImageUri = Uri.fromFile(file);
        this.imgPath = file.getAbsolutePath();
//        ApiImage=imgUri.toString();
        //  ApiImage=this.imgPath;
        return mImageUri;
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == GALLERY_PICK) {

                progressDialog = new ProgressDialog(DocumentListActivity.this);
                progressDialog.setTitle("Uploading Image...");
                progressDialog.setMessage("Please Wait while we upload and process the image.");
                progressDialog.setCanceledOnTouchOutside(false);
                progressDialog.show();

                final Uri imageUri = data.getData();
                final String current_user_id = currentUser.getUid();

                final StorageReference filepath = fileStorage.child("notes_images").child(Objects.requireNonNull(imageUri.getLastPathSegment()));

                filepath.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                        filepath.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                            @Override
                            public void onComplete(@NonNull Task<Uri> task) {
                                if (task.isSuccessful()) {
                                    Toast.makeText(DocumentListActivity.this, "Added Successfully", Toast.LENGTH_SHORT).show();

                                    Uri downloadUri = task.getResult();
                                    DatabaseReference dbRef = database.getReference().child("Documents")
                                            .child(current_user_id).child(subjectId).child(AppConstant.getRandomId());

                                    Bean_Document beanImage = new Bean_Document();
                                    beanImage.setName(imageUri.getLastPathSegment());
                                    beanImage.setFileUrl(downloadUri.toString());
                                    beanImage.setType(uploadType);

                                    dbRef.setValue(beanImage).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {

                                            if (task.isSuccessful()) {
                                                Toast.makeText(DocumentListActivity.this, "Document Uploaded Successfully !",
                                                        Toast.LENGTH_SHORT).show();

                                            } else {
                                                Toast.makeText(DocumentListActivity.this, "Error While Uploading Document", Toast.LENGTH_SHORT).show();
                                            }

                                            progressDialog.dismiss();

                                        }
                                    });

                                } else {
                                    //handle failure
                                    //...
                                }
                            }
                        });
                    }
                });

            } else if (requestCode == REQUEST_CAMERA) {

                progressDialog = new ProgressDialog(DocumentListActivity.this);
                progressDialog.setTitle("Uploading Image...");
                progressDialog.setMessage("Please Wait while we upload and process the image.");
                progressDialog.setCanceledOnTouchOutside(false);
                progressDialog.show();

                final StorageReference filepath = fileStorage.child("notes_images").child(Objects.requireNonNull(mImageUri.getLastPathSegment()));

                filepath.putFile(mImageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                        filepath.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                            @Override
                            public void onComplete(@NonNull Task<Uri> task) {
                                if (task.isSuccessful()) {
                                    Toast.makeText(DocumentListActivity.this, "Added Successfully", Toast.LENGTH_SHORT).show();

                                    Uri downloadUri = task.getResult();
                                    DatabaseReference dbRef = database.getReference().child("Documents")
                                            .child(currentUser.getUid()).child(subjectId).child(AppConstant.getRandomId());

                                    Bean_Document beanImage = new Bean_Document();
                                    beanImage.setName(mImageUri.getLastPathSegment());
                                    beanImage.setFileUrl(downloadUri.toString());
                                    beanImage.setType(uploadType);

                                    dbRef.setValue(beanImage).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {

                                            if (task.isSuccessful()) {
                                                Toast.makeText(DocumentListActivity.this, "Document Uploaded Successfully !",
                                                        Toast.LENGTH_SHORT).show();

                                            } else {
                                                Toast.makeText(DocumentListActivity.this, "Error While Uploading Document", Toast.LENGTH_SHORT).show();
                                            }

                                            progressDialog.dismiss();

                                        }
                                    });

                                } else {
                                    //handle failure
                                    //...
                                }
                            }
                        });
                        Log.e(TAG, "onSuccess:Upload_Image_Sucessful --- ");
                    }
                });

            }


        }

    }


    @Override
    public void showOptionMenu(final int position, View view) {
        //Show Popup to Rename or Delete or Edit
        PopupMenu popupMenu = new PopupMenu(DocumentListActivity.this, view);
        popupMenu.getMenuInflater().inflate(R.menu.menu_subject_edit, popupMenu.getMenu());

        popupMenu.getMenu().findItem(R.id.rename).setTitle("Change file name");

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
        AlertDialog.Builder builder = new AlertDialog.Builder(DocumentListActivity.this);
        builder.setTitle("Delete Subject");
        builder.setMessage("Do you want to delete subject ?");
        builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {


                userDocumentData.child(currentUser.getUid()).child(subjectId)
                        .child(documentList.get(position).getImageId()).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        userDocumentData.child(currentUser.getUid())
                                .child(subjectId).child(dataSnapshot.getKey()).removeValue();

                        documentList.remove(position);
                        documentAdapter.notifyItemRemoved(position);
                        documentAdapter.notifyDataSetChanged();

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
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(DocumentListActivity.this);

        final View dialogView = LayoutInflater.from(DocumentListActivity.this)
                .inflate(R.layout.subjects_dialog, null);
        final EditText etFileName = dialogView.findViewById(R.id.et_subject_name);

        dialogBuilder.setView(dialogView);
        dialogBuilder.setTitle("Update Subject Name");
        dialogBuilder.setPositiveButton("Update", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(final DialogInterface dialog, int which) {

                if (AppConstant.isInternetConnected(DocumentListActivity.this)){
                    final String subjectName = etFileName.getText().toString().trim();

                    userDocumentData.child(currentUser.getUid()).child(subjectId)
                            .child(documentList.get(position).getImageId())
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    Bean_Document beanDocument = new Bean_Document();
                                    beanDocument.setName(subjectName);
                                    beanDocument.setFileUrl(documentList.get(position).getFileUrl());
                                    beanDocument.setType(documentList.get(position).getType());

                                    userDocumentData.child(currentUser.getUid())
                                            .child(subjectId)
                                            .child(Objects.requireNonNull(dataSnapshot.getKey()))
                                            .setValue(beanDocument).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {

                                            if (task.isSuccessful()){
                                                Toast.makeText(DocumentListActivity.this,
                                                        "Updated Successfully !",
                                                        Toast.LENGTH_SHORT).show();

                                                documentAdapter.updateName(subjectName, position);

                                            }else {
                                                Toast.makeText(DocumentListActivity.this,
                                                        "Error While Creating Subject Try Again!",
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
                    Toast.makeText(DocumentListActivity.this, "Check your internet connection", Toast.LENGTH_SHORT).show();
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
