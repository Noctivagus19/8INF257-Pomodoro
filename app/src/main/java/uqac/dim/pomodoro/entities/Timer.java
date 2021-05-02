package uqac.dim.pomodoro.entities;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class Timer {
    public static final String ACTIVE = "ACTIVE";
    public static final String SELECTABLE = "SELECTABLE";
    public static final String ARCHIVED = "ARCHIVED";

    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name = "name")
    public String name;

    @ColumnInfo(name = "workMs")
    public int workMs;

    @ColumnInfo(name = "pauseMs")
    public int pauseMs;

    @ColumnInfo(name = "longPauseMs")
    public int longPauseMs;

    @ColumnInfo(name = "pauseIntervals")
    public int pauseIntervals;

    @ColumnInfo(name = "status")
    public String status;

    public Timer(String name, int workMs, int pauseMs, int longPauseMs, int pauseIntervals) {
        this.name = name;
        this.workMs = workMs;
        this.pauseMs = pauseMs;
        this.longPauseMs = longPauseMs;
        this.pauseIntervals = pauseIntervals;
        this.status = SELECTABLE;
    }

    @Override
    public String toString() {
        return "Timer{" +
                "id=" + this.getId() +
                "name=" + this.getName() +
                ", workMs='" + this.getWorkMs() + "'" +
                ", pauseMs='" + this.getPauseMs() + "'" +
                ", longPauseMs='" + this.getLongPauseMs() + "'" +
                ", pauseIntervals='" + this.getPauseIntervals() + "'" +
                ", status='" + this.getStatus() + "'" +
                "}";
    }

    public int getId(){
        return this.id;
    }

    public void setName(String name) { this.name = name; }

    public String getName() { return this.name; }

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

    public String getStatus() {
        return this.status;
    }

    public void setActive() {
        this.status = ACTIVE;
    }

    public void setSelectable() {
        this.status = SELECTABLE;
    }

    public void setArchived() {
        this.status = ARCHIVED;
    }
}
