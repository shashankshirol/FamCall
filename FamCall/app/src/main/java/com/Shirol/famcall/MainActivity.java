package com.Shirol.famcall;

import android.Manifest;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.sinch.android.rtc.PushPair;
import com.sinch.android.rtc.Sinch;
import com.sinch.android.rtc.SinchClient;
import com.sinch.android.rtc.calling.Call;
import com.sinch.android.rtc.calling.CallClient;
import com.sinch.android.rtc.calling.CallClientListener;
import com.sinch.android.rtc.calling.CallListener;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import java.util.List;

public class MainActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener{
    private static final int PERMISSIONS_CODE = 100;
    private Toast exitToast;
    public static AlertDialog caller_Dialog;
    Ringtone r;
    SinchClient sinchClient;
    private Call call;

    private class SinchCallListener implements CallListener {
        @Override
        public void onCallEnded(Call endedCall){
            //call ended by either party
            call = null;
            setVolumeControlStream(AudioManager.USE_DEFAULT_STREAM_TYPE);
            Toast.makeText(getApplicationContext(), "Call ended", Toast.LENGTH_LONG).show();
            if(r.isPlaying())
                r.stop();
            caller_Dialog.dismiss();
        }
        @Override
        public void onCallEstablished(Call establishedCall) {
            //incoming call was picked up
            setVolumeControlStream(AudioManager.STREAM_VOICE_CALL);
            caller_Dialog.setTitle("On Call");
            if(r.isPlaying())
                r.stop();
            Toast.makeText(getApplicationContext(), "Call Established", Toast.LENGTH_LONG).show();
        }

        @Override
        public void onCallProgressing(Call progressingCall) {
            //call is ringing
            Toast.makeText(getApplicationContext(), "Ringing", Toast.LENGTH_LONG).show();
        }

        @Override
        public void onShouldSendPushNotification(Call call, List<PushPair> pushPairs) {
            //don't worry about this right now
        }
    }

    private class SinchCallClientListener implements CallClientListener{
        @Override
        public void onIncomingCall(CallClient callClient, final Call incomingCall) {
                incomingCall.addCallListener(new SinchCallListener());
                r.play();
                caller_Dialog = new AlertDialog.Builder(MainActivity.this).create();
                caller_Dialog.setTitle("Incoming Call");
                caller_Dialog.setIcon(R.drawable.logo);
                caller_Dialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Reject", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                        call = incomingCall;
                        call.hangup();
                        if(r.isPlaying())
                            r.stop();
                    }
                });
                caller_Dialog.setButton(AlertDialog.BUTTON_POSITIVE, "Accept", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        call = incomingCall;
                        call.answer();
                        call.addCallListener(new SinchCallListener());
                        dialogInterface.dismiss();
                        callRecvDialog(call);
                        if (r.isPlaying())
                            r.stop();
                    }
                });
                caller_Dialog.show();
        }
    }

    private boolean loadFrag(Fragment frag){
        if(frag != null){
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, frag)
                    .commit();
            return true;
        }
        else{
            return false;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        BottomNavigationView navView = findViewById(R.id.nav_view);
        navView.setOnNavigationItemSelectedListener(this);
        sinchClient = Sinch.getSinchClientBuilder()
                .context(this)
                .userId("Shashank")
                .applicationKey("d6c92953-f408-4804-8db4-4df2d73be878")
                .applicationSecret("NXl5xhTBokG1WleICMxopw==")
                .environmentHost("clientapi.sinch.com")
                .build();
        sinchClient.setSupportCalling(true);
        sinchClient.startListeningOnActiveConnection();
        sinchClient.start();
        sinchClient.getCallClient().addCallClientListener(new SinchCallClientListener());
        loadFrag(new HomeFrag());
        Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
        r = RingtoneManager.getRingtone(getApplicationContext(), notification);
        if(ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.RECORD_AUDIO,
                    Manifest.permission.READ_PHONE_STATE, Manifest.permission.ACCESS_NETWORK_STATE}, PERMISSIONS_CODE);
        }
    }

    public void MakeCall(View v){
        if(ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED){
            switch (v.getId()){
                case R.id.imageView3:
                    if(call == null){
                        call = sinchClient.getCallClient().callUser("Shruti");
                        call.addCallListener(new SinchCallListener());
                        Toast.makeText(this, "Calling Shruti", Toast.LENGTH_SHORT).show();
                        openCallerDialog(call);
                    }
                    break;

                case R.id.imageView2:
                    if(call == null){
                        call = sinchClient.getCallClient().callUser("Mom");
                        call.addCallListener(new SinchCallListener());
                        Toast.makeText(this, "Calling Mom", Toast.LENGTH_SHORT).show();
                        openCallerDialog(call);
                    }
                    break;

                case R.id.imageView:
                    if(call == null){
                        call = sinchClient.getCallClient().callUser("Pops");
                        call.addCallListener(new SinchCallListener());
                        Toast.makeText(this, "Calling Pops", Toast.LENGTH_SHORT).show();
                        openCallerDialog(call);
                    }
                    break;
            }
        }
    }

    private void openCallerDialog(final Call calling) {
        caller_Dialog = new AlertDialog.Builder(MainActivity.this).create();
        caller_Dialog.setTitle("Making Call");
        caller_Dialog.setIcon(R.drawable.logo);
        caller_Dialog.setButton(AlertDialog.BUTTON_POSITIVE, "Hang up", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
                calling.hangup();
            }
        });
        caller_Dialog.show();
    }

    private void callRecvDialog(final Call calling) {
        caller_Dialog = new AlertDialog.Builder(MainActivity.this).create();
        caller_Dialog.setTitle("On Call");
        caller_Dialog.setIcon(R.drawable.logo);
        caller_Dialog.setButton(AlertDialog.BUTTON_POSITIVE, "Hang up", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
                calling.hangup();
            }
        });
        caller_Dialog.show();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        Fragment fr = null;
        switch(menuItem.getItemId()){
            case R.id.navigation_home:
                fr = new HomeFrag();
                break;
            case R.id.navigation_history:
                fr = new HistFrag();
                break;
            case R.id.navigation_notifications:
                fr = new NotifFrag();
                break;
            case R.id.navigation_about:
                fr = new AboutFrag();
                break;
        }
        return loadFrag(fr);
    }

    @Override
    public void onBackPressed() {
        if (exitToast == null || exitToast.getView() == null || exitToast.getView().getWindowToken() == null) {
            exitToast = Toast.makeText(this, "Press again to exit", Toast.LENGTH_LONG);
            exitToast.show();
        } else {
            exitToast.cancel();
            finish();
        }
    }
}