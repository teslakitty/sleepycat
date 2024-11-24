package com.teslakitty.sleepycat

import androidx.room.Dao
import androidx.room.Insert

@Dao
interface StoryDao {
    @Insert
    suspend fun insertStory(story: Story)
}
