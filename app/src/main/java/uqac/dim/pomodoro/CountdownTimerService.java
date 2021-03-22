package uqac.dim.pomodoro;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.util.Log;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.util.concurrent.TimeUnit;

public class CountdownTimerService extends Service {
   public static final String TIME_INFO = "time_info";
   private static final String NOTIFICATION_ID_STRING = "0";

   private CounterClass timer;

   @Override
   public void onCreate() {
      super.onCreate();
   }

   @Nullable
   @Override
   public IBinder onBind(Intent intent) {
      return null;
   }

   @Override
   public int onStartCommand(Intent intent, int flags, int startId) {
      timer = new CounterClass(5000, 1000);
      timer.start();

      startForeground(101, getNotification("Pomodoro"));

      return START_NOT_STICKY;
   }

   public Notification getNotification(String contentText) {
      Intent notificationIntent = new Intent(this, MainActivity.class);
       PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
               notificationIntent, 0);

      NotificationManager mNotifyManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);

      NotificationChannel channel = new NotificationChannel(
              NOTIFICATION_ID_STRING,
              "default",
              NotificationManager.IMPORTANCE_DEFAULT
      );
      channel.setDescription("Pomodoro timer");
      mNotifyManager.createNotificationChannel(channel);

      NotificationCompat.Builder notifyBuilder =
              new NotificationCompat.Builder(this, NOTIFICATION_ID_STRING)
              .setContentTitle("Pomodoro titmer")
              .setContentText(contentText)
              .setContentIntent(pendingIntent);

      NotificationManagerCompat nm = NotificationManagerCompat.from(this);
      return notifyBuilder.build();
   }

   @Override
   public void onDestroy() {
      timer.cancel();
      super.onDestroy();
      Intent timerInfoIntent = new Intent(TIME_INFO);
      timerInfoIntent.putExtra("Value", "Stopped");
      LocalBroadcastManager.getInstance(CountdownTimerService.this).sendBroadcast(timerInfoIntent);
   }

   public class CounterClass extends CountDownTimer {

      public CounterClass(long millisInFuture, long countDownInterval) {
         super(millisInFuture, countDownInterval);
      }

      @Override
      public void onTick(long millisUntilFinished) {
         String hh = String.format("%02d", TimeUnit.MILLISECONDS.toHours(millisUntilFinished));
         String mm = String.format("%02d", TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millisUntilFinished)));
         String ss = String.format("%02d", TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished)));

         String hms = "";

         if (!hh.equals("00")) {
            hms = hms + hh + ":";
         }

         hms = hms + mm + ":" + ss;

         Intent timerInfoIntent = new Intent(TIME_INFO);
         timerInfoIntent.putExtra("VALUE", hms);
         timerInfoIntent.putExtra("RAWTIME", ""+millisUntilFinished);
         LocalBroadcastManager.getInstance(CountdownTimerService.this)
                 .sendBroadcast(timerInfoIntent);
      }

      @Override
      public void onFinish() {
         Intent timerInfoIntent = new Intent(TIME_INFO);
         timerInfoIntent.putExtra("VALUE", "Completed");
         LocalBroadcastManager.getInstance(CountdownTimerService.this)
                 .sendBroadcast(timerInfoIntent);
      }
   }
}
