export function canAccessEndpoint(endpointScope: string, userScopes?: string): boolean {
  if (!userScopes) return false
  const scopes = userScopes.split(',').map((s) => s.trim().toUpperCase())
  if (scopes.includes('ADMIN')) return true
  if (scopes.includes('READER') && scopes.includes('WRITER')) return true
  if (endpointScope.toUpperCase() === 'READER' && scopes.includes('READER')) return true
  if (endpointScope.toUpperCase() === 'WRITER' && scopes.includes('WRITER')) return true
  return false
}

export function hasScope(scope: string, userScopes?: string): boolean {
  if (!userScopes) return false
  return userScopes.split(',').map((s) => s.trim().toUpperCase()).includes(scope.toUpperCase())
}
