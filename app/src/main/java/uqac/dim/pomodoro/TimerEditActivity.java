package uqac.dim.pomodoro;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputFilter;
import android.view.View;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

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
            timerId = Integer.parseInt(i.getStringExtra("TIMERID"));
            Timer timer = pdb.timerDao().getTimer(timerId);
            if (timer != null) {
                nameField.setText(timer.getName());
                pomodoroField.setText(toMinutes(timer.getWorkMs()));
                pauseField.setText(toMinutes(timer.getPauseMs()));
                longPauseField.setText(toMinutes(timer.getLongPauseMs()));
                pauseIntervalsField.setText(timer.getPauseIntervals());
            }
        }
    }

    public void saveTimer(View view) {
        Timer timer = new Timer(
                String.valueOf(nameField.getText()),
                toMs(Integer.parseInt(String.valueOf(pomodoroField.getText()))),
                toMs(Integer.parseInt(String.valueOf(pauseField.getText()))),
                toMs(Integer.parseInt(String.valueOf(longPauseField.getText()))),
                Integer.parseInt(String.valueOf(pauseIntervalsField.getText()))
        );
        if (isNew) {
            pdb.timerDao().addTimer(timer);
            List<Timer> timers = pdb.timerDao().getAllTimers();
            Timer testTimer = timers.get(0);
        } else {
            Timer existingTimer = pdb.timerDao().getTimer(timerId);
            existingTimer.setName(timer.getName());
            existingTimer.setWorkMs(timer.getWorkMs());
            existingTimer.setPauseMs(timer.getPauseMs());
            existingTimer.setLongPauseMs(timer.getLongPauseMs());
            existingTimer.setPauseIntervals(timer.getPauseIntervals());
            pdb.timerDao().updateTimer(timer);
        }
    }

    private int toMinutes(int ms) {
        return ms/60000;
    }

    private int toMs(int minutes) {
        return minutes*60000;
    }
}