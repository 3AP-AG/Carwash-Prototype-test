Failures-only reporters: print each failure and one summary line, nothing per
passing test. Used by `vitest.gate.config.ts` and `e2e/playwright.config.ts`.
TODO(CAAS-1383): use a built-in reporter if one prints failures only; otherwise a ~20-line custom one here.
The same output feeds the gates part of the failure summary (docs/platform-contract.md §4).
