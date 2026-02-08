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

export interface RequirementDocumentSectionLock {
  id: number
  locked: boolean
  lockedAt?: string
  expiresAt?: string
  reason?: string
}

export interface RequirementDocumentSection {
  id: number
  sectionKey: string
  type: string
  title: string
  content?: string | null
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
