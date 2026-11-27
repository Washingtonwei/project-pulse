// Artifact keys in the browser: the type's prefix, the key format, and the order keys are shown in. The server is the
// authority (ArtifactKeys and ArtifactKeyPrefix in the backend, design: docs/design/art.md, "Artifact keys"); these
// copies let the key field show the prefix and catch a malformed slug before any request. If the two ever drift, the
// server refuses the key (4a), so a drift is a refusal, never a stored wrong key.

// One prefix per type, entry for entry the backend's ArtifactKeyPrefix.
export const ARTIFACT_KEY_PREFIX: Record<string, string> = {
  GLOSSARY_TERM: 'GLO',
  FUNCTIONAL_REQUIREMENT: 'FR',
  USE_CASE: 'UC',
  BUSINESS_OBJECTIVE: 'BO',
  BUSINESS_PROBLEM: 'BP',
  BUSINESS_OPPORTUNITY: 'BOP',
  SUCCESS_METRIC: 'SM',
  RISK: 'RI',
  BUSINESS_ASSUMPTION: 'BAS',
  BUSINESS_DEPENDENCY: 'BDE',
  ASSUMPTION: 'AS',
  DEPENDENCY: 'DE',
  STAKEHOLDER: 'STK',
  EXTERNAL_SYSTEM: 'SYS',
  BUSINESS_RULE: 'BR',
  FEATURE: 'FEAT',
  PRECONDITION: 'PRE',
  POSTCONDITION: 'POST',
  USER_STORY: 'US',
  QUALITY_ATTRIBUTE: 'QA',
  USER_INTERFACE: 'UI',
  SOFTWARE_INTERFACE: 'SI',
  HARDWARE_INTERFACE: 'HI',
  COMMUNICATIONS_INTERFACE: 'CI',
  OPERATING_ENVIRONMENT: 'OE',
  CONSTRAINT: 'CO',
  DATA_REQUIREMENT: 'DI',
  KEY_DECISION: 'KD',
  QUALITY_SCENARIO: 'QS',
  TECHNICAL_RISK: 'TRI',
  TECHNICAL_DEBT: 'TD'
}

export const MAX_SLUG_LENGTH = 40

const SLUG = /^[a-z][a-z0-9]*(-[a-z0-9]+)*$/

// "FUNCTIONAL_REQUIREMENT" -> "FR"; an unknown or empty type (none chosen yet) -> "".
export function prefixOf(type: string | null | undefined): string {
  return (type && ARTIFACT_KEY_PREFIX[type]) || ''
}

// The slug of a key: "FR-export-csv" -> "export-csv". A key with another prefix, or none, gives "".
export function slugOf(key: string | null | undefined, type: string | null | undefined): string {
  const prefix = prefixOf(type)
  return key && prefix && key.startsWith(prefix + '-') ? key.slice(prefix.length + 1) : ''
}

// What is wrong with a slug, written for the student, or null. "export-csv" -> null; "" -> "Give it a name."; "Export" ->
// the format rule. The same rule the server applies, so a key the field accepts the server accepts too.
export function slugProblem(slug: string): string | null {
  if (!slug) return 'Give it a name.'
  if (slug.length > MAX_SLUG_LENGTH) return `At most ${MAX_SLUG_LENGTH} characters.`
  if (!SLUG.test(slug))
    return 'Use lowercase letters, digits and single hyphens, starting with a letter.'
  return null
}

// Keys in alphabetical order, a missing key last: the prefix groups them by type, the slug orders them within it.
// A plain comparison, not localeCompare, so the order is the server's (ArtifactKeys.KEY_ORDER) character for character.
export function compareArtifactKeys(
  a: string | null | undefined,
  b: string | null | undefined
): number {
  if (!a) return b ? 1 : 0
  if (!b) return -1
  return a < b ? -1 : a > b ? 1 : 0
}
