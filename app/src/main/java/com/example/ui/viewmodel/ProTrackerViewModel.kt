package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.AppDatabase
import com.example.data.model.Milestone
import com.example.data.model.Project
import com.example.data.model.Skill
import com.example.data.model.TimeLog
import com.example.data.model.UserProfile
import com.example.data.model.WorkExperience
import com.example.data.repository.ProTrackerRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class DashboardMetrics(
    val totalBillableHours: Double = 0.0,
    val totalEarnings: Double = 0.0,
    val completedMilestones: Int = 0,
    val totalMilestones: Int = 0,
    val activeProjectsCount: Int = 0,
    val billableEfficiencyPercent: Int = 0
)

class ProTrackerViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: ProTrackerRepository

    init {
        val db = AppDatabase.getDatabase(application, viewModelScope)
        repository = ProTrackerRepository(db)
        viewModelScope.launch {
            repository.ensureDataSeeded()
        }
    }

    val projects: StateFlow<List<Project>> = repository.allProjects
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val milestones: StateFlow<List<Milestone>> = repository.allMilestones
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val timeLogs: StateFlow<List<TimeLog>> = repository.allTimeLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val experiences: StateFlow<List<WorkExperience>> = repository.allWorkExperiences
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val skills: StateFlow<List<Skill>> = repository.allSkills
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val userProfile: StateFlow<UserProfile> = repository.userProfile
        .map { it ?: UserProfile() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UserProfile())

    // Metrics combined flow
    val metrics: StateFlow<DashboardMetrics> = combine(
        projects,
        milestones,
        timeLogs
    ) { projs, miles, logs ->
        val billableLogs = logs.filter { it.isBillable }
        val totalMinutes = logs.sumOf { it.durationMinutes }
        val billableMinutes = billableLogs.sumOf { it.durationMinutes }

        val totalHours = billableMinutes / 60.0
        val totalEarnings = billableLogs.sumOf { it.earnings }

        val completedCount = miles.count { it.isCompleted }
        val activeProjCount = projs.count { it.status == "In Progress" }

        val efficiency = if (totalMinutes > 0) ((billableMinutes.toDouble() / totalMinutes) * 100).toInt() else 100

        DashboardMetrics(
            totalBillableHours = totalHours,
            totalEarnings = totalEarnings,
            completedMilestones = completedCount,
            totalMilestones = miles.size,
            activeProjectsCount = activeProjCount,
            billableEfficiencyPercent = efficiency
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DashboardMetrics())

    // ----------------------------------------------------
    // LIVE TIMER STATE & ENGINE
    // ----------------------------------------------------
    private val _isTimerRunning = MutableStateFlow(false)
    val isTimerRunning: StateFlow<Boolean> = _isTimerRunning.asStateFlow()

    private val _timerSeconds = MutableStateFlow(0L)
    val timerSeconds: StateFlow<Long> = _timerSeconds.asStateFlow()

    private val _timerSelectedProjectId = MutableStateFlow<Long?>(null)
    val timerSelectedProjectId: StateFlow<Long?> = _timerSelectedProjectId.asStateFlow()

    private val _timerTaskTitle = MutableStateFlow("")
    val timerTaskTitle: StateFlow<String> = _timerTaskTitle.asStateFlow()

    private val _timerIsBillable = MutableStateFlow(true)
    val timerIsBillable: StateFlow<Boolean> = _timerIsBillable.asStateFlow()

    private var timerJob: Job? = null

    fun setTimerProject(projectId: Long?) {
        _timerSelectedProjectId.value = projectId
    }

    fun setTimerTaskTitle(title: String) {
        _timerTaskTitle.value = title
    }

    fun setTimerIsBillable(billable: Boolean) {
        _timerIsBillable.value = billable
    }

    fun startTimer() {
        if (_isTimerRunning.value) return
        _isTimerRunning.value = true
        timerJob = viewModelScope.launch {
            while (_isTimerRunning.value) {
                delay(1000L)
                _timerSeconds.value += 1
            }
        }
    }

    fun pauseTimer() {
        _isTimerRunning.value = false
        timerJob?.cancel()
    }

    fun resetTimer() {
        pauseTimer()
        _timerSeconds.value = 0L
    }

    fun saveTimerSession(notes: String = "") {
        val totalSecs = _timerSeconds.value
        if (totalSecs < 5) {
            // Under 5 seconds, simply reset
            resetTimer()
            return
        }

        val minutes = maxOf(1L, (totalSecs + 30) / 60)
        val selectedProj = projects.value.find { it.id == _timerSelectedProjectId.value } ?: projects.value.firstOrNull()
        val pId = selectedProj?.id ?: 1L
        val rate = selectedProj?.hourlyRate ?: userProfile.value.defaultHourlyRate
        val title = _timerTaskTitle.value.ifBlank { "Client Deliverable & Engineering" }

        viewModelScope.launch {
            repository.insertTimeLog(
                TimeLog(
                    projectId = pId,
                    taskTitle = title,
                    durationMinutes = minutes,
                    hourlyRate = rate,
                    isBillable = _timerIsBillable.value,
                    notes = notes
                )
            )
            resetTimer()
            _timerTaskTitle.value = ""
        }
    }

    // ----------------------------------------------------
    // TIME LOG OPERATIONS
    // ----------------------------------------------------
    fun addManualTimeLog(
        projectId: Long,
        title: String,
        durationMinutes: Long,
        hourlyRate: Double,
        isBillable: Boolean,
        notes: String
    ) {
        viewModelScope.launch {
            repository.insertTimeLog(
                TimeLog(
                    projectId = projectId,
                    taskTitle = title,
                    durationMinutes = durationMinutes,
                    hourlyRate = hourlyRate,
                    isBillable = isBillable,
                    notes = notes
                )
            )
        }
    }

    fun deleteTimeLog(log: TimeLog) {
        viewModelScope.launch {
            repository.deleteTimeLog(log)
        }
    }

    // ----------------------------------------------------
    // PROJECT OPERATIONS
    // ----------------------------------------------------
    fun addProject(
        name: String,
        clientName: String,
        hourlyRate: Double,
        budgetHours: Double,
        description: String,
        colorHex: String = "#0284C7",
        portfolioHighlight: String = ""
    ) {
        viewModelScope.launch {
            repository.insertProject(
                Project(
                    name = name,
                    clientName = clientName,
                    hourlyRate = hourlyRate,
                    budgetHours = budgetHours,
                    description = description,
                    colorHex = colorHex,
                    portfolioHighlight = portfolioHighlight,
                    status = "In Progress"
                )
            )
        }
    }

    fun updateProject(project: Project) {
        viewModelScope.launch {
            repository.updateProject(project)
        }
    }

    fun deleteProject(project: Project) {
        viewModelScope.launch {
            repository.deleteProject(project)
        }
    }

    // ----------------------------------------------------
    // MILESTONE OPERATIONS
    // ----------------------------------------------------
    fun toggleMilestone(milestone: Milestone) {
        viewModelScope.launch {
            repository.toggleMilestone(milestone)
        }
    }

    fun addMilestone(
        projectId: Long,
        title: String,
        targetDate: String,
        payoutAmount: Double,
        notes: String
    ) {
        viewModelScope.launch {
            repository.insertMilestone(
                Milestone(
                    projectId = projectId,
                    title = title,
                    targetDate = targetDate,
                    payoutAmount = payoutAmount,
                    notes = notes,
                    isCompleted = false
                )
            )
        }
    }

    fun deleteMilestone(milestone: Milestone) {
        viewModelScope.launch {
            repository.deleteMilestone(milestone)
        }
    }

    // ----------------------------------------------------
    // WORK EXPERIENCE OPERATIONS
    // ----------------------------------------------------
    fun addWorkExperience(
        role: String,
        company: String,
        period: String,
        location: String,
        description: String,
        skillsUsed: String
    ) {
        viewModelScope.launch {
            repository.insertWorkExperience(
                WorkExperience(
                    role = role,
                    company = company,
                    period = period,
                    location = location,
                    description = description,
                    skillsUsed = skillsUsed,
                    orderIndex = (experiences.value.maxOfOrNull { it.orderIndex } ?: 0) + 1
                )
            )
        }
    }

    fun deleteWorkExperience(experience: WorkExperience) {
        viewModelScope.launch {
            repository.deleteWorkExperience(experience)
        }
    }

    // ----------------------------------------------------
    // SKILLS OPERATIONS
    // ----------------------------------------------------
    fun addSkill(
        name: String,
        category: String,
        proficiencyPercent: Int,
        yearsExperience: Int
    ) {
        viewModelScope.launch {
            repository.insertSkill(
                Skill(
                    name = name,
                    category = category,
                    proficiencyPercent = proficiencyPercent,
                    yearsExperience = yearsExperience
                )
            )
        }
    }

    fun deleteSkill(skill: Skill) {
        viewModelScope.launch {
            repository.deleteSkill(skill)
        }
    }

    // ----------------------------------------------------
    // USER PROFILE OPERATIONS
    // ----------------------------------------------------
    fun updateUserProfile(profile: UserProfile) {
        viewModelScope.launch {
            repository.updateUserProfile(profile)
        }
    }

    // Helper formatter for seconds -> "00:00:00"
    fun formatTimerDuration(seconds: Long): String {
        val hrs = seconds / 3600
        val mins = (seconds % 3600) / 60
        val secs = seconds % 60
        return if (hrs > 0) {
            String.format("%02d:%02d:%02d", hrs, mins, secs)
        } else {
            String.format("%02d:%02d", mins, secs)
        }
    }

    // Generate clean text summary for clipboard or invoice share
    fun generateInvoiceReport(): String {
        val prof = userProfile.value
        val projs = projects.value
        val logs = timeLogs.value.filter { it.isBillable }
        val sb = StringBuilder()
        sb.appendLine("=== ${prof.fullName} - BILLABLE HOURS REPORT ===")
        sb.appendLine("Title: ${prof.title}")
        sb.appendLine("Contact: ${prof.email} | ${prof.phone}")
        sb.appendLine("Report Generated: Local Time")
        sb.appendLine("--------------------------------------------")
        val totalMins = logs.sumOf { it.durationMinutes }
        val totalEarn = logs.sumOf { it.earnings }
        sb.appendLine("Total Billable Time: ${String.format("%.1f", totalMins / 60.0)} hrs")
        sb.appendLine("Total Billable Value: $${String.format("%.2f", totalEarn)}")
        sb.appendLine("--------------------------------------------")
        sb.appendLine("PROJECT BREAKDOWN:")
        projs.forEach { p ->
            val pLogs = logs.filter { it.projectId == p.id }
            val pMins = pLogs.sumOf { it.durationMinutes }
            val pEarn = pLogs.sumOf { it.earnings }
            sb.appendLine("• ${p.name} (${p.clientName}): ${String.format("%.1f", pMins / 60.0)} hrs | $${String.format("%.2f", pEarn)}")
        }
        sb.appendLine("--------------------------------------------")
        sb.appendLine("MILESTONE STATUS:")
        milestones.value.forEach { m ->
            val status = if (m.isCompleted) "[COMPLETED]" else "[PENDING]"
            sb.appendLine("$status ${m.title} (Due: ${m.targetDate}) - $${String.format("%.0f", m.payoutAmount)}")
        }
        return sb.toString()
    }
}
