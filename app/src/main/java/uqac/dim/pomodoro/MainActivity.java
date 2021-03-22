package uqac.dim.pomodoro;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.w3c.dom.Text;

import uqac.dim.pomodoro.CountdownTimerService;

public class MainActivity extends AppCompatActivity {

    private boolean mShouldUnbind;
    private CountdownTimerService mBoundService;

    private TimerStatusReceiver receiver;
    private String timerStatus = "STOPPED";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        receiver = new TimerStatusReceiver();

        ((Button)findViewById(R.id.leftButton))
                .setOnClickListener((View.OnClickListener) this::onLeftClick);
        ((Button)findViewById(R.id.rightButton))
                .setOnClickListener((View.OnClickListener) this::onRightClick);

        initializeTimer();
    }

    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this)
                .registerReceiver(receiver, new IntentFilter(CountdownTimerService.TIME_INFO));
    }

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        doUnbindService();
    }

    protected void onLeftClick(View v) {
        Button leftBtn = ((Button)v);
        Button rightBtn = ((Button)findViewById(R.id.rightButton));
        switch (timerStatus) {
            case "STOPPED":
                leftBtn.setText(R.string.pause_btn_lbl);
                rightBtn.setEnabled(true);
                startTimer();
                break;
            case "RUNNING":
                leftBtn.setText(R.string.resume_btn_lbl);
                rightBtn.setText(R.string.done_btn_lbl);
                pauseTimer();
                break;
            case "PAUSED":
                leftBtn.setText(R.string.pause_btn_lbl);
                rightBtn.setText(R.string.stop_btn_lbl);
                resumeTimer();
                break;
        }
    }

    protected void onRightClick(View v) {
        Button leftBtn = ((Button)findViewById(R.id.leftButton));
        Button rightBtn = ((Button)v);
        switch (timerStatus) {
            case "RUNNING":
                stopTimer();
                leftBtn.setText(R.string.start_btn_lbl);
                rightBtn.setText(R.string.stop_btn_lbl);
                rightBtn.setEnabled(false);
                break;
            case "PAUSED":
                Log.i("DIM", "Done btn clicked");
                break;
        }
    }

    public void initializeTimer() {
        ((TextView)findViewById(R.id.time_display)).setText("00:10");
        ((ProgressBar)findViewById(R.id.timer_progress_bar)).setProgress(100);
        ((Button)findViewById(R.id.leftButton)).setText(R.string.start_btn_lbl);
        ((Button)findViewById(R.id.rightButton)).setText(R.string.stop_btn_lbl);
        timerStatus = "STOPPED";
    }

    public void startTimer() {
        doBindService();
        Intent intent = new Intent(this, CountdownTimerService.class);
        startService(intent);
    }

    public void pauseTimer() {
        mBoundService.pauseTimer();
    }

    public void resumeTimer() {
        mBoundService.resumeTimer();
    }

    public void stopTimer() {
        Intent intent = new Intent(this, CountdownTimerService.class);
        stopService(intent);
        doUnbindService();
        initializeTimer();
    }

    private final ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            mBoundService = ((CountdownTimerService.LocalBinder)service).getService();
        }

        public void onServiceDisconnected(ComponentName className) {
            mBoundService = null;
        }
    };

    void doBindService() {
        if (bindService(new Intent(MainActivity.this, CountdownTimerService.class),
                mConnection, Context.BIND_AUTO_CREATE)) {
            mShouldUnbind = true;
        } else {
            Log.e("MY_APP_TAG", "Error: The requested service doesn't " +
                    "exist, or this client isn't allowed access to it.");
        }
    }

    void doUnbindService() {
        if (mShouldUnbind) {
            unbindService(mConnection);
            mShouldUnbind = false;
        }
    }

    private class TimerStatusReceiver extends BroadcastReceiver {
       @Override
       public void onReceive(Context context, Intent intent) {
           if (intent != null && intent.getAction().equals(CountdownTimerService.TIME_INFO)) {
               if (intent.hasExtra("TIME")) {
                   ((TextView)findViewById(R.id.time_display))
                           .setText(intent.getStringExtra("TIME"));
               }

               if (intent.hasExtra("STATUS")) {
                   timerStatus = intent.getStringExtra("STATUS");
                   Log.i("DIM", timerStatus);
               }

               if (intent.hasExtra("RAWTIME")) {
                   String rawTime = intent.getStringExtra("RAWTIME");
                   if (rawTime != null) {
                       double totalTime = 10000.0;
                       int thousand = Integer.parseInt(rawTime)/1000*1000;

                       int completionPercentage = (int)(Math.round(((long) thousand / totalTime) * 100.0));
                       ((ProgressBar) findViewById(R.id.timer_progress_bar))
                               .setProgress(completionPercentage);
                   }
               }

               if (timerStatus.equals("COMPLETED"))
                   initializeTimer();
           }
       }
    }
}