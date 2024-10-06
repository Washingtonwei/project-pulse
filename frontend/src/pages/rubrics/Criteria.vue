<template>
  <el-card class="page-container">
    <template #header>
      <div class="header">
        <span>Criterion Management</span>
        <div class="extra">
          <el-button type="primary" @click="showAddDialog()" icon="Plus">
            Add new criterion
          </el-button>
        </div>
      </div>
    </template>
    <!-- Criterion Table -->
    <el-table :data="criteria" style="width: 100%" stripe border v-loading="loading">
      <el-table-column label="Id" prop="criterionId"></el-table-column>
      <el-table-column label="Name" prop="criterion"></el-table-column>
      <el-table-column label="Description" prop="description"></el-table-column>
      <el-table-column label="Max Score" prop="maxScore"></el-table-column>
      <el-table-column label="Operations" width="120">
        <template #default="{ row }">
          <el-button
            icon="Edit"
            circle
            plain
            type="primary"
            @click="showEditDialog(row)"
          ></el-button>
        </template>
      </el-table-column>
      <template #empty>
        <el-empty description="No data is available." />
      </template>
    </el-table>
    <!-- Dialog for adding/editing criterion -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="30%">
      <el-form
        ref="criterionForm"
        :model="criterionData"
        :rules="rules"
        label-width="110px"
        style="padding-right: 30px"
        label-position="left"
      >
        <el-form-item label="Id:" v-if="dialogTitle == 'Edit a criterion'">
          <el-input v-model="criterionData.criterionId" disabled></el-input>
        </el-form-item>
        <el-form-item label="Name:" prop="criterion">
          <el-input
            v-model="criterionData.criterion"
            placeholder="Please provide the name of the criterion."
          ></el-input>
        </el-form-item>
        <el-form-item label="Description:" prop="description">
          <el-input
            v-model="criterionData.description"
            type="textarea"
            :rows="3"
            placeholder="Please provide a description of the criterion."
          ></el-input>
        </el-form-item>
        <el-form-item label="Max Score:" prop="maxScore">
          <el-input v-model="criterionData.maxScore" type="number" min="0"></el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false"> Cancel </el-button>
          <el-button
            type="primary"
            @click="dialogTitle == 'Add a criterion' ? addCriterion() : updateExistingCriterion()"
            :loading="buttonLoading"
          >
            Confirm
          </el-button>
        </span>
      </template>
    </el-dialog>
  </el-card>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { searchEvaluationCriteria, createCriterion, updateCriterion } from '@/apis/rubric'
import type {
  SearchEvaluationCriteriaByCriteriaResponse,
  Criterion,
  EvaluationCriterionSearchCriteria
} from '@/apis/rubric/types'
import type { FormInstance } from 'element-plus'
import { ElMessage, ElMessageBox } from 'element-plus'

const searchCriteria = ref<EvaluationCriterionSearchCriteria>({
  criterion: ''
})

const criteria = ref<Criterion[]>([])
const loading = ref<boolean>(false)
const buttonLoading = ref<boolean>(false)
const criterionForm = ref<FormInstance>()

// Load data when the component is mounted
onMounted(() => {
  loadCriteria()
})

async function loadCriteria() {
  loading.value = true
  const result: SearchEvaluationCriteriaByCriteriaResponse = await searchEvaluationCriteria(
    searchCriteria.value
  )
  criteria.value = result.data.content
  loading.value = false
}

// Control the visibility of the dialog
const dialogVisible = ref(false)

const criterionData = ref<Criterion>({
  // Data of the criterion being added or edited
  criterionId: NaN,
  criterion: '',
  description: '',
  maxScore: NaN
})

// Validation rules
const rules = {
  criterion: [
    { required: true, message: 'Please provide the name of the criterion.', trigger: 'blur' }
  ],
  description: [
    { required: true, message: 'Please provide a description of the criterion.', trigger: 'blur' }
  ],
  maxScore: [
    {
      required: true,
      message: 'Please provide the maximum score of the criterion.',
      trigger: 'blur'
    }
  ]
}

function clearForm() {
  criterionData.value = {
    criterionId: NaN,
    criterion: '',
    description: '',
    maxScore: NaN
  }
}

const dialogTitle = ref<string>('')

function showAddDialog() {
  clearForm()
  criterionForm.value?.clearValidate() // Clear the validation status of the form. The first time the dialog is opened, the form is not defined, so we need to check if it is defined before calling clearValidate()
  dialogTitle.value = 'Add a criterion'
  dialogVisible.value = true
}

async function addCriterion() {
  await criterionForm.value!.validate() // Validate the form
  buttonLoading.value = true
  const newCriterion: Criterion = {
    criterionId: NaN,
    criterion: criterionData.value.criterion,
    description: criterionData.value.description,
    maxScore: criterionData.value.maxScore
  }

  await createCriterion(newCriterion)
  ElMessage.success('Criterion added successfully')
  buttonLoading.value = false
  // Close the dialog
  dialogVisible.value = false

  // Reload the criterion table
  loadCriteria()
}

function showEditDialog(existingCriterion: Criterion) {
  clearForm()
  criterionForm.value?.clearValidate()
  dialogVisible.value = true
  dialogTitle.value = 'Edit a criterion'

  // Set the data of the criterion being edited
  criterionData.value = {
    criterionId: existingCriterion.criterionId,
    criterion: existingCriterion.criterion,
    description: existingCriterion.description,
    maxScore: existingCriterion.maxScore
  }
}

async function updateExistingCriterion() {
  await criterionForm.value!.validate() // Validate the form
  buttonLoading.value = true
  const updatedCriterion: Criterion = {
    criterionId: criterionData.value.criterionId,
    criterion: criterionData.value.criterion,
    description: criterionData.value.description,
    maxScore: criterionData.value.maxScore
  }

  // Call the API to update the criterion
  await updateCriterion(updatedCriterion)
  ElMessage.success('Criterion updated successfully')
  buttonLoading.value = false
  // Close the dialog
  dialogVisible.value = false

  // Reload the criterion table
  loadCriteria()
}

// async function deleteExistingCriterion(existingCriterion: Criterion) {
//   ElMessageBox.confirm(
//     `${existingCriterion.name} will be permanently deleted. Continue?`,
//     'Warning',
//     {
//       confirmButtonText: 'OK',
//       cancelButtonText: 'Cancel',
//       type: 'warning'
//     }
//   )
//     .then(() => deleteCriterion(existingCriterion.id as number))
//     .then(() => {
//       ElMessage.success('Criterion deleted successfully')
//       loadCriteria()
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
