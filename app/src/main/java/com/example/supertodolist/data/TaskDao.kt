package com.example.supertodolist.data

import androidx.room.*
import com.example.supertodolist.ui.SortOrder
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {

    @Query ("SELECT * FROM task_table WHERE (completed != :hideCompleted OR completed = 0) AND name LIKE '%' || :searchQuery || '%' ORDER BY important DESC, name")
    fun getTasksSortedByName(searchQuery: String, hideCompleted: Boolean) : Flow<List<Task>>

    // OR completed here is to get also all the uncompleted tasks
    @Query ("SELECT * FROM task_table WHERE (completed != :hideCompleted OR completed = 0) AND name LIKE '%' || :searchQuery || '%' ORDER BY important DESC, created")
    fun getTasksSortedByDateCreated(searchQuery: String, hideCompleted: Boolean) : Flow<List<Task>>

    fun getTasks (query: String, sortOrder: SortOrder, hideCompleted: Boolean): Flow<List<Task>> =
        when (sortOrder){
            SortOrder.BY_DATE -> getTasksSortedByDateCreated(query, hideCompleted)
            SortOrder.BY_NAME -> getTasksSortedByName(query, hideCompleted)
        }
    @Insert (onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert (task: Task)

    @Update
    suspend fun update(task: Task)

    @Delete
    suspend fun delete(task: Task)

    @Query("DELETE FROM task_table WHERE completed = 1")
    suspend fun deleteAllCompleted()

}