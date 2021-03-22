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
import android.widget.ProgressBar;
import android.widget.TextView;

import uqac.dim.pomodoro.CountdownTimerService;

public class MainActivity extends AppCompatActivity {

    private boolean mShouldUnbind;

    private CountdownTimerService mBoundService;

    private ServiceConnection mConnection = new ServiceConnection() {
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


    private TimerStatusReceiver receiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        receiver = new TimerStatusReceiver();
        doBindService();
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

    public void startTimer(View v) {
       Intent intent = new Intent(this, CountdownTimerService.class);
       startService(intent);
    }

    public void pauseTimer(View v) {
        mBoundService.pauseTimer();
    }

    public void resumeTimer(View v) {
        mBoundService.resumeTimer();
    }

    public void stopTimer(View view) {
        Intent intent = new Intent(this, CountdownTimerService.class);
        stopService(intent);
    }

    private class TimerStatusReceiver extends BroadcastReceiver {
       @Override
       public void onReceive(Context context, Intent intent) {
           if (intent != null && intent.getAction().equals(CountdownTimerService.TIME_INFO)) {
               if (intent.hasExtra("VALUE")) {
                   ((TextView)findViewById(R.id.time_display))
                           .setText(intent.getStringExtra("VALUE"));
               }

               if (intent.hasExtra("RAWTIME")) {
                   String rawTime = intent.getStringExtra("RAWTIME");
                   Log.i("DIM", rawTime);
                   if (rawTime != null) {
                       double totalTime = 10000.0;
                       int thousand = Integer.parseInt(rawTime)/1000*1000;

                       int completionPercentage = (int)(Math.round(((long) thousand / totalTime) * 100.0));
                       ((ProgressBar) findViewById(R.id.timer_progress_bar))
                               .setProgress(completionPercentage);
                   }
               }
           }
       }
    }
}