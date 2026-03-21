import { describe, expect, it } from 'vitest'
import { mount } from '@vue/test-utils'
import AppointmentCancelModal from '../AppointmentCancelModal.vue'

describe('AppointmentCancelModal', () => {
  it('does not render when hidden', () => {
    const wrapper = mount(AppointmentCancelModal, {
      props: {
        show: false,
        isSeries: false,
      },
    })

    expect(wrapper.find('dialog').exists()).toBe(false)
  })

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

  it('emits cancel when keep is clicked', async () => {
    const wrapper = mount(AppointmentCancelModal, {
      props: {
        show: true,
        isSeries: false,
      },
    })

    await wrapper.find('.btn-secondary').trigger('click')

    expect(wrapper.emitted('cancel')).toEqual([[]])
  })

  it('emits cancel on dialog cancel', async () => {
    const wrapper = mount(AppointmentCancelModal, {
      props: {
        show: true,
        isSeries: false,
      },
    })

    await wrapper.find('dialog').trigger('cancel')

    expect(wrapper.emitted('cancel')).toEqual([[]])
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

  it('defaults to single cancellation for one-off appointments', async () => {
    const wrapper = mount(AppointmentCancelModal, {
      props: {
        show: true,
        isSeries: false,
      },
    })

    expect(wrapper.findAll('.scope-option')).toHaveLength(0)

    await wrapper.find('.btn-danger').trigger('click')

    expect(wrapper.emitted('confirm')).toEqual([['SINGLE']])
  })
})
