export interface Instructor {
  id?: number
  username: string
  password?: string
  enabled?: boolean
  roles?: string
  firstName: string
  lastName: string
  email: string
  defaultSectionId?: number
  defaultCourseId?: number
}

export interface FetchInstructorsResponse {
  flag: boolean
  code: number
  message: string
  data: Instructor[]
}

export interface FindInstructorByIdResponse {
  flag: boolean
  code: number
  message: string
  data: Instructor
}

export interface CreateInstructorResponse {
  flag: boolean
  code: number
  message: string
  data: Instructor
}

export interface UpdateInstructorResponse {
  flag: boolean
  code: number
  message: string
  data: Instructor
}

export interface DeleteInstructorResponse {
  flag: boolean
  code: number
  message: string
  data: null
}

export interface SetDefaultSectionResponse {
  flag: boolean
  code: number
  message: string
}

export interface SetDefaultCourseResponse {
  flag: boolean
  code: number
  message: string
}
