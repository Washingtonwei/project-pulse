import request from '@/utils/request'

import type {
  PeerEvaluation,
  SubmitPeerEvaluationResponse,
  FetchPeerEvaluationsResponse,
  UpdatePeerEvaluationResponse,
  GeneratePeerEvaluationAveragesForOneStudentResponse,
  PeriodParams,
  GenerateWeeklyPeerEvaluationAveragesForSectionResponse,
  GetWeeklyPeerEvaluationsForStudentResponse
} from './types'

enum API {
  EVALUATIONS_ENDPOINT = '/evaluations'
}

export const submitEvaluation = (newEvaluation: PeerEvaluation) =>
  request.post<any, SubmitPeerEvaluationResponse>(API.EVALUATIONS_ENDPOINT, newEvaluation)

export const getEvaluationsByEvaluatorIdAndWeek = (evaluationId: number, week: string) =>
  request.get<any, FetchPeerEvaluationsResponse>(
    `${API.EVALUATIONS_ENDPOINT}/evaluators/${evaluationId}/week/${week}`
  )

export const updateEvaluation = (updatedEvaluation: PeerEvaluation) =>
  request.put<any, UpdatePeerEvaluationResponse>(
    `${API.EVALUATIONS_ENDPOINT}/${updatedEvaluation.evaluationId}`,
    updatedEvaluation
  )

export const generateWeeklyPeerEvaluationAveragesForStudentWithinOnePeriod = (
  studentId: number,
  params: PeriodParams
) =>
  request.get<any, GeneratePeerEvaluationAveragesForOneStudentResponse>(
    `${API.EVALUATIONS_ENDPOINT}/students/${studentId}`,
    { params }
  )

export const generateWeeklyPeerEvaluationAveragesForSection = (sectionId: number, week: string) =>
  request.get<any, GenerateWeeklyPeerEvaluationAveragesForSectionResponse>(
    `${API.EVALUATIONS_ENDPOINT}/sections/${sectionId}/week/${week}`
  )

export const getWeeklyPeerEvaluationsForStudent = (studentId: number, week: string) =>
  request.get<any, GetWeeklyPeerEvaluationsForStudentResponse>(
    `${API.EVALUATIONS_ENDPOINT}/students/${studentId}/week/${week}/details`
  )
