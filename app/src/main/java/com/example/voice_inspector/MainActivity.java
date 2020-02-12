package com.example.voice_inspector;


import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.user.speechrecognizationasservice.R;

import java.util.Arrays;
import java.util.List;

public class MainActivity extends Activity implements SpeechToTextUtil.SpeechToTextListener, TextToSpeechUtil.TextToSpeechListener, Button.OnClickListener {
    private static final String TAG = "MAIN_ACTIVITY";

    private TextToSpeechUtil textToSpeech;
    private SpeechToTextUtil speechToText;
    private TextView textView;
    private String state = "done";
    private EditText editText1, editText2, editText3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button button = findViewById(R.id.button);
        button.setOnClickListener(MainActivity.this);

        textView = findViewById(R.id.textViewTitle);

        editText1 = findViewById(R.id.editText);
        editText2 = findViewById(R.id.editText2);
        editText3 = findViewById(R.id.editText3);
    }

    @Override
    protected void onStart() {
        super.onStart();

        textToSpeech = new TextToSpeechUtil(this, (float) 1.7d);
        speechToText = new SpeechToTextUtil(this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.RECORD_AUDIO}, 1);
        } else {
            Toast.makeText(this, "Ready", Toast.LENGTH_LONG).show();
            speechToText.onResume();
            textToSpeech.onResume();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        speechToText.onPause();
        textToSpeech.onPause();
    }

    public void recognizerIntent() {
        speechToText.onResume();
    }

    public String spotIndicationsSilence(List<String> message) {
        String signStr = message.get(0).toLowerCase();
        switch (state) {
            case "done":
                if (message.contains("квартира")) {
                    if (message.contains("21")) {
                        textToSpeech.speak("Жду:");
                        return "electricity";
                    }
                } else if (message.contains("квартира 21")) {
                    textToSpeech.speak("Жду:");
                    recognizerIntent();
                    return "electricity";
                }
                break;
            case "electricity":
                if ((message.size() >= 1) && (signStr.matches("-?\\d+"))) {
                    processSpot(editText1, signStr);
                    return "hot";
                }
                break;
            case "hot":
                if ((message.size() >= 1) && (signStr.matches("-?\\d+"))) {
                    processSpot(editText2, signStr);
                    return "cold";
                }
                break;
            case "cold":
                if ((message.size() >= 1) && (signStr.matches("-?\\d+"))) {
                    processSpot(editText3, signStr);
                    return "done";
                }
                break;
        }
        return "done";
    }

    public void processSpot(EditText editText, String sign) {
        Toast.makeText(this, sign, Toast.LENGTH_LONG).show();
        textToSpeech.speak(sign + " следующий");
        editText.setText(String.format("%s", sign));
        recognizerIntent();
    }

    @Override
    public void onResult(String[] results) {
        List<String> recognizedResults = Arrays.asList(results);
        if (recognizedResults.contains("проверка")) {
            textToSpeech.speak("Тест пройден");
        }
        textView.setText(recognizedResults.toString());
        state = spotIndicationsSilence(recognizedResults);
    }

    @Override
    public void onError(String message, int code) {
        Log.i(TAG, message);
    }

    @Override
    public void onClick(View v) {
        recognizerIntent();
    }
}
