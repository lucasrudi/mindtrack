import { describe, expect, it } from 'vitest'
import { mount } from '@vue/test-utils'
import AppointmentCancelModal from '../AppointmentCancelModal.vue'

describe('AppointmentCancelModal', () => {
  it('renders an open dialog when visible', () => {
    const wrapper = mount(AppointmentCancelModal, {
      props: {
        show: true,
        isSeries: false,
      },
    })

    const dialog = wrapper.find('dialog')

    expect(dialog.exists()).toBe(true)
    expect(dialog.attributes('open')).toBeDefined()
    expect(dialog.attributes('aria-labelledby')).toBe('cancel-modal-title')
  })

  it('emits the selected scope on confirm', async () => {
    const wrapper = mount(AppointmentCancelModal, {
      props: {
        show: true,
        isSeries: true,
      },
    })

    await wrapper.find('input[value="ALL_IN_SERIES"]').setValue()
    await wrapper.find('.btn-danger').trigger('click')

    expect(wrapper.emitted('confirm')).toEqual([['ALL_IN_SERIES']])
  })
})
