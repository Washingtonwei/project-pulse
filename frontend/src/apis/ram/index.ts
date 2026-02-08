import request from '@/utils/request'
import type {
  PaginationParams,
  SearchDocumentsResponse,
  CreateRequirementDocumentRequest,
  CreateDocumentResponse,
  FindDocumentByIdResponse
} from './types'

enum API {
  TEAM_DOCUMENTS = '/teams'
}

export const searchDocuments = (
  teamId: number,
  params: PaginationParams,
  searchCriteria: Record<string, string>
) =>
  request.post<any, SearchDocumentsResponse>(
    `${API.TEAM_DOCUMENTS}/${teamId}/documents/search`,
    searchCriteria,
    { params }
  )

export const createDocument = (teamId: number, requestBody: CreateRequirementDocumentRequest) =>
  request.post<any, CreateDocumentResponse>(`${API.TEAM_DOCUMENTS}/${teamId}/documents`, requestBody)

export const findDocumentById = (teamId: number, documentId: number) =>
  request.get<any, FindDocumentByIdResponse>(
    `${API.TEAM_DOCUMENTS}/${teamId}/documents/${documentId}`
  )
