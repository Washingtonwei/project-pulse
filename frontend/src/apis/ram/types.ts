export type DocumentType =
  | 'VISION_SCOPE'
  | 'USE_CASES'
  | 'USER_STORIES'
  | 'BUSINESS_RULES'
  | 'SRS'
  | 'GLOSSARY'

export type DocumentStatus = 'DRAFT' | 'SUBMITTED' | 'RETURNED'

export interface PaginationParams {
  page: number
  size: number
}

export interface RequirementDocumentSummary {
  id: number
  type: DocumentType
  teamId: number
  documentKey: string
  title: string
  status: DocumentStatus
  version: number
}

export interface PeerEvaluationUserRef {
  id: number
  name: string
}

export interface RequirementDocumentSectionLock {
  locked: boolean
  documentSectionId: number
  lockedBy?: PeerEvaluationUserRef | null
  lockedAt?: string | null
  expiresAt?: string | null
  reason?: string | null
}

export interface RequirementArtifactSummary {
  id?: number
  type: string
  artifactKey?: string | null
  title: string
  content: string
  priority?: string | null
  sourceSectionId?: number | null
  notes?: string | null
}

export interface RequirementArtifact extends RequirementArtifactSummary {
  createdAt?: string | null
  updatedAt?: string | null
  createdBy?: PeerEvaluationUserRef | null
  updatedBy?: PeerEvaluationUserRef | null
}

export interface SearchRequirementArtifactsResponse extends ApiResult<Page<RequirementArtifact>> {}

export interface RequirementDocumentSection {
  id: number
  sectionKey: string
  type: string
  title: string
  content?: string | null
  requirementArtifacts?: RequirementArtifactSummary[]
  guidance?: string | null
  lock?: RequirementDocumentSectionLock | null
}

export interface RequirementDocument {
  id: number
  type: DocumentType
  teamId: number
  documentKey: string
  title: string
  sections: RequirementDocumentSection[]
  status: DocumentStatus
  version: number
}

export interface Page<T> {
  content: T[]
  pageable: {
    sort: {
      sorted: boolean
      unsorted: boolean
      empty: boolean
    }
    pageNumber: number
    pageSize: number
    offset: number
    unpaged: boolean
    paged: boolean
  }
  totalElements: number
  totalPages: number
  last: boolean
  size: number
  number: number
  sort: {
    sorted: boolean
    unsorted: boolean
    empty: boolean
  }
  numberOfElements: number
  first: boolean
  empty: boolean
}

export interface ApiResult<T> {
  flag: boolean
  code: number
  message: string
  data: T
}

export interface SearchDocumentsResponse extends ApiResult<Page<RequirementDocumentSummary>> {}

export interface FindDocumentByIdResponse extends ApiResult<RequirementDocument> {}

export interface CreateRequirementDocumentRequest {
  type: DocumentType
}

export interface CreateDocumentResponse extends ApiResult<RequirementDocument> {}

export interface UpdateDocumentSectionResponse extends ApiResult<RequirementDocumentSection> {}

export interface LockDocumentSectionRequest {
  reason?: string
}

export interface LockDocumentSectionResponse extends ApiResult<RequirementDocumentSectionLock> {}

export interface GetDocumentSectionLockResponse extends ApiResult<RequirementDocumentSectionLock> {}

export interface ConditionDto {
  id?: number
  condition: string
  type: string
  priority?: string | null
  notes?: string | null
}

export interface UseCaseExtensionStepDto {
  id?: number
  actor: string
  actionText: string
}

export interface UseCaseExtensionDto {
  id?: number
  conditionText: string
  kind: 'ALTERNATE' | 'EXCEPTION'
  exit: 'RESUME' | 'END_SUCCESS' | 'END_FAILURE'
  steps: UseCaseExtensionStepDto[]
}

export interface UseCaseMainStepDto {
  id?: number
  actor: string
  actionText: string
  extensions: UseCaseExtensionDto[]
}

export interface UseCaseDto {
  id?: number
  artifactKey?: string | null
  title: string
  description: string
  teamId: number
  primaryActorId: number
  secondaryActorIds?: number[]
  trigger: string
  preconditions?: ConditionDto[]
  postconditions?: ConditionDto[]
  mainSteps: UseCaseMainStepDto[]
  priority?: string | null
  notes?: string | null
  createdAt?: string | null
  updatedAt?: string | null
  createdBy?: PeerEvaluationUserRef | null
  updatedBy?: PeerEvaluationUserRef | null
}

export interface UseCaseResponse extends ApiResult<UseCaseDto> {}

export interface GlossaryTermResponse extends ApiResult<RequirementArtifact> {}
