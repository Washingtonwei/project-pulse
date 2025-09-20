import request from '@/utils/request'
import type { CheckEmailExistsResponse } from './types'

enum API {
  USERS_ENDPOINT = '/users'
}

export const checkEmailExists = (email: string) => {
  return request.get<any, CheckEmailExistsResponse>(`${API.USERS_ENDPOINT}/exists/${email}`)
}
