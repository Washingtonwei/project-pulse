<template>
  <el-row>
    <el-col :span="18">
      <el-card class="page-container">
        <template #header>
          <div class="header">
            <span>Team Management</span>
            <div class="extra">
              <el-button type="primary" @click="showAddDialog()" icon="Plus">
                Add new team
              </el-button>
            </div>
          </div>
        </template>
        <!-- Search Form -->
        <el-form inline>
          <el-form-item label="Team Name:">
            <el-input v-model="teamSearchCriteria.teamName" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="loadTeams">Query</el-button>
            <el-button @click="resetSearchCriteria">Reset</el-button>
          </el-form-item>
        </el-form>
        <!-- Team Table -->
        <el-table
          :data="teams"
          style="width: 100%"
          stripe
          border
          default-expand-all
          v-loading="loading"
        >
          <el-table-column type="expand">
            <template #default="{ row }">
              <el-card :data-team-id="row.teamId">
                <p>Students:</p>
                <draggable
                  v-model="row.students"
                  group="people"
                  @start="drag = true"
                  @end="onDragEnd"
                  item-key="id"
                  class="trello-team-student-list"
                >
                  <template #item="{ element }">
                    <div class="trello-card">
                      <span>{{ element.firstName }} {{ element.lastName }}</span>
                      <el-icon
                        v-if="element.teamId"
                        class="remove-icon"
                        icon="CircleClose"
                        @click.stop="unassignStudent(row.teamId, element.id)"
                      >
                        <CircleClose />
                      </el-icon>
                    </div>
                  </template>
                </draggable>
              </el-card>
            </template>
          </el-table-column>
          <el-table-column label="Id" prop="teamId"></el-table-column>
          <el-table-column label="Name" prop="teamName"> </el-table-column>
          <el-table-column label="Description" prop="description"> </el-table-column>
          <el-table-column label="Website URL" prop="teamWebsiteUrl"> </el-table-column>
          <el-table-column label="Operations" width="150">
            <template #default="{ row }">
              <el-button
                icon="Edit"
                circle
                plain
                type="primary"
                @click="showEditDialog(row)"
              ></el-button>
              <!-- <el-button
                icon="User"
                circle
                plain
                type="primary"
                @click="showAssignStudentsToTeamDialog(row)"
              ></el-button> -->
              <!-- <el-button
            icon="Delete"
            circle
            plain
            type="danger"
            @click="deleteExistingTeam(row)"
          ></el-button> -->
            </template>
          </el-table-column>
          <template #empty>
            <el-empty description="No data is available." />
          </template>
        </el-table>
        <!-- Pagination -->
        <el-pagination
          v-model:current-page="pageNumber"
          v-model:page-size="pageSize"
          :page-sizes="[2, 5, 10]"
          layout="jumper, total, sizes, prev, pager, next"
          background
          :total="totalElements"
          @size-change="handlePageSizeChange"
          @current-change="handlePageNumberChange"
          style="margin-top: 20px; justify-content: flex-end"
        />
        <!-- Dialog for adding/editing team -->
        <el-dialog v-model="dialogVisible" :title="dialogTitle" width="30%">
          <el-form
            ref="teamForm"
            :model="teamData"
            :rules="rules"
            label-width="auto"
            style="padding-right: 30px"
            label-position="right"
          >
            <el-form-item label="Id:" v-if="dialogTitle == 'Edit a team'">
              <el-input v-model="teamData.teamId" disabled></el-input>
            </el-form-item>
            <el-form-item label="Name:" prop="teamName">
              <el-input v-model="teamData.teamName" minlength="1"></el-input>
            </el-form-item>
            <el-form-item label="Description:" prop="description">
              <el-input v-model="teamData.description" minlength="1"></el-input>
            </el-form-item>
            <el-form-item label="Website URL:" prop="teamWebsiteUrl">
              <el-input v-model="teamData.teamWebsiteUrl" minlength="1"></el-input>
            </el-form-item>
          </el-form>
          <template #footer>
            <span class="dialog-footer">
              <el-button @click="dialogVisible = false"> Cancel </el-button>
              <el-button
                type="primary"
                @click="dialogTitle == 'Add a team' ? addTeam() : updateExistingTeam()"
                :loading="buttonLoading"
              >
                Confirm
              </el-button>
            </span>
          </template>
        </el-dialog>
      </el-card>
    </el-col>
    <el-col :span="6">
      <el-affix :offset="60">
        <el-card style="max-height: 600">
          <!-- Unassigned Students -->
          <template #header>
            <div class="header">
              <span>Students without a Team</span>
            </div>
          </template>
          <!-- Search Form -->
          <el-form inline>
            <el-form-item label="Student Name:">
              <el-input v-model="studentNameSearch" />
            </el-form-item>
          </el-form>
          <el-scrollbar height="600px">
            <!-- Unassigned Students List, teamId is null -->
            <div>
              <draggable
                v-model="studentsWithoutTeam"
                group="people"
                @start="drag = true"
                @end="onDragEnd"
                item-key="id"
                class="trello-student-with-no-team-list"
              >
                <template #item="{ element }">
                  <div class="trello-card">{{ element.firstName }} {{ element.lastName }}</div>
                </template>
              </draggable>
            </div>
          </el-scrollbar>
        </el-card>
      </el-affix>
    </el-col>
  </el-row>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import {
  searchTeams,
  createTeam,
  updateTeam,
  assignStudentToTeam,
  removeStudentFromTeam
} from '@/apis/team'
import type { FormInstance } from 'element-plus'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { SearchTeamByCriteriaResponse, Team, TeamSearchCriteria } from '@/apis/team/types'
import { useSettingsStore } from '@/stores/settings'
import type {
  SearchStudentByCriteriaResponse,
  Student,
  StudentSearchCriteria
} from '@/apis/student/types'
import { searchStudents } from '@/apis/student'
import draggable from 'vuedraggable'

const teamSearchCriteria = ref<TeamSearchCriteria>({
  teamName: '',
  sectionId: NaN
})

const studentSearchCriteria = ref<StudentSearchCriteria>({
  sectionId: NaN
})

const studentNameSearch = ref<string>('')

const teams = ref<Team[]>([])
const loading = ref<boolean>(true)
const buttonLoading = ref<boolean>(false)
const settingsStore = useSettingsStore()
const defaultSectionId = ref<number>(settingsStore.defaultSectionId)

// Pagination information
const pageNumber = ref<number>(1) // current page number, starting from 1
const pageSize = ref<number>(10) // number of elements per page
const totalElements = ref<number>(10) // total number of elements

const teamForm = ref<FormInstance>()

const students = ref<Student[]>([]) // All students in the section

// studentsWithoutTeam is a computed property that returns students who are not in any team and whose name (full name) matches the studentNameSearch input by the user
const studentsWithoutTeam = computed(() =>
  students.value.filter(
    (student) =>
      !student.teamId &&
      (student.firstName + ' ' + student.lastName)
        .toLowerCase()
        .includes(studentNameSearch.value.toLowerCase())
  )
)

// Load data when the component is mounted
onMounted(async () => {
  teamSearchCriteria.value.sectionId = defaultSectionId.value
  studentSearchCriteria.value.sectionId = defaultSectionId.value

  if (!teamSearchCriteria.value.sectionId) {
    ElMessage.error('Please select a section in the Sections page.')
    return
  }
  loading.value = true
  await loadTeams()
  await loadStudents()
  loading.value = false
})

// Load teams based on the search criteria
async function loadTeams() {
  // On the back end, the page number starts from 0, so subtract 1 here
  const result: SearchTeamByCriteriaResponse = await searchTeams(
    { page: pageNumber.value - 1, size: pageSize.value },
    teamSearchCriteria.value
  )
  teams.value = result.data.content

  // Update pagination information
  pageNumber.value = result.data.number + 1 // The page number starts from 0 on the back end, so add 1 here
  pageSize.value = result.data.size
  totalElements.value = result.data.totalElements
}

// Load all students (assigned and unassigned) in the section
async function loadStudents() {
  const result: SearchStudentByCriteriaResponse = await searchStudents(
    { page: 0, size: 100 }, // For now, we only need to load the first 100 students
    studentSearchCriteria.value
  )

  students.value = result.data.content

  // Put students in their teams
  teams.value.forEach((team) => {
    team.students = students.value.filter((student) => student.teamId === team.teamId)
  })
}

function resetSearchCriteria() {
  teamSearchCriteria.value = {
    teamName: ''
  }
  loadTeams()
}

function handlePageNumberChange(newPageNumer: number) {
  pageNumber.value = newPageNumer // Not necessary since pageNumber already has a two-way binding
  loadTeams()
}

function handlePageSizeChange(newSize: number) {
  pageSize.value = newSize // Not necessary since pageSize already has a two-way binding
  pageNumber.value = 1 // Reset the page number to 1 when the page size changes
  loadTeams()
}

// Control the visibility of the dialog
const dialogVisible = ref(false)

const teamData = ref<Team>({
  // Data of the team being added or edited
  teamId: NaN,
  teamName: '',
  description: '',
  teamWebsiteUrl: '',
  sectionId: defaultSectionId.value
})

// Validation rules
const rules = {
  teamName: [{ required: true, message: 'Please enter the team name', trigger: 'blur' }],
  description: [{ required: true, message: 'Please enter the team description', trigger: 'blur' }],
  teamWebsiteUrl: [
    { required: true, message: 'Please enter the team website URL', trigger: 'blur' }
  ],
  sectionId: [{ required: true, message: 'Please select the section', trigger: 'change' }]
}

function clearForm() {
  teamData.value = {
    teamId: NaN,
    teamName: '',
    description: '',
    teamWebsiteUrl: '',
    sectionId: defaultSectionId.value
  }
}

const dialogTitle = ref<string>('')

function showAddDialog() {
  clearForm()
  teamForm.value?.clearValidate() // Clear the validation status of the form. The first time the dialog is opened, the form is not defined, so we need to check if it is defined before calling clearValidate()
  dialogTitle.value = 'Add a team'
  dialogVisible.value = true
}

async function addTeam() {
  await teamForm.value!.validate() // Validate the form
  buttonLoading.value = true
  const newTeam: Team = {
    teamName: teamData.value.teamName,
    description: teamData.value.description,
    teamWebsiteUrl: teamData.value.teamWebsiteUrl,
    sectionId: teamData.value.sectionId
  }

  await createTeam(newTeam)
  ElMessage.success('Team added successfully')
  buttonLoading.value = false
  // Close the dialog
  dialogVisible.value = false

  // Reload the team table
  loadTeams()
  loadStudents()
}

function showEditDialog(existingTeam: Team) {
  clearForm()
  teamForm.value?.clearValidate()
  dialogVisible.value = true
  dialogTitle.value = 'Edit a team'

  teamData.value.teamId = existingTeam.teamId as number
  teamData.value.teamName = existingTeam.teamName
  teamData.value.description = existingTeam.description
  teamData.value.teamWebsiteUrl = existingTeam.teamWebsiteUrl
  teamData.value.sectionId = existingTeam.sectionId
}

async function updateExistingTeam() {
  await teamForm.value!.validate() // Validate the form
  buttonLoading.value = true
  const updatedTeam: Team = {
    teamId: teamData.value.teamId as number,
    teamName: teamData.value.teamName,
    description: teamData.value.description,
    teamWebsiteUrl: teamData.value.teamWebsiteUrl,
    sectionId: teamData.value.sectionId
  }

  // Call the API to update the team
  await updateTeam(updatedTeam)
  ElMessage.success('Team updated successfully')
  buttonLoading.value = false
  // Close the dialog
  dialogVisible.value = false

  // Reload the team table
  loadTeams()
}

async function unassignStudent(teamId: number, studentId: number) {
  await removeStudentFromTeam(teamId, studentId)
  ElMessage.success('Student removed from team successfully')
  loadStudents()
}

// Drag and drop behavior, assign a student to a team or unassign a student from a team
const onDragEnd = async (evt: any) => {
  const draggedElement = evt.item._underlying_vm_
  const draggedStudentId = draggedElement.id
  const newTeamId = evt.to.parentElement.parentElement.dataset.teamId
  const oldTeamId = draggedElement.teamId

  // If the student is dragged to a team
  if (newTeamId) {
    // If the student is dragged to a different team
    if (oldTeamId != newTeamId) {
      // If the student is dragged to a team
      await assignStudentToTeam(newTeamId, draggedStudentId)
      ElMessage.success('Student assigned to team successfully')
      loadStudents()
    }
  }
  // If the student is dragged to the students without a team list
  else {
    // If the student is NOT dragged within the students without a team list
    if (oldTeamId) {
      await unassignStudent(oldTeamId, draggedStudentId)
    }
  }
}

// async function deleteExistingTeam(existingTeam: Team) {
//   ElMessageBox.confirm(
//     `${existingTeam.teamName} will be permanently deleted. Continue?`,
//     'Warning',
//     {
//       confirmButtonText: 'OK',
//       cancelButtonText: 'Cancel',
//       type: 'warning'
//     }
//   )
//     .then(() => deleteTeam(existingTeam.teamId as number))
//     .then(() => {
//       ElMessage.success('Team deleted successfully')
//       loadTeams()
//     })
//     .catch(() => {
//       ElMessage.info('Deletion canceled')
//     })
// }
</script>

<style lang="scss" scoped>
.page-container {
  min-height: 100%;
  box-sizing: border-box;

  .header {
    display: flex;
    align-items: center;
    justify-content: space-between;
  }
}
.trello-team-student-list {
  display: flex;
  list-style-type: none;
  padding: 0;
  margin: 0;
}
.trello-student-with-no-team-list {
  list-style-type: none;
  padding: 0;
  margin: 0;
  max-width: 200px;
}

.trello-card {
  display: flex;
  justify-content: space-between;
  background-color: #6b6d71;
  color: white;
  border: 1px solid #dfe1e6;
  border-radius: 6px;
  padding: 10px;
  margin-bottom: 10px;
  box-shadow: 0 1px 4px rgba(9, 30, 66, 0.25);
  cursor: pointer;
  transition: box-shadow 0.3s ease;
}

.trello-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.sortable-ghost {
  opacity: 0.1; /* Hide the ghost element */
}

.remove-icon {
  margin-left: 5px;
  cursor: pointer;
}
// .sortable-chosen {
//   opacity: 0.8; /* Slight transparency to indicate it's being dragged */
//   transform: scale(1.05); /* Slightly enlarge the original item while dragging */
// }

// .sortable-fallback {
//   opacity: 0.8;
//   transform: scale(1.05);
//   cursor: grabbing;
// }
</style>
