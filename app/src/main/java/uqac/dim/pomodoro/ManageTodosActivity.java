package uqac.dim.pomodoro;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
//import androidx.recyclerview.widget.RecyclerView;

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
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.reginald.editspinner.EditSpinner;

public class ManageTodosActivity extends AppCompatActivity implements MyRecyclerViewAdapter.ItemClickListener{
    public final static String EXTRA_TODO_ID = "uqac.dim.mafag.TODO_ID";
    public final static String EXTRA_TODO_DESCRIPTION = "uqac.dim.mafag.TODO_DESCRIPTION";
    private PomodoroDB pdb;
    EditSpinner mEditSpinnerCategories;
    List<Todo> todos;
    List<Category> categories;
    private Todo todo;
    MyRecyclerViewAdapter rvadapter;
    private int selectedCategoryPosition;

    @Override
    public void onItemClick(View view, int position) {
        if(view instanceof Button){
            openOptionsTodo(view, todos.get(position));
        }else {
            Log.i("LOG", "RecyclerViewClick position: " + rvadapter.getItem(position));
            returnIntent(rvadapter.getItem(position).getId(), rvadapter.getItem(position).getDescription());
        }
    }

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

        // set up the RecyclerView
        RecyclerView recyclerView = findViewById(R.id.rvTodos);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        rvadapter = new MyRecyclerViewAdapter(this, todos);
        rvadapter.setClickListener(this);
        recyclerView.setAdapter(rvadapter);

        initEditSpinner(1);
    }

    private void initEditSpinner(int selected) {
        categories = pdb.categoryDao().getActiveCategories();
        categories.add(0, new Category());
        mEditSpinnerCategories = (EditSpinner) findViewById(R.id.edit_spinner_categories);
        mEditSpinnerCategories.setDropDownDrawable(getResources().getDrawable(R.drawable.spinner), 25, 25);
        mEditSpinnerCategories.setDropDownDrawableSpacing(50);

        mEditSpinnerCategories.setAdapter(new BaseAdapter() {
            public int getCount() {
                //return stringArrayCategories.length;
                return categories.size();
            }

            @Override
            public Category getItem(int position) {
                return categories.get(position);
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

                String data =  getItem(position).getName();

                icon.setImageResource(R.drawable.android_garbage_32);
                icon.setTag(position);

                textView.setText(data);
                Log.i("LOG","DATA : "+ data);

                return convertView;
            }
        });

        // it converts the item in the list to a string shown in EditText.
        mEditSpinnerCategories.setItemConverter(new EditSpinner.ItemConverter() {
            @Override
            public String convertItemToString(Object selectedItem) {
                Category selectedCat = (Category)selectedItem;
                if (selectedCat.getId()==(categories.get(categories.size()-1).getId())) {
                    return selectedCat.getName();
                } else {
                    return selectedCat.getName();
                }
            }
        });

        // triggered when one item in the list is clicked
        mEditSpinnerCategories.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Log.d("LOG", "onItemClick() position = " + position);
                    if (position == categories.size() - 1) {
                        showSoftInputPanel(mEditSpinnerCategories);
                    }
                    selectedCategoryPosition=position;
            }
        });


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

        switch (view.getId()) {

            case R.id.add:
                TextView addTodoDescription = (TextView)findViewById(R.id.addTodoDescription);
                String currentDate = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date());
                String categoryName = mEditSpinnerCategories.getText().toString();
                Category selectedCategory = categories.get(selectedCategoryPosition);

                int categoryId;
                if (selectedCategoryPosition != 0){
                    selectedCategory.setName(categoryName);
                    categoryId = selectedCategory.getId();
                    pdb.categoryDao().updateCategory(selectedCategory);
                }
                else{
                    pdb.categoryDao().addCategory(new Category( categoryName, "Active"));
                    categories = pdb.categoryDao().getAllCategories();
                    Category insertedCat = categories.get(categories.size()-1);
                    categoryId = insertedCat.getId();
                    initEditSpinner(categories.size()-1);
                    selectedCategoryPosition = categories.size()-1;
                }

                pdb.todoDao().addTodo(new Todo( addTodoDescription.getText().toString(), currentDate, categoryId));
                todos = pdb.todoDao().getAllTodos();
                rvadapter.add(todos.get(todos.size()-1));
                break;

            case R.id.delete:	if (rvadapter.getItemCount() > 0) {
                todo = (Todo) rvadapter.getItem(0);
                rvadapter.remove(todo);
                pdb.todoDao().deleteTodo(todo);
                todos = pdb.todoDao().getAllTodos();
            }
                break;
        }

    }


    public void openOptionsTodo(View view, Todo todo){
        Log.i("LOG","Options Todo clicked");
        PopupMenu popup = new PopupMenu(ManageTodosActivity.this, view);
        popup.getMenuInflater().inflate(R.menu.options_todo_menu, popup.getMenu());
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
                Toast.makeText(
                        ManageTodosActivity.this,
                        "You Clicked : " + item.getTitle(),
                        Toast.LENGTH_SHORT
                ).show();

                switch((String)item.getTitle()){
                    case "Supprimer":
                        rvadapter.remove(todo);
                        pdb.todoDao().deleteTodo(todo);
                        todos = pdb.todoDao().getAllTodos();
                        break;
                }
                return true;
            }
        });
        popup.show(); //showing popup menu
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
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

    public void clickDeleteCategory(View view) {
        if ((int)view.getTag() != 0){
            Category softDeleteCategory = categories.get((int)view.getTag());
            softDeleteCategory.setStatus("Archived");
            pdb.categoryDao().updateCategory(softDeleteCategory);
            categories = pdb.categoryDao().getActiveCategories();
            initEditSpinner(1);
        }
    }
}
