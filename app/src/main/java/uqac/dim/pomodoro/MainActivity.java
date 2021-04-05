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
import android.widget.Toast;

import org.w3c.dom.Text;

import java.util.List;

import uqac.dim.pomodoro.CountdownTimerService;
import uqac.dim.pomodoro.entities.Category;
import uqac.dim.pomodoro.entities.PomodoroDB;
import uqac.dim.pomodoro.entities.Timer;
import uqac.dim.pomodoro.entities.Todo;

public class MainActivity extends AppCompatActivity {

    private boolean mShouldUnbind;
    private CountdownTimerService mBoundService;

    private TimerStatusReceiver receiver;
    private String timerStatus = "STOPPED";

    private PomodoroDB pdb;
    private Todo todo;
    private Timer timer;
    private Category category;

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

        pdb = PomodoroDB.getDatabase(getApplicationContext());
        testInsertion();
        testRecherche();

    }

    private void testInsertion() {
        //List<Timer> timers = pdb.timerDao().getAllTimers();
        pdb.timerDao().addTimer(new Timer(1, 1000000000, 10000, 100000,100000));
        for (Timer timer : pdb.timerDao().getAllTimers())
            Toast.makeText(this, "INSERTION : " + timer.toString(), Toast.LENGTH_SHORT).show();

        //List<Category> categories = pdb.categoryDao().getAllCategories();
        pdb.categoryDao().addCategory(new Category(1, "Work", "Active"));
        for (Category category: pdb.categoryDao().getAllCategories())
            Toast.makeText(this, "INSERTION : " + category.toString(), Toast.LENGTH_SHORT).show();

        pdb.todoDao().addTodo(new Todo(1, "Ceci est un Todo", 1, 1));
        for (Todo todo: pdb.todoDao().getAllTodos())
            Toast.makeText(this, "INSERTION : " + todo.toString(), Toast.LENGTH_SHORT).show();
    }

    private void testRecherche() {

        timer = pdb.timerDao().findById(1);
        Toast.makeText(this, "RECHERCHE: " + timer.toString(), Toast.LENGTH_SHORT).show();

        category = pdb.categoryDao().findById(1);
        Toast.makeText(this, "RECHERCHE: " + category.toString(), Toast.LENGTH_SHORT).show();

        todo = pdb.todoDao().findById(1);
        Toast.makeText(this, "RECHERCHE: " + todo.toString(), Toast.LENGTH_SHORT).show();
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