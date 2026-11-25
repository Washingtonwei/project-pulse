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
  RISK: 'RSK',
  BUSINESS_ASSUMPTION: 'BAS',
  BUSINESS_DEPENDENCY: 'BDE',
  ASSUMPTION: 'ASM',
  DEPENDENCY: 'DEP',
  STAKEHOLDER: 'STK',
  EXTERNAL_SYSTEM: 'SYS',
  BUSINESS_RULE: 'RULE',
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
  CONSTRAINT: 'CON',
  DATA_REQUIREMENT: 'DR'
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

// Keys in the order a student reads them: by prefix, then with numbers compared as numbers, so FR-step-2 comes before
// FR-step-10; a missing key last. The backend's ArtifactKeys.NATURAL_ORDER sorts the same way.
export function compareArtifactKeys(
  a: string | null | undefined,
  b: string | null | undefined
): number {
  if (!a) return b ? 1 : 0
  if (!b) return -1
  return a.localeCompare(b, 'en', { numeric: true })
}
