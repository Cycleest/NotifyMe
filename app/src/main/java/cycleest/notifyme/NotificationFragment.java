package cycleest.notifyme;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.Fragment;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.PowerManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;

import java.util.Calendar;
import java.util.GregorianCalendar;

public class NotificationFragment extends Fragment implements View.OnClickListener, DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener, CalendarView.OnDateChangeListener, MyTimePickerDialog.OnTimeChangeListener {

    private EditText notificationTitle;
    private EditText notificationDate;
    private EditText notificationTime;
    private EditText notificationDescription;
    private Button saveButton;
    private Button clearButton;

    private static final String dateTemplate = "%02d.%02d.%d";
    private static final String timeTemplate = "%02d:%02d";

    public static final String TITLE_TAG = "TITLE";
    public static final String DESCRIPTION_TAG = "DESCRIPTION";
    public static final String YEAR_TAG = "YEAR";
    public static final String MONTH_TAG = "MONTH";
    public static final String DAY_TAG = "DAY";
    public static final String HOUR_TAG = "HOUR";
    public static final String MINUTE_TAG = "MINUTE";
    public static final String DATE_TAG = "DATE";
    public static final String TIME_TAG = "TIME";

    private static final int EMPTY_FIELD = -1;
    private static final String EMPTY_STRING = "";

    private DatePickerDialog datePicker;
    private MyTimePickerDialog timePicker;

    private String title;
    private String description;
    private int year;
    private int month;
    private int day;
    private int hour;
    private int minute;

    private boolean datePicked;
    private boolean timePicked;

    AlarmReceiver receiver;
    boolean registered;
    boolean notified = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        receiver = new AlarmReceiver();
        if (savedInstanceState == null) {
            SharedPreferences values = getActivity().getSharedPreferences("preferences", Context.MODE_PRIVATE);
            if (values.contains(YEAR_TAG)) {
                initFields(values);
            } else {
                initEmptyFields();
            }
        } else {
            title = savedInstanceState.getString(TITLE_TAG);
            if (title == null) title = EMPTY_STRING;
            description = savedInstanceState.getString(DESCRIPTION_TAG);
            if (description == null) description = EMPTY_STRING;
            year = savedInstanceState.getInt(YEAR_TAG, EMPTY_FIELD);
            month = savedInstanceState.getInt(MONTH_TAG, EMPTY_FIELD);
            day = savedInstanceState.getInt(DAY_TAG, EMPTY_FIELD);
            hour = savedInstanceState.getInt(HOUR_TAG, EMPTY_FIELD);
            minute = savedInstanceState.getInt(MINUTE_TAG, EMPTY_FIELD);
            datePicked = savedInstanceState.getBoolean(DATE_TAG, false);
            timePicked = savedInstanceState.getBoolean(TIME_TAG, false);
        }
        getActivity().registerReceiver(receiver, new IntentFilter("cycleest.notifyme"));
        registered = true;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        return inflater.inflate(R.layout.fragment_notification, null);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        notificationTitle = (EditText) view.findViewById(R.id.notification_title);
        notificationDate = (EditText) view.findViewById(R.id.notification_date);
        notificationTime = (EditText) view.findViewById(R.id.notification_time);
        notificationDescription = (EditText) view.findViewById(R.id.notification_description);
        saveButton = (Button) view.findViewById(R.id.button);
        clearButton = (Button) view.findViewById(R.id.clearButton);

        notificationDate.setOnClickListener(this);
        notificationDate.setFocusable(false);
        notificationTime.setOnClickListener(this);
        notificationTime.setFocusable(false);
        saveButton.setOnClickListener(this);
        clearButton.setOnClickListener(this);
        datePicker = new DatePickerDialog(getActivity(), this, year, month, day);
        timePicker = new MyTimePickerDialog(getActivity(), this, hour, minute, true);
        datePicker.getDatePicker().getCalendarView().setOnDateChangeListener(this);
        timePicker.setOnTimeChangeListener(this);
        initUI();
        if (notified) {
            deleteNotification();
            notified = false;
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (datePicker.isShowing()) {
            outState.putBoolean("dateDialog", true);
        } else if (timePicker.isShowing()) {
            outState.putBoolean("timeDialog", true);
        }
        outState.putString(TITLE_TAG, title);
        outState.putString(DESCRIPTION_TAG, description);
        outState.putInt(YEAR_TAG, year);
        outState.putInt(MONTH_TAG, month);
        outState.putInt(DAY_TAG, day);
        outState.putInt(HOUR_TAG, hour);
        outState.putInt(MINUTE_TAG, minute);
        outState.putBoolean(DATE_TAG, datePicked);
        outState.putBoolean(TIME_TAG, timePicked);
    }


    @Override
    public void onViewStateRestored(Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey("dateDialog")) {
                Log.d("debug", String.valueOf(year));
                datePicker.updateDate(year, month, day);
                datePicker.show();
            } else if (savedInstanceState.containsKey("timeDialog")) {
                timePicker.updateTime(hour, minute);
                timePicker.show();
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        getActivity().unregisterReceiver(receiver);
        registered = false;
    }

    @Override
    public void onResume() {
        super.onResume();
        if(!registered) {
            getActivity().registerReceiver(receiver, new IntentFilter("cycleest.notifyme"));
        }
    }

    private void initUI() {
        notificationTitle.setText(title);
        if (year == EMPTY_FIELD) {
            notificationDate.setText(getString(R.string.empty_date));
            Calendar launchDateTime = new GregorianCalendar();
            launchDateTime.setTimeInMillis(System.currentTimeMillis());
            datePicker.updateDate(launchDateTime.get(Calendar.YEAR), launchDateTime.get(Calendar.MONTH), launchDateTime.get(Calendar.DAY_OF_MONTH));
            year = EMPTY_FIELD;
            month = EMPTY_FIELD;
            day = EMPTY_FIELD;
        } else {
            notificationDate.setText(String.format(dateTemplate, day, month + 1, year));
            datePicker.updateDate(year, month, day);
        }
        if (hour == EMPTY_FIELD) {
            notificationTime.setText(getString(R.string.empty_time));
            Calendar launchDateTime = new GregorianCalendar();
            launchDateTime.setTimeInMillis(System.currentTimeMillis());
            timePicker.updateTime(launchDateTime.get(Calendar.HOUR_OF_DAY), launchDateTime.get(Calendar.MINUTE));
            hour = EMPTY_FIELD;
            minute = EMPTY_FIELD;
        } else {
            notificationTime.setText(String.format(timeTemplate, hour, minute));
            timePicker.updateTime(hour, minute);
        }
        notificationDescription.setText(description);
    }

    private void initEmptyFields() {
        title = EMPTY_STRING;
        description = EMPTY_STRING;
        year = EMPTY_FIELD;
        month = EMPTY_FIELD;
        day = EMPTY_FIELD;
        hour = EMPTY_FIELD;
        minute = EMPTY_FIELD;
        datePicked = false;
        timePicked = false;
    }

    private void initFields(SharedPreferences values) {
        title = values.getString(TITLE_TAG, EMPTY_STRING);
        description = values.getString(DESCRIPTION_TAG, EMPTY_STRING);
        year = values.getInt(YEAR_TAG, EMPTY_FIELD);
        month = values.getInt(MONTH_TAG, EMPTY_FIELD);
        day = values.getInt(DAY_TAG, EMPTY_FIELD);
        hour = values.getInt(HOUR_TAG, EMPTY_FIELD);
        minute = values.getInt(MINUTE_TAG, EMPTY_FIELD);
        datePicked = true;
        timePicked = true;
    }

    @Override
    public void onClick(View v) {
        if (v == notificationDate) {
            datePicker.show();
        } else if (v == notificationTime) {
            timePicker.show();
        } else if (v == saveButton) {
            onSaveButtonClicked();
        } else if (v == clearButton) {
            onClearButtonClicked();
        }
    }

    @Override
    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
        this.year = year;
        this.month = monthOfYear;
        this.day = dayOfMonth;
        notificationDate.setText(String.format(dateTemplate, dayOfMonth, monthOfYear + 1, year));
        datePicked = true;
    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        this.hour = hourOfDay;
        this.minute = minute;
        notificationTime.setText(String.format(timeTemplate, hourOfDay, minute));
        timePicked = true;
    }

    private void onSaveButtonClicked() {
        title = notificationTitle.getText().toString();
        description = notificationDescription.getText().toString();
        if (notificationTitle.equals(EMPTY_STRING) || !datePicked || !timePicked) {
            Toast.makeText(getActivity(), getString(R.string.toast_empty_fields), Toast.LENGTH_LONG).show();
        } else {
            createNotification();
        }
    }

    private void onClearButtonClicked() {
        initEmptyFields();
        initUI();
        deleteNotification();
    }

    private void createNotification() {
        SharedPreferences values = getActivity().getSharedPreferences("preferences", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = values.edit();
        editor.putString(TITLE_TAG, title);
        if (!description.equals(EMPTY_STRING)) editor.putString(DESCRIPTION_TAG, description);
        editor.putInt(YEAR_TAG, year);
        editor.putInt(MONTH_TAG, month);
        editor.putInt(DAY_TAG, day);
        editor.putInt(HOUR_TAG, hour);
        editor.putInt(MINUTE_TAG, minute);
        editor.commit();

        Intent intent = new Intent(getActivity(), AlarmReceiver.class);
        intent.putExtra(TITLE_TAG, title);
        intent.putExtra(DESCRIPTION_TAG, description);
        PendingIntent notifier = PendingIntent.getBroadcast(getActivity(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        Calendar calendar = new GregorianCalendar();
        calendar.set(year, month, day, hour, minute);

        AlarmManager manager = (AlarmManager)(getActivity().getSystemService(Context.ALARM_SERVICE));
        manager.cancel(notifier);
        manager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), notifier);
    }

    public void deleteNotification() {
        Intent intent = new Intent(getActivity(), AlarmReceiver.class);
        PendingIntent notifier = PendingIntent.getBroadcast(getActivity(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager manager = (AlarmManager)(getActivity().getSystemService(Context.ALARM_SERVICE));
        manager.cancel(notifier);

        SharedPreferences values = getActivity().getSharedPreferences("preferences", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = values.edit();
        editor.remove(TITLE_TAG);
        editor.remove(DESCRIPTION_TAG);
        editor.remove(YEAR_TAG);
        editor.remove(MONTH_TAG);
        editor.remove(DAY_TAG);
        editor.remove(HOUR_TAG);
        editor.remove(MINUTE_TAG);
        editor.commit();
    }

    @Override
    public void onSelectedDayChange(CalendarView view, int year, int month, int dayOfMonth) {
        this.year = year;
        this.month = month;
        this.day = dayOfMonth;
    }

    @Override
    public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
        this.hour = hourOfDay;
        this.minute = minute;
    }

    public void setNotified() {
        notified = true;
        //PowerManager pm = (PowerManager) getActivity().getSystemService(Context.POWER_SERVICE);
        //PowerManager.WakeLock wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.ON_AFTER_RELEASE, "My Tag");
        //wakeLock.release();
    }
}


