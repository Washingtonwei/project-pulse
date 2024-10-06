import request from '@/utils/request'

import type {
  PaginationParams,
  ActivitySearchCriteria,
  Activity,
  SearchActivityByCriteriaResponse,
  CreateActivityResponse,
  UpdateActivityResponse,
  DeleteActivityResponse,
  FindActivityByIdResponse,
  CommentActivityResponse
} from './types'

enum API {
  SEARCH_ACTIVITIES_ENDPOINT = '/activities/search',
  ACTIVITIES_ENDPOINT = '/activities'
}

export const searchActivities = (
  params: PaginationParams,
  searchCriteria: ActivitySearchCriteria
) =>
  request.post<any, SearchActivityByCriteriaResponse>(
    API.SEARCH_ACTIVITIES_ENDPOINT,
    searchCriteria,
    {
      params
    }
  )

export const findActivityById = (id: number) =>
  request.get<any, FindActivityByIdResponse>(`${API.ACTIVITIES_ENDPOINT}/${id}`)

export const createActivity = (newActivity: Activity) =>
  request.post<any, CreateActivityResponse>(API.ACTIVITIES_ENDPOINT, newActivity)

export const updateActivity = (updatedActivity: Activity) =>
  request.put<any, UpdateActivityResponse>(
    `${API.ACTIVITIES_ENDPOINT}/${updatedActivity.activityId}`,
    updatedActivity
  )

export const deleteActivity = (id: number) =>
  request.delete<any, DeleteActivityResponse>(`${API.ACTIVITIES_ENDPOINT}/${id}`)

export const commentActivity = (id: number, comment: string) =>
  request.patch<any, CommentActivityResponse>(`${API.ACTIVITIES_ENDPOINT}/${id}/comments`, {
    comment
  })
