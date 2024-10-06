export interface Student {
  id?: number // Optional field, for example, when creating a new student, the ID should not be provided
  username: string
  password?: string
  enabled?: boolean
  roles?: string
  firstName: string
  lastName: string
  email: string
  sectionId?: number
  sectionName?: string
  teamId?: number
  teamName?: string
}

export interface PaginationParams {
  page: number
  size: number
}

export interface StudentSearchCriteria {
  firstName?: string
  lastName?: string
  email?: string
  sectionId?: number
  sectionName?: string
  teamId?: number
}

export interface SearchStudentByCriteriaResponse {
  flag: boolean
  code: number
  message: string
  data: {
    content: Student[]
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

export interface FindStudentByIdResponse {
  flag: boolean
  code: number
  message: string
  data: Student
}

export interface FindStudentByTeamIdResponse {
  flag: boolean
  code: number
  message: string
  data: Student[]
}

export interface CreateStudentResponse {
  flag: boolean
  code: number
  message: string
  data: Student
}

export interface RegistrationParams {
  courseId: number
  sectionId?: number
  registrationToken: string
  role: string
}

export interface UpdateStudentResponse {
  flag: boolean
  code: number
  message: string
  data: Student
}

export interface DeleteStudentResponse {
  flag: boolean
  code: number
  message: string
  data: null
}
