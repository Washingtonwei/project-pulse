import request from '@/utils/request'

import type { GetSubmissionStatusResponse, SendRemindersResponse, SubmittableItem } from './types'

enum API {
  SECTIONS_ENDPOINT = '/sections'
}

// Who owes the item for the week; the week defaults to the previous week on the server
export const getSubmissionStatus = (sectionId: number, item: SubmittableItem, week?: string) =>
  request.get<any, GetSubmissionStatusResponse>(
    `${API.SECTIONS_ENDPOINT}/${sectionId}/submission-status`,
    { params: { item, week } }
  )

// Always for the previous week; the server decides both the week and the recipients
export const sendReminders = (sectionId: number, item: SubmittableItem) =>
  request.post<any, SendRemindersResponse>(`${API.SECTIONS_ENDPOINT}/${sectionId}/reminders`, {
    item
  })
