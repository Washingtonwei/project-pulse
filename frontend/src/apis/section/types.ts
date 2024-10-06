export interface Section {
  sectionId?: number
  sectionName: string
  startDate: string
  endDate: string
  rubricId?: number
  rubricName?: string
  activeWeeks?: string[]
  courseId?: number
}

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
  weekNumber: string
  monday: string
  sunday: string
  isActive: boolean
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
