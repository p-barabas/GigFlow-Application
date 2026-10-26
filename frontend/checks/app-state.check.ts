import { createPerformanceApi } from '../src/api.ts'
import {
  determinePerformanceListView,
  mergeLoadedPerformances,
  upsertCreatedPerformance,
} from '../src/performanceListView.ts'

const performance = {
  id: '11111111-2222-4333-8444-555555555555',
  title: 'Őszi koncert',
  venue: 'Zeneház',
  startsAt: '2026-10-20T18:30:00Z',
  invitedMemberIds: ['anna', 'bence', 'csilla'],
  responses: { anna: 'ACCEPTED' },
}

const jsonResponse = (body: unknown, status = 200) => new Response(JSON.stringify(body), {
  status,
  headers: { 'Content-Type': 'application/json' },
})

let releaseLoading = () => {}
const delayedApi = createPerformanceApi(() => new Promise<Response>((resolve) => {
  releaseLoading = () => resolve(jsonResponse([performance]))
}))
const delayedRequest = delayedApi.list()
if (determinePerformanceListView(true, '', 0) !== 'loading') {
  throw new Error('A késleltetett kérés alatt nem a loading állapot jelent meg.')
}
releaseLoading()
await delayedRequest

const oneItem = await createPerformanceApi(async () => jsonResponse([performance])).list()
const empty = await createPerformanceApi(async () => jsonResponse([])).list()

let serverError = ''
try {
  await createPerformanceApi(async () => jsonResponse({ detail: 'Próbahiba' }, 500)).list()
} catch (reason) {
  serverError = reason instanceof Error ? reason.message : 'Ismeretlen hiba'
}

const cases = [
  { name: 'one item', actual: determinePerformanceListView(false, '', oneItem.length), expected: 'success' },
  { name: 'empty', actual: determinePerformanceListView(false, '', empty.length), expected: 'empty' },
  { name: 'server error', actual: determinePerformanceListView(false, serverError, 0), expected: 'error' },
  {
    name: 'stale data remains visible after refresh error',
    actual: determinePerformanceListView(false, serverError, oneItem.length),
    expected: 'success',
  },
] as const

for (const testCase of cases) {
  if (testCase.actual !== testCase.expected) {
    throw new Error(`${testCase.name}: ${testCase.expected} helyett ${testCase.actual}`)
  }
}

const createdPerformance = {
  ...performance,
  id: '22222222-3333-4444-8555-666666666666',
  title: 'Új koncert',
}
const afterCreate = upsertCreatedPerformance(oneItem, createdPerformance)
if (afterCreate.length !== 2 || afterCreate[1].title !== 'Új koncert') {
  throw new Error('A sikeres create nem frissítette a listát.')
}

const lateEmptyLoad = mergeLoadedPerformances([], afterCreate)
if (lateEmptyLoad.length !== 2) {
  throw new Error('A későn érkező üres lista elvesztette a már használható adatokat.')
}

console.log('App server-state checks: loading, empty/error split, create update, stale-data preservation passed')
