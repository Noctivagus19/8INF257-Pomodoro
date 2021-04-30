package uqac.dim.pomodoro;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import uqac.dim.pomodoro.entities.PomodoroDB;
import uqac.dim.pomodoro.entities.Timer;

public class ManageTimersActivity extends AppCompatActivity implements TimerRecyclerViewAdapter.ItemClickListener {
    TimerRecyclerViewAdapter adapter;
    private PomodoroDB pdb;
    private List<Timer> timers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_timer);

        timers = new ArrayList<>();
        pdb = PomodoroDB.getDatabase(getApplicationContext());
        timers = pdb.timerDao().getNonArchivedTimers();

        // recycler view setup
        RecyclerView recyclerView = findViewById(R.id.rvTimers);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new TimerRecyclerViewAdapter(this, timers);
        adapter.setClickListener(this);
        recyclerView.setAdapter(adapter);

        findViewById(R.id.fabAddTimer).setOnClickListener(
                (fab) -> {
                    startActivity(new Intent(this, TimerEditActivity.class));
                }
        );
    }


    @Override
    protected void onResume() {
        super.onResume();
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onItemClick(View view, int position) {
        Timer timer = adapter.getItem(position);
        Intent i = new Intent(this, TimerEditActivity.class);
        i.putExtra("TIMERID", ""+timer.getId());
        startActivity(i);
        Log.i("LOG", "You clicked "+adapter.getItem(position) + " on row number " + position);
    }
}