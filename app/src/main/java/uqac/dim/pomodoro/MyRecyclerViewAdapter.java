package uqac.dim.pomodoro;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import java.util.List;

import uqac.dim.pomodoro.entities.Category;
import uqac.dim.pomodoro.entities.PomodoroDB;
import uqac.dim.pomodoro.entities.Todo;

public class MyRecyclerViewAdapter extends RecyclerView.Adapter<MyRecyclerViewAdapter.ViewHolder> {
    private List<Todo> mData;
    private List<Category> categories;
    private LayoutInflater mInflater;
    private ItemClickListener mClickListener;
    private static PomodoroDB pdb;
    private static int editRowPosition;
    private static final int editRow = 0;
    private static final int stdRow = 1;
    private Context globalContext;



    public static void setEditRow(int position){
        editRowPosition = position;
    }

    public static int getEditRow(){
        return editRowPosition;
    }

    // data is passed into the constructor
    MyRecyclerViewAdapter(Context context, List<Todo> data, List<Category> cat) {
        this.mInflater = LayoutInflater.from(context);
        this.mData = data;
        pdb = PomodoroDB.getDatabase(context.getApplicationContext());
        editRowPosition = -1;
        this.categories = cat;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == editRowPosition){
            return editRow;
        }
        else{
            return stdRow;
        }
    }

    // inflates the row layout from xml when needed
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        if (viewType == editRow){
            view = mInflater.inflate(R.layout.recyclerview_row_edit, parent, false);
            globalContext = parent.getContext();
        }
        else{
            view = mInflater.inflate(R.layout.recyclerview_row, parent, false);
            globalContext = parent.getContext();
        }
        return new ViewHolder(view);
    }


    // binds the data to the TextView in each row
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        if (holder.getItemViewType() == stdRow){
            holder.myTodo.setText(mData.get(position).getDescription());
            holder.myCategory.setText(pdb.categoryDao().findById(mData.get(position).getCategoryId()).getName());
        }
        else{
            holder.myTodoEdit.setText(mData.get(position).getDescription());
            ArrayAdapter<Category> adapter = new ArrayAdapter<Category>(globalContext,android.R.layout.simple_spinner_item,pdb.categoryDao().getActiveCategories()){
                @Override
                public Category getItem(int position) {
                    return categories.get(position);
                }

                @Override
                public View getView(int position, View convertView, ViewGroup parent){
                    TextView view = (TextView) super.getView(position, convertView, parent);
                    view.setText(getItem(position).getName());
                    return view;
                }

                @Override
                public View getDropDownView(int position, View convertView, ViewGroup parent) {
                    TextView view = (TextView) super.getView(position, convertView, parent);
                    view.setText(getItem(position).getName());
                    return view;
                }
            };
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            holder.spinner.setAdapter(adapter);

        }
    }


    // total number of rows
    @Override
    public int getItemCount() {
        return mData.size();
    }

    public void add(Todo todo) {
        mData.add(todo);
        notifyDataSetChanged();
    }

    public void remove(Todo todo) {
        mData.remove(todo);
        notifyDataSetChanged();
    }

    public void updateData(final List<Todo> todos){
        mData= todos;
        notifyDataSetChanged();
    }


    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public Button optionsTodo;
        TextView myTodo;
        TextView myCategory;
        EditText myTodoEdit;
        Spinner spinner;

        ViewHolder(View itemView) {
            super(itemView);
            myTodo = itemView.findViewById(R.id.tvTodo);
            myCategory = itemView.findViewById(R.id.tvCategory);
            myTodoEdit = itemView.findViewById(R.id.edTodo);

            spinner = itemView.findViewById(R.id.spinnerCategories);

            itemView.setOnClickListener(this);
            optionsTodo = (Button) itemView.findViewById(R.id.options_todo);
            optionsTodo.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (mClickListener != null) mClickListener.onItemClick(view, getAdapterPosition());
        }


    }

    // convenience method for getting data at click position
    Todo getItem(int id) {
        return mData.get(id);
    }

    // allows clicks events to be caught
    void setClickListener(ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    // parent activity will implement this method to respond to click events
    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }



}


