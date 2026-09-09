package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Project
import com.example.data.model.UserProfile

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTimeLogDialog(
    projects: List<Project>,
    onDismiss: () -> Unit,
    onConfirm: (projectId: Long, title: String, durationMinutes: Long, rate: Double, isBillable: Boolean, notes: String) -> Unit
) {
    var selectedProject by remember { mutableStateOf(projects.firstOrNull()) }
    var expanded by remember { mutableStateOf(false) }
    var taskTitle by remember { mutableStateOf("") }
    var hoursText by remember { mutableStateOf("1") }
    var minutesText by remember { mutableStateOf("30") }
    var isBillable by remember { mutableStateOf(true) }
    var notes by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Log Work Hours", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Project Selector
                if (projects.isNotEmpty()) {
                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = !expanded }
                    ) {
                        OutlinedTextField(
                            value = selectedProject?.name ?: "Select Project",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Project") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                            modifier = Modifier
                                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                                .fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            projects.forEach { proj ->
                                DropdownMenuItem(
                                    text = { Text("${proj.name} (${proj.clientName})") },
                                    onClick = {
                                        selectedProject = proj
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = taskTitle,
                    onValueChange = { taskTitle = it },
                    label = { Text("Task Description") },
                    placeholder = { Text("e.g. API endpoint optimization") },
                    modifier = Modifier.fillMaxWidth().testTag("input_log_title")
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = hoursText,
                        onValueChange = { hoursText = it.filter { c -> c.isDigit() } },
                        label = { Text("Hours") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = minutesText,
                        onValueChange = { minutesText = it.filter { c -> c.isDigit() } },
                        label = { Text("Minutes") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Billable to Client", style = MaterialTheme.typography.bodyMedium)
                    Switch(
                        checked = isBillable,
                        onCheckedChange = { isBillable = it }
                    )
                }

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Deliverable Notes (Optional)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val h = hoursText.toLongOrNull() ?: 0L
                    val m = minutesText.toLongOrNull() ?: 0L
                    val totalMins = maxOf(1L, (h * 60) + m)
                    val p = selectedProject ?: projects.firstOrNull()
                    val pId = p?.id ?: 1L
                    val rate = p?.hourlyRate ?: 95.0
                    onConfirm(
                        pId,
                        taskTitle.ifBlank { "Client Development" },
                        totalMins,
                        rate,
                        isBillable,
                        notes
                    )
                },
                modifier = Modifier.testTag("confirm_add_log")
            ) {
                Text("Save Time Log")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun AddProjectDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, client: String, rate: Double, budgetHours: Double, desc: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var client by remember { mutableStateOf("") }
    var rateText by remember { mutableStateOf("95") }
    var budgetText by remember { mutableStateOf("40") }
    var desc by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New Project & Client", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Project Name") },
                    placeholder = { Text("e.g. Cloud Telemetry Dashboard") },
                    modifier = Modifier.fillMaxWidth().testTag("input_project_name")
                )
                OutlinedTextField(
                    value = client,
                    onValueChange = { client = it },
                    label = { Text("Client or Company Name") },
                    placeholder = { Text("e.g. Acme Tech Solutions") },
                    modifier = Modifier.fillMaxWidth()
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = rateText,
                        onValueChange = { rateText = it.filter { c -> c.isDigit() || c == '.' } },
                        label = { Text("Rate ($/hr)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = budgetText,
                        onValueChange = { budgetText = it.filter { c -> c.isDigit() || c == '.' } },
                        label = { Text("Est. Hours") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                }
                OutlinedTextField(
                    value = desc,
                    onValueChange = { desc = it },
                    label = { Text("Project Scope / Description") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val rate = rateText.toDoubleOrNull() ?: 95.0
                    val budget = budgetText.toDoubleOrNull() ?: 40.0
                    onConfirm(
                        name.ifBlank { "Consulting Project" },
                        client.ifBlank { "Independent Client" },
                        rate,
                        budget,
                        desc
                    )
                },
                modifier = Modifier.testTag("confirm_add_project")
            ) {
                Text("Create Project")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMilestoneDialog(
    projects: List<Project>,
    initialProjectId: Long? = null,
    onDismiss: () -> Unit,
    onConfirm: (projectId: Long, title: String, targetDate: String, payout: Double, notes: String) -> Unit
) {
    var selectedProject by remember {
        mutableStateOf(projects.find { it.id == initialProjectId } ?: projects.firstOrNull())
    }
    var expanded by remember { mutableStateOf(false) }
    var title by remember { mutableStateOf("") }
    var targetDate by remember { mutableStateOf("Oct 30, 2026") }
    var payoutText by remember { mutableStateOf("2500") }
    var notes by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Project Milestone", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (projects.isNotEmpty()) {
                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = !expanded }
                    ) {
                        OutlinedTextField(
                            value = selectedProject?.name ?: "Select Project",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Assigned Project") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                            modifier = Modifier
                                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                                .fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            projects.forEach { proj ->
                                DropdownMenuItem(
                                    text = { Text(proj.name) },
                                    onClick = {
                                        selectedProject = proj
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Milestone Title") },
                    placeholder = { Text("e.g. Phase 2 Database Migration") },
                    modifier = Modifier.fillMaxWidth().testTag("input_milestone_title")
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = targetDate,
                        onValueChange = { targetDate = it },
                        label = { Text("Target Deadline") },
                        modifier = Modifier.weight(1.2f)
                    )
                    OutlinedTextField(
                        value = payoutText,
                        onValueChange = { payoutText = it.filter { c -> c.isDigit() || c == '.' } },
                        label = { Text("Payout ($)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(0.8f)
                    )
                }

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Deliverable Acceptance Criteria") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val pId = selectedProject?.id ?: (projects.firstOrNull()?.id ?: 1L)
                    val payout = payoutText.toDoubleOrNull() ?: 0.0
                    onConfirm(
                        pId,
                        title.ifBlank { "Project Deliverable" },
                        targetDate.ifBlank { "Upcoming" },
                        payout,
                        notes
                    )
                },
                modifier = Modifier.testTag("confirm_add_milestone")
            ) {
                Text("Add Milestone")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun AddExperienceDialog(
    onDismiss: () -> Unit,
    onConfirm: (role: String, company: String, period: String, location: String, desc: String, skills: String) -> Unit
) {
    var role by remember { mutableStateOf("") }
    var company by remember { mutableStateOf("") }
    var period by remember { mutableStateOf("2024 - Present") }
    var location by remember { mutableStateOf("Remote") }
    var desc by remember { mutableStateOf("") }
    var skills by remember { mutableStateOf("Kotlin, Compose, Clean Architecture") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Work Experience", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = role,
                    onValueChange = { role = it },
                    label = { Text("Role / Job Title") },
                    placeholder = { Text("e.g. Principal Mobile Architect") },
                    modifier = Modifier.fillMaxWidth().testTag("input_exp_role")
                )
                OutlinedTextField(
                    value = company,
                    onValueChange = { company = it },
                    label = { Text("Organization / Client") },
                    placeholder = { Text("e.g. Apex Tech Corp") },
                    modifier = Modifier.fillMaxWidth()
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = period,
                        onValueChange = { period = it },
                        label = { Text("Period") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = location,
                        onValueChange = { location = it },
                        label = { Text("Location") },
                        modifier = Modifier.weight(1f)
                    )
                }
                OutlinedTextField(
                    value = desc,
                    onValueChange = { desc = it },
                    label = { Text("Key Achievements & Scope") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )
                OutlinedTextField(
                    value = skills,
                    onValueChange = { skills = it },
                    label = { Text("Skills & Technologies Used") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(
                        role.ifBlank { "Senior Software Engineer" },
                        company.ifBlank { "Independent Contractor" },
                        period.ifBlank { "Recent" },
                        location,
                        desc.ifBlank { "Delivered mission critical features on time." },
                        skills
                    )
                },
                modifier = Modifier.testTag("confirm_add_exp")
            ) {
                Text("Save Experience")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddSkillDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, category: String, proficiency: Int, years: Int) -> Unit
) {
    var name by remember { mutableStateOf("") }
    val categories = listOf("Technical", "Architecture", "Tools & Cloud", "Leadership & Client")
    var selectedCategory by remember { mutableStateOf(categories[0]) }
    var expanded by remember { mutableStateOf(false) }
    var proficiencyText by remember { mutableStateOf("90") }
    var yearsText by remember { mutableStateOf("5") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Professional Skill", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Skill / Capability Name") },
                    placeholder = { Text("e.g. Jetpack Compose & M3") },
                    modifier = Modifier.fillMaxWidth().testTag("input_skill_name")
                )

                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = selectedCategory,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Skill Domain") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        categories.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat) },
                                onClick = {
                                    selectedCategory = cat
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = proficiencyText,
                        onValueChange = { proficiencyText = it.filter { c -> c.isDigit() } },
                        label = { Text("Proficiency (%)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = yearsText,
                        onValueChange = { yearsText = it.filter { c -> c.isDigit() } },
                        label = { Text("Years Exp.") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val prof = (proficiencyText.toIntOrNull() ?: 85).coerceIn(10, 100)
                    val yrs = (yearsText.toIntOrNull() ?: 3).coerceIn(1, 40)
                    onConfirm(name.ifBlank { "Professional Skill" }, selectedCategory, prof, yrs)
                },
                modifier = Modifier.testTag("confirm_add_skill")
            ) {
                Text("Add Skill")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun EditProfileDialog(
    profile: UserProfile,
    onDismiss: () -> Unit,
    onConfirm: (UserProfile) -> Unit
) {
    var fullName by remember { mutableStateOf(profile.fullName) }
    var title by remember { mutableStateOf(profile.title) }
    var bio by remember { mutableStateOf(profile.bio) }
    var email by remember { mutableStateOf(profile.email) }
    var phone by remember { mutableStateOf(profile.phone) }
    var location by remember { mutableStateOf(profile.location) }
    var portfolioUrl by remember { mutableStateOf(profile.portfolioUrl) }
    var availability by remember { mutableStateOf(profile.availabilityStatus) }
    var rateText by remember { mutableStateOf(profile.defaultHourlyRate.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Professional Profile", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = fullName,
                    onValueChange = { fullName = it },
                    label = { Text("Full Name") },
                    modifier = Modifier.fillMaxWidth().testTag("input_profile_name")
                )
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Professional Headline") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = availability,
                    onValueChange = { availability = it },
                    label = { Text("Contract Availability") },
                    placeholder = { Text("e.g. Available (20 hrs/wk)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = rateText,
                        onValueChange = { rateText = it.filter { c -> c.isDigit() || c == '.' } },
                        label = { Text("Default Rate ($/hr)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = location,
                        onValueChange = { location = it },
                        label = { Text("Location") },
                        modifier = Modifier.weight(1f)
                    )
                }
                OutlinedTextField(
                    value = bio,
                    onValueChange = { bio = it },
                    label = { Text("About Me / Executive Bio") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Contact Email") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Phone Number") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = portfolioUrl,
                    onValueChange = { portfolioUrl = it },
                    label = { Text("Portfolio or GitHub URL") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val rate = rateText.toDoubleOrNull() ?: profile.defaultHourlyRate
                    onConfirm(
                        profile.copy(
                            fullName = fullName.ifBlank { profile.fullName },
                            title = title.ifBlank { profile.title },
                            bio = bio.ifBlank { profile.bio },
                            email = email.ifBlank { profile.email },
                            phone = phone.ifBlank { profile.phone },
                            location = location.ifBlank { profile.location },
                            portfolioUrl = portfolioUrl.ifBlank { profile.portfolioUrl },
                            availabilityStatus = availability.ifBlank { profile.availabilityStatus },
                            defaultHourlyRate = rate
                        )
                    )
                },
                modifier = Modifier.testTag("confirm_edit_profile")
            ) {
                Text("Save Changes")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun ExportReportDialog(
    reportText: String,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Billable Hours & Milestones Report", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(320.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = reportText,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clip = ClipData.newPlainText("ProTracker Billable Hours", reportText)
                    clipboard.setPrimaryClip(clip)
                    Toast.makeText(context, "Report copied to clipboard!", Toast.LENGTH_SHORT).show()
                    onDismiss()
                },
                modifier = Modifier.testTag("button_copy_report")
            ) {
                Text("Copy to Clipboard")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}
