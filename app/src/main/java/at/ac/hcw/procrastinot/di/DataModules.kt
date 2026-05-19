/*
 * Copyright 2022 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package at.ac.hcw.procrastinot.di

import android.content.Context
import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import at.ac.hcw.procrastinot.data.DefaultTaskRepository
import at.ac.hcw.procrastinot.data.TaskRepository
import at.ac.hcw.procrastinot.data.source.local.TaskDao
import at.ac.hcw.procrastinot.data.source.local.ToDoDatabase
import at.ac.hcw.procrastinot.data.source.network.NetworkDataSource
import at.ac.hcw.procrastinot.data.source.network.TaskNetworkDataSource
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import java.util.UUID
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Singleton
    @Binds
    abstract fun bindTaskRepository(repository: DefaultTaskRepository): TaskRepository
}

@Module
@InstallIn(SingletonComponent::class)
abstract class DataSourceModule {

    @Singleton
    @Binds
    abstract fun bindNetworkDataSource(dataSource: TaskNetworkDataSource): NetworkDataSource
}

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Singleton
    @Provides
    fun provideDataBase(@ApplicationContext context: Context): ToDoDatabase {
        return Room.databaseBuilder(
            context.applicationContext,
            ToDoDatabase::class.java,
            "Tasks.db"
        )
            .fallbackToDestructiveMigration()
            .addCallback(object : RoomDatabase.Callback() {
                override fun onOpen(db: SupportSQLiteDatabase) {
                    super.onOpen(db)
                    val prefs = context.getSharedPreferences(
                        "db_prefs", Context.MODE_PRIVATE
                    )
                    if (!prefs.getBoolean("db_seeded", false)) {
                        seedTasks().forEach { (id, title, desc, priority) ->
                            val values = ContentValues().apply {
                                put("id", id)
                                put("title", title)
                                put("description", desc)
                                put("isCompleted", 0)
                                put("priority", priority)
                            }
                            db.insert("task", SQLiteDatabase.CONFLICT_IGNORE, values)
                        }
                        prefs.edit().putBoolean("db_seeded", true).apply()
                    }
                }
            })
            .build()
    }

    @Provides
    fun provideTaskDao(database: ToDoDatabase): TaskDao = database.taskDao()

    private data class SeedTask(
        val id: String, val title: String, val description: String, val priority: String
    )

    private fun seedTasks(): List<SeedTask> = listOf(
        SeedTask(
            id = UUID.randomUUID().toString(),
            title = "Prepare AI Eindhoven presentation",
            description = "Create slides and demo for the AI Eindhoven conference talk.",
            priority = "HIGH"
        ),
        SeedTask(
            id = UUID.randomUUID().toString(),
            title = "Code review for Leon",
            description = "Review Leon's pull request for the new feature branch.",
            priority = "MEDIUM"
        ),
        SeedTask(
            id = UUID.randomUUID().toString(),
            title = "Help Flo and Mihaiel with brainstorming",
            description = "Discuss app architecture ideas with Flo and Mihaiel over coffee.",
            priority = "LOW"
        ),
    )
}

