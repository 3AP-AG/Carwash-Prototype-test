One file per backlog item: `D-01.spec.ts`, tagged `@D-01`, one test per acceptance bullet.
A PR may add or change only the tests of the item in flight. The CI check reads the backlog ID from
the tag, not the file name (CAAS-1390); the file name is a convention that keeps items apart.
Follow the shape of `e2e/exemplars/examples/`.
