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
  SendEmailInvitationsResponse,
  InviteOrAddInstructorsResponse,
  GetInstructorsResponse,
  RemoveInstructorResponse
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

// The two invite routes send one email per address before they answer, so a course section's worth of
// invitations takes far longer than the shared 10-second timeout allows. That timeout does not stop the server:
// it abandons the response while the backend finishes, commits every invitation and delivers every email, which
// leaves the course admin looking at a failure toast for a batch that in fact went out. Three minutes covers a
// large course section and still sits under the platform's own request timeout.
const INVITATION_TIMEOUT_MS = 180_000

export const sendEmailInvitationsToStudents = (
  courseId: number,
  sectionId: number,
  emails: string[]
) =>
  request.post<any, SendEmailInvitationsResponse>(
    `${API.SECTIONS_ENDPOINT}/${sectionId}/students/email-invitations`,
    emails, // This is the request body
    {
      params: {
        courseId
      },
      timeout: INVITATION_TIMEOUT_MS
    }
  )

export const inviteOrAddInstructors = (courseId: number, sectionId: number, emails: string[]) =>
  request.post<any, InviteOrAddInstructorsResponse>(
    `${API.SECTIONS_ENDPOINT}/${sectionId}/instructors/invite-or-add`,
    emails, // This is the request body
    {
      params: {
        courseId
      },
      timeout: INVITATION_TIMEOUT_MS
    }
  )

export const getInstructors = (sectionId: number) =>
  request.get<any, GetInstructorsResponse>(`${API.SECTIONS_ENDPOINT}/${sectionId}/instructors`)

export const removeInstructorFromSection = (sectionId: number, instructorId: number) =>
  request.delete<any, RemoveInstructorResponse>(
    `${API.SECTIONS_ENDPOINT}/${sectionId}/instructors/${instructorId}`
  )
