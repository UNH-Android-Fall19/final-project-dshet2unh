package com.example.snapit;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
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
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.snapit.adapter.DocumentAdapter;
import com.example.snapit.beans.Bean_Document;
import com.example.snapit.beans.Bean_Subject;
import com.example.snapit.constant.AppConstant;
import com.google.android.gms.tasks.Continuation;
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
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import id.zelory.compressor.Compressor;

public class DocumentListActivity extends AppCompatActivity implements DocumentAdapter.DocumentInterfaceCallback {

    private static final String TAG = DocumentListActivity.class.getSimpleName();
    private FirebaseUser currentUser;
    private FirebaseDatabase database;
    private StorageReference fileStorage;
    private DatabaseReference userDocumentData;

    private List<Bean_Document> documentList =new ArrayList<>();
    private String subjectId, subjectName;
    private static final int MY_PERMISSION_REQUEST_CODE = 100;
    private static final int GALLERY_PICK = 101;
    private ProgressDialog progressDialog;
    public RecyclerView recyclerView;
    private String uploadType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_document_list);

        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        currentUser = firebaseAuth.getCurrentUser();
        database = FirebaseDatabase.getInstance();
        fileStorage = FirebaseStorage.getInstance().getReference();

        userDocumentData = database.getReference().child("Documents");
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

        recyclerView = findViewById(R.id.docs_recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new GridLayoutManager(this,2));

        if (currentUser != null){

            dataFetchFromFirebase();

        }

    }

    private ArrayList<Bean_Document> dataFetchFromFirebase() {

        userDocumentData.child(currentUser.getUid()).child(subjectId).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                fetchData(dataSnapshot);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                fetchData(dataSnapshot);
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

        return (ArrayList<Bean_Document>) documentList;

    }

    private void fetchData(DataSnapshot dataSnapshot) {
        Bean_Document beanDocument= dataSnapshot.getValue(Bean_Document.class);

        documentList.add(beanDocument);

        DocumentAdapter documentAdapter = new DocumentAdapter(this, documentList);
        documentAdapter.setCallback(DocumentListActivity.this);
        recyclerView.setAdapter(documentAdapter);

    }

    private boolean checkPermission() {
        boolean permission = false;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
        {
            permission = true;
        }

        return permission;
    }

    private void selectOptionDialog() {

        if (checkPermission()){

            if (AppConstant.isInternetConnected(this)){

                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Choose Option");

                final CharSequence[] items = {"Camera", "Choose Existing Image","Choose PDF File"};

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

            }else {
                Toast.makeText(this, "Check your internet connection", Toast.LENGTH_SHORT).show();
            }

        }else {
            //request run time permission
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.CAMERA,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
            }, MY_PERMISSION_REQUEST_CODE);

        }

    }

    private void choosePDFFile() {
        Toast.makeText(this, "updating soon", Toast.LENGTH_SHORT).show();
        /*Intent galleryIntent = new Intent();
        galleryIntent.setType("application/pdf/*");
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(galleryIntent,"SELECT FILE"),GALLERY_PICK);
        uploadType = "Pdf";*/
    }

    private void chooseFromExistingFile() {
        Intent galleryIntent = new Intent();
        galleryIntent.setType("image/*");
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(galleryIntent,"SELECT IMAGE"),GALLERY_PICK);
        uploadType = "Gallery";
    }

    private void openCamera() {
        Toast.makeText(this, "Updating soon", Toast.LENGTH_SHORT).show();
        /*Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
        startActivity(intent);
        uploadType = "Camera";*/
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GALLERY_PICK && resultCode == RESULT_OK) {

            /*progressDialog = new ProgressDialog(DocumentListActivity.this);
            progressDialog.setTitle("Uploading Image...");
            progressDialog.setMessage("Please Wait while we upload and process the image.");
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.show();*/

            final Uri imageUri = data.getData();
          //  File thumb_filePath = new File(imageUri.getPath());
            final String current_user_id = currentUser.getUid();

            //compress images comment

        /*    try {
                Bitmap thumb_bitmap = new Compressor(this)
                        .setMaxWidth(200)
                        .setMaxHeight(200)
                        .setQuality(75)
                        .compressToBitmap(thumb_filePath);

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                thumb_bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
//                byte[] thumb_byte = baos.toByteArray();

            } catch (IOException e) {
                e.printStackTrace();
            }
*/

            Bitmap bitmap = null;

            try {
                bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                /*if (imageUri != null) {

                   // bitmap = ImageDecoder.decodeBitmap(ImageDecoder.createSource(this.getContentResolver(), imageUri));
                    ImageDecoder.Source source = ImageDecoder.createSource(this.getContentResolver(), imageUri);
                    bitmap = ImageDecoder.decodeBitmap(source);

                }*/
            } catch (IOException e) {
                e.printStackTrace();
            }

            runTextRecognition(bitmap);

            // comment temporary
            /*final StorageReference filepath = fileStorage.child("notes_images").child(Objects.requireNonNull(imageUri.getLastPathSegment()));

            filepath.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                    filepath.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                        @Override
                        public void onComplete(@NonNull Task<Uri> task) {
                            if (task.isSuccessful()){
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

                                        if (task.isSuccessful()){
                                            Toast.makeText(DocumentListActivity.this,"Document Uploaded Successfully !",
                                                    Toast.LENGTH_SHORT).show();

                                        }else {
                                            Toast.makeText(DocumentListActivity.this, "Error While Uploading Document", Toast.LENGTH_SHORT).show();
                                        }

                                        progressDialog.dismiss();

                                    }
                                });

                            }else {
                                //handle failure
                                //...
                            }
                        }
                    });
                    Log.e(TAG, "onSuccess:Upload_Image_Sucessful --- ");
                }
            });*/


            /*filepath.putFile(imageUri).continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()) {

                    }

                    // Continue with the task to get the download URL
                    return filepath.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()) {


                    } else {
                        // Handle failures
                        // ...
                    }
                }
            });*/

        }
    }

    private void runTextRecognition(Bitmap bitmap) {
        FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(bitmap);
        FirebaseVisionTextRecognizer detector = FirebaseVision.getInstance().getOnDeviceTextRecognizer();
        detector.processImage(image).addOnSuccessListener(
                new OnSuccessListener<FirebaseVisionText>() {
                    @Override
                    public void onSuccess(FirebaseVisionText texts) {
                        processTextRecognitionResult(texts);
                    }
                });
    }

    private void processTextRecognitionResult(FirebaseVisionText texts) {
        List<FirebaseVisionText.TextBlock> blocks = texts.getTextBlocks();
        if (blocks.size() == 0) {
            // text.setText("No text found!");
            Toast.makeText(getApplication(), "not found", Toast.LENGTH_SHORT).show();
            return;
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < blocks.size(); i++) {
            List<FirebaseVisionText.Line> lines = blocks.get(i).getLines();
            for (int j = 0; j < lines.size(); j++) {
                List<FirebaseVisionText.Element> elements = lines.get(j).getElements();
                for (int k = 0; k < elements.size(); k++) sb.append(elements.get(k).getText()).append(" ");
                sb.append("\n");
            }
            sb.append("\n");
        }
        Toast.makeText(this, ""+sb.toString(), Toast.LENGTH_SHORT).show();

        //next activity
        Intent intentToAudio=new Intent(getApplicationContext(), AudioOrPdfActivity.class);
        Bundle bundle = new Bundle();
        //Add your data from getFactualResults method to bundle
        bundle.putString("VENUE_NAME", sb.toString());
        //Add the bundle to the intent
        intentToAudio.putExtras(bundle);
        startActivity(intentToAudio);


        // text.setText(sb.toString());
    }

    @Override
    public void showOption(int position, View view) {

    }
}
