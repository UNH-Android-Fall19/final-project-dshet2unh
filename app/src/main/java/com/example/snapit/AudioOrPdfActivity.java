package com.example.snapit;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.itextpdf.text.Document;
import com.itextpdf.text.Font;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class AudioOrPdfActivity extends AppCompatActivity {

    TextToSpeech tts;
    TextView tvdectect;
    Button btnAudio;
    Button btnpdf;
    String venName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio_or_pdf);


        btnAudio=findViewById(R.id.audiobtn);
        btnAudio.setOnClickListener(new View.OnClickListener() {
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




        btnpdf=findViewById(R.id.pdfbtn);
        btnpdf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(Build.VERSION.SDK_INT> Build.VERSION_CODES.M)
                {
                    if(checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)== PackageManager.PERMISSION_DENIED)
                    {
                        String[] parmission={Manifest.permission.WRITE_EXTERNAL_STORAGE};
                        requestPermissions(parmission,1000);
                    }
                    else savepdf();
                }
                else savepdf();
            }

        });


        Bundle bundle = getIntent().getExtras();

//Extract the data…
        venName = bundle.getString("VENUE_NAME");

//Create the text view
        tvdectect=findViewById(R.id.textView);
        tvdectect.setText(venName);

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

        // String text = txtText.getText().toString();
        tts.speak(venName, TextToSpeech.QUEUE_FLUSH, null, null);

    }


    private  void savepdf()
    {
        Document doc=new Document();
        String mfile=new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(System.currentTimeMillis());
        String mfilepath= Environment.getExternalStorageState() +"/"+mfile+".pdf";
        Log.i("PDF","pdf"+mfilepath.toString());
        Font smallBold=new Font(Font.FontFamily.TIMES_ROMAN,12, Font.BOLD);
        try{
            PdfWriter.getInstance(doc,new FileOutputStream(mfilepath));
            doc.open();
            String mtext=tvdectect.getText().toString();
            doc.addAuthor("harikesh");
            doc.add(new Paragraph(mtext,smallBold));
            doc.close();
            Toast.makeText(this, ""+mfile+".pdf"+" is saved to "+mfilepath, Toast.LENGTH_SHORT).show();
        }
        catch (Exception e)
        {
            Toast.makeText(this,"This is Error msg : " +e.getMessage(), Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode)
        {
            case  1000:
                if(grantResults.length>0 && grantResults[0]== PackageManager.PERMISSION_GRANTED)
                {
                    savepdf();
                }
                else Toast.makeText(this, "parmission denied..", Toast.LENGTH_SHORT).show();
        }
    }


}
