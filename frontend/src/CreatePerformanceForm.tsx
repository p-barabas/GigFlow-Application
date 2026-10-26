import { useRef, useState, type FormEvent } from 'react'
import { createSubmissionGate } from './submissionGate'

export type PerformanceDraft = {
  title: string
  venue: string
  startsAt: string
}

export type CreatePerformanceFormProps = {
  onCreate: (draft: PerformanceDraft) => Promise<void>
}

type FormError = {
  field: 'title' | 'startsAt' | 'venue' | 'form'
  message: string
}

function CreatePerformanceForm({ onCreate }: CreatePerformanceFormProps) {
  const [title, setTitle] = useState('')
  const [startsAt, setStartsAt] = useState('')
  const [venue, setVenue] = useState('')
  const [formError, setFormError] = useState<FormError | null>(null)
  const [submitting, setSubmitting] = useState(false)
  const submissionGate = useRef(createSubmissionGate()).current

  const handleSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault()

    const trimmedTitle = title.trim()
    const trimmedVenue = venue.trim()

    if (!trimmedTitle) {
      setFormError({
        field: 'title',
        message: 'Adj meg egy eseménynevet — a csak szóközből álló cím nem érvényes.',
      })
      return
    }
    if (!startsAt) {
      setFormError({
        field: 'startsAt',
        message: 'Add meg az esemény dátumát és kezdési időpontját.',
      })
      return
    }
    if (!trimmedVenue) {
      setFormError({ field: 'venue', message: 'Add meg a helyszínt.' })
      return
    }

    await submissionGate.run(async () => {
      setSubmitting(true)
      setFormError(null)
      try {
        await onCreate({ title: trimmedTitle, startsAt, venue: trimmedVenue })
        setTitle('')
        setStartsAt('')
        setVenue('')
      } catch (reason) {
        setFormError({
          field: 'form',
          message: reason instanceof Error
            ? reason.message
            : 'A fellépést nem sikerült elmenteni. Próbáld újra.',
        })
      } finally {
        setSubmitting(false)
      }
    })
  }

  return (
    <form
      onSubmit={handleSubmit}
      noValidate
      aria-busy={submitting}
      aria-describedby={formError?.field === 'form' ? 'form-error' : undefined}
    >
      <label htmlFor="title">Esemény neve</label>
      <input
        id="title"
        name="title"
        value={title}
        onChange={(event) => setTitle(event.target.value)}
        placeholder="például Téli kamarazene"
        aria-describedby={formError?.field === 'title' ? 'title-error' : undefined}
        aria-invalid={formError?.field === 'title'}
      />

      <label htmlFor="startsAt">Dátum és kezdés</label>
      <input
        id="startsAt"
        name="startsAt"
        type="datetime-local"
        value={startsAt}
        onChange={(event) => setStartsAt(event.target.value)}
        aria-describedby={formError?.field === 'startsAt' ? 'startsAt-error' : undefined}
        aria-invalid={formError?.field === 'startsAt'}
      />

      <label htmlFor="venue">Helyszín</label>
      <input
        id="venue"
        name="venue"
        value={venue}
        onChange={(event) => setVenue(event.target.value)}
        placeholder="például Bartók Terem"
        aria-describedby={formError?.field === 'venue' ? 'venue-error' : undefined}
        aria-invalid={formError?.field === 'venue'}
      />

      {formError && (
        <p
          className="form-error"
          id={`${formError.field}-error`}
          role="alert"
          aria-live="assertive"
        >
          {formError.message}
        </p>
      )}

      <p className="sr-only" role="status" aria-live="polite">
        {submitting ? 'A fellépés mentése folyamatban.' : ''}
      </p>

      <button className="primary-button" type="submit" disabled={submitting}>
        <span>{submitting ? 'Mentés a szerverre…' : 'Fellépés létrehozása'}</span>
        <span aria-hidden="true">→</span>
      </button>
    </form>
  )
}

export default CreatePerformanceForm
