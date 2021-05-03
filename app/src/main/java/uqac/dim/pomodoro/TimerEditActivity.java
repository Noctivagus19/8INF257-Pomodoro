package uqac.dim.pomodoro;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputFilter;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import uqac.dim.pomodoro.entities.PomodoroDB;
import uqac.dim.pomodoro.entities.Timer;

public class TimerEditActivity extends AppCompatActivity {
    private boolean isNew = false;
    private int timerId = 0;
    private PomodoroDB pdb;

    private EditText nameField;
    private EditText pomodoroField;
    private EditText pauseField;
    private EditText longPauseField;
    private EditText pauseIntervalsField;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timer_edit);

        nameField = ((EditText)findViewById(R.id.editTimerName));
        pomodoroField = ((EditText)findViewById(R.id.editPomodoroLength));
        pomodoroField.setFilters(new InputFilter[]{ new InputFilterMinMax("1", "99")});
        pauseField = ((EditText)findViewById(R.id.editPauseLength));
        pauseField.setFilters(new InputFilter[]{ new InputFilterMinMax("1", "99")});
        longPauseField = ((EditText)findViewById(R.id.editLongPauseLength));
        longPauseField.setFilters(new InputFilter[]{ new InputFilterMinMax("1", "99")});
        pauseIntervalsField = ((EditText)findViewById(R.id.editPauseIntervals));
        pauseIntervalsField.setFilters(new InputFilter[]{ new InputFilterMinMax("1", "10")});

        pdb = PomodoroDB.getDatabase(getApplicationContext());

        Intent i = getIntent();
        if (!i.hasExtra("TIMERID"))
            isNew = true;

        if (!isNew) {
            String sTimerId = i.getStringExtra("TIMERID");
            if (sTimerId != null) {
                timerId = Integer.parseInt(sTimerId);
                Timer timer = pdb.timerDao().getTimer(timerId);
                if (timer != null) {
                    nameField.setText(timer.getName());
                    pomodoroField.setText(String.valueOf(toMinutes(timer.getWorkMs())));
                    pauseField.setText(String.valueOf(toMinutes(timer.getPauseMs())));
                    longPauseField.setText(String.valueOf(toMinutes(timer.getLongPauseMs())));
                    pauseIntervalsField.setText(String.valueOf(timer.getPauseIntervals()));
                }
            }
        }
    }

    public void saveTimer(View view) {
        String name = String.valueOf(nameField.getText());
        String pomodoro = String.valueOf(pomodoroField.getText());
        String pause = String.valueOf(pauseField.getText());
        String longPause = String.valueOf(longPauseField.getText());
        String pauseIntervals = String.valueOf(pauseIntervalsField.getText());
        if (!name.isEmpty() &&
                !pomodoro.isEmpty() &&
                !pause.isEmpty() &&
                !longPause.isEmpty() &&
                !pauseIntervals.isEmpty()
        ) {
            Timer timer = new Timer(
                    name,
                    toMs(Integer.parseInt(pomodoro)),
                    toMs(Integer.parseInt(pause)),
                    toMs(Integer.parseInt(longPause)),
                    Integer.parseInt(pauseIntervals)
            );
            if (isNew) {
                if (pdb.timerDao().getNonArchivedTimers().size() == 0)
                    timer.setActive();
                pdb.timerDao().addTimer(timer);
            } else {
                Timer existingTimer = pdb.timerDao().getTimer(timerId);
                existingTimer.setName(timer.getName());
                existingTimer.setWorkMs(timer.getWorkMs());
                existingTimer.setPauseMs(timer.getPauseMs());
                existingTimer.setLongPauseMs(timer.getLongPauseMs());
                existingTimer.setPauseIntervals(timer.getPauseIntervals());
                pdb.timerDao().updateTimer(timer);
            }
            startActivity(new Intent(this, ManageTimersActivity.class));
        } else {
            Toast toast = Toast.makeText(TimerEditActivity.this, "Fields can't be empty." , Toast.LENGTH_LONG);
            toast.setGravity(Gravity.BOTTOM, 0, 140);
            toast.show();
        }
    }

    public void deleteTimer(View view) {
        pdb.timerDao().deleteById(timerId);
        startActivity(new Intent(this, ManageTimersActivity.class));
    }

    public static int toMinutes(int ms) {
        return ms/60000;
    }

    public static int toMs(int minutes) {
        return minutes*60000;
    }
}