export interface Criterion {
  criterionId?: number
  criterion: string
  description: string
  maxScore: number
  courseId?: number
}

export interface EvaluationCriterionSearchCriteria {
  criterionId?: number
  criterion?: string
}

export interface SearchEvaluationCriteriaByCriteriaResponse {
  flag: boolean
  code: number
  message: string
  data: {
    content: Criterion[]
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
}

export interface FindCriterionByIdResponse {
  flag: boolean
  code: number
  message: string
  data: Criterion
}

export interface CreateCriterionResponse {
  flag: boolean
  code: number
  message: string
  data: Criterion
}

export interface UpdateCriterionResponse {
  flag: boolean
  code: number
  message: string
  data: Criterion
}

export interface Rubric {
  rubricId?: number
  rubricName: string
  criteria?: Criterion[]
  courseId?: number
}

export interface RubricSearchCriteria {
  rubricId?: number
  rubricName?: string
}

export interface SearchRubricByCriteriaResponse {
  flag: boolean
  code: number
  message: string
  data: {
    content: Rubric[]
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
}

export interface FindRubricByIdResponse {
  flag: boolean
  code: number
  message: string
  data: Rubric
}

export interface CreateRubricResponse {
  flag: boolean
  code: number
  message: string
  data: Rubric
}

export interface UpdateRubricResponse {
  flag: boolean
  code: number
  message: string
  data: Rubric
}

export interface AssignCriterionToRubricResponse {
  flag: boolean
  code: number
  message: string
}

export interface UnassignCriterionFromRubricResponse {
  flag: boolean
  code: number
  message: string
}
