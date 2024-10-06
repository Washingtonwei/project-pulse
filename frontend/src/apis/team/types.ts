import type { Activity } from '../activity/types'
import type { Student } from '../student/types'

export interface Team {
  teamId?: number
  teamName: string
  description: string
  teamWebsiteUrl: string
  sectionId: number
  sectionName?: string
  students?: Student[] // For team assignment use only
  activitiesInAWeek?: Activity[]
  studentsMissingActivity?: Student[]
}

export interface TeamSearchCriteria {
  teamId?: number
  teamName?: string
  sectionId?: number
  sectionName?: string
}

export interface PaginationParams {
  page: number
  size: number
}

export interface SearchTeamByCriteriaResponse {
  flag: boolean
  code: number
  message: string
  data: {
    content: Team[]
    pageable: {
      sort: {
        sorted: boolean
        unsorted: boolean
        empty: boolean
      }
      pageNumber: number
      pageSize: number
      offset: number
      unpaged: boolean
      paged: boolean
    }
    totalElements: number
    totalPages: number
    last: boolean
    size: number
    number: number
    sort: {
      sorted: boolean
      unsorted: boolean
      empty: boolean
    }
    numberOfElements: number
    first: boolean
    empty: boolean
  }
}

export interface FindTeamByIdResponse {
  flag: boolean
  code: number
  message: string
  data: Team
}

export interface CreateTeamResponse {
  flag: boolean
  code: number
  message: string
  data: Team
}

export interface UpdateTeamResponse {
  flag: boolean
  code: number
  message: string
  data: Team
}

export interface AssignStudentToTeamResponse {
  flag: boolean
  code: number
  message: string
}

export interface RemoveStudentFromTeamResponse {
  flag: boolean
  code: number
  message: string
}

export interface AssignInstructorToTeamResponse {
  flag: boolean
  code: number
  message: string
}
