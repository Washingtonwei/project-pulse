import request from '@/utils/request'

import type {
  PaginationParams,
  Team,
  TeamSearchCriteria,
  CreateTeamResponse,
  FindTeamByIdResponse,
  SearchTeamByCriteriaResponse,
  UpdateTeamResponse,
  AssignStudentToTeamResponse,
  RemoveStudentFromTeamResponse,
  AssignInstructorToTeamResponse,
  TransferTeamRequest,
  TransferTeamResponse
} from './types'

enum API {
  SEARCH_TEAMS_ENDPOINT = '/teams/search',
  TEAMS_ENDPOINT = '/teams'
}

export const searchTeams = (params: PaginationParams, searchCriteria: TeamSearchCriteria) =>
  request.post<any, SearchTeamByCriteriaResponse>(API.SEARCH_TEAMS_ENDPOINT, searchCriteria, {
    params
  })

export const findTeamById = (id: number) =>
  request.get<any, FindTeamByIdResponse>(`${API.TEAMS_ENDPOINT}/${id}`)

export const createTeam = (newTeam: Team) =>
  request.post<any, CreateTeamResponse>(API.TEAMS_ENDPOINT, newTeam)

export const updateTeam = (updatedTeam: Team) =>
  request.put<any, UpdateTeamResponse>(`${API.TEAMS_ENDPOINT}/${updatedTeam.teamId}`, updatedTeam)

export const assignStudentToTeam = (teamId: number, studentId: number) =>
  request.put<any, AssignStudentToTeamResponse>(
    `${API.TEAMS_ENDPOINT}/${teamId}/students/${studentId}`
  )

export const removeStudentFromTeam = (teamId: number, studentId: number) =>
  request.delete<any, RemoveStudentFromTeamResponse>(
    `${API.TEAMS_ENDPOINT}/${teamId}/students/${studentId}`
  )

export const assignInstructorToTeam = (teamId: number, instructorId: number) =>
  request.put<any, AssignInstructorToTeamResponse>(
    `${API.TEAMS_ENDPOINT}/${teamId}/instructors/${instructorId}`
  )

export const transferTeam = (teamId: number, transferRequest: TransferTeamRequest) =>
  request.patch<any, TransferTeamResponse>(
    `${API.TEAMS_ENDPOINT}/${teamId}`,
    transferRequest
  )
