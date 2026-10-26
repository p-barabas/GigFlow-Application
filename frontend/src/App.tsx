import { useEffect, useMemo, useState } from 'react'
import CreatePerformanceForm, { type PerformanceDraft } from './CreatePerformanceForm'
import LoginForm from './LoginForm'
import PerformanceCard, { availabilityOptions } from './PerformanceCard'
import { createPerformanceApi, isAbortError } from './api'
import { createPerformanceErrorMessage } from './createPerformanceError'
import {
  determinePerformanceListView,
  mergeLoadedPerformances,
  upsertCreatedPerformance,
} from './performanceListView'
import type { Availability, Credentials, Member, Performance, UserSession } from './types'

const members: Member[] = [
  { id: 'anna', name: 'Anna', instrument: 'hegedű', initials: 'AN' },
  { id: 'bence', name: 'Bence', instrument: 'cselló', initials: 'BE' },
  { id: 'csilla', name: 'Csilla', instrument: 'fuvola', initials: 'CS' },
]

function App() {
  const [credentials, setCredentials] = useState<Credentials | null>(null)
  const [session, setSession] = useState<UserSession | null>(null)
  const [performances, setPerformances] = useState<Performance[]>([])
  const [notice, setNotice] = useState('')
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)
  const api = useMemo(() => createPerformanceApi(fetch, credentials ?? undefined), [credentials])

  const activeMember = session?.memberId
    ? members.find((member) => member.id === session.memberId)
    : undefined
  const isLeader = session?.roles.includes('LEADER') === true
  const sortedPerformances = useMemo(
    () => [...performances].sort((a, b) => a.startsAt.localeCompare(b.startsAt)),
    [performances],
  )
  const listView = determinePerformanceListView(loading, error, performances.length)

  useEffect(() => {
    if (!session || !credentials) return
    const controller = new AbortController()
    setLoading(true)
    api.list({ signal: controller.signal })
      .then((items) => {
        setPerformances((current) => mergeLoadedPerformances(items, current))
        setError('')
      })
      .catch((reason: unknown) => {
        if (!isAbortError(reason)) setError(toMessage(reason))
      })
      .finally(() => {
        if (!controller.signal.aborted) setLoading(false)
      })
    return () => controller.abort()
  }, [api, credentials, session])

  useEffect(() => {
    if (!notice) return
    const timeout = window.setTimeout(() => setNotice(''), 3200)
    return () => window.clearTimeout(timeout)
  }, [notice])

  const login = async (nextCredentials: Credentials) => {
    try {
      const nextApi = createPerformanceApi(fetch, nextCredentials)
      const nextSession = await nextApi.session()
      setCredentials(nextCredentials)
      setSession(nextSession)
      setError('')
    } catch (reason) {
      throw new Error(reason instanceof Error && reason.message
        ? reason.message
        : 'Hibás felhasználónév vagy jelszó.')
    }
  }

  const logout = () => {
    setCredentials(null)
    setSession(null)
    setPerformances([])
    setError('')
    setNotice('')
  }

  const createPerformance = async (draft: PerformanceDraft) => {
    try {
      const created = await api.create(draft, members.map((member) => member.id))
      setPerformances((current) => upsertCreatedPerformance(current, created))
      setNotice(`A(z) „${created.title}” fellépés létrejött, a tagok meghívást kaptak.`)
    } catch (reason) {
      throw new Error(createPerformanceErrorMessage(reason))
    }
  }

  const respond = async (performanceId: string, response: Availability) => {
    if (!activeMember) return
    try {
      const updated = await api.respond(performanceId, response)
      setPerformances((current) =>
        current.map((performance) => performance.id === updated.id ? updated : performance),
      )
      setError('')
      const label = availabilityOptions.find((option) => option.value === response)?.label
      setNotice(`${activeMember.name} válasza elmentve: ${label}.`)
    } catch (reason) {
      setError(toMessage(reason))
    }
  }

  if (!session) {
    return <div className="app-shell"><PublicHeader /><LoginForm onLogin={login} /></div>
  }

  return (
    <div className="app-shell">
      <header className="topbar">
        <Brand />
        <div className="signed-in-user">
          <span className="member-avatar" aria-hidden="true">
            {(activeMember?.initials ?? session.displayName.slice(0, 2)).toUpperCase()}
          </span>
          <span><small>Bejelentkezve</small><strong>{session.displayName}</strong></span>
          <button type="button" onClick={logout}>Kilépés</button>
        </div>
      </header>

      <div className="prototype-strip" role="note">
        <span className="live-dot" aria-hidden="true" />
        <strong>Spring Boot kiadás</strong>
        <span>hitelesített REST API</span>
        <span>PostgreSQL tárolás</span>
      </div>

      <main id="top">
        <section className="hero">
          <div>
            <p className="eyebrow">KÖVETKEZŐ FELLÉPÉSEK</p>
            <h1>A zenekar egy ritmusban.</h1>
            <p className="hero-copy">
              Rögzítsétek a koncerteket, jelezzétek az elérhetőséget, és lássátok azonnal,
              hogy összeáll-e a csapat.
            </p>
          </div>
          <div className="hero-stat" aria-label={`${performances.length} tervezett esemény`}>
            <strong>{performances.length}</strong><span>tervezett<br />esemény</span>
          </div>
        </section>

        <section className="workspace-grid">
          <aside className="create-panel" aria-labelledby="create-title">
            <div className="section-heading">
              <span className="step-number">01</span>
              <div><p className="eyebrow">{isLeader ? 'ÚJ IDŐPONT' : 'SAJÁT FIÓK'}</p>
                <h2 id="create-title">{isLeader ? 'Fellépés rögzítése' : 'Zenekari tag'}</h2></div>
            </div>
            {isLeader ? <CreatePerformanceForm onCreate={createPerformance} /> : (
              <div className="member-guidance">
                <strong>{activeMember?.name ?? session.displayName}</strong>
                <p>A jobb oldali kártyákon válaszolhatsz a meghívásaidra. A válaszodat a fellépés kezdetéig módosíthatod.</p>
              </div>
            )}
            <div className="storage-note"><span aria-hidden="true">◇</span>
              <p><strong>Spring Boot backend</strong><br />API: <code>/api/performances</code></p></div>
          </aside>

          <section className="events-panel" aria-labelledby="events-title" aria-busy={loading}>
            <div className="events-heading"><div className="section-heading">
              <span className="step-number">02</span><div><p className="eyebrow">ÁTTEKINTÉS</p>
                <h2 id="events-title">Fellépések</h2></div></div></div>
            <div className="events-list">
              {error && <p className="api-error" role="alert" aria-live="assertive">{error}</p>}
              {listView === 'loading' && <div className="empty-state" role="status"><p>Adatok betöltése a szerverről…</p></div>}
              {listView === 'empty' && <div className="empty-state" role="status"><span aria-hidden="true">♪</span>
                <h3>Még nincs fellépés</h3><p>A zenekarvezető még nem rögzített fellépést.</p></div>}
              {listView === 'success' && <p className="sr-only" role="status">{performances.length} fellépés betöltve.</p>}
              {sortedPerformances.map((performance) => <PerformanceCard key={performance.id}
                performance={performance} members={members} activeMember={activeMember}
                onRespond={activeMember ? respond : undefined} />)}
            </div>
          </section>
        </section>
      </main>

      <footer><span>GigFlow · oktatási alkalmazás</span>
        <span>React + TypeScript kliens · Spring Boot REST backend</span></footer>
      <div className={`toast ${notice ? 'visible' : ''}`} role="status" aria-live="polite">
        <span aria-hidden="true">✓</span> {notice}
      </div>
    </div>
  )
}

function Brand() {
  return <a className="brand" href="#top" aria-label="GigFlow kezdőlap"><span className="brand-mark" aria-hidden="true">G</span>
    <span><strong>GigFlow</strong><small>Duna Session</small></span></a>
}

function PublicHeader() {
  return <header className="topbar"><Brand /><span className="public-label">Zenekari fellépésszervezés</span></header>
}

const toMessage = (reason: unknown) =>
  reason instanceof Error ? reason.message : 'Nem sikerült kapcsolódni a Spring Boot szerverhez.'

export default App
