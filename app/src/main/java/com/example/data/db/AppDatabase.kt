package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.Milestone
import com.example.data.model.Project
import com.example.data.model.Skill
import com.example.data.model.TimeLog
import com.example.data.model.UserProfile
import com.example.data.model.WorkExperience
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        Project::class,
        Milestone::class,
        TimeLog::class,
        WorkExperience::class,
        Skill::class,
        UserProfile::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun projectDao(): ProjectDao
    abstract fun milestoneDao(): MilestoneDao
    abstract fun timeLogDao(): TimeLogDao
    abstract fun workExperienceDao(): WorkExperienceDao
    abstract fun skillDao(): SkillDao
    abstract fun userProfileDao(): UserProfileDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "protracker_database"
                )
                .fallbackToDestructiveMigration()
                .addCallback(DatabaseCallback(scope))
                .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateInitialData(database)
                    }
                }
            }
        }

        suspend fun populateInitialData(db: AppDatabase) {
            // Seed User Profile
            db.userProfileDao().insertOrUpdateProfile(
                UserProfile(
                    id = 1,
                    fullName = "Alex Rivera",
                    title = "Principal Mobile Solutions Architect & Consultant",
                    bio = "10+ years engineering mission-critical Android systems, reactive architectures, and enterprise client platforms. Dedicated to verifiable milestone delivery and high-standard craftsmanship.",
                    email = "alex.rivera@proarchitect.dev",
                    phone = "+1 (415) 555-0192",
                    location = "San Francisco, CA (Remote Consultant)",
                    portfolioUrl = "https://github.com/alexrivera-architect",
                    availabilityStatus = "Open for Q4 Contract (25 hrs/week)",
                    defaultHourlyRate = 95.0
                )
            )

            // Seed Projects
            val proj1 = Project(
                id = 1,
                name = "FinEdge Global Payment Engine",
                clientName = "Horizon Capital & Pay",
                hourlyRate = 110.0,
                status = "In Progress",
                colorHex = "#0284C7",
                budgetHours = 80.0,
                description = "Modernizing high-security biometric transaction confirmation flow with offline queue sync and cryptographic signing.",
                portfolioHighlight = "Engineered sub-50ms transaction signing layer and zero-failure Room ledger synchronization.",
                isFeatured = true
            )
            val proj2 = Project(
                id = 2,
                name = "PulseHealth Telemedicine Suite",
                clientName = "Apex Health Group",
                hourlyRate = 95.0,
                status = "In Progress",
                colorHex = "#0D9488",
                budgetHours = 60.0,
                description = "HIPAA-compliant video consultation & real-time telemetry streaming for regional specialists.",
                portfolioHighlight = "Architected WebRTC video streaming bridge with fallback push protocols.",
                isFeatured = true
            )
            val proj3 = Project(
                id = 3,
                name = "OmniLogistics Fleet Tracker",
                clientName = "TransLogix Global",
                hourlyRate = 85.0,
                status = "Completed",
                colorHex = "#F59E0B",
                budgetHours = 120.0,
                description = "Battery-optimized background GPS geo-fence telemetry logging with MQTT broadcast.",
                portfolioHighlight = "Reduced background battery consumption by 62% across 8,000 active driver tablets.",
                isFeatured = true
            )
            db.projectDao().insertProjects(listOf(proj1, proj2, proj3))

            // Seed Milestones
            val milestones = listOf(
                Milestone(
                    id = 1,
                    projectId = 1,
                    title = "Architecture Specification & API Contracts",
                    targetDate = "Sep 15, 2026",
                    isCompleted = true,
                    payoutAmount = 3500.0,
                    notes = "Finalized OpenAPI schemas and zero-trust auth sequence"
                ),
                Milestone(
                    id = 2,
                    projectId = 1,
                    title = "Biometric Vault & Hardware Key Attestation",
                    targetDate = "Sep 30, 2026",
                    isCompleted = true,
                    payoutAmount = 4800.0,
                    notes = "Keystore integration tested on Pixel and Samsung hardware"
                ),
                Milestone(
                    id = 3,
                    projectId = 1,
                    title = "Production Load & Pen-Testing Sign-off",
                    targetDate = "Oct 20, 2026",
                    isCompleted = false,
                    payoutAmount = 5200.0,
                    notes = "Pending external auditor evaluation"
                ),
                Milestone(
                    id = 4,
                    projectId = 2,
                    title = "WebRTC Core Audio/Video Pipeline",
                    targetDate = "Sep 22, 2026",
                    isCompleted = true,
                    payoutAmount = 3000.0,
                    notes = "STUN/TURN negotiation with packet loss concealment"
                ),
                Milestone(
                    id = 5,
                    projectId = 2,
                    title = "EHR Medical Record Sync & Attachment Viewer",
                    targetDate = "Oct 10, 2026",
                    isCompleted = false,
                    payoutAmount = 4200.0,
                    notes = "Encrypted cache layer with auto-purge"
                ),
                Milestone(
                    id = 6,
                    projectId = 3,
                    title = "Geofence Trigger Service Deployment",
                    targetDate = "Aug 30, 2026",
                    isCompleted = true,
                    payoutAmount = 6000.0,
                    notes = "Complete fleet rollout verified"
                )
            )
            db.milestoneDao().insertMilestones(milestones)

            // Seed Time Logs
            val now = System.currentTimeMillis()
            val day = 86400000L
            val timeLogs = listOf(
                TimeLog(
                    id = 1,
                    projectId = 1,
                    taskTitle = "Biometric prompt fallback & error handling refinement",
                    durationMinutes = 180, // 3 hrs
                    hourlyRate = 110.0,
                    isBillable = true,
                    timestamp = now - (day * 1) + 3600000,
                    notes = "Verified face unlock fallback to passcode"
                ),
                TimeLog(
                    id = 2,
                    projectId = 1,
                    taskTitle = "Code review & contract interface documentation",
                    durationMinutes = 90, // 1.5 hrs
                    hourlyRate = 110.0,
                    isBillable = true,
                    timestamp = now - (day * 1) + 14400000,
                    notes = "Synced with backend team on error codes"
                ),
                TimeLog(
                    id = 3,
                    projectId = 2,
                    taskTitle = "WebRTC peer connection candidate renegotiation",
                    durationMinutes = 240, // 4 hrs
                    hourlyRate = 95.0,
                    isBillable = true,
                    timestamp = now - (day * 2) + 7200000,
                    notes = "Fixed reconnection when network transitions 5G to Wi-Fi"
                ),
                TimeLog(
                    id = 4,
                    projectId = 2,
                    taskTitle = "Consultation UI layout testing on Foldables",
                    durationMinutes = 120, // 2 hrs
                    hourlyRate = 95.0,
                    isBillable = true,
                    timestamp = now - (day * 3),
                    notes = "Added adaptive multi-pane layout"
                ),
                TimeLog(
                    id = 5,
                    projectId = 1,
                    taskTitle = "Internal tooling setup & CI pipeline optimization",
                    durationMinutes = 75,
                    hourlyRate = 110.0,
                    isBillable = false,
                    timestamp = now - (day * 4),
                    notes = "Non-billable internal productivity setup"
                )
            )
            db.timeLogDao().insertTimeLogs(timeLogs)

            // Seed Work Experience
            val experiences = listOf(
                WorkExperience(
                    id = 1,
                    role = "Principal Mobile Solutions Architect",
                    company = "Rivera Tech Consulting LLC",
                    period = "2022 - Present",
                    location = "San Francisco, CA (Global Remote)",
                    description = "Providing high-impact architectural consulting for Series B+ startups and enterprise enterprises. Architecting reactive Android systems, milestone-gated engineering deliverables, and CI/CD pipelines.",
                    skillsUsed = "Kotlin, Jetpack Compose, Coroutines/Flow, Room, Distributed Architecture, Client Milestone Delivery",
                    orderIndex = 1
                ),
                WorkExperience(
                    id = 2,
                    role = "Lead Android Engineer",
                    company = "Starlight FinTech Systems",
                    period = "2019 - 2022",
                    location = "San Francisco, CA",
                    description = "Headed a distributed 8-engineer mobile team. Designed and shipped the core multi-currency investment wallet used by 2.4M active accounts, achieving 99.98% crash-free sessions.",
                    skillsUsed = "Android SDK, Security/Biometrics, Clean Architecture, Reactive Systems, Team Mentorship",
                    orderIndex = 2
                ),
                WorkExperience(
                    id = 3,
                    role = "Senior Software Engineer",
                    company = "Nexus Data Labs",
                    period = "2016 - 2019",
                    location = "Boston, MA",
                    description = "Engineered real-time telemetry ingestion clients and local SQLite database caching engines for enterprise IoT tablets.",
                    skillsUsed = "Kotlin, Java, SQLite/Room, WebSockets, Performance Profiling",
                    orderIndex = 3
                )
            )
            db.workExperienceDao().insertWorkExperiences(experiences)

            // Seed Skills
            val skills = listOf(
                Skill(name = "Kotlin & Modern Android", category = "Technical", proficiencyPercent = 98, yearsExperience = 9),
                Skill(name = "Jetpack Compose & Material 3", category = "Technical", proficiencyPercent = 95, yearsExperience = 5),
                Skill(name = "Room & Offline-First Persistence", category = "Technical", proficiencyPercent = 94, yearsExperience = 8),
                Skill(name = "Kotlin Coroutines & Flow", category = "Technical", proficiencyPercent = 96, yearsExperience = 7),
                Skill(name = "Clean Architecture & Modularization", category = "Architecture", proficiencyPercent = 95, yearsExperience = 8),
                Skill(name = "Biometrics & Hardware Keystore", category = "Architecture", proficiencyPercent = 90, yearsExperience = 5),
                Skill(name = "WebRTC & Real-Time Streaming", category = "Architecture", proficiencyPercent = 88, yearsExperience = 4),
                Skill(name = "Gradle, KSP & CI/CD Tooling", category = "Tools & Cloud", proficiencyPercent = 92, yearsExperience = 7),
                Skill(name = "Git, Profiler & Performance Tuning", category = "Tools & Cloud", proficiencyPercent = 94, yearsExperience = 10),
                Skill(name = "Milestone Planning & Scoping", category = "Leadership & Client", proficiencyPercent = 96, yearsExperience = 8),
                Skill(name = "Client Communication & Consulting", category = "Leadership & Client", proficiencyPercent = 95, yearsExperience = 9)
            )
            db.skillDao().insertSkills(skills)
        }
    }
}
