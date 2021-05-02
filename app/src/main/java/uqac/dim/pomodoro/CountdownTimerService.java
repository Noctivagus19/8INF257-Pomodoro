package uqac.dim.pomodoro;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.util.concurrent.TimeUnit;

import uqac.dim.pomodoro.entities.PomodoroDB;
import uqac.dim.pomodoro.entities.Timer;
import uqac.dim.pomodoro.entities.Todo;

public class CountdownTimerService extends Service {
   public static final String TIME_INFO = "time_info";
   private static final String NOTIFICATION_ID_STRING = "0";
   private NotificationManager mNM;

   private CountDownTimer workTimer;

   private PomodoroDB pdb;
   private Todo todo;
   private Todo currentTodo;
   private Timer timer;
   private String HMSTIME;
   private String RAWTIME;
   private String TOTALTIME;
   private String TIMERTYPE = "WORK";
   private String STATUS;
   Timer currentTimer;

   public class LocalBinder extends Binder {
      CountdownTimerService getService() {
         return CountdownTimerService.this;
      }
   }

   @Override
   public void onCreate() {
      mNM = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
      pdb = PomodoroDB.getDatabase(getApplicationContext());

      showNotification();
   }

   @Nullable
   @Override
   public IBinder onBind(Intent intent) {
      return mBinder;
   }

   private final IBinder mBinder = new LocalBinder();

   public static String toHms(long ms) {
      String hh = String.format("%02d", TimeUnit.MILLISECONDS.toHours(ms));
      String mm = String.format("%02d", TimeUnit.MILLISECONDS.toMinutes(ms) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(ms)));
      String ss = String.format("%02d", TimeUnit.MILLISECONDS.toSeconds(ms) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(ms)));

      return hh + ":" + mm + ":" + ss;
   }

   @Override
   public int onStartCommand(Intent intent, int flags, int startId) {
      if (intent.hasExtra("TIMERTYPE")) {
         setTimerType(intent.getStringExtra("TIMERTYPE"));
      }

      currentTimer = pdb.timerDao().getActiveTimer();
      currentTodo = pdb.todoDao().getTopActiveTodo();
      if (currentTodo != null)
         startTimer();

      return START_NOT_STICKY;
   }

   private void startTimer() {
      setTimerStatus("STARTED");
      LocalBroadcastManager
              .getInstance(CountdownTimerService.this)
              .sendBroadcast(getTimerInfoIntent());

      int timerMs = 0;
      if (TIMERTYPE.equals("WORK"))
          timerMs = currentTimer.workMs;
      else
         timerMs = currentTimer.pauseMs;

      workTimer = new CountDownTimer(timerMs) {
         @Override
         public void onTick(long mMillisUntilFinished) {
            updateTimerInfo("RUNNING", mMillisUntilFinished);
            LocalBroadcastManager
                    .getInstance(CountdownTimerService.this)
                    .sendBroadcast(getTimerInfoIntent());
         }

         @Override
         public void onFinish() {
            updateTimerInfo("COMPLETED", 0);
            LocalBroadcastManager.getInstance(CountdownTimerService.this)
                    .sendBroadcast(getTimerInfoIntent());
            if (TIMERTYPE.equals("WORK")) {
               currentTodo = pdb.todoDao().getTopActiveTodo();
               consumeTask();

               toggleTimerType();
               startTimer();
            }
         }
      };
      workTimer.start();
   }

   private void consumeTask() {
      currentTodo.setCompletionTime(currentTimer.workMs);
      pdb.todoDao().updateTodo(currentTodo);
   }

   public void pauseTimer() {
      workTimer.pause();
   }

   public void resumeTimer() {
      workTimer.resume();
   }

   public void skipTask() {
      consumeTask();
      Todo task = pdb.todoDao().getTopActiveTodo();
      updateTimerInfo("STOPPED", 0);
      LocalBroadcastManager.getInstance(CountdownTimerService.this)
              .sendBroadcast(getTimerInfoIntent());
   }

   public void showNotification() {
      Intent notificationIntent = new Intent(this, MainActivity.class);
      PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
              notificationIntent, 0);

      NotificationChannel channel = new NotificationChannel(
              NOTIFICATION_ID_STRING,
              "default",
              NotificationManager.IMPORTANCE_DEFAULT
      );
      channel.setDescription("Pomodoro timer");
      mNM.createNotificationChannel(channel);

      NotificationCompat.Builder notifyBuilder =
              new NotificationCompat.Builder(this, NOTIFICATION_ID_STRING)
                      .setContentTitle("Pomodoro titmer")
                      .setSmallIcon(R.drawable.ic_launcher_foreground)
                      .setContentText("Pomodoro timer is running")
                      .setContentIntent(pendingIntent);

      mNM.notify(0, notifyBuilder.build());
   }

   @Override
   public void onDestroy() {
/*
      String nextTimerType = "";
      if (TIMERTYPE.equals("WORK")) {
         nextTimerType = "PAUSE";
         updateTimerInfo("STOPPED", 0, "WORK");
      } else if (TIMERTYPE.equals("PAUSE")) {
         nextTimerType = "WORK";
         updateTimerInfo("STOPPED", 0, "PAUSE");
      }
*/
      updateTimerInfo("STOPPED", 0);
      LocalBroadcastManager
              .getInstance(CountdownTimerService.this)
              .sendBroadcast(getTimerInfoIntent());
      mNM.cancel(Integer.parseInt(NOTIFICATION_ID_STRING));
      workTimer.cancel();
      super.onDestroy();
   }

   private void updateTimerInfo(String status, long rawTime) {
      STATUS = status;
      RAWTIME = ""+rawTime;
      HMSTIME = toHms(rawTime);
      TOTALTIME = ""+currentTimer.workMs;
   }

   private void setTimerType(String type) {
      this.TIMERTYPE = type;
   }

   private void toggleTimerType() {
      if (TIMERTYPE.equals("WORK")) {
         TIMERTYPE = "PAUSE";
      }
      else if (TIMERTYPE.equals("PAUSE")) {
         TIMERTYPE = "WORK";
      }
   }

   private void setTimerStatus(String status) {
      this.STATUS = status;
   }

   private Intent getTimerInfoIntent() {
      Intent timerInfoIntent = new Intent(TIME_INFO);

      timerInfoIntent.putExtra("TIME", HMSTIME);
      timerInfoIntent.putExtra("STATUS", STATUS);
      timerInfoIntent.putExtra("RAWTIME", RAWTIME);
      timerInfoIntent.putExtra("TOTALTIME", TOTALTIME);
      timerInfoIntent.putExtra("TIMERTYPE", TIMERTYPE);

      return timerInfoIntent;
   }

   public abstract class CountDownTimer {
      private final long mMillisInFuture;
      private final long mCountdownInterval;
      private long mStopTimeInFuture;
      private long mPauseTime;
      private boolean mCancelled = false;
      private boolean mPaused = false;


      /*
       * @param millisInFuture: Number of millis in the future from the call
       *    to {@link #start()} until the countdown is done and {@link #onFinish()}
       *    is called.
       *
       * @param countDownInterval: The interval along the way to receive
       *    {@link #onTick(long)} callbacks.
       */
      public CountDownTimer(long millisInFuture) {
         mMillisInFuture = millisInFuture;
         mCountdownInterval = 1000;
      }

      /*
       * Cancel the countdown
       *
       * Should not be called inside the CounterClass threads
       */
      public final void cancel() {
         mHandler.removeMessages(MSG);
         mCancelled = true;
      }

      /*
       * Start the timer.
       */
      public synchronized final CountDownTimer start() {
         if (mMillisInFuture <= 0) {
            onFinish();
            return this;
         }
         mStopTimeInFuture = SystemClock.elapsedRealtime() + mMillisInFuture;
         mHandler.sendMessage(mHandler.obtainMessage(MSG));
         mCancelled = false;
         mPaused = false;
         return this;
      }

      /*
       * Pause the countdown.
       */
      public long pause() {
         mHandler.removeMessages(MSG);
         mPauseTime = mStopTimeInFuture - SystemClock.elapsedRealtime();
         mPaused = true;
         setTimerStatus("PAUSED");
         LocalBroadcastManager
                 .getInstance(CountdownTimerService.this)
                 .sendBroadcast(getTimerInfoIntent());
         return mPauseTime;
      }

      /*
       * Resume the countdown.
       */
      public long resume() {
         mStopTimeInFuture = mPauseTime + SystemClock.elapsedRealtime();
         mPaused = false;
         mHandler.sendMessage(mHandler.obtainMessage(MSG));
         setTimerStatus("RUNNING");
         LocalBroadcastManager
                 .getInstance(CountdownTimerService.this)
                 .sendBroadcast(getTimerInfoIntent());
         return mPauseTime;
      }

      /*
       * Callback fired on regular interval.
       *
       * @param millisUntilFinished: The amount of time until finished.
       */
      public abstract void onTick(long mMillisUntilFinished);

      /*
       * Callback fired when the time is up.
       */
      public abstract void onFinish();

      private static final int MSG = 1;

      /*
       * Handles the counting down
       */
      private final Handler mHandler = new Handler(Looper.getMainLooper()) {

         @Override
         public void handleMessage(Message msg) {
            synchronized (CountDownTimer.this) {
               if (!mPaused) {
                  final long millisLeft = mStopTimeInFuture - SystemClock.elapsedRealtime();

                  if (millisLeft <=0) {
                     onFinish();
                  } else if (millisLeft < mCountdownInterval) {
                     sendMessageDelayed(obtainMessage(MSG), millisLeft);
                  } else {
                     long lastTickStart = SystemClock.elapsedRealtime();
                     onTick(millisLeft);

                     long delay = lastTickStart + mCountdownInterval - SystemClock.elapsedRealtime();

                     // if user's clock took more than interval to complete, skip to next interval
                     while (delay < 0) delay += mCountdownInterval;

                     if (!mCancelled) {
                        sendMessageDelayed(obtainMessage(MSG), delay);
                     }
                  }
               }
            }
         }
      };
   }
}
