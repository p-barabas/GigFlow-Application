import type { Availability, Member, Performance } from './types'

export const availabilityOptions: Array<{
  value: Availability
  label: string
  shortLabel: string
  icon: string
}> = [
  { value: 'ACCEPTED', label: 'Részt veszek', shortLabel: 'Igen', icon: '✓' },
  { value: 'TENTATIVE', label: 'Bizonytalan', shortLabel: 'Talán', icon: '?' },
  { value: 'DECLINED', label: 'Nem érek rá', shortLabel: 'Nem', icon: '×' },
]

const dateFormatter = new Intl.DateTimeFormat('hu-HU', {
  year: 'numeric',
  month: 'long',
  day: 'numeric',
  weekday: 'short',
})

const timeFormatter = new Intl.DateTimeFormat('hu-HU', {
  hour: '2-digit',
  minute: '2-digit',
})

const formatDate = (value: string) => {
  const date = new Date(value)
  return {
    day: dateFormatter.format(date),
    time: timeFormatter.format(date),
  }
}

type ResponseSummaryProps = {
  responses: Performance['responses']
  memberCount: number
}

function ResponseSummary({ responses, memberCount }: ResponseSummaryProps) {
  const counts = availabilityOptions.map((option) => ({
    ...option,
    count: Object.values(responses).filter((response) => response === option.value).length,
  }))
  const missing = memberCount - Object.keys(responses).length

  return (
    <div className="response-summary" aria-label="Válaszok összesítése">
      {counts.map((item) => (
        <span className={`summary-chip ${item.value.toLowerCase()}`} key={item.value}>
          <b>{item.icon}</b> {item.count}
        </span>
      ))}
      <span className="summary-chip missing"><b>·</b> {missing} nem válaszolt</span>
    </div>
  )
}

export type PerformanceCardProps = {
  performance: Performance
  members: Member[]
  activeMember?: Member
  onRespond?: (performanceId: string, availability: Availability) => void
}

export default function PerformanceCard({
  performance,
  members,
  activeMember,
  onRespond,
}: PerformanceCardProps) {
  const date = formatDate(performance.startsAt)
  const activeResponse = activeMember ? performance.responses[activeMember.id] : undefined
  const canRespond = Boolean(
    activeMember && onRespond && performance.invitedMemberIds.includes(activeMember.id),
  )
  const invitedMembers = performance.invitedMemberIds.map((memberId) => ({
    id: memberId,
    name: members.find((member) => member.id === memberId)?.name ?? memberId,
    response: performance.responses[memberId],
  }))

  return (
    <article className="event-card">
      <div className="event-accent" aria-hidden="true" />
      <div className="event-main">
        <div className="event-topline">
          <span className="draft-badge">TERVEZET</span>
          <span className="event-date">{date.day} · {date.time}</span>
        </div>
        <h3>{performance.title}</h3>
        <p className="venue"><span aria-hidden="true">⌖</span> {performance.venue}</p>
        <ResponseSummary responses={performance.responses} memberCount={performance.invitedMemberIds.length} />
      </div>

      {canRespond && activeMember && onRespond ? <div className="response-box">
        <p><strong>{activeMember.name}</strong>, számíthatunk rád?</p>
        <div className="response-buttons" role="group" aria-label={`${activeMember.name} válasza`}>
          {availabilityOptions.map((option) => (
            <button
              key={option.value}
              className={`response-button ${option.value.toLowerCase()} ${
                activeResponse === option.value ? 'selected' : ''
              }`}
              type="button"
              aria-pressed={activeResponse === option.value}
              onClick={() => onRespond(performance.id, option.value)}
              title={option.label}
            >
              <span aria-hidden="true">{option.icon}</span>
              {option.shortLabel}
            </button>
          ))}
        </div>
        <small>{activeResponse ? 'A választ bármikor módosíthatod.' : 'Még nem válaszoltál.'}</small>
      </div> : <div className="response-box response-info">
        {activeMember ? <>
          <p>{activeMember.name} nem kapott meghívást erre a fellépésre.</p>
          <small>Ehhez az eseményhez nem küldhető válasz.</small>
        </> : <>
          <p><strong>Vezetői áttekintés</strong></p>
          <ul className="member-responses" aria-label="Meghívott tagok válaszai">
            {invitedMembers.map((member) => {
              const option = availabilityOptions.find((item) => item.value === member.response)
              return <li key={member.id} className={member.response?.toLowerCase() ?? 'missing'}>
                <span>{member.name}</span>
                <strong>{option?.label ?? 'Még nem válaszolt'}</strong>
              </li>
            })}
          </ul>
        </>}
      </div>}
    </article>
  )
}
