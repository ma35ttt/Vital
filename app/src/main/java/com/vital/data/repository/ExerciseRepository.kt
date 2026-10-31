package com.vital.data.repository

import com.vital.data.dao.ExerciseDao
import com.vital.data.entity.Exercise
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExerciseRepository @Inject constructor(
    private val exerciseDao: ExerciseDao
) {
    fun getAllExercises(): Flow<List<Exercise>> = exerciseDao.getAllExercises()
    fun getExercise(id: String): Flow<Exercise?> = exerciseDao.getExercise(id)
    fun getExercisesByMuscleGroup(muscleGroup: com.vital.data.entity.MuscleGroup): Flow<List<Exercise>> = exerciseDao.getExercisesByMuscleGroup(muscleGroup)
    fun searchExercises(query: String): Flow<List<Exercise>> = exerciseDao.searchExercises("%$query%")

    suspend fun insertExercise(exercise: Exercise) = exerciseDao.insertExercise(exercise)
    suspend fun insertExercises(exercises: List<Exercise>) = exerciseDao.insertExercises(exercises)
    suspend fun updateExercise(exercise: Exercise) = exerciseDao.updateExercise(exercise)
    suspend fun softDeleteExercise(id: String, updatedAt: Long) = exerciseDao.softDeleteExercise(id, updatedAt)
    suspend fun getExercisesForSync(since: Long): List<Exercise> = exerciseDao.getExercisesForSync(since)
    suspend fun getExerciseCount(): Int = exerciseDao.getExerciseCount()
}