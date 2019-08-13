package com.ocrapplication;

import android.Manifest;
import android.app.Activity;
import android.app.DownloadManager;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Locale;

public class ReadTextActivity extends AppCompatActivity {

    TextView readText;
    TextToSpeech textToSpeech;
    Button readTextButton, saveTextButton;
    RequestQueue requestQueue;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_speech);

        readTextButton = (Button)findViewById(R.id.ReadOcrTextButton);
        saveTextButton = (Button)findViewById(R.id.SaveOcrTextButton);

        readText = (TextView) findViewById(R.id.ocrOutputText);
        String message = this.getIntent().getStringExtra("ReadText").replace('\n', ' ');
        readText.setText(message);

        textToSpeech = new TextToSpeech(ReadTextActivity.this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status == TextToSpeech.SUCCESS) {
                    int result = textToSpeech.setLanguage(Locale.ENGLISH);

                    if(result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED){
                        Log.e("TTS", "Language not supported");
                    }
                }else{
                    Log.e("TTS", "Initialization failed");
                }
            }
        });

        readTextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "Narrating Text", Toast.LENGTH_SHORT).show();
                textToSpeech.speak(readText.getText().toString(), TextToSpeech.QUEUE_FLUSH, null);
            }
        });


        // SQLite code

        final SQLiteHandler sqLiteHandler = new SQLiteHandler(getApplicationContext());

        Button saveTextButton = (Button) findViewById(R.id.SaveOcrTextButton);
        saveTextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String saveText = readText.getText().toString();
                if(saveText.length() != 0){
                    boolean status = sqLiteHandler.addData(saveText);
                    if(status)
                        Toast.makeText(getApplicationContext(), "Successfully saved!", Toast.LENGTH_SHORT).show();
                    else
                        Toast.makeText(getApplicationContext(), "Unable to save!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Translation Text

        Button translateTextButton = (Button) findViewById(R.id.TranslateTextButton);
        translateTextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(ReadTextActivity.this,
                            new String[]{Manifest.permission.INTERNET},
                            1019);
                }

                try {
                    requestQueue = Volley.newRequestQueue(getApplicationContext());
                    detectText();
                }catch (Exception e){
                    Toast.makeText(getApplicationContext(), "URL error : " + e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void detectText() throws UnsupportedEncodingException {
        String detectURL = "https://translate.yandex.net/api/v1.5/tr.json/detect" +
                "?key=trnsl.1.1.20181007T044830Z.a0e5b383a27777ef.584c008725492f1b73ccc814d25637ceedce7578" +
                "&text="+URLEncoder.encode(readText.getText().toString(), "UTF-8");

        requestQueue.add(new StringRequest(Request.Method.POST, detectURL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        onDetectCallMe(response);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplicationContext(), "Error in detection", Toast.LENGTH_SHORT).show();
            }
        }));
    }

    private void translateText() throws UnsupportedEncodingException {

        String translateURL = "https://translate.yandex.net/api/v1.5/tr.json/translate" +
                "?key=trnsl.1.1.20181007T044830Z.a0e5b383a27777ef.584c008725492f1b73ccc814d25637ceedce7578" +
                "&text=" + URLEncoder.encode(readText.getText().toString(), "UTF-8") +
                "&lang="+ getDetectResult() +"-en";

        requestQueue.add(new StringRequest(Request.Method.POST, translateURL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                            onTranslateCallMe(response);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplicationContext(), "Error in translation", Toast.LENGTH_SHORT).show();
            }
        }));
        readText.setText(this.translateResult);
    }

    public String detectResult="Nil", translateResult="en";

    private void onDetectCallMe(String response){
        try {
            this.detectResult = new JSONObject(response).getString("lang");
        }catch (Exception e){
            Toast.makeText(getApplicationContext(), "Detection Error!", Toast.LENGTH_SHORT).show();
        }

        Toast.makeText(getApplicationContext(), "Language detected : " + this.detectResult, Toast.LENGTH_SHORT).show();
        try {
            translateText();
        }catch (Exception e){
            Toast.makeText(getApplicationContext(), "Translation error! " + e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void onTranslateCallMe(String response){
        try{
            String text = new JSONObject(response).getString("text");
            readText.setText(text.substring(2, text.length()-2));
        }catch (Exception e){
            Toast.makeText(getApplicationContext(), "Translation error!", Toast.LENGTH_SHORT).show();
        }
        Toast.makeText(getApplicationContext(), "Translated successfully!", Toast.LENGTH_SHORT).show();
    }

    private String getDetectResult(){return this.detectResult;}
}
