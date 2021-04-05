package uqac.dim.pomodoro.entities;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.ColumnInfo;

@Entity
public class Timer {
    @PrimaryKey
    public int id;

    @ColumnInfo(name = "workMs")
    public int workMs;

    @ColumnInfo(name = "pauseMs")
    public int pauseMs;

    @ColumnInfo(name = "longPauseMs")
    public int longPauseMs;

    @ColumnInfo(name = "pauseIntervals")
    public int pauseIntervals;

    public Timer(int id, int workMs, int pauseMs, int longPauseMs, int pauseIntervals) {
        this.id = id;
        this.workMs = workMs;
        this.pauseMs = pauseMs;
        this.longPauseMs = longPauseMs;
        this.pauseIntervals = pauseIntervals;
    }

    @Override
    public String toString() {
        return "Timer{" +
                "id=" + id +
                ", workMs='" + workMs + '\'' +
                ", pauseMs='" + pauseMs + '\'' +
                ", longPauseMs='" + longPauseMs + '\'' +
                ", pauseIntervals='" + pauseIntervals + '\'' +
                '}';
    }
}
