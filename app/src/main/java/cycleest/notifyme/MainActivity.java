package cycleest.notifyme;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.PowerManager;


public class MainActivity extends Activity {

    private String FRAGMENT_TAG = "FRAGMENT";
    private boolean notified;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        FragmentManager fragmentManager = getFragmentManager();
        NotificationFragment ui = (NotificationFragment) fragmentManager.findFragmentByTag(FRAGMENT_TAG);
        if(ui == null){
            ui = new NotificationFragment();
            fragmentManager.beginTransaction().add(R.id.container, ui, FRAGMENT_TAG).commit();
            if (notified) {
                ui.setNotified();
            }
        }

    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (intent.getBooleanExtra("cancel", false)) {
            FragmentManager fragmentManager = getFragmentManager();
            NotificationFragment ui = (NotificationFragment) fragmentManager.findFragmentByTag(FRAGMENT_TAG);
            if (ui == null) {
                notified = true;
            } else {
                ui.setNotified();
                ui.deleteNotification();
            }
        }
    }
}
