import request from '@/utils/request'
import type {
  PaginationParams,
  SearchDocumentsResponse,
  CreateRequirementDocumentRequest,
  CreateDocumentResponse,
  FindDocumentByIdResponse,
  FindDocumentSectionByIdResponse,
  UpdateDocumentSectionResponse,
  LockDocumentSectionRequest,
  LockDocumentSectionResponse,
  GetDocumentSectionLockResponse,
  SearchRequirementArtifactsResponse,
  UseCaseResponse,
  UseCase,
  UseCaseLockRequest,
  UseCaseLockResponse,
  GlossaryTermResponse,
  RequirementArtifact,
  RequirementArtifactResponse,
  RequirementArtifactSummary,
  UpdateDocumentSectionRequest,
  ArtifactReferrersResponse,
  DeleteRequirementArtifactResponse,
  ArtifactKeyHolderResponse,
  ArtifactKeySuggestionRequest,
  ArtifactKeySuggestionResponse
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

export const findDocumentSectionById = (
  teamId: number,
  documentId: number,
  documentSectionId: number
) =>
  request.get<any, FindDocumentSectionByIdResponse>(
    `${API.TEAM_DOCUMENTS}/${teamId}/documents/${documentId}/document-sections/${documentSectionId}`
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

export const getUseCaseLock = (teamId: number, useCaseId: number) =>
  request.get<any, UseCaseLockResponse>(`${API.TEAM_DOCUMENTS}/${teamId}/use-cases/${useCaseId}/lock`)

export const lockUseCase = (teamId: number, useCaseId: number, requestBody: UseCaseLockRequest) =>
  request.put<any, UseCaseLockResponse>(
    `${API.TEAM_DOCUMENTS}/${teamId}/use-cases/${useCaseId}/lock`,
    requestBody
  )

export const unlockUseCase = (teamId: number, useCaseId: number) =>
  request.delete<any, UseCaseLockResponse>(`${API.TEAM_DOCUMENTS}/${teamId}/use-cases/${useCaseId}/lock`)

// The artifact of the team that holds an exact key, or no data when it is free (the key field's availability line).
export const findArtifactKeyHolder = (teamId: number, artifactKey: string) =>
  request.get<any, ArtifactKeyHolderResponse>(`${API.TEAM_DOCUMENTS}/${teamId}/requirement-artifacts/key-holder`, {
    params: { artifactKey }
  })

// A key suggested when the student presses Suggest. Always a success when well formed; no key is a normal answer.
export const suggestArtifactKey = (teamId: number, payload: ArtifactKeySuggestionRequest) =>
  request.post<any, ArtifactKeySuggestionResponse>(
    `${API.TEAM_DOCUMENTS}/${teamId}/requirement-artifacts/key-suggestion`,
    payload
  )

// UC-ART-create-artifact: the server places the artifact in the home its type gives it (BR-artifact-home) and
// returns it with its key and version.
export const createRequirementArtifact = (teamId: number, payload: RequirementArtifactSummary) =>
  request.post<any, RequirementArtifactResponse>(`${API.TEAM_DOCUMENTS}/${teamId}/requirement-artifacts`, payload)

// handleNotFound: the caller handles a 404 itself (see utils/request.ts), as Remove does when a teammate deleted the
// artifact first.
export const findRequirementArtifactById = (
  teamId: number,
  requirementArtifactId: number,
  handleNotFound = false
) =>
  request.get<any, RequirementArtifactResponse>(
    `${API.TEAM_DOCUMENTS}/${teamId}/requirement-artifacts/${requirementArtifactId}`,
    { handleNotFound }
  )

// UC-ART-edit-artifact: the payload carries the version the artifact was read at; a stale one is refused with 409.
export const updateRequirementArtifact = (
  teamId: number,
  requirementArtifactId: number,
  payload: RequirementArtifactSummary
) =>
  request.put<any, RequirementArtifactResponse>(
    `${API.TEAM_DOCUMENTS}/${teamId}/requirement-artifacts/${requirementArtifactId}`,
    payload
  )

// UC-ART-delete-artifact step 4: what refers to the artifact. Advisory: the DELETE checks again. The editor handles
// a 404 (a teammate already deleted it) itself, so the interceptor does not leave the page.
export const getArtifactReferences = (teamId: number, requirementArtifactId: number) =>
  request.get<any, ArtifactReferrersResponse>(
    `${API.TEAM_DOCUMENTS}/${teamId}/requirement-artifacts/${requirementArtifactId}/references`,
    { handleNotFound: true }
  )

// UC-ART-delete-artifact: a physical delete at the version the artifact was read at; a stale one is refused with 409,
// a referenced artifact with 400 (4a), and a 404 is the editor's to handle, as for the references.
export const deleteRequirementArtifact = (
  teamId: number,
  requirementArtifactId: number,
  version: number
) =>
  request.delete<any, DeleteRequirementArtifactResponse>(
    `${API.TEAM_DOCUMENTS}/${teamId}/requirement-artifacts/${requirementArtifactId}`,
    { params: { version }, handleNotFound: true }
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
