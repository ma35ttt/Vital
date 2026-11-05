package com.vital.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.vital.data.entity.Exercise
import kotlinx.coroutines.flow.Flow

@Dao
interface ExerciseDao {
    @Query("SELECT * FROM exercises WHERE deleted = 0 ORDER BY muscleGroup, name ASC")
    fun getAllExercises(): Flow<List<Exercise>>

    @Query("SELECT * FROM exercises WHERE id = :id AND deleted = 0")
    fun getExercise(id: String): Flow<Exercise?>

    @Query("SELECT * FROM exercises WHERE muscleGroup = :muscleGroup AND deleted = 0 ORDER BY name ASC")
    fun getExercisesByMuscleGroup(muscleGroup: com.vital.data.entity.MuscleGroup): Flow<List<Exercise>>

    @Query("SELECT * FROM exercises WHERE name LIKE :query AND deleted = 0 ORDER BY name ASC LIMIT 20")
    fun searchExercises(query: String): Flow<List<Exercise>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExercise(exercise: Exercise)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExercises(exercises: List<Exercise>)

    @Update
    suspend fun updateExercise(exercise: Exercise)

    @Query("UPDATE exercises SET deleted = 1, updatedAt = :updatedAt WHERE id = :id")
    suspend fun softDeleteExercise(id: String, updatedAt: Long)

    // Sync reads include tombstones and skip the seeded catalog (ids are `seed:<slug>`).
    @Query("SELECT * FROM exercises WHERE updatedAt > :since AND id NOT LIKE 'seed:%'")
    suspend fun getExercisesForSync(since: Long): List<Exercise>

    @Query("SELECT COUNT(*) FROM exercises WHERE deleted = 0")
    suspend fun getExerciseCount(): Int
}