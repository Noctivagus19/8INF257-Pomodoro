package uqac.dim.pomodoro;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import uqac.dim.pomodoro.entities.Category;
import uqac.dim.pomodoro.entities.PomodoroDB;
import uqac.dim.pomodoro.entities.Todo;
import android.app.ListActivity;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.reginald.editspinner.EditSpinner;

public class ManageTodosActivity extends ListActivity{
    public final static String EXTRA_TODO_ID = "uqac.dim.mafag.TODO_ID";
    public final static String EXTRA_TODO_DESCRIPTION = "uqac.dim.mafag.TODO_DESCRIPTION";
    private PomodoroDB pdb;
    EditSpinner mEditSpinnerCategories;
    List<Todo> todos;
    List<Category> categories;
    private Todo todo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_managetodos);
        Log.i("LOG", "MANAGE TODOS ACTIVITY ONCREATE");
        pdb = PomodoroDB.getDatabase(getApplicationContext());
        initViews();
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private void initViews() {
        todos = pdb.todoDao().getAllTodos();
        ArrayAdapter<Todo> adapter = new ArrayAdapter<Todo>(
                this, android.R.layout.simple_list_item_1, todos);
        setListAdapter(adapter);

        ListView lv = (ListView)findViewById(android.R.id.list);

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,int position, long id) {

                Log.i("LOG", "Listview click position: " + position);
                todo = (Todo) getListAdapter().getItem(position);
                returnIntent(todo.getId(), todo.getDescription());
            }
        });

        initEditSpinner(1);
    }

    private void initEditSpinner(int selected) {
        categories = pdb.categoryDao().getAllCategories();
        final String[] stringArrayCategories = new String[categories.size()];
        for (int i=0; i< categories.size(); i++){
            stringArrayCategories[i] = categories.get(i).getName();
        }
        mEditSpinnerCategories = (EditSpinner) findViewById(R.id.edit_spinner_categories);
        mEditSpinnerCategories.setDropDownDrawable(getResources().getDrawable(R.drawable.spinner), 25, 25);
        mEditSpinnerCategories.setDropDownDrawableSpacing(50);
        mEditSpinnerCategories.setAdapter(new BaseAdapter() {
            public int getCount() {
                return stringArrayCategories.length;
            }

            @Override
            public Object getItem(int position) {
                return stringArrayCategories[position];
            }

            @Override
            public long getItemId(int position) {
                return position;
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                if (convertView == null) {
                    convertView = View.inflate(ManageTodosActivity.this, R.layout.layout_item, null);
                }

                ImageView icon = convertView.findViewById(R.id.item_icon);
                TextView textView = convertView.findViewById(R.id.item_text);

                String data = (String) getItem(position);

                icon.setImageResource(R.mipmap.ic_launcher);
                textView.setText(data);

                return convertView;
            }
        });

        // it converts the item in the list to a string shown in EditText.
        mEditSpinnerCategories.setItemConverter(new EditSpinner.ItemConverter() {
            @Override
            public String convertItemToString(Object selectedItem) {
                if (selectedItem.toString().equals(stringArrayCategories[stringArrayCategories.length - 1])) {
                    return selectedItem.toString();
                } else {
                    Log.d("LOG", "Selected item to string ");
                    return selectedItem.toString();
                }
            }
        });

        // triggered when one item in the list is clicked
        mEditSpinnerCategories.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d("LOG", "onItemClick() position = " + position);
                if (position == stringArrayCategories.length - 1) {
                    showSoftInputPanel(mEditSpinnerCategories);
                }
            }
        });

        // select the first item initially
        mEditSpinnerCategories.selectItem(selected);
    }

    private void hideSoftInputPanel() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(mEditSpinnerCategories.getWindowToken(), 0);
        }
    }

    private void showSoftInputPanel(View view) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.showSoftInput(view, 0);
        }
    }

    public void onClick(View view) {
        ArrayAdapter<Todo> adapter = (ArrayAdapter<Todo>) getListAdapter();

        switch (view.getId()) {

            case R.id.add:
                TextView addTodoDescription = (TextView)findViewById(R.id.addTodoDescription);
                String currentDate = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date());
                String categoryName = mEditSpinnerCategories.getText().toString();
                int categoryId = -1;
                for (int i=0; i<categories.size(); i++){
                    if (categoryName.equals(categories.get(i).getName())){
                        categoryId = categories.get(i).getId();
                    }
                }
                if (categoryId == -1){
                    pdb.categoryDao().addCategory(new Category( categoryName, "Active"));
                    categories = pdb.categoryDao().getAllCategories();
                    Category insertedCat = categories.get(categories.size()-1);
                    categoryId = insertedCat.getId();
                    initEditSpinner(categories.size()+1);
                }
                pdb.todoDao().addTodo(new Todo( addTodoDescription.getText().toString(), currentDate, categoryId));
                todos = pdb.todoDao().getAllTodos();
                adapter.add(todos.get(todos.size()-1));
                break;

            case R.id.delete:	if (getListAdapter().getCount() > 0) {
                todo = (Todo) getListAdapter().getItem(0);
                adapter.remove(todo);
                pdb.todoDao().deleteById(todo.getId());
                todos = pdb.todoDao().getAllTodos();
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

    public void returnIntent(int extra_todoId, String extra_TodoDescription){
        Intent returnIntent = new Intent();
        returnIntent.putExtra(EXTRA_TODO_ID, extra_todoId);
        returnIntent.putExtra(EXTRA_TODO_DESCRIPTION, extra_TodoDescription);
        setResult(MainActivity.RESULT_OK, returnIntent);
        finish();
    }

    public void returnIntent(){
        Intent returnIntent = new Intent();
        setResult(MainActivity.RESULT_CANCELED, returnIntent);
        finish();
    }
}
