<template>
  <el-card class="page-container">
    <template #header>
      <div class="header">
        <span>Rubric Management</span>
        <div class="extra">
          <el-button type="primary" @click="showAddDialog()" icon="Plus">
            Add new rubric
          </el-button>
        </div>
      </div>
    </template>
    <!-- Rubric Table -->
    <el-table :data="rubrics" style="width: 100%" stripe border v-loading="loading">
      <el-table-column label="Id" prop="rubricId"></el-table-column>
      <el-table-column label="Name" prop="rubricName"></el-table-column>
      <el-table-column label="Operations" width="120">
        <template #default="{ row }">
          <el-button
            icon="Edit"
            circle
            plain
            type="primary"
            @click="showEditDialog(row)"
          ></el-button>
          <el-button
            icon="DocumentChecked"
            circle
            plain
            type="primary"
            @click="showAssignCriteriaDialog(row)"
          ></el-button>
        </template>
      </el-table-column>
      <template #empty>
        <el-empty description="No data is available." />
      </template>
    </el-table>
    <!-- Dialog for adding/editing rubric -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="30%">
      <el-form
        ref="rubricForm"
        :model="rubricData"
        :rules="rules"
        label-width="auto"
        style="padding-right: 30px"
        label-position="right"
      >
        <el-form-item label="Id:" v-if="dialogTitle == 'Edit a rubric'">
          <el-input v-model="rubricData.rubricId" disabled></el-input>
        </el-form-item>
        <el-form-item label="Name:" prop="rubricName">
          <el-input
            v-model="rubricData.rubricName"
            placeholder="Please provide the name of the rubric."
          ></el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false"> Cancel </el-button>
          <el-button
            type="primary"
            @click="dialogTitle == 'Add a rubric' ? addRubric() : updateExistingRubric()"
            :loading="buttonLoading"
          >
            Confirm
          </el-button>
        </span>
      </template>
    </el-dialog>
    <!-- Dialog for assigning criteria to rubric -->
    <el-dialog v-model="criteriaDialogVisible" title="Assign Criteria to Rubric" width="30%">
      <el-form
        :model="rubricData"
        label-width="110px"
        style="padding-right: 30px"
        label-position="left"
      >
        <el-form-item label="Id:">
          <el-input v-model="rubricData.rubricId" disabled></el-input>
        </el-form-item>
        <el-form-item label="Name:" prop="rubricName">
          <el-input v-model="rubricData.rubricName" disabled></el-input>
        </el-form-item>
        <!-- Use a checkbox to select the rubric's criteria -->
        <el-form-item label="Criteria:">
          <el-checkbox
            v-model="checkAll"
            :indeterminate="isIndeterminate"
            @change="handleCheckAllChange"
          >
            Check all
          </el-checkbox>
          <el-checkbox-group v-model="checkedCriterionIds" @change="handleCheckedCriteriaChange">
            <el-checkbox
              v-for="criterion in criteria"
              :key="criterion.criterionId"
              :label="criterion.criterion"
              :value="criterion.criterionId"
              @change="handleCheckedCriterionChange(criterion.criterionId as number)"
            >
            </el-checkbox>
          </el-checkbox-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="criteriaDialogVisible = false"> Cancel </el-button>
        </span>
      </template>
    </el-dialog>
  </el-card>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import {
  searchRubrics,
  createRubric,
  updateRubric,
  searchEvaluationCriteria,
  assignCriterionToRubric,
  unassignCriterionFromRubric
} from '@/apis/rubric'
import type {
  Criterion,
  Rubric,
  RubricSearchCriteria,
  SearchEvaluationCriteriaByCriteriaResponse,
  SearchRubricByCriteriaResponse
} from '@/apis/rubric/types'
import type { FormInstance } from 'element-plus'
import { ElMessage, ElMessageBox } from 'element-plus'

const searchCriteria = ref<RubricSearchCriteria>({
  rubricName: ''
})

const rubrics = ref<Rubric[]>([])
const rubricForm = ref<FormInstance>()
const criteria = ref<Criterion[]>([])
const loading = ref<boolean>(true)
const buttonLoading = ref<boolean>(false)

// Load data when the component is mounted
onMounted(() => {
  loadRubrics()
})

async function loadRubrics() {
  loading.value = true
  const result: SearchRubricByCriteriaResponse = await searchRubrics(searchCriteria.value)
  rubrics.value = result.data.content
  loading.value = false
}

async function loadCriteria() {
  const result: SearchEvaluationCriteriaByCriteriaResponse = await searchEvaluationCriteria({})
  criteria.value = result.data.content
}

// Control the visibility of the dialog
const dialogVisible = ref(false)

const rubricData = ref<Rubric>({
  // Data of the rubric being added or edited
  rubricId: NaN,
  rubricName: ''
})

// Validation rules
const rules = {
  rubricName: [
    { required: true, message: 'Please provide the name of the rubric.', trigger: 'blur' }
  ]
}

function clearForm() {
  rubricData.value = {
    rubricId: NaN,
    rubricName: ''
  }
}

const dialogTitle = ref<string>('')

function showAddDialog() {
  clearForm()
  rubricForm.value?.clearValidate() // Clear the validation status of the form. The first time the dialog is opened, the form is not defined, so we need to check if it is defined before calling clearValidate()
  dialogTitle.value = 'Add a rubric'
  dialogVisible.value = true
}

async function addRubric() {
  await rubricForm.value!.validate() // Validate the form
  buttonLoading.value = true
  const newRubric: Rubric = {
    rubricId: NaN,
    rubricName: rubricData.value.rubricName
  }

  await createRubric(newRubric)
  ElMessage.success('Rubric added successfully')
  buttonLoading.value = false
  // Close the dialog
  dialogVisible.value = false

  // Reload the rubric table
  loadRubrics()
}

const checkedCriterionIds = ref<number[]>([])

function showEditDialog(existingRubric: Rubric) {
  clearForm()
  rubricForm.value?.clearValidate()
  dialogVisible.value = true
  dialogTitle.value = 'Edit a rubric'

  // Set the data of the rubric being edited
  rubricData.value = {
    rubricId: existingRubric.rubricId,
    rubricName: existingRubric.rubricName
  }
}

async function updateExistingRubric() {
  await rubricForm.value!.validate() // Validate the form
  buttonLoading.value = true
  const updatedRubric: Rubric = {
    rubricId: rubricData.value.rubricId,
    rubricName: rubricData.value.rubricName
  }

  // Call the API to update the rubric
  await updateRubric(updatedRubric)
  ElMessage.success('Rubric updated successfully')
  buttonLoading.value = false
  // Close the dialog
  dialogVisible.value = false

  // Reload the rubric table
  loadRubrics()
}

const criteriaDialogVisible = ref(false)

function showAssignCriteriaDialog(existingRubric: Rubric) {
  clearForm()
  rubricForm.value?.clearValidate()
  criteriaDialogVisible.value = true
  dialogTitle.value = 'Assign Criteria to Rubric'

  // Set the data of the rubric being edited
  rubricData.value = {
    rubricId: existingRubric.rubricId,
    rubricName: existingRubric.rubricName
  }
  loadCriteria()
  checkedCriterionIds.value =
    existingRubric.criteria?.map((criterion) => criterion.criterionId as number) || []
}

const checkAll = ref(false)
const isIndeterminate = ref(true)

const handleCheckAllChange = async (val: boolean) => {
  checkedCriterionIds.value = val
    ? criteria.value.map((criterion) => criterion.criterionId as number)
    : []
  isIndeterminate.value = false

  // Assign or unassign all criteria to the rubric
  const rubricId = rubricData.value.rubricId
  if (val) {
    // Use Promise.all to wait for all promises to resolve
    const submitPromises = criteria.value.map(async (criterion) => {
      await assignCriterionToRubric(rubricId!, criterion.criterionId!)
    })
    await Promise.all(submitPromises)
    ElMessage.success('All criteria assigned successfully')
  } else {
    // Use Promise.all to wait for all promises to resolve
    const submitPromises = criteria.value.map(async (criterion) => {
      await unassignCriterionFromRubric(rubricId!, criterion.criterionId!)
    })
    await Promise.all(submitPromises)
    ElMessage.success('All criteria unassigned successfully')
  }
  loadRubrics()
}

const handleCheckedCriteriaChange = (value: string[]) => {
  const checkedCount = value.length
  checkAll.value = checkedCount === criteria.value.length
  isIndeterminate.value = checkedCount > 0 && checkedCount < criteria.value.length
}

const handleCheckedCriterionChange = async (criterionId: number) => {
  const rubricId = rubricData.value.rubricId
  const isChecked = checkedCriterionIds.value.includes(criterionId)
  if (isChecked) {
    await assignCriterionToRubric(rubricId!, criterionId)
    ElMessage.success('Criterion assigned successfully')
  } else {
    await unassignCriterionFromRubric(rubricId!, criterionId)
    ElMessage.success('Criterion unassigned successfully')
  }
  loadRubrics()
}

// async function deleteExistingRubric(existingRubric: Rubric) {
//   ElMessageBox.confirm(
//     `${existingRubric.name} will be permanently deleted. Continue?`,
//     'Warning',
//     {
//       confirmButtonText: 'OK',
//       cancelButtonText: 'Cancel',
//       type: 'warning'
//     }
//   )
//     .then(() => deleteRubric(existingRubric.id as number))
//     .then(() => {
//       ElMessage.success('Rubric deleted successfully')
//       loadRubrics()
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
</style>
