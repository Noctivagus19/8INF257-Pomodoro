package uqac.dim.pomodoro.entities;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.ForeignKey;

@Entity(foreignKeys = {@ForeignKey(entity = Category.class,
        parentColumns = "id",
        childColumns = "fk_categoryId"),
        @ForeignKey(entity = Timer.class,
        parentColumns = "id",
        childColumns = "fk_timerId")
})
public class Todo {
    @PrimaryKey
    public int id;

    @ColumnInfo(name = "description") //FK
    public String description;

    @ColumnInfo(name = "fk_timerId") //FK
    public int fk_timerId;

    @ColumnInfo(name = "fk_categoryId") //FK
    public int fk_categoryId;


    public int workMs;
    public int pauseMs;
    public int longPauseMs;
    public int pauseIntervals;
    public String category;
    public String status;

    public Todo(int id, String description , int fk_timerId, int fk_categoryId) {
        this.id = id;
        this.description = description;
        this.fk_timerId = fk_timerId;
        this.fk_categoryId = fk_categoryId;
    }

   /* public Todo(int id, String description , int fk_timerId, int fk_categoryId, int workMs, int pauseMs, int longPauseMs, int pauseIntervals, String category, String status) {
        this.id = id;
        this.description = description;
        this.fk_timerId = fk_timerId;
        this.fk_categoryId = fk_categoryId;
        this.workMs = workMs;
        this.pauseMs = pauseMs;
        this.longPauseMs = longPauseMs;
        this.pauseIntervals = pauseIntervals;
        this.category = category;
        this.status = status;
    }*/

    @Override
    public String toString() {
        return "Todo{" +
                "id=" + id +
                ", description='" + description + '\'' +
                ", fk_timerId='" + fk_timerId + '\'' +
                ", fk_categoryId='" + fk_categoryId + '\'' +
                ", workMs='" + workMs + '\'' +
                ", pauseMs='" + pauseMs+ '\'' +
                ", longPauseMs='" + longPauseMs + '\'' +
                ", pauseIntervals='" + pauseIntervals + '\'' +
                ", category='" + category + '\'' +
                ", status='" + status + '\'' +
                '}';
    }

}

