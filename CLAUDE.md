## Project Overview

ReadLog is an Android application for book tracking and logging. It follows a professional-grade technical stack with Clean Architecture, offline-first persistence, and Jetpack Compose UI.

## Architecture

- **Pattern**: Clean Architecture (3 modules)
  - `ui` — Presentation layer (Jetpack Compose screens, ViewModels)
  - `domain` — Business logic, use cases, and repositories interfaces
  - `data` — Data sources, repositories implementations, and mappers
- **Goal**: Testability and clear separation of concerns.

## Tech Stack

| Concern | Technology |
|---------|-----------|
| UI | Jetpack Compose |
| Architecture | Clean Architecture (ui / domain / data modules) |
| Local Persistence | Room (SQLite) — offline-first for collections and reading sessions |
| Cloud Persistence | Firebase (Firestore / Realtime DB) |
| Authentication | Firebase Auth with Google Sign-In |
| Book Metadata API | Open Library API (ISBN / barcode lookup) |
| Monitoring | Firebase Crashlytics |
| Analytics | Firebase Analytics |
| Push Notifications | Firebase Cloud Messaging (FCM) |
| Widgets | Jetpack Glance |

## Offline-First Strategy

- Collections and reading sessions must work **without network connectivity**.
- Room is the source of truth locally; Firebase syncs in the background when online.
- All write operations target Room first, then eagerly push to Firestore if the user is signed in.
- On login, a full two-way sync runs: **download cloud data first** (newer `lastModified` wins), then upload any local-only or newer records.
- Every syncable entity (`Book`, `Session`, `Annotation`) carries a stable `remoteId` (UUID) and a `lastModified` timestamp for conflict resolution.

## Testing

- **Unit tests** — Domain logic and ViewModels.
- **UI tests** — Compose screen interactions.
- **End-to-end tests** — Critical user flows (e.g., add book, start session, sync).

### Testing Strategy

- Use **mockk** for all unit test doubles (mocks/stubs) across `domain` and `ui` modules.
- Do not create hand-written `Fake…` classes unless the fake encodes complex business logic that is simpler to maintain as a real implementation.
- Avoid duplicating fake classes across modules — a mocked interface can be reused in any test.
- When you add a new class that contains logic (use case, mapper, repository, ViewModel, etc.), add unit tests for it in the same PR.
- After modifying existing logic (refactoring, bug fixes, feature changes), review test coverage and update or add tests if behavior changed or new paths were introduced.
- Aim to keep the test suite green; if a change breaks existing tests, fix them immediately.

## Code Style & Conventions

- Kotlin-only codebase.
- Prefer dependency injection (likely Hilt/Koin; verify `build.gradle` if unsure).
- Repository pattern in `data` module; use cases in `domain` module.
- Compose UI should be stateless where possible, hoisting state to ViewModels.
- Use Kotlin Coroutines / Flow for async operations and reactive data streams.
- Every string must be defined in `strings.xml` and provided in **Spanish and English**.
- **English is the main language** of the app and default resources.

### Mapping Strategy

- **Never** write inline extension-function mappers inside repository implementations (e.g., `private fun Entity.toDomain(): DomainModel`).
- Every bidirectional mapping between a **data-layer entity** and a **domain-layer model** must live in a dedicated mapper class:
  - Define an interface (e.g., `BookDataMapper`) with `toDomain()` and `toEntity()` methods.
  - Provide a concrete implementation (e.g., `BookDataMapperImpl`) annotated with `@Inject constructor()`.
  - Bind the interface to its implementation in the DI module (`DataModule`).
- Inject the mapper interface into the repository constructor.
- Create mapper classes even for mappings that have no repository yet (e.g., `Session`, `Annotation`) so the codebase stays consistent.
- Mappers are an explicit part of the `data` module. The `domain` module must never contain mapping logic that references Room entities, Firebase SDK classes, or any other data-layer type.
- Mapper implementations are pure logic; unit test them directly with real objects rather than mocks.
- Firestore mappings (e.g., `BookFirestoreMapper`) follow the same dedicated-class pattern: interface + impl + DI binding in `DataModule`.

## Firebase Services

Ensure Firebase initialization (`google-services.json`) is present. Used services:
- Auth (Google)
- Crashlytics
- Analytics
- Cloud Messaging
- Cloud data sync (**Firestore** — active for Books, Sessions, Annotations)

## Widget

- Home screen widget built with **Jetpack Glance**.
- Keep widget data source aligned with the same Room/Firebase hybrid model.

## API Integration

- **Open Library API** for fetching book metadata by ISBN or barcode.
- Handle API failures gracefully; cache results in Room.

## Important Notes for Agents

- Always respect the **module boundaries**: `ui` → `domain` → `data`.
- Never access Room or Firebase directly from the `ui` module.
- Maintain offline-first behavior when modifying collections or sessions.
- When adding new features, include only unit tests. UI tests and E2E tests will be added later.
- If tests fail, try to fix them.
- Mappers are an explicit part of the `data` module. The `domain` module must never contain mapping logic that references Room entities, Firebase SDK classes, or any other data-layer type.
