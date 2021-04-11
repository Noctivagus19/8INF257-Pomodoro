package uqac.dim.pomodoro;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;

import androidx.appcompat.app.AppCompatActivity;

import java.util.List;
import java.util.Random;

import uqac.dim.pomodoro.entities.PomodoroDB;
import uqac.dim.pomodoro.entities.Todo;
import android.app.ListActivity;
import android.widget.ArrayAdapter;

public class ManageTodosActivity extends ListActivity{
    private PomodoroDB pdb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_managetodos);
        Log.i("LOG", "MAFAG ONCREATE");
        //Intent intent = getIntent();
        //String selectedChoice = intent.getStringExtra(MainActivity.EXTRA_SELECTED_CHOICE);
        //disableSelectedChoice(selectedChoice);
        pdb = PomodoroDB.getDatabase(getApplicationContext());

        List<Todo> todos = pdb.todoDao().getAllTodos();

        ArrayAdapter<Todo> adapter = new ArrayAdapter<Todo>(
                this, android.R.layout.simple_list_item_1, todos);
        setListAdapter(adapter);
    }

    public void onClick(View view) {

        ArrayAdapter<Todo> adapter = (ArrayAdapter<Todo>) getListAdapter();
        Todo todo = null;

        switch (view.getId()) {

            case R.id.add:		String[] comments = new String[] { "Cool", "Very nice", "Hate it" };
                int nextInt = new Random().nextInt(3);
                // save the new comment to the database
                //pdb.todoDao().addTodo(new Todo( "Todo 2", currentDate, timers.get(0).getId(), categories.get(0).getId()));
                break;
            case R.id.delete:	if (getListAdapter().getCount() > 0) {

            }
                break;
        }

        adapter.notifyDataSetChanged();            // IMPORTANT : Mettre a jour l'interface
    }

    @Override
    protected void onResume() {

        //datasource.open();
        super.onResume();
    }

    @Override
    protected void onPause() {

        //datasource.close();
        super.onPause();
    }
}
