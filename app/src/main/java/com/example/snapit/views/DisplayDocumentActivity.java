package com.example.snapit.views;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.snapit.R;
import com.github.barteksc.pdfviewer.PDFView;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Objects;

public class DisplayDocumentActivity extends AppCompatActivity {

    private PDFView pdfView;
    public String  imageUrl;
    public  Bitmap myBitmap;
    String subjectName;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_document);

        Intent intent = getIntent();
        String docName = intent.getStringExtra("docName");
        String type = intent.getStringExtra("docType");
        final String url = intent.getStringExtra("docUrl");
        subjectName = intent.getStringExtra("subjectName");

        Toolbar toolbar = findViewById(R.id.display_doc_toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle(docName);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });


        pdfView = findViewById(R.id.pdf_view);
        final ImageView imgView = findViewById(R.id.img_doc);

        if (type != null) {
            if (type.equalsIgnoreCase("Pdf")) {
                pdfView.setVisibility(View.VISIBLE);
                imgView.setVisibility(View.GONE);

                new RetrievePDFStream().execute(url);

            } else {
                pdfView.setVisibility(View.GONE);
                imgView.setVisibility(View.VISIBLE);
                Log.e("imageUri", "" + url.toString());
                imageUrl=url;
                Picasso.get().load(url)
                        .into(imgView);

            }
        }


        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);


    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_ocr_function, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if (item.getItemId() == R.id.grab_img_text) {
            try {

                URL urlimage=new URL(imageUrl);
                HttpURLConnection connection = (HttpURLConnection) urlimage.openConnection();
                connection.setDoInput(true);
                connection.connect();
                InputStream input = connection.getInputStream();
                myBitmap = BitmapFactory.decodeStream(input);
                runTextRecognition(myBitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        return super.onOptionsItemSelected(item);
    }



    private void runTextRecognition(Bitmap images) {
            FirebaseVisionImage imageFirebase = FirebaseVisionImage.fromBitmap(images);
            Log.e("uri",""+imageFirebase);
            FirebaseVisionTextRecognizer detector = FirebaseVision.getInstance().getOnDeviceTextRecognizer();
            detector.processImage(imageFirebase).addOnSuccessListener(
                    new OnSuccessListener<FirebaseVisionText>() {
                        @Override
                        public void onSuccess(FirebaseVisionText texts) {
                            processTextRecognitionResult(texts);
                        }
                    });
    }
    @SuppressLint("SetTextI18n")
    private void processTextRecognitionResult(FirebaseVisionText texts) {
        Toast.makeText(this, "process text recognition method", Toast.LENGTH_SHORT).show();


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

        Intent intentToAudio = new Intent(getApplicationContext(),AudioOrPdfActivity.class);
        Bundle bundle = new Bundle();
        //Add your data from getFactualResults method to bundle
        bundle.putString("VENUE_NAME", sb.toString());
        bundle.putString("SUBJECT_NAME", subjectName);
        //Add the bundle to the intent
        intentToAudio.putExtras(bundle);
        startActivity(intentToAudio);

    }
    class RetrievePDFStream extends AsyncTask<String, Void, InputStream> {
        @Override
        protected InputStream doInBackground(String... strings) {
            InputStream inputStream = null;

            try {

                URL urlx = new URL(strings[0]);
                HttpURLConnection urlConnection = (HttpURLConnection) urlx.openConnection();
                if (urlConnection.getResponseCode() == 200) {
                    inputStream = new BufferedInputStream(urlConnection.getInputStream());

                }
            } catch (IOException e) {
                return null;
            }
            return inputStream;

        }

        @Override
        protected void onPostExecute(InputStream inputStream) {
            pdfView.fromStream(inputStream).load();
        }
    }
}
