package cycleest.notifyme;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.PowerManager;
import android.util.Log;
import android.view.WindowManager;

public class AlarmReceiver extends BroadcastReceiver {
    @Override public void onReceive( Context context, Intent intent )
    {
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        final PowerManager.WakeLock wakeLock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.ON_AFTER_RELEASE, "My Tag");
        wakeLock.acquire();
        //context.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        String title = intent.getStringExtra(NotificationFragment.TITLE_TAG);
        String description = intent.getStringExtra(NotificationFragment.DESCRIPTION_TAG);
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        Intent activityIntent = new Intent(context, MainActivity.class);
        activityIntent.putExtra("cancel", true);
        Notification notification = new Notification();
        notification.icon = R.mipmap.ic_launcher;
        notification.setLatestEventInfo(context, title, description, PendingIntent.getActivity(context, 0, activityIntent, PendingIntent.FLAG_UPDATE_CURRENT));
        notification.flags = Notification.DEFAULT_LIGHTS | Notification.FLAG_AUTO_CANCEL;
        Intent serviceIntent = new Intent(context, CleanupService.class);
        serviceIntent.putExtra("cancel", true);
        notification.deleteIntent = PendingIntent.getService(context, 1, serviceIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        manager.notify(1228, notification);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                wakeLock.release();
            }
        }, 10000);
    }
}
