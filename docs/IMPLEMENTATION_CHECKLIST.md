# StockQ implementation checklist

Internal execution checklist, reviewed 30 October 2026. The repository was clean on `main` at `8868818` before this pass. A Git tag checkpoint was attempted, but the workspace policy denied writes to `.git`; no history or user data was changed.

## Audit snapshot

- Existing Android app uses Kotlin, Compose, Room, KSP, coroutines, DataStore and a manual `AppContainer`; application ID and namespace are both `com.stockq`.
- Existing screens cover dashboard, catalog, product detail/edit, stock ledger, purchases, suppliers, supplier comparison, reorder, settings and backup navigation.
- Manifest currently declares no permissions, including INTERNET.
- Financial storage is mostly paise `Long`, but `Money` converts to `Double`; product quantities and stock ledger balances are `Double`.
- Room is schema version 1 with `exportSchema = false` and no migration path. Current entities cover profile, category, product, supplier, purchase, purchase item and stock movement.
- Existing unit tests cover arithmetic, stock engine, purchase management and supplier comparison. Reported earlier results are historical claims and must be rerun.
- Branding images, mipmap launcher resources and splash theme resources exist.
- Backup screen actions are placeholders, and its copy incorrectly claims encryption.
- Settings hardcodes the version and lacks the requested updater action; fixed in this pass using installed package metadata and an external browser intent.
- Planned customers, sales, quotations/orders, jobs, expenses, batches/expiry, employees/wages, reports and business documents are not present in the current schema/module tree.

## Remaining implementation sequence

1. Replace floating-point money/quantity paths and add deterministic validated arithmetic.
2. Enable Room schema export, add migration tests, and preserve transactional ledger invariants.
3. Implement tested SAF backup/restore with versioning and safe failure handling.
4. Extend relational models/repositories/UI in business-module slices: sales/customers/dues; quotations/orders; expenses/jobs; batches; employees; reports; documents/reminders.
5. Complete settings/preferences/localisation and document real privacy/data behaviour.
6. Add CI, run unit/instrumented checks and assemble; inspect merged manifest, package, icon and remaining placeholder actions.

Do not describe unimplemented modules as complete. Keep the no-network permission policy and manual browser-based update check.
