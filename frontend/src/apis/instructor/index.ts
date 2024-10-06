import request from '@/utils/request'
import type {
  CreateInstructorResponse,
  DeleteInstructorResponse,
  FetchInstructorsResponse,
  FindInstructorByIdResponse,
  UpdateInstructorResponse,
  Instructor,
  SetDefaultSectionResponse,
  SetDefaultCourseResponse
} from './types'
import type { RegistrationParams } from '../student/types'

enum API {
  INSTRUCTORS_ENDPOINT = '/instructors'
}

export const fetchInstructors = () =>
  request.get<any, FetchInstructorsResponse>(API.INSTRUCTORS_ENDPOINT)

export const findInstructorById = (id: number) =>
  request.get<any, FindInstructorByIdResponse>(`${API.INSTRUCTORS_ENDPOINT}/${id}`)

export const createInstructor = (newInstructor: Instructor, params: RegistrationParams) =>
  request.post<any, CreateInstructorResponse>(API.INSTRUCTORS_ENDPOINT, newInstructor, { params })

export const updateInstructor = (updatedInstructor: Instructor) =>
  request.put<any, UpdateInstructorResponse>(
    `${API.INSTRUCTORS_ENDPOINT}/${updatedInstructor.id}`,
    updatedInstructor
  )

export const deleteInstructor = (id: number) =>
  request.delete<any, DeleteInstructorResponse>(`${API.INSTRUCTORS_ENDPOINT}/${id}`)

export const setDefaultSection = (sectionId: number) =>
  request.put<any, SetDefaultSectionResponse>(
    `${API.INSTRUCTORS_ENDPOINT}/sections/${sectionId}/default`
  )

export const setDefaultCourse = (courseId: number) =>
  request.put<any, SetDefaultCourseResponse>(
    `${API.INSTRUCTORS_ENDPOINT}/courses/${courseId}/default`
  )
