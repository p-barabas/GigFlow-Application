export type Availability = 'ACCEPTED' | 'DECLINED' | 'TENTATIVE'

export type Member = {
  id: string
  name: string
  instrument: string
  initials: string
}

export type PerformanceResponse = {
  id: string
  title: string
  startsAt: string
  venue: string
  invitedMemberIds: string[]
  responses: Record<string, Availability>
}

export type Performance = PerformanceResponse

export type UserRole = 'MEMBER' | 'LEADER'

export type UserSession = {
  username: string
  displayName: string
  memberId: string | null
  roles: UserRole[]
}

export type Credentials = {
  username: string
  password: string
}
