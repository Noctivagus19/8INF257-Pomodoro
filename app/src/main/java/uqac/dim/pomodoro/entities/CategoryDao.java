package uqac.dim.pomodoro.entities;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface CategoryDao {
    @Query("SELECT * FROM category")
    List<Category> getAllCategories();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void addCategory(Category category);

    @Query("SELECT * FROM category WHERE id IS :id LIMIT 1")
    Category findById(int id);

    @Query("delete from category")
    void deleteCategories();

    @Query("delete from category WHERE id=:id")
    void deleteById(int id);

    @Delete()
    void deleteById(Category category);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    void updateCategory(Category category);

}
