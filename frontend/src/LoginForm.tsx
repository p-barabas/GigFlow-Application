import { useState, type FormEvent } from 'react'
import type { Credentials } from './types'

type LoginFormProps = {
  onLogin: (credentials: Credentials) => Promise<void>
}

export default function LoginForm({ onLogin }: LoginFormProps) {
  const [username, setUsername] = useState('anna')
  const [password, setPassword] = useState('')
  const [error, setError] = useState('')
  const [submitting, setSubmitting] = useState(false)

  const submit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault()
    if (!username.trim() || !password) {
      setError('Add meg a felhasználónevet és a jelszót.')
      return
    }
    setSubmitting(true)
    setError('')
    try {
      await onLogin({ username: username.trim(), password })
      setPassword('')
    } catch (reason) {
      setError(reason instanceof Error ? reason.message : 'A bejelentkezés sikertelen.')
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <main className="login-page">
      <section className="login-card" aria-labelledby="login-title">
        <p className="eyebrow">BIZTONSÁGOS DEMÓ</p>
        <h1 id="login-title">Belépés a GigFlow-ba</h1>
        <p>A saját felhasználóddal válaszolhatsz a meghívásokra. A zenekarvezető új fellépést rögzíthet.</p>
        <form onSubmit={submit} aria-busy={submitting}>
          <label htmlFor="username">Felhasználónév</label>
          <input id="username" autoComplete="username" value={username}
            onChange={(event) => setUsername(event.target.value)} />
          <label htmlFor="password">Jelszó</label>
          <input id="password" type="password" autoComplete="current-password" value={password}
            onChange={(event) => setPassword(event.target.value)} />
          {error && <p className="form-error" role="alert">{error}</p>}
          <button className="primary-button" disabled={submitting} type="submit">
            <span>{submitting ? 'Ellenőrzés…' : 'Bejelentkezés'}</span><span aria-hidden="true">→</span>
          </button>
        </form>
        <div className="demo-users">
          <strong>Demó felhasználók</strong>
          <span>anna, bence, csilla · zenekari tag</span>
          <span>leader · zenekarvezető</span>
        </div>
      </section>
    </main>
  )
}
