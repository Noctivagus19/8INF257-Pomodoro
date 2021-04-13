package uqac.dim.pomodoro;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.textclassifier.TextClassification;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import uqac.dim.pomodoro.entities.PomodoroDB;
import uqac.dim.pomodoro.entities.Todo;

public class MyRecyclerViewAdapter extends RecyclerView.Adapter<MyRecyclerViewAdapter.ViewHolder> {
    private List<Todo> mData;
    private LayoutInflater mInflater;
    private ItemClickListener mClickListener;
    private static PomodoroDB pdb;

    // data is passed into the constructor
    MyRecyclerViewAdapter(Context context, List<Todo> data) {
        this.mInflater = LayoutInflater.from(context);
        this.mData = data;
        pdb = PomodoroDB.getDatabase(context.getApplicationContext());
    }

    // inflates the row layout from xml when needed
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.recyclerview_row, parent, false);
        return new ViewHolder(view);
    }


    // binds the data to the TextView in each row
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.myTodo.setText(mData.get(position).getDescription());
        holder.myCategory.setText(pdb.categoryDao().findById(mData.get(position).getCategoryId()).getName());
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


    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public Button optionsTodo;
        TextView myTodo;
        TextView myCategory;

        ViewHolder(View itemView) {
            super(itemView);
            myTodo = itemView.findViewById(R.id.tvTodo);
            myCategory = itemView.findViewById(R.id.tvCategory);
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


