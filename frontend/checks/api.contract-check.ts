import { ApiProblemError, createPerformanceApi, isAbortError } from '../src/api.ts'

const validPerformance = {
  id: '11111111-2222-4333-8444-555555555555',
  title: 'Őszi koncert',
  venue: 'Zeneház',
  startsAt: '2026-10-20T18:30:00Z',
  invitedMemberIds: ['anna', 'bence', 'csilla'],
  responses: { anna: 'ACCEPTED', bence: 'TENTATIVE' },
}

const jsonResponse = (body: unknown, status = 200, mediaType = 'application/json; charset=utf-8') =>
  new Response(JSON.stringify(body), {
    status,
    headers: { 'Content-Type': mediaType },
  })

const expectFailure = async (operation: () => Promise<unknown>, expectedMessage: string) => {
  try {
    await operation()
  } catch (reason) {
    if (reason instanceof Error && reason.message.includes(expectedMessage)) return
    throw reason
  }
  throw new Error(`A várt hiba nem történt meg: ${expectedMessage}`)
}

const validApi = createPerformanceApi(async () => jsonResponse([validPerformance]))
const performances = await validApi.list()
if (performances.length !== 1 || performances[0].responses.anna !== 'ACCEPTED') {
  throw new Error('A helyes Performance lista feldolgozása sikertelen.')
}

const invalidApi = createPerformanceApi(async () => jsonResponse([
  { ...validPerformance, responses: { anna: 'MAYBE' } },
]))
await expectFailure(() => invalidApi.list(), 'Ismeretlen Availability')

const nonJsonApi = createPerformanceApi(async () => new Response('<html>hiba</html>', {
  status: 200,
  headers: { 'Content-Type': 'text/html' },
}))
await expectFailure(() => nonJsonApi.list(), 'nem JSON választ adott')

const abortedApi = createPerformanceApi(async () => {
  throw new DOMException('A kérés megszakadt.', 'AbortError')
})
try {
  await abortedApi.list()
  throw new Error('A megszakított kérés nem dobott hibát.')
} catch (reason) {
  if (!isAbortError(reason)) throw reason
}

const draft = {
  title: 'Őszi koncert',
  venue: 'Zeneház',
  startsAt: '2026-10-20T18:30:00Z',
}

const createApi = createPerformanceApi(async (input, init) => {
  if (input !== '/api/performances' || init?.method !== 'POST') {
    throw new Error('A create nem a POST /api/performances műveletet hívta.')
  }
  return jsonResponse(validPerformance, 201)
})
const created = await createApi.create(draft, ['anna', 'bence', 'csilla'])
if (created.id !== validPerformance.id || created.responses.anna !== 'ACCEPTED') {
  throw new Error('A sikeres create PerformanceResponse feldolgozása sikertelen.')
}

const validationProblem = {
  type: 'https://gigflow.example/problems/validation-error',
  title: 'Invalid request',
  status: 400,
  detail: 'The request contains invalid fields.',
  instance: '/api/performances',
  violations: [{ field: 'title', message: 'must not be blank' }],
}
const problemApi = createPerformanceApi(async () =>
  jsonResponse(validationProblem, 400, 'application/problem+json'))
try {
  await problemApi.create(draft, ['anna'])
  throw new Error('A ProblemDetail válasz nem eredményezett alkalmazáshibát.')
} catch (reason) {
  if (!(reason instanceof ApiProblemError)) throw reason
  if (reason.problem.status !== 400 || reason.problem.violations?.[0]?.field !== 'title') {
    throw new Error('A ProblemDetail alkalmazáshiba tartalma hibás.')
  }
}

const invalidCreateSchemaApi = createPerformanceApi(async () =>
  jsonResponse({ id: validPerformance.id }, 201))
await expectFailure(
  () => invalidCreateSchemaApi.create(draft, ['anna']),
  'title mezője nem szöveg',
)

const conflictProblem = {
  type: 'https://gigflow.example/problems/performance-conflict',
  title: 'Performance conflict',
  status: 409,
  detail: 'A performance with the same title, venue and start time already exists.',
  instance: '/api/performances',
}
const conflictApi = createPerformanceApi(async () =>
  jsonResponse(conflictProblem, 409, 'application/problem+json'))
try {
  await conflictApi.create(draft, ['anna'])
  throw new Error('A 409 válasz nem eredményezett alkalmazáshibát.')
} catch (reason) {
  if (!(reason instanceof ApiProblemError) || reason.problem.status !== 409) throw reason
}

const networkFailureApi = createPerformanceApi(async () => {
  throw new TypeError('Network connection failed')
})
await expectFailure(() => networkFailureApi.create(draft, ['anna']), 'Network connection failed')

const abortedCreateApi = createPerformanceApi(async () => {
  throw new DOMException('A kérés megszakadt.', 'AbortError')
})
try {
  await abortedCreateApi.create(draft, ['anna'])
  throw new Error('A megszakított create kérés nem dobott hibát.')
} catch (reason) {
  if (!isAbortError(reason)) throw reason
}

let observedAuthorization = ''
const securedApi = createPerformanceApi(async (_input, init) => {
  observedAuthorization = new Headers(init?.headers).get('Authorization') ?? ''
  return jsonResponse({ username: 'anna', displayName: 'Anna', memberId: 'anna', roles: ['MEMBER'] })
}, { username: 'anna', password: 'titok' })
const session = await securedApi.session()
if (session.memberId !== 'anna' || !observedAuthorization.startsWith('Basic ')) {
  throw new Error('A hitelesített session kérés szerződése hibás.')
}

let respondPath = ''
const respondApi = createPerformanceApi(async (input) => {
  respondPath = String(input)
  return jsonResponse(validPerformance)
}, { username: 'anna', password: 'titok' })
await respondApi.respond(validPerformance.id, 'DECLINED')
if (respondPath !== `/api/performances/${validPerformance.id}/response`) {
  throw new Error('A válaszadás nem a principalhoz kötött végpontot használja.')
}

console.log('API contract checks: 12 passed')
