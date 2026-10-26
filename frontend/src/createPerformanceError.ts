const fieldLabels: Record<string, string> = {
  title: 'Esemény neve',
  venue: 'Helyszín',
  startsAt: 'Kezdési időpont',
}

export const createPerformanceErrorMessage = (reason: unknown): string => {
  const problem = readProblem(reason)
  if (problem) {
    if (problem.status === 400) {
      const violation = problem.violations?.[0]
      if (violation) {
        return `${fieldLabels[violation.field] ?? violation.field}: ${violation.message}`
      }
    }
    if (problem.status === 409) {
      return 'Már létezik fellépés ugyanezzel a címmel, helyszínnel és kezdési időponttal.'
    }
    return problem.detail
  }

  if (reason instanceof Error && reason.message && reason.name !== 'TypeError') return reason.message
  return 'A fellépést nem sikerült elmenteni. Ellenőrizd a hálózati kapcsolatot, majd próbáld újra.'
}

type ErrorProblem = {
  status: number
  detail: string
  violations?: Array<{ field: string; message: string }>
}

const readProblem = (reason: unknown): ErrorProblem | undefined => {
  if (typeof reason !== 'object' || reason === null || !("problem" in reason)) return undefined
  const problem = reason.problem
  if (typeof problem !== 'object' || problem === null) return undefined
  if (!("status" in problem) || typeof problem.status !== 'number') return undefined
  if (!("detail" in problem) || typeof problem.detail !== 'string') return undefined
  const violations = "violations" in problem && Array.isArray(problem.violations)
    ? problem.violations.flatMap((violation) => {
      if (typeof violation !== 'object' || violation === null) return []
      if (!("field" in violation) || typeof violation.field !== 'string') return []
      if (!("message" in violation) || typeof violation.message !== 'string') return []
      return [{ field: violation.field, message: violation.message }]
    })
    : undefined
  return {
    status: problem.status,
    detail: problem.detail,
    ...(violations === undefined ? {} : { violations }),
  }
}
