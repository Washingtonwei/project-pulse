import type { Instructor } from '../instructor/types'
import type { Student } from '../student/types'

export interface LoginData {
  username: string
  password: string
}

export interface LoginResponse {
  flag: boolean
  code: number
  message: string
  data: {
    userInfo: Student | Instructor
    token: string
  }
}

export interface SendResetPasswordEmailResponse {
  flag: boolean
  code: number
  message: string
}

export interface ResetUserPasswordResponse {
  flag: boolean
  code: number
  message: string
}
