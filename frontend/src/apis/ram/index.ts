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
  GetDocumentSectionLockResponse,
  SearchRequirementArtifactsResponse,
  UseCaseResponse,
  UseCase,
  GlossaryTermResponse,
  RequirementArtifact,
  UpdateDocumentSectionRequest
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
  request.post<any, CreateDocumentResponse>(
    `${API.TEAM_DOCUMENTS}/${teamId}/documents`,
    requestBody
  )

export const findDocumentById = (teamId: number, documentId: number) =>
  request.get<any, FindDocumentByIdResponse>(
    `${API.TEAM_DOCUMENTS}/${teamId}/documents/${documentId}`
  )

export const searchRequirementArtifacts = (
  teamId: number,
  params: PaginationParams,
  searchCriteria: Record<string, string>
) =>
  request.post<any, SearchRequirementArtifactsResponse>(
    `${API.TEAM_DOCUMENTS}/${teamId}/requirement-artifacts/search`,
    searchCriteria,
    { params }
  )

export const updateDocumentSection = (
  teamId: number,
  documentId: number,
  documentSectionId: number,
  section: UpdateDocumentSectionRequest
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

export const getUseCaseById = (teamId: number, useCaseId: number) =>
  request.get<any, UseCaseResponse>(`${API.TEAM_DOCUMENTS}/${teamId}/use-cases/${useCaseId}`)

export const createUseCase = (teamId: number, payload: UseCase) =>
  request.post<any, UseCaseResponse>(`${API.TEAM_DOCUMENTS}/${teamId}/use-cases`, payload)

export const updateUseCase = (teamId: number, useCaseId: number, payload: UseCase) =>
  request.put<any, UseCaseResponse>(
    `${API.TEAM_DOCUMENTS}/${teamId}/use-cases/${useCaseId}`,
    payload
  )

export const getGlossaryTermById = (teamId: number, glossaryTermId: number) =>
  request.get<any, GlossaryTermResponse>(
    `${API.TEAM_DOCUMENTS}/${teamId}/glossary-terms/${glossaryTermId}`
  )

export const createGlossaryTerm = (teamId: number, payload: RequirementArtifact) =>
  request.post<any, GlossaryTermResponse>(`${API.TEAM_DOCUMENTS}/${teamId}/glossary-terms`, payload)

export const updateGlossaryTermDefinition = (
  teamId: number,
  glossaryTermId: number,
  payload: RequirementArtifact
) =>
  request.patch<any, GlossaryTermResponse>(
    `${API.TEAM_DOCUMENTS}/${teamId}/glossary-terms/${glossaryTermId}`,
    payload
  )

export const renameGlossaryTerm = (
  teamId: number,
  glossaryTermId: number,
  payload: RequirementArtifact
) =>
  request.patch<any, GlossaryTermResponse>(
    `${API.TEAM_DOCUMENTS}/${teamId}/glossary-terms/${glossaryTermId}/rename`,
    payload
  )
