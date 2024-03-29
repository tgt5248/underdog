package com.underdog.raver;

import static android.content.ContentValues.TAG;
import static com.arthenica.mobileffmpeg.Config.RETURN_CODE_CANCEL;
import static com.arthenica.mobileffmpeg.Config.RETURN_CODE_SUCCESS;
import static java.lang.String.format;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.MediaController;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import com.arthenica.mobileffmpeg.Config;
import com.arthenica.mobileffmpeg.ExecuteCallback;
import com.arthenica.mobileffmpeg.FFmpeg;
import com.arthenica.mobileffmpeg.LogCallback;
import com.arthenica.mobileffmpeg.LogMessage;
import com.arthenica.mobileffmpeg.Statistics;
import com.arthenica.mobileffmpeg.StatisticsCallback;

import java.io.File;
import java.util.Arrays;
import java.util.TimerTask;

public class mixing extends AppCompatActivity {

    private SeekBar change_vol, play_vol, play_mix, filter1,filter2, filter3, filter4, filter5;
    private Button btn_set_vol, btn_set_mix, btn_play_vol, btn_play_mix;
    Button btn_merge, btn_save;
    Button btn1, btn2,btn3,btn4,btn5;
    TextView num_vol, num1,num2,num3,num4,num5, time_vol,time_mix;
    String[] command_mute, command_extract, command_vol, command_mix, command_merge, command_save, command_mp3_change;
    private MediaPlayer mediaPlayer_mp3, mediaPlayer_mp4;
    Uri uri_mp3, uri_mp4, uri_merge, uri_video, uri_ori; //바꾸는거 mp3
    private Handler handler_vol, handler_mix;
    private Runnable runnable_vol, runnable_mix ;
    String videoPath, mp3Path, mutemp4, extract,changedmp3,changedmp4, merge,save;
    String cmd_type = "none";
    int mixpreset=1;
    int bv,mv,meq,mc,mh,meh;
    String mf;

    File f_extract,f_mute,f_vol,f_mix,f_merge;
    final LoadingDialog loadingDialog = new LoadingDialog(mixing.this);

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mixing);
        getSupportActionBar().setTitle("믹싱");
        getSupportActionBar().hide();
        handler_vol = new Handler();
        handler_mix = new Handler();

        Toast.makeText(this, "동영상 자르기 완료", Toast.LENGTH_LONG).show();


        /* 파일 경로*/
        //trim된 원본영상
        videoPath = getIntent().getStringExtra("videoPath"); //trim.mp4
        //trim된 원본배경음
        mp3Path = getIntent().getStringExtra("mp3Path");  //trim.mp3
        //음소거된 영상
        mutemp4 = videoPath.replaceAll("_trim.mp4","_mute.mp4");
        //영상에서 Mp3 추출
        extract = videoPath.replaceAll("_trim.mp4","_extract.mp3");
        //볼륨조절된 배경음
        changedmp3=mp3Path.replaceAll("_trim.mp3","_changeVol.mp3");
        //믹싱된 mp4
        changedmp4=videoPath.replaceAll("_trim.mp4","_changemix.mp3");
        //같이듣기 mp3
        merge = mp3Path.replaceAll("_trim.mp3","_mergeMp3.mp3");
        //최종 저장
        save = mp3Path.replaceAll("_trim.mp3", "_final.mp4");



        uri_video = Uri.parse(videoPath);
        uri_ori = Uri.parse(mp3Path);
        uri_mp3 = Uri.parse(changedmp3);
        uri_mp4 = Uri.parse(changedmp4);
        uri_merge = Uri.parse(merge);

        num_vol=findViewById(R.id.num_vol); //옆에 뜨는 숫자
        change_vol=findViewById(R.id.seek_bgm);
        btn_set_vol=findViewById(R.id.set_bgm);
        btn_play_vol=findViewById(R.id.play_bgm);
        play_vol=findViewById(R.id.play_bgm_seek);
        btn_merge = findViewById(R.id.btn_merge);
        btn_save = findViewById(R.id.btn_save);
        num1=findViewById(R.id.num1);
        num2=findViewById(R.id.num2);
        num3=findViewById(R.id.num3);
        num4=findViewById(R.id.num4);
        num5=findViewById(R.id.num5);
        btn_set_mix=findViewById(R.id.set_mix);
        btn_play_mix=findViewById(R.id.play_mix);
        play_mix=findViewById(R.id.play_mix_seek);
        btn1=findViewById(R.id.btn1);
        btn2=findViewById(R.id.btn2);
        btn3=findViewById(R.id.btn3);
        btn4=findViewById(R.id.btn4);
        btn5=findViewById(R.id.btn5);
        filter1=findViewById(R.id.filter1);
        filter2=findViewById(R.id.filter2);
        filter3=findViewById(R.id.filter3);
        filter4=findViewById(R.id.filter4);
        filter5=findViewById(R.id.filter5);

        time_vol = (TextView)findViewById(R.id.time_vol);
        time_mix = (TextView)findViewById(R.id.time_mix);


        //음소거 영상 만들기
        command_mute =new String[]{"-y","-i",videoPath,"-c:v","copy","-an",mutemp4};

        command_mp3_change = new String[]{ "-y", "-i", String.valueOf(mp3Path),"-c:v","copy", changedmp3};

        //영상에서 mp3(목소리)추출
        command_extract = new String[]{"-y","-i",videoPath,"-vn","-acodec","libmp3lame","-ar","44.1k","-ac","2","-ab","128k",extract};



        cmd_type="none";
        execffmpegBinary_merge(command_extract);
        execffmpegBinary_merge(command_mute);
        execffmpegBinary_merge(command_mp3_change);

        btn1.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary));
        btn1.setTextColor(Color.parseColor("#FFFFFF"));
        filter1.setProgress(80);
        filter2.setProgress(70);
        filter3.setProgress(90);
        filter4.setProgress(40);
        filter5.setProgress(30);


        //볼륨조절쪽
        mediaPlayer_mp3 = MediaPlayer.create(this,uri_ori);
        play_vol.setMax(mediaPlayer_mp3.getDuration());
        //볼륨값
        change_vol.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                num_vol.setText(String.valueOf(progress));
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
        //설정버튼
        btn_set_vol.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                File files = new File(mp3Path);

                if(files.exists()==true) {
                    float volvalue = Float.parseFloat(num_vol.getText().toString());
                    mediaPlayer_mp3.stop();
                    command_vol = new String[]{"-y","-i",mp3Path,"-filter:a","volume="+volvalue/100,changedmp3};
                    cmd_type= "vol";
                    execffmpegBinary_merge(command_vol);
                    btn_set_vol.setBackgroundTintList(ContextCompat.getColorStateList(getApplicationContext(), R.color.colorPrimary));
                    btn_set_vol.setTextColor(Color.parseColor("#FFFFFF"));
                    btn_merge.setBackgroundTintList(null);
                    btn_merge.setTextColor(ContextCompat.getColorStateList(getApplicationContext(), R.color.colorPrimary));
                    btn_play_vol.setText(">");
                    time_vol.setText("00:00");
                    bv= (int) volvalue;
                    loadingDialog.startLoadingDialog();
                }
                else{
                    Toast.makeText(mixing.this, "잠시후 시도해주세요!", Toast.LENGTH_SHORT).show();
                }


            }

        });
        //미디어플레이어
        mediaPlayer_mp3.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer_mp3) {
                mediaPlayer_mp3.start();
                mediaPlayer_mp3.pause();

                btn_play_vol.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        if(btn_play_vol.getText().toString().equals(">")) {
                            mediaPlayer_mp3.start();
                            btn_play_vol.setText("||");
                            changeSeekbar_vol();
                        }
                        else{
                            mediaPlayer_mp3.pause();
                            btn_play_vol.setText(">");
                        }
                    }
                });

            }
        });
        //볼륨시작
        play_vol.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(fromUser){
                    mediaPlayer_mp3.seekTo(progress);
                }
                int m = progress / 60000;
                int s = (progress % 60000) / 1000;
                String strTime = String.format("%02d:%02d", m, s);
                time_vol.setText(strTime);

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });


        /*믹싱쪽*/
        mediaPlayer_mp4 = MediaPlayer.create(this,uri_video);
        play_mix.setMax(mediaPlayer_mp4.getDuration());
        btn_set_mix.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mediaPlayer_mp4.stop();
                cmd_type = "mix";

                float num_vol = Float.parseFloat(num1.getText().toString());
                float num_eq = Float.parseFloat(num2.getText().toString());
                float num_comp = Float.parseFloat(num3.getText().toString());
                float num_low = Float.parseFloat(num4.getText().toString());
                float num_echo = Float.parseFloat(num5.getText().toString());

                switch (mixpreset) {

                    case 1:
                        mf = "BRIGHT";
                        command_mix = new String[]{"-y","-i", extract, "-map","0","-c:v","copy",
                                "-af","equalizer=f=1240:t=q:w=0.3:g=-3:m="+(num_eq+1)/100+", "+  //이퀄라이저
                                "volume="+(num_vol+1)/100+", "+ //볼륨
                                "highpass="+(num_low+1)*6+", " + //하이패스(로우컷)
                                "acompressor=threshold=-14dB:ratio=8:attack=23:release=100:makeup=1:mix="+(num_comp+1)/100+", " + //컴프
                                "aecho=0.5:0.5:200:"+(num_echo+1)/500+", " + //에코
                                "loudnorm=I=-16:LRA=11:TP=-1.5",changedmp4 }; //피크제거
                        break;

                    case 2:
                        mf = "COMPRESSED";
                        command_mix = new String[]{"-y","-i", extract, "-map","0","-c:v","copy",
                                "-af","equalizer=f=100:t=q:w=0.6:g=5:m="+(num_eq+1)/100+", "+  //이퀄라이저
                                "volume="+(num_vol+1)/100+", "+ //볼륨
                                "highpass="+(num_low+1)*6+", " + //하이패스(로우컷)
                                "acompressor=threshold=-14dB:ratio=4:attack=35:release=260:makeup=1:mix="+(num_comp+1)/100+", " + //컴프
                                "acompressor=threshold=-14dB:ratio=4:attack=10:release=61:makeup=1:mix="+(num_comp+1)/100+", " + //컴프
                                "aecho=0.5:0.5:200:"+(num_echo+1)/500+", " + //에코
                                "loudnorm=I=-16:LRA=11:TP=-1.5",changedmp4 }; //피크제거
                        break;

                    case 3:
                        mf = "WARM";
                        command_mix = new String[]{"-y","-i", extract, "-map","0","-c:v","copy",
                                "-af","equalizer=f=250:t=q:w=0.6:g=4:m="+(num_eq+1)/100+", "+  //이퀄라이저
                                "equalizer=f=520:t=q:w=0.3:g=-2.5:m="+(num_eq+1)/100+", "+  //이퀄라이저
                                "volume="+(num_vol+1)/100+", "+ //볼륨
                                "highpass="+(num_low+1)*6+", " + //하이패스(로우컷)
                                "acompressor=threshold=-14dB:ratio=6:attack=10:release=48:makeup=1:mix="+(num_comp+1)/100+", " + //컴프
                                "aecho=0.5:0.5:200:"+(num_echo+1)/500+", " + //에코
                                "loudnorm=I=-16:LRA=11:TP=-1.5",changedmp4 }; //피크제거
                        break;

                    case 4:
                        mf = "CLASSIC";
                        command_mix = new String[]{"-y","-i", extract, "-map","0","-c:v","copy",
                                "-af","equalizer=f=810:t=q:w=0.43:g=3:m="+(num_eq+1)/100+", "+  //이퀄라이저
                                "volume="+(num_vol+1)/100+", "+ //볼륨
                                "highpass="+(num_low+1)*6+", " + //하이패스(로우컷)
                                "acompressor=threshold=-14dB:ratio=4:attack=10:release=51:makeup=1:mix="+(num_comp+1)/100+", " + //컴프
                                "aecho=0.5:0.5:200:"+(num_echo+1)/500+", " + //에코
                                "loudnorm=I=-16:LRA=11:TP=-1.5",changedmp4 }; //피크제거
                        break;

                    case 5:
                        mf = "VINTAGE";
                        command_mix = new String[]{"-y","-i", extract, "-map","0","-c:v","copy",
                                "-af","equalizer=f=1000:t=q:w=0.4:g=3:m="+(num_eq+1)/100+", "+  //이퀄라이저
                                "volume="+(num_vol+1)/100+", "+ //볼륨
                                "highpass="+(num_low+1)*6+", " + //하이패스(로우컷)
                                "lowpass=16800, "+ //로우패스 (하이컷)
                                "acompressor=threshold=-14dB:ratio=4:attack=10:release=51:makeup=1:mix="+(num_comp+1)/100+", " + //컴프
                                "aecho=0.5:0.5:50:"+(num_echo+1)/500+", " + //에코
                                "loudnorm=I=-16:LRA=11:TP=-1.5",changedmp4 }; //피크제거
                        break;

                }

                File files = new File(extract);

                if(files.exists()==true) {
                    execffmpegBinary_merge(command_mix);
                    btn_set_mix.setBackgroundTintList(ContextCompat.getColorStateList(getApplicationContext(), R.color.colorPrimary));
                    btn_set_mix.setTextColor(Color.parseColor("#FFFFFF"));
                    btn_play_mix.setText(">");
                    time_mix.setText("00:00");
                    loadingDialog.startLoadingDialog();
                    mv= (int) num_vol;
                    meq=(int)num_eq;
                    mc=(int)num_comp;
                    mh=(int)num_low;
                    meh=(int)num_echo;
                }
                else{
                    Toast.makeText(mixing.this, "잠시후 시도해주세요!", Toast.LENGTH_SHORT).show();
                }

            }

        });
        mediaPlayer_mp4.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer_mp3) {
                mediaPlayer_mp4.start();
                mediaPlayer_mp4.pause();

                btn_play_mix.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        if(btn_play_mix.getText().toString().equals(">")) {
                            mediaPlayer_mp4.start();
                            btn_play_mix.setText("||");
                            changeSeekbar_mix();
                        }
                        else{
                            mediaPlayer_mp4.pause();
                            btn_play_mix.setText(">");
                        }
                    }
                });

            }
        });
        play_mix.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(fromUser){
                    mediaPlayer_mp4.seekTo(progress);
                }
                int m = progress / 60000;
                int s = (progress % 60000) / 1000;
                String strTime = String.format("%02d:%02d", m, s);
                time_mix.setText(strTime);

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });


        /*버튼, 필터*/
        filter1.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                num1.setText(String.valueOf(progress));
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
        filter2.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                num2.setText(String.valueOf(progress));
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
        filter3.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                num3.setText(String.valueOf(progress));
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
        filter4.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                num4.setText(String.valueOf(progress));
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
        filter5.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                num5.setText(String.valueOf(progress));
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        btn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btn1.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary));
                btn1.setTextColor(Color.parseColor("#FFFFFF"));
                btn2.setBackgroundColor(Color.parseColor("#D6D7D7"));
                btn2.setTextColor(Color.parseColor("#000000"));
                btn3.setBackgroundColor(Color.parseColor("#D6D7D7"));
                btn3.setTextColor(Color.parseColor("#000000"));
                btn4.setBackgroundColor(Color.parseColor("#D6D7D7"));
                btn4.setTextColor(Color.parseColor("#000000"));
                btn5.setBackgroundColor(Color.parseColor("#D6D7D7"));
                btn5.setTextColor(Color.parseColor("#000000"));
                mixpreset=1;
                filter1.setProgress(80);
                filter2.setProgress(70);
                filter3.setProgress(90);
                filter4.setProgress(40);
                filter5.setProgress(30);

            }
        });
        btn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btn2.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary));
                btn2.setTextColor(Color.parseColor("#FFFFFF"));

                btn1.setBackgroundColor(Color.parseColor("#D6D7D7"));
                btn1.setTextColor(Color.parseColor("#000000"));
                btn3.setBackgroundColor(Color.parseColor("#D6D7D7"));
                btn3.setTextColor(Color.parseColor("#000000"));
                btn4.setBackgroundColor(Color.parseColor("#D6D7D7"));
                btn4.setTextColor(Color.parseColor("#000000"));
                btn5.setBackgroundColor(Color.parseColor("#D6D7D7"));
                btn5.setTextColor(Color.parseColor("#000000"));
                mixpreset=2;
                filter1.setProgress(60);
                filter2.setProgress(60);
                filter3.setProgress(1000);
                filter4.setProgress(100);
                filter5.setProgress(20);

            }
        });
        btn3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btn3.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary));
                btn3.setTextColor(Color.parseColor("#FFFFFF"));

                btn1.setBackgroundColor(Color.parseColor("#D6D7D7"));
                btn1.setTextColor(Color.parseColor("#000000"));
                btn2.setBackgroundColor(Color.parseColor("#D6D7D7"));
                btn2.setTextColor(Color.parseColor("#000000"));
                btn4.setBackgroundColor(Color.parseColor("#D6D7D7"));
                btn4.setTextColor(Color.parseColor("#000000"));
                btn5.setBackgroundColor(Color.parseColor("#D6D7D7"));
                btn5.setTextColor(Color.parseColor("#000000"));
                mixpreset=3;
                filter1.setProgress(90);
                filter2.setProgress(20);
                filter3.setProgress(80);
                filter4.setProgress(80);
                filter5.setProgress(10);
            }
        });
        btn4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btn4.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary));
                btn4.setTextColor(Color.parseColor("#FFFFFF"));

                btn1.setBackgroundColor(Color.parseColor("#D6D7D7"));
                btn1.setTextColor(Color.parseColor("#000000"));
                btn2.setBackgroundColor(Color.parseColor("#D6D7D7"));
                btn2.setTextColor(Color.parseColor("#000000"));
                btn3.setBackgroundColor(Color.parseColor("#D6D7D7"));
                btn3.setTextColor(Color.parseColor("#000000"));
                btn5.setBackgroundColor(Color.parseColor("#D6D7D7"));
                btn5.setTextColor(Color.parseColor("#000000"));
                mixpreset=4;
                filter1.setProgress(100);
                filter2.setProgress(80);
                filter3.setProgress(70);
                filter4.setProgress(60);
                filter5.setProgress(10);
            }
        });
        btn5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btn5.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary));
                btn5.setTextColor(Color.parseColor("#FFFFFF"));

                btn1.setBackgroundColor(Color.parseColor("#D6D7D7"));
                btn1.setTextColor(Color.parseColor("#000000"));
                btn2.setBackgroundColor(Color.parseColor("#D6D7D7"));
                btn2.setTextColor(Color.parseColor("#000000"));
                btn3.setBackgroundColor(Color.parseColor("#D6D7D7"));
                btn3.setTextColor(Color.parseColor("#000000"));
                btn4.setBackgroundColor(Color.parseColor("#D6D7D7"));
                btn4.setTextColor(Color.parseColor("#000000"));
                mixpreset=5;
                filter1.setProgress(85);
                filter2.setProgress(70);
                filter3.setProgress(80);
                filter4.setProgress(40);
                filter5.setProgress(50);
            }
        });



        /*같이 듣기겸 mp3합치기*/
        btn_merge.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                File files = new File(changedmp4);

                if(files.exists()==true) {
                    cmd_type = "merge";
                    command_merge = new String[]{"-y","-i", changedmp3,"-i",changedmp4,"-filter_complex","amerge=inputs=2","-ac", "2",merge};
                    execffmpegBinary_merge(command_merge);
                    btn_merge.setBackgroundTintList(ContextCompat.getColorStateList(getApplicationContext(), R.color.colorPrimary));
                    btn_merge.setTextColor(Color.parseColor("#FFFFFF"));
                    loadingDialog.startLoadingDialog();
                }
                else{
                    Toast.makeText(mixing.this, "볼륨/믹싱 설정을 먼저 진행해주세요!", Toast.LENGTH_SHORT).show();
                }


            }
        });

        /*저장 mp3파일들(볼륨+믹싱) + 영상(음소거)*/
        btn_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                File files = new File(merge);

                if(files.exists()==true) {
                    AlertDialog.Builder builder1 = new AlertDialog.Builder(mixing.this);
                    builder1.setMessage("선택하신 값 \n\n배경 볼륨 : "+bv+"\nPreset : " + mf + "\n믹싱 Volume : " + mv +
                            "\nEQ : " + meq +"\nComp : " + mc +"\nHighPass : " + mh +"\nEcho: " + meh)
                            .setTitle("저장하시겠습니까?")
                            .setCancelable(false)
                            .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    cmd_type = "save";
                                    command_save = new String[]{"-y","-i", mutemp4, "-i", merge, "-c", "copy", save};
                                    execffmpegBinary_merge(command_save);
                                    btn_save.setBackgroundTintList(ContextCompat.getColorStateList(getApplicationContext(), R.color.colorPrimary));
                                    btn_save.setTextColor(Color.parseColor("#FFFFFF"));
                                    loadingDialog.startLoadingDialog();
                                    finish();
                                }
                            })
                            .setNegativeButton("취소", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                }
                            });
                    AlertDialog alert1 = builder1.create();
                    alert1.show();
                } else {
                    Toast.makeText(mixing.this, "같이듣기 버튼을 먼저 눌러주세요 !", Toast.LENGTH_SHORT).show();
                }




            }
        });





    }


    public void changeSeekbar_vol(){
        play_vol.setProgress(mediaPlayer_mp3.getCurrentPosition());
        if(mediaPlayer_mp3.isPlaying()){
            runnable_vol = new Runnable() {
                @Override
                public void run() {
                    changeSeekbar_vol();
                }
            };
            handler_vol.postDelayed(runnable_vol,1000);
        }
    }
    public void changeSeekbar_mix(){
        play_mix.setProgress(mediaPlayer_mp4.getCurrentPosition());
        if(mediaPlayer_mp4.isPlaying()){
            runnable_mix = new Runnable() {
                @Override
                public void run() {
                    changeSeekbar_mix();
                }
            };
            handler_mix.postDelayed(runnable_mix,1000);
        }
    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:{ //toolbar의 back키 눌렀을 때 동작
                androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
                builder.setMessage("정말 이전으로 가겠습니까?\n작업 내용이 사라질 수 있습니다.")
                        .setTitle("뒤로가기")
                        .setCancelable(false)
                        .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                finish();
                            }
                        })
                        .setNegativeButton("취소", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });
                androidx.appcompat.app.AlertDialog alert = builder.create();
                alert.show();
            }
        }
        return super.onOptionsItemSelected(item);
    }
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
                builder.setMessage("정말 이전으로 가겠습니까?\n작업 내용이 사라질 수 있습니다.")
                        .setTitle("뒤로가기")
                        .setCancelable(false)
                        .setPositiveButton("저장", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                finish();
                            }
                        })
                        .setNegativeButton("취소", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });
                androidx.appcompat.app.AlertDialog alert = builder.create();
                alert.show();
                break;
        }
        return false;
    }

    private void execffmpegBinary_merge(final String[] command) {
        Config.enableLogCallback(new LogCallback() {
            @Override
            public void apply(LogMessage message) {
                Log.e(Config.TAG, message.getText());
            }
        });

        Config.enableLogCallback(new LogCallback() {
            @Override
            public void apply(LogMessage message) {
                Log.e(Config.TAG, message.getText());
            }
        });

        Config.enableStatisticsCallback(new StatisticsCallback() {
            @Override
            public void apply(Statistics newStatistics) {

            }
        });
        Log.d(TAG, "Started command : ffmpeg " + Arrays.toString(command));


        long executionId = FFmpeg.executeAsync(command, new ExecuteCallback() {
            @Override
            public void apply(long executionId, int returnCode) {
                if (returnCode == RETURN_CODE_SUCCESS) {
                    //  progressDialog.dismiss();료
                    Toast.makeText(getApplicationContext(), "변경 완료", Toast.LENGTH_SHORT).show();

                    switch (cmd_type){

                        case "none":
                            Log.d(Config.TAG, "finished command: ffmpeg 음소거 + mp3 추출");
                            break;

                        case "vol":
                            loadingDialog.dismissDialog();
                            Log.d(Config.TAG, "finished command: ffmpeg" + Arrays.toString(command_vol));
                            mediaPlayer_mp3 = MediaPlayer.create(getApplicationContext(),uri_mp3);
                            mediaPlayer_mp3.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                                @Override
                                public void onPrepared(MediaPlayer mediaPlayer_mp3) {
                                    mediaPlayer_mp3.start();
                                    mediaPlayer_mp3.pause();

                                    btn_play_vol.setOnClickListener(new View.OnClickListener() {

                                        @Override
                                        public void onClick(View v) {
                                            if(btn_play_vol.getText().toString().equals(">")) {
                                                mediaPlayer_mp3.start();
                                                btn_play_vol.setText("||");
                                                changeSeekbar_vol();
                                            }
                                            else{
                                                mediaPlayer_mp3.pause();
                                                btn_play_vol.setText(">");
                                            }
                                        }
                                    });

                                }
                            });

                            mediaPlayer_mp3.seekTo(0);
                            play_vol.setProgress(0);
                            break;

                        case "mix":
                            loadingDialog.dismissDialog();
                            Log.d(Config.TAG, "finished command: ffmpeg" + Arrays.toString(command_mix));
                            mediaPlayer_mp4 = MediaPlayer.create(getApplicationContext(),uri_mp4);
                            mediaPlayer_mp4.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                                @Override
                                public void onPrepared(MediaPlayer mediaPlayer_mp4) {
                                    mediaPlayer_mp4.start();
                                    mediaPlayer_mp4.pause();

                                    btn_play_mix.setOnClickListener(new View.OnClickListener() {

                                        @Override
                                        public void onClick(View v) {
                                            if(btn_play_mix.getText().toString().equals(">")) {
                                                mediaPlayer_mp4.start();
                                                btn_play_mix.setText("||");
                                                changeSeekbar_mix();
                                            }
                                            else{
                                                mediaPlayer_mp4.pause();
                                                btn_play_mix.setText(">");
                                            }
                                        }
                                    });

                                }
                            });

                            mediaPlayer_mp4.seekTo(0);
                            play_mix.setProgress(0);
                            break;

                        case "merge":
                            loadingDialog.dismissDialog();
                            Log.d(Config.TAG, "finished command: ffmpeg" + Arrays.toString(command_merge));
                            mediaPlayer_mp3.seekTo(0);
                            mediaPlayer_mp4.seekTo(0);

                            play_mix.setProgress(0);
                            play_vol.setProgress(0);
                            btn_play_mix.setText(">");
                            btn_play_vol.setText(">");
                            time_mix.setText("00:00");
                            time_vol.setText("00:00");

                            btn_play_mix.callOnClick();
                            btn_play_vol.callOnClick();
                            break;
                        case "save":
                            loadingDialog.dismissDialog();
                            mediaPlayer_mp4.stop();
                            mediaPlayer_mp3.stop();
                            f_mix= new File (changedmp4);
                            f_vol = new File (changedmp3);
                            f_merge = new File (merge);
                            f_extract = new File (extract);
                            f_mute = new File (mutemp4);
                            f_mix.delete();
                            f_vol.delete();
                            f_mute.delete();
                            f_merge.delete();
                            f_extract.delete();

                            Log.d(Config.TAG, "finished command: ffmpeg" + Arrays.toString(command_save));
                            Intent intent = new Intent(mixing.this, PreviewActivity.class);
                            intent.putExtra("final_path",save);
                            startActivity(intent);
                            overridePendingTransition(0, 0);


                            break;
                    }

                } else if (returnCode == RETURN_CODE_CANCEL) {
                    Log.e(Config.TAG, "Async command execution canceled by user");
                } else {
                    Log.e(Config.TAG, format("Async command execution failed with returncode = %d", returnCode));
                }
            }
        });
    }



}

