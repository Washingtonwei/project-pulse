import request from '@/utils/request'

import type {
  PaginationParams,
  Course,
  CourseSearchCriteria,
  CreateCourseResponse,
  FindCourseByIdResponse,
  SearchCourseByCriteriaResponse,
  UpdateCourseResponse,
  SetDefaultCourseResponse,
  SendEmailInvitationsToInstructorsResponse
} from './types'

enum API {
  SEARCH_COURSES_ENDPOINT = '/courses/search',
  COURSES_ENDPOINT = '/courses'
}

export const searchCourses = (params: PaginationParams, searchCriteria: CourseSearchCriteria) =>
  request.post<any, SearchCourseByCriteriaResponse>(API.SEARCH_COURSES_ENDPOINT, searchCriteria, {
    params
  })

export const findCourseById = (id: number) =>
  request.get<any, FindCourseByIdResponse>(`${API.COURSES_ENDPOINT}/${id}`)

export const createCourse = (newCourse: Course) =>
  request.post<any, CreateCourseResponse>(API.COURSES_ENDPOINT, newCourse)

export const updateCourse = (updatedCourse: Course) =>
  request.put<any, UpdateCourseResponse>(
    `${API.COURSES_ENDPOINT}/${updatedCourse.courseId}`,
    updatedCourse
  )

export const findDefaultCourse = () =>
  request.get<any, FindCourseByIdResponse>(`${API.COURSES_ENDPOINT}/default`)

export const setDefaultCourse = (courseId: number) =>
  request.put<any, SetDefaultCourseResponse>(`${API.COURSES_ENDPOINT}/${courseId}/default`)

export const sendEmailInvitationsToInstructors = (courseId: number, emails: string[]) =>
  request.post<any, SendEmailInvitationsToInstructorsResponse>(
    `${API.COURSES_ENDPOINT}/${courseId}/instructors/email-invitations`,
    emails
  )
