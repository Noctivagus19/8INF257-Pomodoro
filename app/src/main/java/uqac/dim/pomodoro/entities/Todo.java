package uqac.dim.pomodoro.entities;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.ForeignKey;

@Entity(foreignKeys = {@ForeignKey(entity = Category.class,
        parentColumns = "id",
        childColumns = "fk_categoryId")
})
public class Todo {
    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name = "description")
    public String description;

    @ColumnInfo(name = "date")
    public String date;

    @ColumnInfo(name = "fk_categoryId", index = true) //FK
    public int fk_categoryId;

    @ColumnInfo(name = "completionTime")
    public int completionTime;


    public Todo(String description, String date, int fk_categoryId) {
        this.description = description;
        this.date = date;
        this.fk_categoryId = fk_categoryId;
        this.completionTime = -1;
    }


    public String toString() {
        return "Todo{" +
                "id=" + id +
                ", description='" + description + '\'' +
                ", date='" + date + '\'' +
                ", fk_categoryId='" + fk_categoryId + '\'' +
                ", completion time='" + completionTime + '\'' +
                '}';
    }

    public int getId(){
        return this.id;
    }

    public void setCategoryId(int categoryId){
        this.fk_categoryId = categoryId;
    }

    public int getCategoryId(){
        return this.fk_categoryId;
    }

    public void setDescription(String description){
        this.description = description;
    }

    public String getDescription(){
        return this.description;
    }

    public void setDate(){
        this.date = date;
    }

    public String getDate(){
        return this.date;
    }

    public void setCompletionTime(int completionTime){
        this.completionTime = completionTime;
    }

    public int getCompletionTime(){
        return this.completionTime;
    }

}

