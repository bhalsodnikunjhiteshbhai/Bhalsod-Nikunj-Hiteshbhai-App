package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.Project
import com.example.data.model.TimeLog
import com.example.ui.components.AddTimeLogDialog
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.EmeraldTeal
import com.example.ui.theme.SlateNavy800
import com.example.ui.theme.SlateNavy900
import com.example.ui.theme.TechBlueLight
import com.example.ui.viewmodel.ProTrackerViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun TimeTrackerScreen(
    viewModel: ProTrackerViewModel,
    modifier: Modifier = Modifier
) {
    val projects by viewModel.projects.collectAsStateWithLifecycle()
    val timeLogs by viewModel.timeLogs.collectAsStateWithLifecycle()
    val metrics by viewModel.metrics.collectAsStateWithLifecycle()

    val isRunning by viewModel.isTimerRunning.collectAsStateWithLifecycle()
    val timerSeconds by viewModel.timerSeconds.collectAsStateWithLifecycle()
    val selectedProjId by viewModel.timerSelectedProjectId.collectAsStateWithLifecycle()
    val taskTitle by viewModel.timerTaskTitle.collectAsStateWithLifecycle()
    val isBillable by viewModel.timerIsBillable.collectAsStateWithLifecycle()

    var showAddManualDialog by remember { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                // Header Title
                Column {
                    Text(
                        text = "Billable Hours & Timer",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Real-time tracking, client invoicing rates & delivery logs",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // High-Impact Live Timer Hero Card
            item {
                LiveTimerHeroCard(
                    isRunning = isRunning,
                    timerSeconds = timerSeconds,
                    projects = projects,
                    selectedProjectId = selectedProjId,
                    taskTitle = taskTitle,
                    isBillable = isBillable,
                    onStart = { viewModel.startTimer() },
                    onPause = { viewModel.pauseTimer() },
                    onReset = { viewModel.resetTimer() },
                    onSave = { viewModel.saveTimerSession() },
                    onProjectSelect = { viewModel.setTimerProject(it) },
                    onTaskTitleChange = { viewModel.setTimerTaskTitle(it) },
                    onBillableToggle = { viewModel.setTimerIsBillable(it) },
                    formattedDuration = viewModel.formatTimerDuration(timerSeconds)
                )
            }

            // Analytics Metrics Grid
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MetricCard(
                        title = "Billable Time",
                        value = "${String.format("%.1f", metrics.totalBillableHours)}h",
                        subtitle = "${metrics.billableEfficiencyPercent}% billable efficiency",
                        icon = Icons.Default.Schedule,
                        accentColor = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f)
                    )
                    MetricCard(
                        title = "Earned Value",
                        value = "$${String.format("%.0f", metrics.totalEarnings)}",
                        subtitle = "${metrics.activeProjectsCount} active contracts",
                        icon = Icons.Default.AttachMoney,
                        accentColor = EmeraldTeal,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Section Header: Recent Logs
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Recent Activity Logs",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${timeLogs.size} logs recorded",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (timeLogs.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                "No Time Logs Yet",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "Start the live timer above or tap '+' to manually add hours.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                items(timeLogs, key = { it.id }) { log ->
                    val proj = projects.find { it.id == log.projectId }
                    TimeLogItemCard(
                        log = log,
                        project = proj,
                        onDelete = { viewModel.deleteTimeLog(log) }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(80.dp)) // padding for bottom nav & FAB
            }
        }

        // Floating Action Button to add manual hours
        FloatingActionButton(
            onClick = { showAddManualDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = 16.dp)
                .testTag("fab_add_time_log"),
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ) {
            Icon(Icons.Default.Add, contentDescription = "Manually Log Hours")
        }
    }

    if (showAddManualDialog) {
        AddTimeLogDialog(
            projects = projects,
            onDismiss = { showAddManualDialog = false },
            onConfirm = { pId, title, duration, rate, isBillable, notes ->
                viewModel.addManualTimeLog(pId, title, duration, rate, isBillable, notes)
                showAddManualDialog = false
            }
        )
    }
}

@Composable
fun LiveTimerHeroCard(
    isRunning: Boolean,
    timerSeconds: Long,
    projects: List<Project>,
    selectedProjectId: Long?,
    taskTitle: String,
    isBillable: Boolean,
    onStart: () -> Unit,
    onPause: () -> Unit,
    onReset: () -> Unit,
    onSave: () -> Unit,
    onProjectSelect: (Long?) -> Unit,
    onTaskTitleChange: (String) -> Unit,
    onBillableToggle: (Boolean) -> Unit,
    formattedDuration: String
) {
    var projectMenuOpen by remember { mutableStateOf(false) }
    val currentProject = projects.find { it.id == selectedProjectId } ?: projects.firstOrNull()

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = SlateNavy900),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(SlateNavy900, SlateNavy800)
                    )
                )
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Bar: Project selector chip and Billable toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Project Selector Pill
                Box {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = Color(0xFF1E293B),
                        modifier = Modifier.clickable { projectMenuOpen = true }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF38BDF8))
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = currentProject?.name ?: "Select Project",
                                style = MaterialTheme.typography.labelLarge,
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    DropdownMenu(
                        expanded = projectMenuOpen,
                        onDismissRequest = { projectMenuOpen = false }
                    ) {
                        projects.forEach { proj ->
                            DropdownMenuItem(
                                text = { Text("${proj.name} ($${proj.hourlyRate.toInt()}/h)") },
                                onClick = {
                                    onProjectSelect(proj.id)
                                    projectMenuOpen = false
                                }
                            )
                        }
                    }
                }

                // Billable Toggle
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (isBillable) "Billable" else "Internal",
                        style = MaterialTheme.typography.labelMedium,
                        color = if (isBillable) EmeraldLight else Color(0xFF94A3B8),
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Switch(
                        checked = isBillable,
                        onCheckedChange = onBillableToggle,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = EmeraldLight,
                            checkedTrackColor = Color(0xFF134E4A),
                            uncheckedThumbColor = Color(0xFF64748B),
                            uncheckedTrackColor = Color(0xFF334155)
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Task title input
            OutlinedTextField(
                value = taskTitle,
                onValueChange = onTaskTitleChange,
                placeholder = { Text("What are you working on?", color = Color(0xFF94A3B8)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_timer_task_title"),
                shape = RoundedCornerShape(12.dp),
                colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = TechBlueLight,
                    unfocusedBorderColor = Color(0xFF334155),
                    focusedContainerColor = Color(0xFF0F172A),
                    unfocusedContainerColor = Color(0xFF0F172A)
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Digital Stopwatch Display
            Text(
                text = formattedDuration,
                fontFamily = FontFamily.Monospace,
                fontSize = 44.sp,
                fontWeight = FontWeight.Bold,
                color = if (isRunning) EmeraldLight else Color.White,
                letterSpacing = 2.sp,
                modifier = Modifier.testTag("text_timer_duration")
            )

            if (currentProject != null && isBillable) {
                val currentRate = currentProject.hourlyRate
                val accrued = (timerSeconds / 3600.0) * currentRate
                Text(
                    text = "Accrued value: $${String.format("%.2f", accrued)} (Rate: $${currentRate.toInt()}/h)",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF94A3B8)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Stopwatch Controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Reset Button
                IconButton(
                    onClick = onReset,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF334155))
                        .testTag("btn_timer_reset")
                ) {
                    Icon(
                        Icons.Default.Refresh,
                        contentDescription = "Reset Timer",
                        tint = Color.White
                    )
                }

                Spacer(modifier = Modifier.width(20.dp))

                // Play / Pause Button
                Button(
                    onClick = { if (isRunning) onPause() else onStart() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isRunning) Color(0xFFDC2626) else EmeraldTeal
                    ),
                    shape = CircleShape,
                    modifier = Modifier
                        .size(68.dp)
                        .testTag("btn_timer_toggle")
                ) {
                    Icon(
                        imageVector = if (isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isRunning) "Pause Timer" else "Start Timer",
                        modifier = Modifier.size(36.dp),
                        tint = Color.White
                    )
                }

                Spacer(modifier = Modifier.width(20.dp))

                // Save Session Button
                Button(
                    onClick = onSave,
                    enabled = timerSeconds > 0,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = TechBlueLight,
                        contentColor = SlateNavy900,
                        disabledContainerColor = Color(0xFF334155),
                        disabledContentColor = Color(0xFF64748B)
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .height(48.dp)
                        .testTag("btn_timer_save")
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Save", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun MetricCard(
    title: String,
    value: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(accentColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun TimeLogItemCard(
    log: TimeLog,
    project: Project?,
    onDelete: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("MMM d, h:mm a", Locale.getDefault()) }
    val dateStr = remember(log.timestamp) { dateFormat.format(Date(log.timestamp)) }
    val hours = log.durationMinutes / 60
    val mins = log.durationMinutes % 60
    val durationText = if (hours > 0) "${hours}h ${mins}m" else "${mins}m"

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Billable accent indicator
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        if (log.isBillable) EmeraldTeal.copy(alpha = 0.15f)
                        else MaterialTheme.colorScheme.surfaceVariant
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (log.isBillable) "$" else "•",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (log.isBillable) EmeraldTeal else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            // Details
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = log.taskTitle,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${project?.name ?: "Project"} • $dateStr",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (log.notes.isNotBlank()) {
                    Text(
                        text = log.notes,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    )
                }
            }

            // Duration & Earnings
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = durationText,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (log.isBillable) {
                    Text(
                        text = "+$${String.format("%.2f", log.earnings)}",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = EmeraldTeal
                    )
                } else {
                    Text(
                        text = "Non-billable",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            IconButton(
                onClick = onDelete,
                modifier = Modifier
                    .padding(start = 4.dp)
                    .size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.DeleteOutline,
                    contentDescription = "Delete Time Log",
                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
