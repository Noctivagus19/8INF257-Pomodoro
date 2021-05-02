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

    @Query("SELECT * FROM todo WHERE completionTime=-1")
    List<Todo> getActiveTodos();

    @Query("SELECT * FROM todo WHERE completionTime=-1 LIMIT 1")
    Todo getTopActiveTodo();

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

    @Query("SELECT * FROM todo WHERE Todo.id=:id")
    Todo findById(int id);

    @Query("SELECT * FROM todo WHERE Todo.completionTime!=-1")
    List<Todo> getCompletedTodos();
}
