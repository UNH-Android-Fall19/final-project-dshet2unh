package com.example.snapit.views;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.widget.ImageView;

import com.example.snapit.R;
import com.github.barteksc.pdfviewer.PDFView;
import com.squareup.picasso.Picasso;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Objects;

public class DisplayDocumentActivity extends AppCompatActivity {

    private PDFView pdfView;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_document);

        Intent intent = getIntent();
        String docName = intent.getStringExtra("docName");
        String type = intent.getStringExtra("docType");
        String url = intent.getStringExtra("docUrl");


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
        ImageView imgView = findViewById(R.id.img_doc);

        if (type != null) {
            if (type.equalsIgnoreCase("Pdf")){
                pdfView.setVisibility(View.VISIBLE);
                imgView.setVisibility(View.GONE);

                new RetrievePDFStream().execute(url);

            }else {
                pdfView.setVisibility(View.GONE);
                imgView.setVisibility(View.VISIBLE);
                Picasso.get().load(url).into(imgView);

            }
        }


    }

    class RetrievePDFStream extends AsyncTask<String,Void,InputStream> {
        @Override
        protected InputStream doInBackground(String... strings) {
            InputStream inputStream=null;

            try{

                URL urlx=new URL(strings[0]);
                HttpURLConnection urlConnection=(HttpURLConnection) urlx.openConnection();
                if(urlConnection.getResponseCode()==200){
                    inputStream=new BufferedInputStream(urlConnection.getInputStream());

                }
            }catch (IOException e){
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
