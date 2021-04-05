package uqac.dim.pomodoro.entities;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface TodoDao {
    @Query("SELECT * FROM todo")
    List<Todo> getAllTodos();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void addTodo(Todo todo);

    /*
    @Query("SELECT " +
            "Todo.id, " +
            "Todo.description, " +
            "Todo.fk_categoryId, " +
            "Todo.fk_timerId " +
            "FROM todo " +
            "WHERE Todo.id=:id"
    )
    Todo findById(int id);
    */



    @Query("SELECT " +
            "Todo.id, " +
            "Todo.description, " +
            "Todo.fk_categoryId, " +
            "Todo.fk_timerId, " +
            "Timer.workMS, " +
            "Timer.pauseMs, " +
            "Timer.longPauseMs, " +
            "Timer.pauseIntervals, " +
            "Category.name as \"category\", " +
            "Category.status FROM todo " +
            "INNER JOIN Timer on Todo.fk_timerId = Timer.id " +
            "INNER JOIN Category on Todo.fk_categoryId = Category.id " +
            "WHERE Todo.id=:id"
    )
    Todo findById(int id);


}
