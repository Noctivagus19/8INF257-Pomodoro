package uqac.dim.pomodoro;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import uqac.dim.pomodoro.entities.Category;
import uqac.dim.pomodoro.entities.PomodoroDB;
import uqac.dim.pomodoro.entities.Timer;
import uqac.dim.pomodoro.entities.Todo;

public class MainActivity extends AppCompatActivity {

    private static final int LAUNCH_MANAGETODOS_ACTIVITY = 1;
    private boolean mShouldUnbind;
    private CountdownTimerService mBoundService;

    private TimerStatusReceiver receiver;
    private String timerStatus = "STOPPED";

    private PomodoroDB pdb;
    private Todo todo;
    private Timer timer;
    private Category category;

    private List<Todo> todos;
    private List<Timer> timers;
    private List<Category> categories;

    private int workPbId;
    private int pausePbId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        workPbId = R.drawable.circular_progress_bar_work;
        pausePbId = R.drawable.circular_progress_bar_pause;

        receiver = new TimerStatusReceiver();

        findViewById(R.id.leftButton)
                .setOnClickListener((View.OnClickListener) this::onLeftClick);
        findViewById(R.id.rightButton)
                .setOnClickListener((View.OnClickListener) this::onRightClick);


        pdb = PomodoroDB.getDatabase(getApplicationContext());
        initializeTimer();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    private void testCreateCompletedPomodoros() {
        for(int i=0;i<4;i++) {
            Todo todo = new Todo("test", "2021-05-02", pdb.categoryDao().getActiveCategories().get(0).getId());
            todo.setCompletionTime(60000);
            pdb.todoDao().addTodo(todo);
        }

        for(int i=0;i<2;i++) {
            Todo todo = new Todo("test", "2021-05-01", pdb.categoryDao().getActiveCategories().get(0).getId());
            todo.setCompletionTime(80000);
            pdb.todoDao().addTodo(todo);
        }
    }

    private Timer testCreateTimer() {
        pdb.timerDao().addTimer(
            new Timer("Test timer ",2*60000, 60000, 5*60000, 4)
        );
        List<Timer> timers = pdb.timerDao().getAllTimers();
        Timer timer = timers.get(0);
        timer.setActive();
        pdb.timerDao().updateTimer(timer);
        return timer;
    }

    private void testInsertion() {
        pdb.timerDao().addTimer(new Timer( "Test timer", 1000000000, 10000, 100000,100000));
        for (Timer timer : pdb.timerDao().getAllTimers()) {
            Log.i("LOG", "INSERTION TIMER : " + timer.toString());
        }
        timers = pdb.timerDao().getAllTimers();


        pdb.categoryDao().addCategory(new Category( "Work", "Active"));
        pdb.categoryDao().addCategory(new Category( "Personnal", "Active"));
        for (Category category: pdb.categoryDao().getAllCategories()) {
            Log.i("LOG", "INSERTION CATEGORY : " + category.toString());
        }
        categories = pdb.categoryDao().getAllCategories();


        String currentDate = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date());
        pdb.todoDao().addTodo(new Todo( "Ceci est un Todo", currentDate, categories.get(0).getId()));
        pdb.todoDao().addTodo(new Todo( "Bounty hunt d'un rogue squirrel de Jonquière", currentDate, categories.get(0).getId()));
        pdb.todoDao().addTodo(new Todo( "Ceci est un autre Todo", currentDate, categories.get(0).getId()));
        for (Todo todo: pdb.todoDao().getAllTodos()) {
            Log.i("LOG", "INSERTION TODO: " + todo.toString());
        }
        todos = pdb.todoDao().getAllTodos();
    }

    private void testRecherche() {
        Log.i("LOG", "Recherche TODO DANS LISTE : " + todos.get(0).toString());
        todo = pdb.todoDao().findById(todos.get(0).getId());
        Log.i("LOG", "Recherche LE MEME TODO DANS DB : " + todo.toString());

        category = pdb.categoryDao().findById(todo.getCategoryId());
        Log.i("LOG", "Category of TODO : " + category.toString());

        Log.i("LOG", "Un timer dans la liste: " + timers.get(0).toString());
        timer = pdb.timerDao().findById(timers.get(0).getId());
        Log.i("LOG", "Le meme timer dans la bd: " + timer.toString());
    }

    private void testUpdate() {
        todo = pdb.todoDao().findById(todos.get(0).getId());
        Log.i("LOG", "Todo actuel : " + todo.toString());

        todos.get(0).setDescription("Desctiprion updatée !");
        pdb.todoDao().updateTodo(todos.get(0));
        todo = pdb.todoDao().findById(todos.get(0).getId());
        Log.i("LOG", "Update TODO : " + todo.toString());

        timers.get(0).setWorkMs(500000000);
        pdb.timerDao().updateTimer(timers.get(0));
        timer = pdb.timerDao().findById(timers.get(0).getId());
        Log.i("LOG", "Update Timer : " + timer.toString());
    }

    private void testDelete() {
        pdb.todoDao().deleteTodo(todos.get(0));
        for (Todo todo: pdb.todoDao().getAllTodos()) {
            Log.i("LOG", "Apres delete TODO: " + todo.toString());
        }
        todos = pdb.todoDao().getAllTodos();
    }

    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this)
                .registerReceiver(receiver, new IntentFilter(CountdownTimerService.TIME_INFO));
        setTimerWithTopTask();
    }

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
    }

    @Override
    protected void onDestroy() {
        pdb.destroyInstance();
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
                skipTask();
                break;
        }
    }

    public void startTimer() {
        doBindService();
        Intent intent = new Intent(this, CountdownTimerService.class);
        intent.putExtra("TIMERTYPE", "WORK");
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
    }

    public void skipTask() {
        mBoundService.skipTask();
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
            Log.e("LOG", "Error: The requested service doesn't " +
                    "exist, or this client isn't allowed access to it.");
        }
    }

    void doUnbindService() {
        if (mShouldUnbind) {
            unbindService(mConnection);
            mShouldUnbind = false;
        }
    }

    public void startManageTodosActivity(MenuItem menuItem) {
        startActivity(new Intent(this, ManageTodosActivity.class));
    }

    public void startManageTimersActivity(MenuItem menuItem) {
        startActivity(new Intent(this, ManageTimersActivity.class));
    }

    public void startStatsActivity(MenuItem menuItem) {
        startActivity(new Intent(this, StatsActivity.class));
    }

    private void setButtons() {
        Button leftBtn = ((Button)findViewById(R.id.leftButton));
        Button rightBtn = ((Button)findViewById(R.id.rightButton));
        leftBtn.setText(R.string.start_btn_lbl);
        rightBtn.setText(R.string.stop_btn_lbl);
        leftBtn.setEnabled(false);
        rightBtn.setEnabled(false);
    }

    public void initializeTimer() {
        ((TextView)findViewById(R.id.time_display)).setText("00:00:00");
        ((ProgressBar)findViewById(R.id.timer_progress_bar)).setProgress(0);
        ((TextView)findViewById(R.id.task_display)).setText("");
        ((TextView)findViewById(R.id.category_display)).setText("");
        setButtons();
        timerStatus = "STOPPED";
    }

    public void setTimerWithTopTask(){
        if (!timerStatus.equals("RUNNING")) {
            initializeTimer();
            Todo topTodo = pdb.todoDao().getTopActiveTodo();
            if (topTodo != null) {
                Timer activeTimer = pdb.timerDao().getActiveTimer();
                if (activeTimer != null) {
                    redrawProgressBar(workPbId);
                    ((TextView)findViewById(R.id.time_display)).setText(CountdownTimerService.toHms(activeTimer.workMs));
                    ((ProgressBar)findViewById(R.id.timer_progress_bar)).setProgress(100);
                    int categoryId = topTodo.getCategoryId();
                    category = pdb.categoryDao().findById(categoryId);
                    ((TextView)findViewById(R.id.category_display)).setText(truncateText(category.getName()));
                    ((TextView)findViewById(R.id.task_display)).setText(truncateText(topTodo.getDescription()));
                    setButtons();
                    ((Button)findViewById(R.id.leftButton)).setEnabled(true);
                }
            }
        }
    }

    private String truncateText(String text) {
        if (text.length() > 30) {
            return text.substring(0,26) + "...";
        }
        return text;
    }

    private class TimerStatusReceiver extends BroadcastReceiver {
       @Override
       public void onReceive(Context context, Intent intent) {
           if (intent != null && intent.getAction().equals(CountdownTimerService.TIME_INFO)) {
               if (intent.hasExtra("TIME")) {
                   ((TextView)findViewById(R.id.time_display))
                           .setText(intent.getStringExtra("TIME"));
               }

               if (intent.hasExtra("RAWTIME") && intent.hasExtra("TOTALTIME")) {
                   String rawTime = intent.getStringExtra("RAWTIME");
                   String sTotalTime = intent.getStringExtra("TOTALTIME");
                   if (rawTime != null) {
                       double totalTime = Double.parseDouble(sTotalTime);
                       int thousand = Integer.parseInt(rawTime)/1000*1000;

                       int completionPercentage = (int)(Math.round(((long) thousand / totalTime) * 100.0));
                       ((ProgressBar) findViewById(R.id.timer_progress_bar))
                               .setProgress(completionPercentage);
                   }
               }

               if (intent.hasExtra("STATUS")) {
                   timerStatus = intent.getStringExtra("STATUS");
               }

               if (intent.hasExtra("TIMERTYPE")) {
                   String timerType = intent.getStringExtra("TIMERTYPE");
                   if (timerStatus.equals("STARTED")) {
                       if (timerType.equals("WORK")) {
                           redrawProgressBar(workPbId);
                       } else {
                           redrawProgressBar(pausePbId);
                       }
                   } else if (timerStatus.equals("COMPLETED") && timerType.equals("PAUSE")) {
                       setTimerWithTopTask();
                   }
               }

               if (timerStatus.equals("STOPPED")) {
                   setTimerWithTopTask();
               }
           }
       }
    }

    private void redrawProgressBar(int drawableId) {
        Drawable d = ResourcesCompat.getDrawable(getResources(), drawableId, getApplicationContext().getTheme());
        ((ProgressBar)findViewById(R.id.timer_progress_bar)).setProgressDrawable(d);
    }
}