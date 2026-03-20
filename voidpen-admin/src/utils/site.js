const DEFAULT_FRONTEND_BASE_URL = 'http://localhost:5173'

function trimTrailingSlash(url) {
  return String(url || '').replace(/\/+$/, '')
}

export function getFrontendBaseUrl() {
  const envUrl = trimTrailingSlash(import.meta.env.VITE_FRONTEND_BASE_URL)
  if (envUrl) {
    return envUrl
  }
  return DEFAULT_FRONTEND_BASE_URL
}

export function resolveFrontendUrl(path = '/') {
  const normalizedPath = path.startsWith('/') ? path : `/${path}`
  return `${getFrontendBaseUrl()}${normalizedPath}`
}
