export interface Course {
  courseId?: number
  courseName: string
  courseDescription: string
}

export interface CourseSearchCriteria {
  courseName?: string
  courseDescription?: string
}

export interface PaginationParams {
  page: number
  size: number
}

export interface SearchCourseByCriteriaResponse {
  flag: boolean
  code: number
  message: string
  data: {
    content: Course[]
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

export interface FindCourseByIdResponse {
  flag: boolean
  code: number
  message: string
  data: Course
}

export interface CreateCourseResponse {
  flag: boolean
  code: number
  message: string
  data: Course
}

export interface UpdateCourseResponse {
  flag: boolean
  code: number
  message: string
  data: Course
}

export interface SetDefaultCourseResponse {
  flag: boolean
  code: number
  message: string
}

export interface SendEmailInvitationsToInstructorsResponse {
  flag: boolean
  code: number
  message: string
}
