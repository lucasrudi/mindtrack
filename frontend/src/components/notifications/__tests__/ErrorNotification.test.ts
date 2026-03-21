import { beforeEach, describe, expect, it } from 'vitest'
import { mount } from '@vue/test-utils'
import ErrorNotification from '../ErrorNotification.vue'
import { useErrorHandler } from '@/composables/useErrorHandler'

describe('ErrorNotification', () => {
  beforeEach(() => {
    const { clearAll } = useErrorHandler()
    clearAll()
  })

  it('renders and dismisses global errors', async () => {
    const { globalErrors } = useErrorHandler()
    globalErrors.value = [
      {
        id: 'error-1',
        message: 'Something went wrong',
        type: 'error',
        timestamp: Date.now(),
      },
      {
        id: 'warning-1',
        message: 'Check your settings',
        type: 'warning',
        timestamp: Date.now(),
      },
    ]

    const wrapper = mount(ErrorNotification)

    expect(wrapper.findAll('.toast')).toHaveLength(2)
    expect(wrapper.find('.toast--error').text()).toContain('Something went wrong')
    expect(wrapper.find('.toast--warning').text()).toContain('Check your settings')

    await wrapper.find('.toast__dismiss').trigger('click')

    expect(globalErrors.value).toHaveLength(1)
    expect(globalErrors.value[0].id).toBe('warning-1')
  })
})
