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

export const REQUIREMENT_ARTIFACT_TYPES = [
  'GLOSSARY_TERM',
  'BUSINESS_PROBLEM',
  'BUSINESS_OPPORTUNITY',
  'BUSINESS_OBJECTIVE',
  'SUCCESS_METRIC',
  'VISION_STATEMENT',
  'BUSINESS_RISK',
  'ASSUMPTION',
  'RISK',
  'STAKEHOLDER',
  'BUSINESS_RULE',
  'FUNCTIONAL_REQUIREMENT',
  'FEATURE',
  'USE_CASE',
  'PRECONDITION',
  'POSTCONDITION',
  'USER_STORY',
  'QUALITY_ATTRIBUTE',
  'EXTERNAL_INTERFACE_REQUIREMENT',
  'CONSTRAINT',
  'DATA_REQUIREMENT',
  'OTHER'
] as const

export const PRIORITIES = ['LOW', 'MEDIUM', 'HIGH', 'CRITICAL'] as const

// The following interfaces define the structure of the data returned by the RAM API endpoints and the requests sent to them.
// They are consistent with the backend RAM DTOs.

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

export interface RequirementDocumentSection {
  id: number
  sectionKey: string
  type: string
  title: string
  content?: string | null
  requirementArtifacts?: RequirementArtifactSummary[]
  guidance?: string | null
  createdAt?: string | null
  updatedAt?: string | null
  version?: number | null
  lock?: RequirementDocumentSectionLock | null
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

// Response and request interfaces for RAM API endpoints
export interface SearchDocumentsResponse extends ApiResult<Page<RequirementDocumentSummary>> {}

export interface FindDocumentByIdResponse extends ApiResult<RequirementDocument> {}
export interface FindDocumentSectionByIdResponse extends ApiResult<RequirementDocumentSection> {}

export interface CreateRequirementDocumentRequest {
  type: DocumentType
}

export interface CreateDocumentResponse extends ApiResult<RequirementDocument> {}

export interface UpdateDocumentSectionRequest {
  content?: string
  requirementArtifacts?: RequirementArtifactSummary[]
  version?: number | null
}

export interface UpdateDocumentSectionResponse extends ApiResult<RequirementDocumentSection> {}

export interface LockDocumentSectionRequest {
  reason?: string
}

export interface GetDocumentSectionLockResponse extends ApiResult<RequirementDocumentSectionLock> {}

export interface LockDocumentSectionResponse extends ApiResult<RequirementDocumentSectionLock> {}

export interface SearchRequirementArtifactsResponse extends ApiResult<Page<RequirementArtifact>> {}

// The following interfaces define the structure of Use Cases and Glossary Terms, which are specific types of Requirement Artifacts.
export interface Condition {
  id?: number
  condition: string
  type: string
  priority?: string | null
  notes?: string | null
}

export interface UseCaseExtensionStep {
  id?: number
  actor: string
  actionText: string
}

export interface UseCaseExtension {
  id?: number
  conditionText: string
  kind: 'ALTERNATE' | 'EXCEPTION'
  exit: 'RESUME' | 'END_SUCCESS' | 'END_FAILURE'
  steps: UseCaseExtensionStep[]
}

export interface UseCaseMainStep {
  id?: number
  actor: string
  actionText: string
  extensions: UseCaseExtension[]
}

export interface UseCaseLock {
  locked: boolean
  useCaseId: number
  lockedBy?: PeerEvaluationUserRef | null
  lockedAt?: string | null
  expiresAt?: string | null
  reason?: string | null
}

export interface UseCase {
  id?: number
  artifactKey?: string | null
  title: string
  description: string
  teamId: number
  primaryActorId: number
  secondaryActorIds?: number[]
  trigger: string
  preconditions?: Condition[]
  postconditions?: Condition[]
  mainSteps: UseCaseMainStep[]
  priority?: string | null
  notes?: string | null
  createdAt?: string | null
  updatedAt?: string | null
  createdBy?: PeerEvaluationUserRef | null
  updatedBy?: PeerEvaluationUserRef | null
  version?: number | null
  lock?: UseCaseLock | null
}

export interface UseCaseLockRequest {
  reason?: string
}

export interface UseCaseResponse extends ApiResult<UseCase> {}

export interface UseCaseLockResponse extends ApiResult<UseCaseLock> {}

export interface GlossaryTermResponse extends ApiResult<RequirementArtifact> {}
