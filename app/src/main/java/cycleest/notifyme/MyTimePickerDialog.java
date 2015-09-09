package cycleest.notifyme;

import android.app.TimePickerDialog;
import android.content.Context;
import android.widget.TimePicker;

public class MyTimePickerDialog extends TimePickerDialog {

    private OnTimeChangeListener listener;

    public MyTimePickerDialog(Context context, OnTimeSetListener callBack, int hourOfDay, int minute, boolean is24HourView) {
        super(context, callBack, hourOfDay, minute, is24HourView);
    }

    public MyTimePickerDialog(Context context, int theme, OnTimeSetListener callBack, int hourOfDay, int minute, boolean is24HourView) {
        super(context, theme, callBack, hourOfDay, minute, is24HourView);
    }

    @Override
    public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
        super.onTimeChanged(view, hourOfDay, minute);
        if(listener != null){
            listener.onTimeChanged(view, hourOfDay, minute);
        }
    }

    public void setOnTimeChangeListener(OnTimeChangeListener listener){
        this.listener = listener;
    }

    interface OnTimeChangeListener {
        public void onTimeChanged(TimePicker view, int hourOfDay, int minute);
    }
}
