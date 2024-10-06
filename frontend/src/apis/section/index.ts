import request from '@/utils/request'

import type {
  PaginationParams,
  Section,
  SectionSearchCriteria,
  CreateSectionResponse,
  FindSectionByIdResponse,
  SearchSectionByCriteriaResponse,
  UpdateSectionResponse,
  AssignRubricToSectionResponse,
  SetUpActiveWeeksResponse,
  SendEmailInvitationsToStudentsResponse
} from './types'

enum API {
  SEARCH_SECTIONS_ENDPOINT = '/sections/search',
  SECTIONS_ENDPOINT = '/sections'
}

export const searchSections = (params: PaginationParams, searchCriteria: SectionSearchCriteria) =>
  request.post<any, SearchSectionByCriteriaResponse>(API.SEARCH_SECTIONS_ENDPOINT, searchCriteria, {
    params
  })

export const findSectionById = (id: number) =>
  request.get<any, FindSectionByIdResponse>(`${API.SECTIONS_ENDPOINT}/${id}`)

export const createSection = (newSection: Section) =>
  request.post<any, CreateSectionResponse>(API.SECTIONS_ENDPOINT, newSection)

export const updateSection = (updatedSection: Section) =>
  request.put<any, UpdateSectionResponse>(
    `${API.SECTIONS_ENDPOINT}/${updatedSection.sectionId}`,
    updatedSection
  )

export const assignRubricToSection = (sectionId: number, rubricId: number) =>
  request.put<any, AssignRubricToSectionResponse>(
    `${API.SECTIONS_ENDPOINT}/${sectionId}/rubrics/${rubricId}`
  )

export const setUpActiveWeeks = (sectionId: number, activeWeeks: string[]) => {
  return request.post<any, SetUpActiveWeeksResponse>(
    `${API.SECTIONS_ENDPOINT}/${sectionId}/weeks`,
    activeWeeks
  )
}

export const sendEmailInvitationsToStudents = (
  courseId: number,
  sectionId: number,
  emails: string[]
) =>
  request.post<any, SendEmailInvitationsToStudentsResponse>(
    `${API.SECTIONS_ENDPOINT}/${sectionId}/students/email-invitations`,
    emails, // This is the request body
    {
      params: {
        courseId
      }
    }
  )
