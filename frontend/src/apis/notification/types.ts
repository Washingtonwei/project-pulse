// The two weekly items a student owes (BR-submission-owed)
export type SubmittableItem = 'WEEKLY_ACTIVITY_REPORT' | 'PEER_EVALUATION'

export interface OwingStudent {
  id: number
  firstName: string
  lastName: string
}

export interface OwingTeam {
  teamId: number
  teamName: string
  students: OwingStudent[]
}

export interface SubmissionStatus {
  item: SubmittableItem
  week: string // e.g., "2023-W32"
  weekRange: string // e.g., "08-07-2023 to 08-13-2023"
  weekActive: boolean
  dueDay: string | null // e.g., "MONDAY"
  dueTime: string | null // e.g., "23:59"
  teams: OwingTeam[] // Only teams with at least one student who owes the item
}

export interface GetSubmissionStatusResponse {
  flag: boolean
  code: number
  message: string
  data: SubmissionStatus
}

export interface ReminderResult {
  item: SubmittableItem
  week: string
  sent: number
  failed: OwingStudent[]
}

export interface SendRemindersResponse {
  flag: boolean
  code: number
  message: string
  data: ReminderResult
}
