package com.scholartrackapp.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.scholartrackapp.data.local.ApplicationEntity
import com.scholartrackapp.data.local.ApplicationStatus
import com.scholartrackapp.data.repository.ScholarshipRepository
import com.scholartrackapp.util.parseDate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

data class FormState(
    val title: String = "",
    val provider: String = "",
    val category: String = "Scholarship",
    val school: String = "",
    val course: String = "",
    val requirements: String = "",
    val deadline: String = "",
    val status: ApplicationStatus = ApplicationStatus.NOT_STARTED,
    val notes: String = "",
    val titleError: String? = null,
    val providerError: String? = null,
    val schoolError: String? = null,
    val courseError: String? = null,
    val deadlineError: String? = null,
    val formError: String? = null,
    val isEdit: Boolean = false,
    val saved: Boolean = false
)

class ApplicationFormViewModel(
    handle: SavedStateHandle,
    private val repo: ScholarshipRepository
) : ViewModel() {

    private val applicationId: Int = handle["applicationId"] ?: -1
    private val scholarshipId: Int = handle["scholarshipId"] ?: -1

    private val _state = MutableStateFlow(FormState(isEdit = applicationId != -1))
    val state: StateFlow<FormState> = _state

    init {
        viewModelScope.launch {
            val user = repo.getCurrentUser()
            val defaultSchool = user?.school ?: ""
            val defaultCourse = user?.course ?: ""

            if (applicationId != -1) {
                repo.getApplication(applicationId)?.let { a ->
                    _state.value = FormState(
                        title = a.title,
                        provider = a.provider,
                        category = a.category,
                        school = a.school.ifBlank { defaultSchool },
                        course = a.course.ifBlank { defaultCourse },
                        requirements = a.requirements,
                        deadline = a.deadline,
                        status = runCatching { ApplicationStatus.valueOf(a.status) }
                            .getOrDefault(ApplicationStatus.NOT_STARTED),
                        notes = a.notes,
                        isEdit = true
                    )
                }
            } else if (scholarshipId != -1) {
                repo.getScholarship(scholarshipId)?.let { s ->
                    val defaultReqs = if (s.category.equals("Attachment", true) || s.category.equals("Internship", true)) {
                        "School Introduction Letter, CV, Academic Transcripts, National ID, Insurance Cover"
                    } else {
                        "Academic Transcripts, National ID / Birth Certificate, Admission Letter, Recommendation Letter"
                    }
                    _state.update {
                        it.copy(
                            title = s.name,
                            provider = s.provider,
                            category = s.category,
                            school = defaultSchool,
                            course = defaultCourse,
                            requirements = defaultReqs,
                            deadline = s.deadline
                        )
                    }
                }
            } else {
                _state.update {
                    it.copy(
                        school = defaultSchool,
                        course = defaultCourse,
                        requirements = "CV, Academic Transcripts, National ID, Recommendation / Intro Letter"
                    )
                }
            }
        }
    }

    fun onTitleChange(v: String) = _state.update { it.copy(title = v, titleError = null, formError = null) }
    fun onProviderChange(v: String) = _state.update { it.copy(provider = v, providerError = null) }
    fun onCategoryChange(v: String) = _state.update { it.copy(category = v) }
    fun onSchoolChange(v: String) = _state.update { it.copy(school = v, schoolError = null) }
    fun onCourseChange(v: String) = _state.update { it.copy(course = v, courseError = null) }
    fun onRequirementsChange(v: String) = _state.update { it.copy(requirements = v) }
    fun onDeadlineChange(v: String) = _state.update { it.copy(deadline = v, deadlineError = null) }
    fun onStatusChange(v: ApplicationStatus) = _state.update { it.copy(status = v) }
    fun onNotesChange(v: String) = _state.update { it.copy(notes = v) }

    fun save() {
        val s = _state.value
        val date = parseDate(s.deadline)

        val titleError = if (s.title.isBlank()) "Title is required" else null
        val providerError = if (s.provider.isBlank()) "Organization / Provider is required" else null
        val schoolError = if (s.school.isBlank()) "School / University is required" else null
        val courseError = if (s.course.isBlank()) "Course / Program is required" else null
        val deadlineError = when {
            date == null -> "Use format yyyy-MM-dd, e.g. 2027-03-31"
            !s.isEdit && date.isBefore(LocalDate.now()) -> "Deadline is in the past"
            else -> null
        }

        if (titleError != null || providerError != null || schoolError != null || courseError != null || deadlineError != null) {
            _state.update {
                it.copy(
                    titleError = titleError,
                    providerError = providerError,
                    schoolError = schoolError,
                    courseError = courseError,
                    deadlineError = deadlineError
                )
            }
            return
        }

        viewModelScope.launch {
            val entity = ApplicationEntity(
                id = if (s.isEdit) applicationId else 0,
                title = s.title.trim(),
                provider = s.provider.trim(),
                category = s.category,
                school = s.school.trim(),
                course = s.course.trim(),
                requirements = s.requirements.trim(),
                deadline = s.deadline.trim(),
                status = s.status.name,
                notes = s.notes.trim(),
                scholarshipId = if (scholarshipId != -1) scholarshipId else null
            )
            val ok = if (s.isEdit) repo.updateApplication(entity) else repo.addApplication(entity)
            _state.update {
                if (ok) it.copy(saved = true)
                else it.copy(formError = "You are already tracking an application with this title")
            }
        }
    }
}
