package uqac.dim.pomodoro;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import uqac.dim.pomodoro.entities.PomodoroDB;
import uqac.dim.pomodoro.entities.Timer;

public class ManageTimersActivity extends AppCompatActivity
        implements TimerRecyclerViewAdapter.ItemClickListener,
        TimerRecyclerViewAdapter.SwitchClickListener {
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
        adapter.setSwitchClickListener(this);
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
        if (adapter != null)
            adapter.notifyDataSetChanged();
    }

    @Override
    public void onItemClick(View view, int position) {
        Timer timer = adapter.getItem(position);
        Intent i = new Intent(this, TimerEditActivity.class);
        i.putExtra("TIMERID", ""+timer.getId());
        startActivity(i);
    }

    @Override
    public void onSwitchClick(View view, int position) {
        if (adapter.getItemCount() > 1) {
            Timer timer = adapter.getItem(position);
            Timer activeTimer = pdb.timerDao().getActiveTimer();
            if (timer != null && activeTimer != null) {
                activeTimer.setSelectable();
                pdb.timerDao().updateTimer(activeTimer);

                timer.setActive();
                pdb.timerDao().updateTimer(timer);
            }
        }
        adapter.updateData(pdb.timerDao().getNonArchivedTimers());
        adapter.notifyDataSetChanged();
    }
}