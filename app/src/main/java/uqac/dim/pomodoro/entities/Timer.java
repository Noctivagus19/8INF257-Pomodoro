package uqac.dim.pomodoro.entities;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.ColumnInfo;

@Entity
public class Timer {
    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name = "workMs")
    public int workMs;

    @ColumnInfo(name = "pauseMs")
    public int pauseMs;

    @ColumnInfo(name = "longPauseMs")
    public int longPauseMs;

    @ColumnInfo(name = "pauseIntervals")
    public int pauseIntervals;

    public Timer(int workMs, int pauseMs, int longPauseMs, int pauseIntervals) {
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

    public int getId(){
        return this.id;
    }

    public void setWorkMs(int workMs){
        this.workMs = workMs;
    }

    public int getWorkMs(){
        return this.workMs;
    }

    public void setPauseMs(int pauseMs){
        this.pauseMs = pauseMs;
    }

    public int getPauseMs(){
        return this.pauseMs;
    }

    public void setLongPauseMs(int longLauseMs){
        this.longPauseMs = longPauseMs;
    }

    public int getLongPauseMs(){
        return this.longPauseMs;
    }

    public void setPauseIntervals(int pauseIntervals){
        this.pauseIntervals = pauseIntervals;
    }

    public int getPauseIntervals(){
        return this.pauseIntervals;
    }
}
