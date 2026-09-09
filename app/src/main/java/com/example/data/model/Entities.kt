package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "projects")
data class Project(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val clientName: String,
    val hourlyRate: Double = 85.0,
    val status: String = "In Progress", // "In Progress", "Completed", "On Hold"
    val colorHex: String = "#0284C7",
    val budgetHours: Double = 40.0,
    val description: String = "",
    val portfolioHighlight: String = "",
    val isFeatured: Boolean = true
)

@Entity(tableName = "milestones")
data class Milestone(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val projectId: Long,
    val title: String,
    val targetDate: String,
    val isCompleted: Boolean = false,
    val payoutAmount: Double = 0.0,
    val notes: String = ""
)

@Entity(tableName = "time_logs")
data class TimeLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val projectId: Long,
    val taskTitle: String,
    val durationMinutes: Long,
    val hourlyRate: Double,
    val isBillable: Boolean = true,
    val timestamp: Long = System.currentTimeMillis(),
    val notes: String = ""
) {
    val earnings: Double
        get() = if (isBillable) (durationMinutes / 60.0) * hourlyRate else 0.0
}

@Entity(tableName = "work_experiences")
data class WorkExperience(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val role: String,
    val company: String,
    val period: String,
    val location: String = "",
    val description: String,
    val skillsUsed: String,
    val orderIndex: Int = 0
)

@Entity(tableName = "skills")
data class Skill(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val category: String, // "Technical", "Architecture", "Tools & Cloud", "Leadership"
    val proficiencyPercent: Int = 85, // 0 to 100
    val yearsExperience: Int = 4
)

@Entity(tableName = "user_profile")
data class UserProfile(
    @PrimaryKey val id: Long = 1,
    val fullName: String = "Alex Rivera",
    val title: String = "Principal Mobile Solutions Architect & Consultant",
    val bio: String = "10+ years crafting enterprise Android solutions, high-throughput backend integrations, and leading distributed engineering teams. Specialized in milestone-driven contract delivery and robust architecture.",
    val email: String = "alex.rivera@proarchitect.dev",
    val phone: String = "+1 (415) 555-0192",
    val location: String = "San Francisco, CA (Available Worldwide)",
    val portfolioUrl: String = "https://github.com/alexrivera-architect",
    val availabilityStatus: String = "Available for Contract (20 hrs/week)",
    val defaultHourlyRate: Double = 95.0
)
