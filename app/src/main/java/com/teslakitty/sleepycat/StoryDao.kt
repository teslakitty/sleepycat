package com.teslakitty.sleepycat

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface StoryDao {
    @Insert
    suspend fun insertStory(story: Story)

    @Query("SELECT * FROM stories")
    suspend fun getAllStories(): List<Story>
}
