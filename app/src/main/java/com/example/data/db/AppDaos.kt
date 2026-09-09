package com.example.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.Milestone
import com.example.data.model.Project
import com.example.data.model.Skill
import com.example.data.model.TimeLog
import com.example.data.model.UserProfile
import com.example.data.model.WorkExperience
import kotlinx.coroutines.flow.Flow

@Dao
interface ProjectDao {
    @Query("SELECT * FROM projects ORDER BY id DESC")
    fun getAllProjects(): Flow<List<Project>>

    @Query("SELECT * FROM projects WHERE id = :id")
    fun getProjectById(id: Long): Flow<Project?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProject(project: Project): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProjects(projects: List<Project>)

    @Update
    suspend fun updateProject(project: Project)

    @Delete
    suspend fun deleteProject(project: Project)
}

@Dao
interface MilestoneDao {
    @Query("SELECT * FROM milestones ORDER BY id ASC")
    fun getAllMilestones(): Flow<List<Milestone>>

    @Query("SELECT * FROM milestones WHERE projectId = :projectId ORDER BY id ASC")
    fun getMilestonesForProject(projectId: Long): Flow<List<Milestone>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMilestone(milestone: Milestone): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMilestones(milestones: List<Milestone>)

    @Update
    suspend fun updateMilestone(milestone: Milestone)

    @Delete
    suspend fun deleteMilestone(milestone: Milestone)
}

@Dao
interface TimeLogDao {
    @Query("SELECT * FROM time_logs ORDER BY timestamp DESC")
    fun getAllTimeLogs(): Flow<List<TimeLog>>

    @Query("SELECT * FROM time_logs WHERE projectId = :projectId ORDER BY timestamp DESC")
    fun getTimeLogsForProject(projectId: Long): Flow<List<TimeLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTimeLog(timeLog: TimeLog): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTimeLogs(timeLogs: List<TimeLog>)

    @Delete
    suspend fun deleteTimeLog(timeLog: TimeLog)
}

@Dao
interface WorkExperienceDao {
    @Query("SELECT * FROM work_experiences ORDER BY orderIndex ASC, id DESC")
    fun getAllWorkExperiences(): Flow<List<WorkExperience>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkExperience(experience: WorkExperience): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkExperiences(experiences: List<WorkExperience>)

    @Update
    suspend fun updateWorkExperience(experience: WorkExperience)

    @Delete
    suspend fun deleteWorkExperience(experience: WorkExperience)
}

@Dao
interface SkillDao {
    @Query("SELECT * FROM skills ORDER BY category ASC, proficiencyPercent DESC")
    fun getAllSkills(): Flow<List<Skill>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSkill(skill: Skill): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSkills(skills: List<Skill>)

    @Update
    suspend fun updateSkill(skill: Skill)

    @Delete
    suspend fun deleteSkill(skill: Skill)
}

@Dao
interface UserProfileDao {
    @Query("SELECT * FROM user_profile WHERE id = 1 LIMIT 1")
    fun getUserProfile(): Flow<UserProfile?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateProfile(profile: UserProfile)
}
