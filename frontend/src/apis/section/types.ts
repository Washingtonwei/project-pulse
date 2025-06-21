export interface Section {
  sectionId?: number
  sectionName: string
  startDate: string
  endDate: string
  rubricId?: number
  rubricName?: string
  activeWeeks?: string[]
  courseId?: number
  isActive: boolean
  warWeeklyDueDay: DayOfWeek
  warDueTime: string // "HH:mm"
  peerEvaluationWeeklyDueDay: DayOfWeek
  peerEvaluationDueTime: string // "HH:mm"
}

export type DayOfWeek =
  | 'MONDAY'
  | 'TUESDAY'
  | 'WEDNESDAY'
  | 'THURSDAY'
  | 'FRIDAY'
  | 'SATURDAY'
  | 'SUNDAY'

export interface SectionSearchCriteria {
  sectionId?: number
  sectionName?: string
}

export interface PaginationParams {
  page: number
  size: number
}

export interface SearchSectionByCriteriaResponse {
  flag: boolean
  code: number
  message: string
  data: {
    content: Section[]
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

export interface FindSectionByIdResponse {
  flag: boolean
  code: number
  message: string
  data: Section
}

export interface CreateSectionResponse {
  flag: boolean
  code: number
  message: string
  data: Section
}

export interface UpdateSectionResponse {
  flag: boolean
  code: number
  message: string
  data: Section
}

export interface AssignRubricToSectionResponse {
  flag: boolean
  code: number
  message: string
}

export type WeekInfo = {
  weekNumber: string // e.g., "2023-W31"
  monday: string // e.g., "07-31-2023"
  sunday: string // e.g., "08-06-2023"
  isActive: boolean // true if the week is active, false otherwise
}

export interface SetUpActiveWeeksResponse {
  flag: boolean
  code: number
  message: string
}

export interface SendEmailInvitationsToStudentsResponse {
  flag: boolean
  code: number
  message: string
}
