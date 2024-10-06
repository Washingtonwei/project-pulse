import request from '@/utils/request'

import type {
  Rubric,
  FindRubricByIdResponse,
  CreateRubricResponse,
  UpdateRubricResponse,
  RubricSearchCriteria,
  SearchRubricByCriteriaResponse,
  Criterion,
  SearchEvaluationCriteriaByCriteriaResponse,
  FindCriterionByIdResponse,
  CreateCriterionResponse,
  UpdateCriterionResponse,
  EvaluationCriterionSearchCriteria,
  AssignCriterionToRubricResponse,
  UnassignCriterionFromRubricResponse
} from './types'

enum API {
  SEARCH_CRITERIA_ENDPOINT = '/criteria/search',
  CRITERIA_ENDPOINT = '/criteria',
  SEARCH_RUBRICS_ENDPOINT = '/rubrics/search',
  RUBRICS_ENDPOINT = '/rubrics'
}

export const searchEvaluationCriteria = (searchCriteria: EvaluationCriterionSearchCriteria) =>
  request.post<any, SearchEvaluationCriteriaByCriteriaResponse>(
    API.SEARCH_CRITERIA_ENDPOINT,
    searchCriteria
  )

export const findCriterionById = (id: number) =>
  request.get<any, FindCriterionByIdResponse>(`${API.CRITERIA_ENDPOINT}/${id}`)

export const createCriterion = (newCriterion: Criterion) =>
  request.post<any, CreateCriterionResponse>(API.CRITERIA_ENDPOINT, newCriterion)

export const updateCriterion = (updatedCriterion: Criterion) =>
  request.put<any, UpdateCriterionResponse>(
    `${API.CRITERIA_ENDPOINT}/${updatedCriterion.criterionId}`,
    updatedCriterion
  )

export const searchRubrics = (searchCriteria: RubricSearchCriteria) =>
  request.post<any, SearchRubricByCriteriaResponse>(API.SEARCH_RUBRICS_ENDPOINT, searchCriteria)

export const findRubricById = (id: number) =>
  request.get<any, FindRubricByIdResponse>(`${API.RUBRICS_ENDPOINT}/${id}`)

export const createRubric = (newRubric: Rubric) =>
  request.post<any, CreateRubricResponse>(API.RUBRICS_ENDPOINT, newRubric)

export const updateRubric = (updatedRubric: Rubric) =>
  request.put<any, UpdateRubricResponse>(
    `${API.RUBRICS_ENDPOINT}/${updatedRubric.rubricId}`,
    updatedRubric
  )

export const assignCriterionToRubric = (rubricId: number, criterionId: number) => {
  return request.put<any, AssignCriterionToRubricResponse>(
    `${API.RUBRICS_ENDPOINT}/${rubricId}/criteria/${criterionId}`,
    {}
  )
}

export const unassignCriterionFromRubric = (rubricId: number, criterionId: number) => {
  return request.delete<any, UnassignCriterionFromRubricResponse>(
    `${API.RUBRICS_ENDPOINT}/${rubricId}/criteria/${criterionId}`
  )
}
