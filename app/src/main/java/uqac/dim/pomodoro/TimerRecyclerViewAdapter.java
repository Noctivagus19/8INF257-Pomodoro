package uqac.dim.pomodoro;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.switchmaterial.SwitchMaterial;

import java.util.List;

import uqac.dim.pomodoro.entities.Timer;

public class TimerRecyclerViewAdapter extends RecyclerView.Adapter<TimerRecyclerViewAdapter.ViewHolder> {

    private List<Timer> mData;
    private LayoutInflater mInflater;
    private ItemClickListener mClickListener;
    private SwitchClickListener mSwitchClickListener;

    TimerRecyclerViewAdapter(Context context, List<Timer> data) {
        this.mInflater = LayoutInflater.from(context);
        this.mData = data;
    }

    // inflates row layout from xml
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.recyclerview_timer_row, parent, false);
        return new ViewHolder(view);
    }

    // bind data to textViews in each row
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        String timerName = mData.get(position).getName();
        holder.timerName.setText(timerName);
        String workLength = String.valueOf(TimerEditActivity.toMinutes(mData.get(position).getWorkMs()));
        holder.workLength.setText(workLength);
        String pauseLength = String.valueOf(TimerEditActivity.toMinutes(mData.get(position).getPauseMs()));
        holder.pauseLength.setText(pauseLength);
        String longPauseLength = String.valueOf(TimerEditActivity.toMinutes(mData.get(position).getLongPauseMs()));
        holder.longPauseLength.setText(longPauseLength);
        String pauseIntervals = "" + mData.get(position).getPauseIntervals();
        holder.pauseIntervals.setText(pauseIntervals);
        if (mData.get(position).getStatus().equals(Timer.ACTIVE))
            holder.activeSwitch.setChecked(true);
        else
            holder.activeSwitch.setChecked(false);
    }

    // total number of rows
    @Override
    public int getItemCount() {
        return mData.size();
    }

    // stores and recycles views as they scroll off screen
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView timerName;
        TextView workLength;
        TextView pauseLength;
        TextView longPauseLength;
        TextView pauseIntervals;
        SwitchMaterial activeSwitch;

        ViewHolder(View itemView) {
            super(itemView);
            timerName = itemView.findViewById(R.id.timerName);
            workLength = itemView.findViewById(R.id.workLength);
            pauseLength = itemView.findViewById(R.id.pauseLength);
            longPauseLength = itemView.findViewById(R.id.longPauseLength);
            pauseIntervals = itemView.findViewById(R.id.pauseIntervals);
            activeSwitch = itemView.findViewById(R.id.activeSwitch);
            activeSwitch.setOnClickListener(this);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (view instanceof SwitchMaterial) {
                if (mSwitchClickListener != null)
                    mSwitchClickListener.onSwitchClick(view, getAbsoluteAdapterPosition());
            }
            else {
                if (mClickListener != null)
                    mClickListener.onItemClick(view, getAbsoluteAdapterPosition());
            }
        }

    }

    // return data at click position
    Timer getItem(int id) {
        return mData.get(id);
    }

    // catch click events
    void setClickListener(ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    void setSwitchClickListener(SwitchClickListener switchClickListener) {
        this.mSwitchClickListener = switchClickListener;
    }

    // parent activity implement these methods to handle click events
    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }

    public interface SwitchClickListener {
        void onSwitchClick(View view, int position);
    }

    public void updateData(List<Timer> timers) {
        mData = timers;
    }
}
