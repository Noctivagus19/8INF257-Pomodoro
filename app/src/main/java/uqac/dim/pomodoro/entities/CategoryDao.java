package uqac.dim.pomodoro.entities;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface CategoryDao {
    @Query("SELECT * FROM category")
    List<Category> getAllCategories();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void addCategory(Category category);

    @Query("SELECT * FROM category WHERE id IS :id LIMIT 1")
    Category findById(int id);
}
