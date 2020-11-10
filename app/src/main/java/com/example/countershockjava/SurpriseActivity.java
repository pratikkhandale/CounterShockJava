package com.example.countershockjava;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentResolver;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.io.File;
import java.util.HashMap;

public class SurpriseActivity extends AppCompatActivity {

    ImageView imageView;

    Uri photoUri;
    Uri soundUri;

    TextToSpeech tts;
    MediaPlayer mediaPlayer;

    AudioModel audioModel;
    ImageModel imageModel;

    boolean acceptingTouches = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_surprise);

        imageView = findViewById(R.id.imageView);

        audioModel = new AudioStorer(this).getSelectedAudio();
        imageModel = new ImageStorer(this).getSelectedImage();

        if(imageModel.isAsset()){
            photoUri = ShockUtils.getDrawableUri(this, imageModel.getImgFilename());
        }else{
            photoUri = Uri.fromFile(new File(imageModel.getImgFilename()));
        }

        if(!audioModel.isTTS()){
            if(audioModel.isAsset()){
                soundUri = ShockUtils.getRawUri(this, audioModel.getAudioFilename());
            }else{
                soundUri = Uri.fromFile(new File(audioModel.getAudioFilename()));
            }
        }

        Toast.makeText(this, "Ready", Toast.LENGTH_SHORT).show();

        Window w = getWindow();
        w.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
    }

    private void showImage(){
        Glide.with(this)
                .load(photoUri)
                .into(imageView);

        imageView.setVisibility(View.VISIBLE);
    }

    private void playSoundClip(){
        mediaPlayer = MediaPlayer.create(this, soundUri);
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                finish();
            }
        });
        mediaPlayer.start();
    }

    private void handleTTS(){
        final String toSpeak = audioModel.getDescriptionMessage();
        tts = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int i) {
                if(i == TextToSpeech.SUCCESS){
                    HashMap<String, String> params =  new HashMap<>();

                    params.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "stringId");

                    if(i == TextToSpeech.SUCCESS){
                        tts.setOnUtteranceCompletedListener(new TextToSpeech.OnUtteranceCompletedListener() {
                            @Override
                            public void onUtteranceCompleted(String s) {
                                finish();
                            }
                        });
                        tts.speak(toSpeak, TextToSpeech.QUEUE_FLUSH, params);
                    }else{
                        finish();
                    }
                }
            }
        });
    }

    private void userTriggeredActions(){
        if(!acceptingTouches){
            return;
        }
        acceptingTouches = false;

        showImage();


        if(audioModel.isTTS()){
            handleTTS();
        }else{
            playSoundClip();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        userTriggeredActions();

        return super.onTouchEvent(event);

    }
}
