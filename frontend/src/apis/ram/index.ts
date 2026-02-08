import request from '@/utils/request'
import type {
  PaginationParams,
  SearchDocumentsResponse,
  CreateRequirementDocumentRequest,
  CreateDocumentResponse,
  FindDocumentByIdResponse,
  UpdateDocumentSectionResponse,
  LockDocumentSectionRequest,
  LockDocumentSectionResponse,
  GetDocumentSectionLockResponse
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

export const updateDocumentSection = (
  teamId: number,
  documentId: number,
  documentSectionId: number,
  section: any
) =>
  request.put<any, UpdateDocumentSectionResponse>(
    `${API.TEAM_DOCUMENTS}/${teamId}/documents/${documentId}/document-sections/${documentSectionId}`,
    section
  )

export const getDocumentSectionLock = (
  teamId: number,
  documentId: number,
  documentSectionId: number
) =>
  request.get<any, GetDocumentSectionLockResponse>(
    `${API.TEAM_DOCUMENTS}/${teamId}/documents/${documentId}/document-sections/${documentSectionId}/lock`
  )

export const lockDocumentSection = (
  teamId: number,
  documentId: number,
  documentSectionId: number,
  requestBody: LockDocumentSectionRequest
) =>
  request.put<any, LockDocumentSectionResponse>(
    `${API.TEAM_DOCUMENTS}/${teamId}/documents/${documentId}/document-sections/${documentSectionId}/lock`,
    requestBody
  )

export const unlockDocumentSection = (
  teamId: number,
  documentId: number,
  documentSectionId: number
) =>
  request.delete<any, LockDocumentSectionResponse>(
    `${API.TEAM_DOCUMENTS}/${teamId}/documents/${documentId}/document-sections/${documentSectionId}/lock`
  )
