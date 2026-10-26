import { ApiProblemError } from '../src/api.ts'
import { createPerformanceErrorMessage } from '../src/createPerformanceError.ts'
import { createSubmissionGate } from '../src/submissionGate.ts'

const gate = createSubmissionGate()
let calls = 0
let releaseFirst = () => {}

const first = gate.run(() => new Promise<void>((resolve) => {
  calls += 1
  releaseFirst = resolve
}))
const duplicateStarted = await gate.run(async () => {
  calls += 1
})

if (duplicateStarted || calls !== 1) {
  throw new Error('A single-flight kapu nem fogta meg a gyors dupla küldést.')
}

releaseFirst()
await first
const nextStarted = await gate.run(async () => {
  calls += 1
})
if (!nextStarted || calls !== 2) {
  throw new Error('A single-flight kapu a befejezés után sem engedett új kérést.')
}

const validationMessage = createPerformanceErrorMessage(new ApiProblemError({
  type: 'https://gigflow.example/problems/validation-error',
  title: 'Invalid request',
  status: 400,
  detail: 'The request contains invalid fields.',
  violations: [{ field: 'title', message: 'must not be blank' }],
}))
if (!validationMessage.includes('Esemény neve')) {
  throw new Error('A 400-as mezőhiba nem kapott érthető mezőnevet.')
}

const conflictMessage = createPerformanceErrorMessage(new ApiProblemError({
  type: 'https://gigflow.example/problems/performance-conflict',
  title: 'Performance conflict',
  status: 409,
  detail: 'Conflict',
}))
if (!conflictMessage.includes('Már létezik fellépés')) {
  throw new Error('A 409 konfliktus felhasználói üzenete hibás.')
}

const networkMessage = createPerformanceErrorMessage(new TypeError('Failed to fetch'))
if (!networkMessage.includes('hálózati kapcsolatot')) {
  throw new Error('A hálózati hiba felhasználói üzenete hibás.')
}

console.log('Create flow checks: double-submit guard, 400, 409, network error passed')
