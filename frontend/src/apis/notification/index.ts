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

// Always for the previous week; the server decides both the week and the recipients, and refuses the send if the
// week the list was shown for is no longer the previous week
// The send is synchronous, one email after another, so it gets longer than the default 10 seconds
export const sendReminders = (sectionId: number, item: SubmittableItem, week: string) =>
  request.post<any, SendRemindersResponse>(
    `${API.SECTIONS_ENDPOINT}/${sectionId}/reminders`,
    { item, week },
    { timeout: 120_000 }
  )
