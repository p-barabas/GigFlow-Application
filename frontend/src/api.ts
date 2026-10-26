import type {
  Availability,
  Credentials,
  Performance,
  PerformanceResponse,
  UserRole,
  UserSession,
} from './types'
import type { PerformanceDraft } from './CreatePerformanceForm'

export const API_ROOT = '/api/performances'

type FetchImplementation = typeof fetch

const uuidPattern = /^[0-9a-f]{8}-(?:[0-9a-f]{4}-){3}[0-9a-f]{12}$/i
const utcIsoInstantPattern = /^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}(?:\.\d+)?Z$/

const isRecord = (value: unknown): value is Record<string, unknown> =>
  typeof value === 'object' && value !== null && !Array.isArray(value)

const isAvailability = (value: unknown): value is Availability =>
  value === 'ACCEPTED' || value === 'DECLINED' || value === 'TENTATIVE'

const parseResponses = (value: unknown): Record<string, Availability> => {
  if (!isRecord(value)) throw new Error('A Performance responses mezője nem objektum.')

  const responses: Record<string, Availability> = {}
  for (const [memberId, availability] of Object.entries(value)) {
    if (!isAvailability(availability)) {
      throw new Error(`Ismeretlen Availability érték a(z) ${memberId} válaszánál.`)
    }
    responses[memberId] = availability
  }
  return responses
}

const parseStringList = (value: unknown, fieldName: string): string[] => {
  if (!Array.isArray(value) || value.some((item) => typeof item !== 'string')) {
    throw new Error(`A ${fieldName} mező nem szöveges lista.`)
  }
  return value
}

export const parsePerformance = (value: unknown): Performance => {
  if (!isRecord(value)) throw new Error('A Performance elem nem objektum.')
  if (typeof value.id !== 'string' || !uuidPattern.test(value.id)) {
    throw new Error('A Performance id mezője nem érvényes UUID.')
  }
  if (typeof value.title !== 'string') throw new Error('A Performance title mezője nem szöveg.')
  if (typeof value.venue !== 'string') throw new Error('A Performance venue mezője nem szöveg.')
  if (
    typeof value.startsAt !== 'string'
    || !utcIsoInstantPattern.test(value.startsAt)
    || Number.isNaN(Date.parse(value.startsAt))
  ) {
    throw new Error('A Performance startsAt mezője nem UTC ISO 8601 időpont.')
  }

  return {
    id: value.id,
    title: value.title,
    venue: value.venue,
    startsAt: value.startsAt,
    invitedMemberIds: parseStringList(value.invitedMemberIds, 'Performance invitedMemberIds'),
    responses: parseResponses(value.responses),
  }
}

const isUserRole = (value: string): value is UserRole => value === 'MEMBER' || value === 'LEADER'

export const parseUserSession = (value: unknown): UserSession => {
  if (!isRecord(value)) throw new Error('A munkamenet-válasz nem objektum.')
  if (typeof value.username !== 'string' || typeof value.displayName !== 'string') {
    throw new Error('A munkamenet felhasználói adatai hibásak.')
  }
  if (value.memberId !== null && typeof value.memberId !== 'string') {
    throw new Error('A munkamenet memberId mezője hibás.')
  }
  const roleValues = parseStringList(value.roles, 'munkamenet roles')
  if (roleValues.some((role) => !isUserRole(role))) throw new Error('A munkamenet ismeretlen szerepkört tartalmaz.')
  const roles = roleValues.filter(isUserRole)
  return {
    username: value.username,
    displayName: value.displayName,
    memberId: value.memberId,
    roles,
  }
}

export const parsePerformanceList = (value: unknown): Performance[] => {
  if (!Array.isArray(value)) throw new Error('A performances válasz nem lista.')
  return value.map((item, index) => {
    try {
      return parsePerformance(item)
    } catch (reason) {
      const detail = reason instanceof Error ? reason.message : 'Ismeretlen sémahiba.'
      throw new Error(`Hibás Performance elem a(z) ${index}. indexen: ${detail}`)
    }
  })
}

export type ProblemViolation = {
  field: string
  message: string
}

export type ProblemDetail = {
  type: string
  title: string
  status: number
  detail: string
  instance?: string
  violations?: ProblemViolation[]
}

export class ApiProblemError extends Error {
  readonly problem: ProblemDetail

  constructor(problem: ProblemDetail) {
    super(problem.detail)
    this.name = 'ApiProblemError'
    this.problem = problem
  }
}

const parseProblemViolations = (value: unknown): ProblemViolation[] => {
  if (!Array.isArray(value)) throw new Error('A ProblemDetail violations mezője nem lista.')
  return value.map((item, index) => {
    if (!isRecord(item) || typeof item.field !== 'string' || typeof item.message !== 'string') {
      throw new Error(`A ProblemDetail ${index}. violations eleme hibás.`)
    }
    return { field: item.field, message: item.message }
  })
}

export const parseProblemDetail = (value: unknown, responseStatus: number): ProblemDetail => {
  if (!isRecord(value)) throw new Error('A szerver hibaválasza nem ProblemDetail objektum.')
  if (typeof value.type !== 'string') throw new Error('A ProblemDetail type mezője hiányzik.')
  if (typeof value.title !== 'string') throw new Error('A ProblemDetail title mezője hiányzik.')
  if (typeof value.status !== 'number' || !Number.isInteger(value.status) || value.status !== responseStatus) {
    throw new Error('A ProblemDetail status mezője nem egyezik a HTTP státusszal.')
  }
  if (typeof value.detail !== 'string') throw new Error('A ProblemDetail detail mezője hiányzik.')
  if (value.instance !== undefined && typeof value.instance !== 'string') {
    throw new Error('A ProblemDetail instance mezője nem szöveg.')
  }

  return {
    type: value.type,
    title: value.title,
    status: value.status,
    detail: value.detail,
    ...(value.instance === undefined ? {} : { instance: value.instance }),
    ...(value.violations === undefined ? {} : { violations: parseProblemViolations(value.violations) }),
  }
}

const hasJsonMediaType = (response: Response) => {
  const mediaType = response.headers.get('content-type')?.split(';', 1)[0].trim().toLowerCase()
  return mediaType === 'application/json' || mediaType?.endsWith('+json') === true
}

const readJson = async (response: Response): Promise<unknown> => {
  if (!hasJsonMediaType(response)) {
    throw new Error(`A szerver nem JSON választ adott (${response.status}).`)
  }
  try {
    return await response.json()
  } catch (reason) {
    if (isAbortError(reason)) throw reason
    throw new Error(`A szerver hibás JSON választ adott (${response.status}).`)
  }
}

const problemMessage = (value: unknown): string | undefined => {
  if (!isRecord(value)) return undefined
  if (typeof value.detail === 'string') return value.detail
  if (typeof value.message === 'string') return value.message
  return undefined
}

const requestJson = async (
  fetchImplementation: FetchImplementation,
  path: string,
  authorization: string | undefined,
  init?: RequestInit,
): Promise<unknown> => {
  const response = await fetchImplementation(path, {
    ...init,
    headers: {
      Accept: 'application/json',
      ...(authorization ? { Authorization: authorization } : {}),
      ...init?.headers,
    },
  })

  if (!response.ok) {
    const fallback = `A szerver ${response.status} hibával válaszolt.`
    if (!hasJsonMediaType(response)) throw new Error(fallback)
    const body = await readJson(response)
    try {
      throw new ApiProblemError(parseProblemDetail(body, response.status))
    } catch (reason) {
      if (reason instanceof ApiProblemError) throw reason
      throw new Error(problemMessage(body) ?? fallback)
    }
  }

  return readJson(response)
}

export const isAbortError = (reason: unknown): boolean =>
  isRecord(reason) && reason.name === 'AbortError'

const toUtcInstant = (startsAt: string): string => {
  if (utcIsoInstantPattern.test(startsAt) && !Number.isNaN(Date.parse(startsAt))) return startsAt
  const parsed = new Date(startsAt)
  if (Number.isNaN(parsed.getTime())) throw new Error('A startsAt mező nem érvényes időpont.')
  return parsed.toISOString()
}

const createPerformance = async (
  fetchImplementation: FetchImplementation,
  draft: PerformanceDraft,
  invitedMemberIds: string[],
  authorization: string | undefined,
  options?: { signal?: AbortSignal },
): Promise<PerformanceResponse> => {
  const response = await fetchImplementation(API_ROOT, {
    method: 'POST',
    signal: options?.signal,
    headers: {
      Accept: 'application/json',
      'Content-Type': 'application/json',
      ...(authorization ? { Authorization: authorization } : {}),
    },
    body: JSON.stringify({
      title: draft.title,
      venue: draft.venue,
      startsAt: toUtcInstant(draft.startsAt),
      invitedMemberIds,
    }),
  })

  if (!response.ok) {
    if (!hasJsonMediaType(response)) {
      throw new Error(`A szerver ${response.status} hibával, nem JSON válasszal tért vissza.`)
    }
    throw new ApiProblemError(parseProblemDetail(await readJson(response), response.status))
  }

  return parsePerformance(await readJson(response))
}

const basicAuthorization = (credentials?: Credentials): string | undefined => {
  if (!credentials) return undefined
  const bytes = new TextEncoder().encode(`${credentials.username}:${credentials.password}`)
  let binary = ''
  bytes.forEach((byte) => { binary += String.fromCharCode(byte) })
  return `Basic ${btoa(binary)}`
}

export const createPerformanceApi = (
  fetchImplementation: FetchImplementation = fetch,
  credentials?: Credentials,
) => {
  const authorization = basicAuthorization(credentials)
  return {
    session: async () => parseUserSession(
      await requestJson(fetchImplementation, '/api/session', authorization),
    ),

    list: async (options?: { signal?: AbortSignal }) =>
      parsePerformanceList(await requestJson(
        fetchImplementation,
        API_ROOT,
        authorization,
        { signal: options?.signal },
      )),

    create: (draft: PerformanceDraft, invitedMemberIds: string[], options?: { signal?: AbortSignal }) =>
      createPerformance(fetchImplementation, draft, invitedMemberIds, authorization, options),

    respond: async (performanceId: string, availability: Availability) =>
      parsePerformance(await requestJson(
        fetchImplementation,
        `${API_ROOT}/${performanceId}/response`,
        authorization,
        {
          method: 'PUT',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ availability }),
        },
      )),
  }
}
