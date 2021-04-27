package uqac.dim.pomodoro.entities;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface TimerDao {
    @Query("SELECT * FROM timer")
    List<Timer> getAllTimers();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void addTimer(Timer timer);

    @Query("SELECT * FROM timer WHERE id IS :id LIMIT 1")
    Timer findById(int id);

    @Query("delete from timer")
    void deleteTimers();

    @Query("delete from timer WHERE id=:id")
    void deleteById(int id);

    @Delete()
    void deleteTimer(Timer timer);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    void updateTimer(Timer timer);
}
