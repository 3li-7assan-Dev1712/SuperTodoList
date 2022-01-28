package com.example.supertodolist.data

import android.content.Context
import android.util.Log
import androidx.datastore.preferences.createDataStore
import androidx.datastore.preferences.edit
import androidx.datastore.preferences.emptyPreferences
import androidx.datastore.preferences.preferencesKey
import com.example.supertodolist.ui.SortOrder
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton


private const val TAG = "PreferencesManager"

data class FilterPreferences (val sortOrder: SortOrder, val hideCompleted: Boolean)


@Singleton
class PreferencesManager @Inject constructor(@ApplicationContext context: Context) {

    private val dataStore = context.createDataStore("user_preferences")

    val preferencesFlow = dataStore.data
        .catch {
            if (it is IOException) {
                Log.e(TAG, "error reading data: ${it.message}")
                emit(emptyPreferences())
            } else {
                // if the exception is not input output exception throw the exception and
                    // do not let the app continue.
                throw it
            }
        }
        .map { preferences ->

            val sortOrder = SortOrder.valueOf(preferences[PreferencesKeys.SORT_ORDER] ?: SortOrder.BY_DATE.name)
            val hideCompleted = preferences[PreferencesKeys.HIDE_COMPLETED] ?: false
            FilterPreferences(sortOrder, hideCompleted)
        }


    private object PreferencesKeys {
        val SORT_ORDER = preferencesKey<String>("sort_order")
        val HIDE_COMPLETED = preferencesKey<Boolean>("hide_completed")
    }

    // update the preferences
    suspend fun updateSortOrder (sortOrder: SortOrder) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.SORT_ORDER] = sortOrder.name
        }
    }
    suspend fun updateHideCompleted (hideCompleted: Boolean) {
        dataStore.edit {
            it [PreferencesKeys.HIDE_COMPLETED] = hideCompleted
        }
    }
}