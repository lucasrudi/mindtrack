import { ref } from 'vue'

export interface AppError {
  id: string
  message: string
  type: 'error' | 'warning' | 'info'
  timestamp: number
}

const globalErrors = ref<AppError[]>([])
let errorCounter = 0

export function useErrorHandler() {
  function addError(message: string, type: AppError['type'] = 'error') {
    const id = globalThis.crypto?.randomUUID?.() ?? `${Date.now()}-${++errorCounter}`
    const error: AppError = { id, message, type, timestamp: Date.now() }
    globalErrors.value.push(error)

    setTimeout(() => {
      dismissError(id)
    }, 8000)
  }

  function dismissError(id: string) {
    const index = globalErrors.value.findIndex((e) => e.id === id)
    if (index !== -1) {
      globalErrors.value.splice(index, 1)
    }
  }

  function clearAll() {
    globalErrors.value = []
  }

  return { globalErrors, addError, dismissError, clearAll }
}
