export interface CheckEmailExistsResponse {
  flag: boolean
  code: number
  message: string
  data: boolean // true: exists, false: does not exist
}
