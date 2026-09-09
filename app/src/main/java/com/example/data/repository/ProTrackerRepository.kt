package com.example.data.repository

import com.example.data.db.AppDatabase
import com.example.data.model.Milestone
import com.example.data.model.Project
import com.example.data.model.Skill
import com.example.data.model.TimeLog
import com.example.data.model.UserProfile
import com.example.data.model.WorkExperience
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

class ProTrackerRepository(private val database: AppDatabase) {

    val allProjects: Flow<List<Project>> = database.projectDao().getAllProjects()
    val allMilestones: Flow<List<Milestone>> = database.milestoneDao().getAllMilestones()
    val allTimeLogs: Flow<List<TimeLog>> = database.timeLogDao().getAllTimeLogs()
    val allWorkExperiences: Flow<List<WorkExperience>> = database.workExperienceDao().getAllWorkExperiences()
    val allSkills: Flow<List<Skill>> = database.skillDao().getAllSkills()
    val userProfile: Flow<UserProfile?> = database.userProfileDao().getUserProfile()

    fun getMilestonesForProject(projectId: Long): Flow<List<Milestone>> {
        return database.milestoneDao().getMilestonesForProject(projectId)
    }

    fun getTimeLogsForProject(projectId: Long): Flow<List<TimeLog>> {
        return database.timeLogDao().getTimeLogsForProject(projectId)
    }

    suspend fun insertProject(project: Project): Long = withContext(Dispatchers.IO) {
        database.projectDao().insertProject(project)
    }

    suspend fun updateProject(project: Project) = withContext(Dispatchers.IO) {
        database.projectDao().updateProject(project)
    }

    suspend fun deleteProject(project: Project) = withContext(Dispatchers.IO) {
        database.projectDao().deleteProject(project)
    }

    suspend fun insertMilestone(milestone: Milestone): Long = withContext(Dispatchers.IO) {
        database.milestoneDao().insertMilestone(milestone)
    }

    suspend fun updateMilestone(milestone: Milestone) = withContext(Dispatchers.IO) {
        database.milestoneDao().updateMilestone(milestone)
    }

    suspend fun toggleMilestone(milestone: Milestone) = withContext(Dispatchers.IO) {
        database.milestoneDao().updateMilestone(milestone.copy(isCompleted = !milestone.isCompleted))
    }

    suspend fun deleteMilestone(milestone: Milestone) = withContext(Dispatchers.IO) {
        database.milestoneDao().deleteMilestone(milestone)
    }

    suspend fun insertTimeLog(timeLog: TimeLog): Long = withContext(Dispatchers.IO) {
        database.timeLogDao().insertTimeLog(timeLog)
    }

    suspend fun deleteTimeLog(timeLog: TimeLog) = withContext(Dispatchers.IO) {
        database.timeLogDao().deleteTimeLog(timeLog)
    }

    suspend fun insertWorkExperience(experience: WorkExperience): Long = withContext(Dispatchers.IO) {
        database.workExperienceDao().insertWorkExperience(experience)
    }

    suspend fun updateWorkExperience(experience: WorkExperience) = withContext(Dispatchers.IO) {
        database.workExperienceDao().updateWorkExperience(experience)
    }

    suspend fun deleteWorkExperience(experience: WorkExperience) = withContext(Dispatchers.IO) {
        database.workExperienceDao().deleteWorkExperience(experience)
    }

    suspend fun insertSkill(skill: Skill): Long = withContext(Dispatchers.IO) {
        database.skillDao().insertSkill(skill)
    }

    suspend fun updateSkill(skill: Skill) = withContext(Dispatchers.IO) {
        database.skillDao().updateSkill(skill)
    }

    suspend fun deleteSkill(skill: Skill) = withContext(Dispatchers.IO) {
        database.skillDao().deleteSkill(skill)
    }

    suspend fun updateUserProfile(profile: UserProfile) = withContext(Dispatchers.IO) {
        database.userProfileDao().insertOrUpdateProfile(profile)
    }

    suspend fun ensureDataSeeded() = withContext(Dispatchers.IO) {
        val projects = database.projectDao().getAllProjects().first()
        if (projects.isEmpty()) {
            AppDatabase.populateInitialData(database)
        }
    }
}
