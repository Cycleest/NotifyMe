package cycleest.notifyme;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

public class CleanupService extends IntentService {

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public CleanupService(String name) {
        super(name);
    }

    public CleanupService(){
        super("name");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        SharedPreferences values = getApplicationContext().getSharedPreferences("preferences", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = values.edit();
        editor.remove(NotificationFragment.TITLE_TAG);
        editor.remove(NotificationFragment.DESCRIPTION_TAG);
        editor.remove(NotificationFragment.YEAR_TAG);
        editor.remove(NotificationFragment.MONTH_TAG);
        editor.remove(NotificationFragment.DAY_TAG);
        editor.remove(NotificationFragment.HOUR_TAG);
        editor.remove(NotificationFragment.MINUTE_TAG);
        editor.commit();
    }
}
