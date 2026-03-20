import { reactive } from 'vue'

const toastState = reactive({
  items: [],
})

let seed = 0

function removeToast(id) {
  const index = toastState.items.findIndex((item) => item.id === id)
  if (index >= 0) {
    toastState.items.splice(index, 1)
  }
}

function pushToast(message, type = 'info', duration = 2600) {
  if (!message) {
    return
  }
  const id = ++seed
  toastState.items.push({ id, message, type })
  window.setTimeout(() => removeToast(id), duration)
}

export { toastState, pushToast, removeToast }
