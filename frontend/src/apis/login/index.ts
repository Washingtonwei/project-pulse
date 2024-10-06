import request from '@/utils/request'
import type {
  LoginData,
  LoginResponse,
  ResetUserPasswordResponse,
  SendResetPasswordEmailResponse
} from './types'

enum API {
  LOGIN_ENDPOINT = '/users/login',
  USERS_ENDPOINT = '/users',
  // Forget password endpoint
  FORGET_PASSWORD_ENDPOINT = '/users/forget-password'
}

export const loginUser = (loginData: LoginData) =>
  // We override the default AxiosResponse<T> with just LoginResponse, since our response interceptor transforms the Axios response to directly return the data.
  request.post<any, LoginResponse>(
    API.LOGIN_ENDPOINT,
    {},
    {
      auth: loginData
    }
  )

export const sendResetPasswordLink = (email: string) =>
  request.post<any, SendResetPasswordEmailResponse>(`${API.FORGET_PASSWORD_ENDPOINT}/${email}`)

export const resetUserPassword = (email: string, token: string, newPassword: string) =>
  request.patch<any, ResetUserPasswordResponse>(`${API.USERS_ENDPOINT}/reset-password`, {
    email,
    token,
    newPassword
  })
