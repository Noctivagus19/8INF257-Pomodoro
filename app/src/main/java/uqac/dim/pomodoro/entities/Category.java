package uqac.dim.pomodoro.entities;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.ColumnInfo;

@Entity
public class Category {
    @PrimaryKey
    public int id;

    @ColumnInfo(name = "name")
    public String name;

    @ColumnInfo(name = "status")
    public String status;

    public Category(int id, String name, String status) {
        this.id = id;
        this.name = name;
        this.status = status;
    }

    @Override
    public String toString() {
        return "Category{" +
                "id=" + id +
                ", nams='" + name + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}
