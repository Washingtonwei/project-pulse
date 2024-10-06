export interface Activity {
  activityId?: number
  studentId?: number
  studentName?: string
  week: string
  teamId?: number
  teamName?: string
  category: ActivityCategory
  activity: string
  description: string
  plannedHours: number
  actualHours: number
  status: ActivityStatus
  comments?: string
  createdAt?: string // In the format "MM-dd-yyyy HH:mm:ss"
  updatedAt?: string // In the format "MM-dd-yyyy HH:mm:ss"
}

export enum ActivityCategory {
  DEVELOPMENT = 'DEVELOPMENT',
  TESTING = 'TESTING',
  BUGFIX = 'BUGFIX',
  COMMUNICATION = 'COMMUNICATION',
  DOCUMENTATION = 'DOCUMENTATION',
  DESIGN = 'DESIGN',
  PLANNING = 'PLANNING',
  LEARNING = 'LEARNING',
  DEPLOYMENT = 'DEPLOYMENT',
  SUPPORT = 'SUPPORT',
  MISCELLANEOUS = 'MISCELLANEOUS'
}

export enum ActivityStatus {
  IN_PROGRESS = 'IN_PROGRESS',
  COMPLETED = 'COMPLETED'
}

export interface PaginationParams {
  page: number
  size: number
  sort?: string
}

export interface ActivitySearchCriteria {
  studentId?: number
  teamId?: number
  week?: string
  category?: ActivityCategory
  status?: ActivityStatus
  startWeek?: string
  endWeek?: string
}

export interface SearchActivityByCriteriaResponse {
  flag: boolean
  code: number
  message: string
  data: {
    content: Activity[]
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

export interface FindActivityByIdResponse {
  flag: boolean
  code: number
  message: string
  data: Activity
}

export interface CreateActivityResponse {
  flag: boolean
  code: number
  message: string
  data: Activity
}

export interface UpdateActivityResponse {
  flag: boolean
  code: number
  message: string
  data: Activity
}

export interface DeleteActivityResponse {
  flag: boolean
  code: number
  message: string
  data: null
}

export interface CommentActivityResponse {
  flag: boolean
  code: number
  message: string
  data: null
}
