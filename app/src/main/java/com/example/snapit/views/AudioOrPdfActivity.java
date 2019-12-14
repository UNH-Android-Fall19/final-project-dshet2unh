package com.example.snapit.views;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.speech.tts.TextToSpeech;
import android.speech.tts.Voice;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.snapit.R;
import com.itextpdf.text.Document;
import com.itextpdf.text.Font;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Objects;

public class AudioOrPdfActivity extends AppCompatActivity {

    private TextToSpeech tts;
    private TextView tvdectect;
    private Button btnAudio;
    private Button btnpdf;
    private String venName, subjectName;
    private Button btnspeech;
    private Button btnback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio_or_pdf);

        Toolbar toolbar = findViewById(R.id.toolbaraudio);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("File");

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        btnspeech = findViewById(R.id.btn_speech);
        btnspeech.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tts = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
                    @Override
                    public void onInit(int status) {
                        if (status == TextToSpeech.SUCCESS) {

                            int result = tts.setLanguage(Locale.US);

                            if (result == TextToSpeech.LANG_MISSING_DATA
                                    || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                                Log.e("TTS", "This Language is not supported");

                            } else {
                                speakOut();
                            }

                        } else {
                            Log.e("TTS", "Initilization Failed!");
                        }

                    }
                });
            }
        });


        btnpdf = findViewById(R.id.pdf_btn);
        btnpdf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
                    if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
                        String[] parmission = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
                        requestPermissions(parmission, 1000);
                    } else savepdf();
                } else savepdf();
            }

        });

        btnAudio=findViewById(R.id.btn_audio);
        btnAudio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tts = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
                    @Override
                    public void onInit(int status) {
                        if (status == TextToSpeech.SUCCESS) {

                            int result = tts.setLanguage(Locale.US);

                            if (result == TextToSpeech.LANG_MISSING_DATA
                                    || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                                Log.e("TTS", "This Language is not supported");
                            } else {
                                //speakOut();
                                SaveFile();
                            }

                        } else {
                            Log.e("TTS", "Initilization Failed!");
                        }

                    }
                });
            }
        });

        Bundle bundle = getIntent().getExtras();

        //Extract the data…
        venName = bundle.getString("VENUE_NAME");
        subjectName = bundle.getString("SUBJECT_NAME");
        Log.e("1111", "onCreate: "+ subjectName );
        //Create the text view
        tvdectect = findViewById(R.id.textView);
        tvdectect.setText(venName);

    }



    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void SaveFile() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
        {
            Locale locale = new Locale("en");

            tts.setLanguage(Locale.US);

            tts.setPitch(1.2f);
            tts.setSpeechRate(0.8f);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                tts.setVoice(new Voice("nomeVoce", locale, Voice.QUALITY_VERY_HIGH, Voice.LATENCY_NORMAL, false, null));
            }


            String mfile = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(System.currentTimeMillis());
            File f = new File(Environment.getExternalStorageDirectory() +"/Snapit/"+ subjectName + "/" );
            if (!f.exists()) {
                f.mkdir();
            }
//            File file1 = new File(f.getAbsolutePath(), mfile + ".pdf");


            String completePath =f.getAbsolutePath()
                    + "/"+ subjectName + "_" +mfile+ ".wav";
            File fileToCreate = new File(completePath);
            int r= tts.synthesizeToFile
                    (
                            venName
                            , null
                            , fileToCreate
                            , completePath
                    );
            if(r == TextToSpeech.SUCCESS){
                Toast.makeText(this, "save success！", Toast.LENGTH_SHORT).show();
            }
        }

    }

    @Override
    public void onDestroy() {
        // Don't forget to shutdown tts!
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        super.onDestroy();
    }

    private void speakOut() {
        tts.speak(venName, TextToSpeech.QUEUE_FLUSH, null, null);
    }


    private void savepdf() {
        Document doc = new Document();
        String mfile = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(System.currentTimeMillis());

        boolean success = true;
//        String ext=Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString();
        File f = new File(Environment.getExternalStorageDirectory() +"/Snapit/"+ subjectName + "/" );
        if (!f.exists()) {
            f.mkdir();
        }

        File file1 = new File(f.getAbsolutePath(), subjectName + "_" + mfile + ".pdf");
        Font smallBold = new Font(Font.FontFamily.TIMES_ROMAN, 12, Font.BOLD);
        try {
            PdfWriter.getInstance(doc, new FileOutputStream(file1));
            doc.open();
            String mtext = tvdectect.getText().toString();
            doc.addAuthor("snapit");
            doc.add(new Paragraph(mtext, smallBold));
            doc.close();
            Log.e("Storage",""+file1);
            Toast.makeText(this, ""+mfile+".pdf"+" is saved to "+file1, Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "This is Error msg : " + e.getMessage(), Toast.LENGTH_SHORT).show();
            Log.e("error", "" + e);
        }

    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1000:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    savepdf();
                } else Toast.makeText(this, "parmission denied..", Toast.LENGTH_SHORT).show();
        }
    }


}
