# Where tokens.json comes from

| Field | Value |
|---|---|
| Source | The 3ap Design System (Figma): pages Colors, Typography, Spacing |
| Export method | A read-only script over the Figma file, kept by the template maintainers, not in this repo. Output: W3C DTCG JSON |
| Exported on | 2026-10-01 |
| Exported by | Claude Code session for CAAS-1374, on request of the template owner |
| Changed since | 2026-10-05: the `$description` text only; no value changed |
| SHA-256 | `857a62c207e03732f088ef739674035294558a16b7e1fbf8c576df4ffe6eef04` (`shasum -a 256 design-tokens/tokens.json`) |

Tokens come from the export, never from transcription. A prototype never changes them: a new
export arrives with a template update. The template maintainers change the value in Figma, re-run
the export, replace the whole of `tokens.json` with its output, update this file and run
`make generate`. `make check` fails if `frontend/src/ui/tokens/` or
`backend/src/main/resources/platform/entry-tokens.css` (the subset the Spring-rendered `/gate` and
`/login` pages use) no longer matches `tokens.json`.

## Why a script and not a Figma Variables export

The design system has no Figma Variables. Only the 4 status colours are paint styles, and the type
scale is 27 text styles; every other colour is a painted swatch with a hex label, and spacing is a
table. The script reads each value from its source node and stops if a swatch's fill and its label
differ. Once the designers move the values into Figma Variables, a Variables export replaces it.

## What the design system says about using them

From the Colors page, for whoever builds `ui/`. `ui/theme.ts` already maps them onto MUI:

- Colours are for backgrounds and design elements only, never for text (text is `grayscale.black`
  or `grayscale.grey`).
- Status colours only for success and error messages, never for backgrounds or other elements.
- Hover and illustration shades are the colour plus 20% black: the `accent.*.shade` tokens.

## Not in the export

| Missing | Why | Until then |
|---|---|---|
| Font files | Figma holds the family name, not the files | `--font-family-base` falls back to `system-ui` (docs/open-points.md #19) |
| Corner radius, shadows | Not defined in the design system; the Elevation page is empty | Square corners and MUI's default shadows (docs/open-points.md #20) |
| Dark mode | The design system has one theme | Light only |