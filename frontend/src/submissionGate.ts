export const createSubmissionGate = () => {
  let active = false

  return {
    run: async (operation: () => Promise<void>): Promise<boolean> => {
      if (active) return false
      active = true
      try {
        await operation()
        return true
      } finally {
        active = false
      }
    },
  }
}
