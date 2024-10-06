<template>
  <div>
    <el-card class="page-container">
      <template #header>
        <div class="header">
          <span>Home</span>
        </div>
      </template>
      <div class="content">
        <p>Welcome to Project Pulse!</p>
        <p>As a student, you are required to submit weekly activities and peer evaluations.</p>
        <el-space wrap alignment="flex-start">
          <el-card style="max-width: 550px">
            <template #header>
              <div class="card-header">
                <span>Weekly Activity Reports</span>
              </div>
            </template>
            <div>
              Click "My Activities" to add your weekly activities.
              <ul>
                <li>You can add several activities for the week.</li>
                <li>Be specific about the activity name and description.</li>
                <li>
                  Remember to update the actual hours spent on one activity and the activity's
                  status.
                </li>
              </ul>
            </div>
            <div>
              Click "Team's Activities" to view your teammates' activities.
              <ul>
                <li>
                  You can comment on your teammates' activities by clicking the
                  <el-icon color="#66b1ff"><Comment /></el-icon> icon.
                </li>
                <li>
                  Being familiar with your team's activities is important for peer evaluations.
                </li>
              </ul>
            </div>
          </el-card>

          <el-card style="max-width: 550px">
            <template #header>
              <div class="card-header">
                <span>Peer Evaluations</span>
              </div>
            </template>
            <div>
              Click "Submit Evaluations" to evaluate your teammates.
              <ul>
                <li>Evaluate your teammates based on the provided criteria.</li>
                <li>Offer constructive feedback through public comments.</li>
                <li>Include a self-assessment as part of your evaluation.</li>
                <li>
                  Peer evaluations can only be submitted for the previous week. Missed evaluations
                  cannot be made up.
                </li>
                <li>Private comments are visible only to the instructor.</li>
              </ul>
            </div>
            <div>
              Click "My Evaluations" to view how your teammates evaluated you and your scores.
              <ul>
                <li>
                  The "Average Total Score" is used to calucated your final score of this course.
                </li>
              </ul>
            </div>
          </el-card>
        </el-space>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts"></script>

<style lang="scss" scoped>
.content {
  padding: 20px;
  font-size: larger;
}
</style>
