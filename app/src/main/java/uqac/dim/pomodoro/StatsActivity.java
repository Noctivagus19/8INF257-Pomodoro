package uqac.dim.pomodoro;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import uqac.dim.pomodoro.entities.PomodoroDB;
import uqac.dim.pomodoro.entities.Todo;

public class StatsActivity extends AppCompatActivity {
    private PomodoroDB pdb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stats);

        pdb = PomodoroDB.getDatabase(getApplicationContext());
        List<Todo> todos = pdb.todoDao().getCompletedTodos();

        // # pomodoros
        ((TextView)findViewById(R.id.pomodoroCount)).setText(String.valueOf(todos.size()));

        // pomodoro avg time
        double totalTime = 0.0;
        for(Todo todo: todos) {
            totalTime += todo.getCompletionTime();
        }
        int avg = (int)(totalTime/todos.size());
        ((TextView)findViewById(R.id.pomodoroAvgTime)).setText(CountdownTimerService.toHms((avg)));

        // most pomodoros in a day
        List<String> dates = new ArrayList<>();
        for(Todo todo: todos) {
            dates.add(todo.date);
        }

        Map<String, Integer> map = new HashMap<>();
        for(String s: dates) {
            map.put(s, Collections.frequency(dates,s));
        }

        Map.Entry entry = null;
        int mostPomodoros = 0;
        Iterator it = map.entrySet().iterator();
        while (it.hasNext()) {
           Map.Entry pair = (Map.Entry)it.next();
           if (Integer.parseInt(String.valueOf(pair.getValue())) > mostPomodoros) {
               entry = pair;
           }
        }

        if (entry != null) {
            ((TextView)findViewById(R.id.pomodoroMostDate)).setText(String.valueOf(entry.getKey()));
        }
    }
}