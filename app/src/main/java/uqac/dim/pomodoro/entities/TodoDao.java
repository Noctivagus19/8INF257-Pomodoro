package uqac.dim.pomodoro.entities;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface TodoDao {
    @Query("SELECT * FROM todo")
    List<Todo> getAllTodos();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void addTodo(Todo todo);

    @Query("delete from todo")
    void deleteTodos();

    @Query("delete from todo WHERE id=:id")
    void deleteById(int id);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    void updateTodo(Todo todo);

    @Delete()
    void deleteTodo(Todo todo);

    @Query("SELECT " +
            "Todo.id, " +
            "Todo.description, " +
            "Todo.date, " +
            "Todo.fk_categoryId, " +
            "Todo.completionTime " +
            "FROM todo " +
            "WHERE Todo.id=:id"
    )
    Todo findById(int id);

}
