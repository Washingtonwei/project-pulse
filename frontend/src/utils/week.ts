// Get current week in "YYYY-Www" format, e.g., "2023-W31" using moment.js
import type { Section, WeekInfo } from '@/apis/section/types'
import moment from 'moment'

export const getCurrentWeekNumber = () => {
  return moment().format('GGGG-[W]WW') // GGGG is the ISO week year and WW the ISO week number, so Dec 29, 2025 is 2026-W01 as on the server (WeekKeys)
}

export const getCurrentWeekRange = () => {
  return (
    moment().startOf('isoWeek').format('MM-DD-YYYY') +
    ' -- ' +
    moment().endOf('isoWeek').format('MM-DD-YYYY')
  )
}

export const getPreviousWeekRange = () => {
  return (
    moment().subtract(1, 'weeks').startOf('isoWeek').format('MM-DD-YYYY') +
    ' -- ' +
    moment().subtract(1, 'weeks').endOf('isoWeek').format('MM-DD-YYYY')
  )
}

export const getPreviousWeek = () => {
  return moment().subtract(1, 'weeks').format('GGGG-[W]WW') // GGGG is the ISO week year and WW the ISO week number, so Dec 29, 2025 is 2026-W01 as on the server (WeekKeys)
}

export const getAllWeeksInSection = (section: Section): WeekInfo[] => {
  const allWeeks: WeekInfo[] = []
  const startDate = moment(section.startDate, 'MM-DD-YYYY')
  const endDate = moment(section.endDate, 'MM-DD-YYYY')

  const currentMonday = startDate.clone()

  while (currentMonday.isSameOrBefore(endDate)) {
    const currentSunday = currentMonday.clone().endOf('week').isoWeekday(7)

    const weekNumber = currentMonday.format('GGGG-[W]WW')

    allWeeks.push({
      weekNumber,
      monday: currentMonday.format('MM-DD-YYYY'),
      sunday: currentSunday.format('MM-DD-YYYY'),
      isActive: section.activeWeeks?.length != 0 ? section.activeWeeks!.includes(weekNumber) : true // If the section has active weeks, set the active status based on the active weeks; otherwise, set all weeks as active
    })

    currentMonday.add(1, 'week')
  }

  return allWeeks
}
