package com.sonnysappbakery.gorillaalarm;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

//Gorilla photo by Josh Durham on Unsplash
//Chimpanzee photo by Janosch Diggelmann on Unsplash
//Orangutan photo by Dušan veverkolog on Unsplash
//Siamang photo by Joshua J. Cotten on Unsplash
//Gorilla sound by Sound Bible (Public Domain)
//Chimpanzee sound by Mike Koenig on Sound Bible (Attribution 3.0)
//No orangutan sound yet
//Gibbon sounds by Daniel Simon on Sound Bible (Attribution 3.0)

enum Ape {
    GORILLA,
    CHIMPANZEE,
    ORANGUTAN,
    GIBBON
}

public class MainActivity extends AppCompatActivity {

    ImageView imageView;
    Ape ape;
    SeekBar seekBar;
    TextView textView;
    Button button;
    CountDownTimer countDownTimer;
    MediaPlayer mediaPlayer;
    boolean timeUp = false;
    boolean ticking = false;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textView = findViewById(R.id.textView);
        imageView = findViewById(R.id.imageView);
        ape = Ape.GORILLA;


        button = findViewById(R.id.button);

        seekBar = findViewById(R.id.seekBar);
        seekBar.setMax(600); //maximum ten minutes
        seekBar.setProgress(60); //default one minute

        //unresolved issue: making user unable to move seekBar while ticking also makes seekBar's content description inaccessible
        //however, visually impaired users can consult timer's content description instead
        //added suppress annotation for now

        seekBar.setOnTouchListener((v, event) -> ticking || timeUp);

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (!ticking) {
                    long minutes = progress / 60;
                    long seconds = progress % 60;

                    establishTimerText(seconds, minutes);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        warn();
    }

    public void onClick(View view) {
        if (timeUp || ticking) {
            restart();
            return;
        }

        ticking = true;

        button.setText(R.string.stop_button);


        int initial = seekBar.getProgress();

        countDownTimer = new CountDownTimer(seekBar.getProgress() * 1000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                //unresolved bug: seekbar skips at the last second.
                seekBar.setProgress((int) (((double) millisUntilFinished / 1000 / initial) * seekBar.getMax()));
                long minutes = millisUntilFinished / 1000 / 60;
                long seconds = millisUntilFinished / 1000 % 60;

                establishTimerText(seconds, minutes);

                //timer's content description changes every second to help users who are visually impaired
                textView.setContentDescription(getString(R.string.time_left) + " " + textView.getText().toString());
            }

            @Override
            public void onFinish() {
                textView.setText(R.string.zero_time); //temporary fix to bug; otherwise, stops at 00:01
                int clip;
                int credits;
                //refactored to assign clip and credits toast in one switch statement
                switch (ape) {
                    case CHIMPANZEE:
                        clip = R.raw.chimpanzee;
                        credits = R.string.chimpanzee_credits;
                        break;
                    case ORANGUTAN:
                        //can't find orangutan clip with convenient licensing, so using same sound as gorilla for now
                        clip = R.raw.gorilla;
                        credits = R.string.orangutan_credits;
                        break;
                    case GIBBON:
                        clip = R.raw.gibbon;
                        credits = R.string.gibbon_credits;
                        break;
                    default: //default case necessary, so using this for gorilla
                        clip = R.raw.gorilla;
                        credits = R.string.gorilla_credits;
                }
                seekBar.setProgress(0);
                mediaPlayer = MediaPlayer.create(getApplicationContext(), clip);
                Toast.makeText(getApplicationContext(), credits, Toast.LENGTH_LONG).show();
                mediaPlayer.setLooping(true);
                mediaPlayer.start();
                timeUp = true;
            }
        };
        countDownTimer.start();
        warn();
    }

    public void changePic(View view) {
        if (timeUp) {
            button.setText(R.string.start_button);
            countDownTimer.cancel();
            timeUp = false;
            ticking = false;
            seekBar.setProgress(60);
            endMediaPlayer();
        }
        switch (ape) {
            case GORILLA:
                imageView.setImageResource(R.drawable.chimp);
                ape = Ape.CHIMPANZEE;
                break;
            case CHIMPANZEE:
                imageView.setImageResource(R.drawable.orangutan);
                ape = Ape.ORANGUTAN;
                break;
            case ORANGUTAN:
                imageView.setImageResource(R.drawable.gibbon);
                ape = Ape.GIBBON;
                break;
            case GIBBON:
                imageView.setImageResource(R.drawable.gorilla);
                ape = Ape.GORILLA;
                break;
        }
        warn();
    }

    public void establishTimerText(long seconds, long minutes) {
        if (minutes >= 10) {
            if (seconds >= 10) {
                StringBuilder moreMinMoreSec = new StringBuilder();
                moreMinMoreSec.append(minutes);
                moreMinMoreSec.append(":");
                moreMinMoreSec.append(seconds);
                textView.setText(moreMinMoreSec);
            } else {
                StringBuilder moreMinLessSec = new StringBuilder();
                moreMinLessSec.append(minutes);
                moreMinLessSec.append(":0");
                moreMinLessSec.append(seconds);
                textView.setText(moreMinLessSec);
            }
        } else {
            if (seconds >= 10) {
                StringBuilder lessMinMoreSec = new StringBuilder("0");
                lessMinMoreSec.append(minutes);
                lessMinMoreSec.append(":");
                lessMinMoreSec.append(seconds);
                textView.setText(lessMinMoreSec);
            } else {
                StringBuilder lessMinLessSec = new StringBuilder("0");
                lessMinLessSec.append(minutes);
                lessMinLessSec.append(":0");
                lessMinLessSec.append(seconds);
                textView.setText(lessMinLessSec);
            }
        }
    }

    public void endMediaPlayer() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
            mediaPlayer.reset();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    public void restart() {
        button.setText(R.string.start_button);
        countDownTimer.cancel();
        timeUp = false;
        ticking = false;
        seekBar.setProgress(60);
        endMediaPlayer();
    }

    public void warn() {
        Toast.makeText(this, R.string.warning, Toast.LENGTH_LONG).show();
    }
}