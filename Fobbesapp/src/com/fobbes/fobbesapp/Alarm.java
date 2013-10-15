package com.fobbes.fobbesapp;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.os.PowerManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

public class Alarm extends BroadcastReceiver 
{    
	private int schedType;
	private long endTime;
     @Override
     public void onReceive(Context context, Intent intent) 
     {   
    	 String pollName = intent.getStringExtra("Pollname");    	 
         /*PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
         PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "");
         wl.acquire();*/       
         
         NotificationCompat.Builder nBuilder = new NotificationCompat.Builder(context)
											         .setSmallIcon(R.drawable.ic_launcher_logo)
											         .setContentTitle("Insight")
											         .setContentText("Your survey \""+pollName+"\" is ready for input!");
         
         nBuilder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
         nBuilder.setDefaults(Notification.DEFAULT_VIBRATE);
         Intent inputIntent = new Intent(context, InputActivity.class);
         int pollId = intent.getIntExtra("Pollid", 0);
         //Log.e("Pollid", ""+pollId);
         inputIntent.putExtra("Pollid", pollId);
         inputIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
         PendingIntent pi = PendingIntent.getActivity(context, 0, inputIntent, 0);
         nBuilder.setContentIntent(pi);
         Notification n = nBuilder.build();
         n.defaults |= Notification.DEFAULT_LIGHTS;
         n.flags |= Notification.FLAG_AUTO_CANCEL | Notification.FLAG_SHOW_LIGHTS;
         NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
         notificationManager.notify("Insight", intent.getIntExtra("Pollid", 0), n);

         //wl.release();
     }

 public void SetAlarm(Context context, Intent intent)
 {
	schedType = intent.getIntExtra("Endtime", 1);
	String pollName = intent.getStringExtra("Pollname");
     
		switch(schedType){
			case 2: //1 hour
				endTime = System.currentTimeMillis() + 3600000;
				break;
			case 3: //3 hours
				endTime = System.currentTimeMillis() + 10800000;
				break;
			case 4: //1 day
				endTime = System.currentTimeMillis() + 86400000;
				break;
			case 5: //1 week
				endTime = System.currentTimeMillis() + 604800000;
				break;
			default:
				endTime = System.currentTimeMillis();
				break;
		}
	/*Log.e("Alarm", ""+endTime);	
	 final String SOME_ACTION = "com.fobbes.fobbesapp.Alarm";
	 IntentFilter intentFilter = new IntentFilter(SOME_ACTION);
	 context.registerReceiver(this, intentFilter);*/
	 
    AlarmManager am=(AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
    Intent i = new Intent("POLL_NOTIFY");
    int pollId = intent.getIntExtra("Pollid", 0);
    i.putExtra("Pollid", pollId);
    i.putExtra("Pollname", pollName);
    PendingIntent pi = PendingIntent.getBroadcast(context, (pollId*10)+schedType, i, 0);
    am.set(AlarmManager.RTC, endTime, pi); // Millisec * Second * Minute
 }

 public void CancelAlarm(Context context)
 {
     Intent intent = new Intent(context, Alarm.class);
     PendingIntent sender = PendingIntent.getBroadcast(context, 0, intent, 0);
     AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
     alarmManager.cancel(sender);
 }
}