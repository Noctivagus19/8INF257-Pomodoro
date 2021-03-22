package uqac.dim.pomodoro;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.lang.invoke.MethodHandle;
import java.util.concurrent.TimeUnit;

public class CountdownTimerService extends Service {
   public static final String TIME_INFO = "time_info";
   private static final String NOTIFICATION_ID_STRING = "0";
   private NotificationManager mNM;

   private CountDownTimer timer;

   public class LocalBinder extends Binder {
      CountdownTimerService getService() {
         return CountdownTimerService.this;
      }
   }

   @Override
   public void onCreate() {
      mNM = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);

      showNotification();
   }

   @Nullable
   @Override
   public IBinder onBind(Intent intent) {
      return mBinder;
   }

   private final IBinder mBinder = new LocalBinder();

   @Override
   public int onStartCommand(Intent intent, int flags, int startId) {

      timer = new CountDownTimer(10000, 1000) {
         @Override
         public void onTick(long mMillisUntilFinished) {
            String hh = String.format("%02d", TimeUnit.MILLISECONDS.toHours(mMillisUntilFinished));
            String mm = String.format("%02d", TimeUnit.MILLISECONDS.toMinutes(mMillisUntilFinished) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(mMillisUntilFinished)));
            String ss = String.format("%02d", TimeUnit.MILLISECONDS.toSeconds(mMillisUntilFinished) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(mMillisUntilFinished)));

            String hms = "";

            if (!hh.equals("00")) {
               hms = hms + hh + ":";
            }

            hms = hms + mm + ":" + ss;

            Intent timerInfoIntent = new Intent(TIME_INFO);
            timerInfoIntent.putExtra("TIME", hms);
            timerInfoIntent.putExtra("STATUS", "RUNNING");
            timerInfoIntent.putExtra("RAWTIME", ""+mMillisUntilFinished);
            LocalBroadcastManager.getInstance(CountdownTimerService.this)
                    .sendBroadcast(timerInfoIntent);
         }

         @Override
         public void onFinish() {
            Intent timerInfoIntent = new Intent(TIME_INFO);
            timerInfoIntent.putExtra("STATUS", "COMPLETED");
            timerInfoIntent.putExtra("RAWTIME", "0");
            timerInfoIntent.putExtra("TIME", "00:00");
            LocalBroadcastManager.getInstance(CountdownTimerService.this)
                    .sendBroadcast(timerInfoIntent);
         }
      };
      timer.start();

      return START_NOT_STICKY;
   }

   public void pauseTimer() {
      timer.pause();
   }

   public void resumeTimer() {
      timer.resume();
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
      Intent timerInfoIntent = new Intent(TIME_INFO);
      timerInfoIntent.putExtra("STATUS", "STOPPED");
      LocalBroadcastManager.getInstance(CountdownTimerService.this).sendBroadcast(timerInfoIntent);
      //TODO: remove that integer parsing
      mNM.cancel(Integer.parseInt(NOTIFICATION_ID_STRING));
      timer.cancel();
      super.onDestroy();
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
      public CountDownTimer(long millisInFuture, long countDownInterval) {
          mMillisInFuture = millisInFuture;
          mCountdownInterval = countDownInterval;
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
         Intent timerInfoIntent = new Intent(TIME_INFO);
         timerInfoIntent.putExtra("STATUS", "PAUSED");
         LocalBroadcastManager.getInstance(CountdownTimerService.this).sendBroadcast(timerInfoIntent);
         return mPauseTime;
      }

      /*
       * Resume the countdown.
       */
      public long resume() {
         mStopTimeInFuture = mPauseTime + SystemClock.elapsedRealtime();
         mPaused = false;
         mHandler.sendMessage(mHandler.obtainMessage(MSG));
         Intent timerInfoIntent = new Intent(TIME_INFO);
         timerInfoIntent.putExtra("STATUS", "RUNNING");
         LocalBroadcastManager.getInstance(CountdownTimerService.this).sendBroadcast(timerInfoIntent);
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
