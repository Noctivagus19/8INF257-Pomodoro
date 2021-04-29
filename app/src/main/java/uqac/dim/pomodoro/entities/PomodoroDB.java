package uqac.dim.pomodoro.entities;
import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {Todo.class, Category.class, Timer.class}, version = 4, exportSchema = false)
public abstract class PomodoroDB extends RoomDatabase {
    private static PomodoroDB INSTANCE;
    public abstract TodoDao todoDao();
    public abstract TimerDao timerDao();
    public abstract CategoryDao categoryDao();


    public static PomodoroDB getDatabase(Context context) {
        if (INSTANCE == null) {
            INSTANCE =
                    Room.databaseBuilder(context, PomodoroDB.class, "pomodorodatabase")
                            // To simplify the exercise, allow queries on the main thread.
                            // Don't do this on a real app!
                            .allowMainThreadQueries()
                            // recreate the database if necessary
                            .fallbackToDestructiveMigration()
                            .build();
        }
        return INSTANCE;
    }

    public static void destroyInstance() {
        INSTANCE = null;
    }

}
