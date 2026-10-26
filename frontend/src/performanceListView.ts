export type PerformanceListView = 'loading' | 'empty' | 'error' | 'success'

export const determinePerformanceListView = (
  loading: boolean,
  error: string,
  performanceCount: number,
): PerformanceListView => {
  if (loading && performanceCount === 0) return 'loading'
  if (error && performanceCount === 0) return 'error'
  return performanceCount === 0 ? 'empty' : 'success'
}
import type { Performance } from './types'


export const mergeLoadedPerformances = (
  loaded: Performance[],
  current: Performance[],
): Performance[] => [
  ...loaded,
  ...current.filter((existing) => !loaded.some((item) => item.id === existing.id)),
]

export const upsertCreatedPerformance = (
  current: Performance[],
  created: Performance,
): Performance[] => [
  ...current.filter((performance) => performance.id !== created.id),
  created,
]
