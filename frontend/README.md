# IRES — Intelligent Requirement Engineering System (Frontend)

A modern, responsive frontend for IRES, built with **React + Vite** and **Tailwind CSS**,
featuring a live animated **Aurora AI** background, glassmorphism UI, **Framer Motion**
transitions, and five fully functional role-based dashboards. There's still no real backend —
but registration, login, validation and session persistence all genuinely work against a mock
user store in `localStorage`, so the whole flow can be clicked through end-to-end.

## Pages & flow

1. **Landing (`/`)** — Navbar, hero, 6 animated feature cards, About section, footer.
2. **Register (`/register`)** — Full Name, Email, Password, Confirm Password, Role. Validated
   client-side (required fields, email format, password strength, matching confirmation,
   duplicate-email check). On success, the account is saved and you're logged straight in.
3. **Login (`/login`)** — Email + password, checked against the same mock user store.
   Wrong email/password shows a real inline error. A **demo accounts** panel at the bottom
   lets you one-click fill credentials for each role (see below) — the app also seeds these
   automatically on first run so login works with a totally empty browser.
4. **Role-based dashboards** — after login/register you're routed straight to:
   - `/admin/dashboard`
   - `/analyst/dashboard` (Business Analyst)
   - `/client/dashboard`
   - `/developer/dashboard`
   - `/tester/dashboard`

   Every dashboard route is wrapped in `ProtectedRoute`: if you're not logged in you're sent to
   `/login`; if you're logged in as the *wrong* role for that route, you're sent to
   `/unauthorized` (a 403 page) instead of the dashboard. The session (`{ name, email, role }`)
   is stored in `localStorage` and restored automatically on refresh. **Log out** (bottom of
   every sidebar) clears it and returns you to `/login`.

## Demo accounts (seeded automatically)

| Role | Email | Password |
|---|---|---|
| Admin | admin@ires.ai | Admin123 |
| Business Analyst | analyst@ires.ai | Analyst123 |
| Client | client@ires.ai | Client123 |
| Developer | developer@ires.ai | Developer123 |
| Tester | tester@ires.ai | Tester123 |

Registering a new account through `/register` adds it to the same store — it'll show up in the
Admin dashboard's **Users & Roles** table after a refresh.

## What each dashboard actually does

All five share the same sidebar + topbar shell (`DashboardLayout`), swap sections in place
(no extra routes needed), and reuse the same glass/gradient theme.

- **Admin** — system stats, role-distribution bar chart, recent activity, a real table of
  registered users (with CSV export), and a requirements table (CSV export).
- **Client** — project cards with progress bars, a **Submit Requirement** form (rich-text
  description, category/project selects, drag-and-drop file upload for PDF/DOCX/CSV, *and* a
  CSV bulk-importer that parses a `.csv` of requirements with PapaParse and previews it before
  import — a template CSV can be downloaded from the same screen), AI suggestions, reports,
  notifications, profile.
- **Business Analyst** — requirement management (search, filter, approve/reject, comment —
  all stateful), AI analysis breakdown, user story generator, a traceability matrix, and an
  **SRS Generator** that builds a real multi-page PDF (via jsPDF) from the current
  requirements/user stories/traceability data and downloads it.
- **Developer** — assigned projects, requirement details, a **drag-and-drop Kanban board**
  (native HTML5 DnD, no extra library) for tasks, a development-status pie chart + progress
  bars, and a milestone timeline.
- **Tester** — assigned projects, test cases (CSV export), a Testing Progress Kanban board,
  a full **bug reporting form** (title, severity, status, assigned developer, screenshot
  upload) with a live bug list, and pass/fail + bug-severity charts.

## CSV support

- **Import**: `src/components/CsvImporter.jsx` uses PapaParse to parse an uploaded `.csv`
  (used on the Client "Submit Requirements" screen), previews the parsed rows in a table, and
  only commits them on "Import all".
- **Export**: `src/utils/csv.js` (`exportToCsv(filename, rows)`) builds a CSV client-side and
  triggers a real browser download — wired up on the Admin users/requirements tables, the
  Business Analyst requirements table, and the Tester test-case table.

## Getting started

```bash
npm install
npm run dev
```

Then open the printed local URL (defaults to `http://localhost:5173`).

To build for production:

```bash
npm run build
npm run preview
```

## Project structure

```
src/
  assets/            static assets
  components/        shared UI pieces
    dashboard/        ProfilePanel, NotificationsPanel, ReportsPanel (reused by every dashboard)
  context/           AuthContext.jsx — mock session, persisted to localStorage
  data/              per-role mock datasets (clientData.js, analystData.js, developerData.js, testerData.js, mockData.js for admin)
  layouts/           MainLayout, AuthLayout, DashboardLayout
  pages/
    dashboards/        AdminDashboard, AnalystDashboard, ClientDashboard, DeveloperDashboard, TesterDashboard
    Landing.jsx, Login.jsx, Register.jsx, Unauthorized.jsx
  utils/             validators.js, userStore.js, roleRoutes.js, csv.js, srsPdf.js
  App.jsx            routes + route protection + page transitions
  main.jsx           app entry point, seeds demo accounts, wraps AuthProvider
  index.css          Tailwind directives + shared utility classes (glass, gradients, buttons)
```

## Theme

The **AuroraBackground** component (`src/components/AuroraBackground.jsx`) is the signature
visual: slow-drifting blurred gradient blobs (blue → indigo → purple → cyan) behind a faint
circuit grid, with a scanning highlight line. It's rendered once in `App.jsx` and sits fixed
behind every page. Typography: **Poppins** (headings) + **Inter** (body). Icons:
**lucide-react**. Charts: **recharts**. PDF generation: **jsPDF**. CSV parsing: **PapaParse**.

## Honest limitations

- There is still no real backend. `userStore.js` keeps accounts in `localStorage` in plain
  text — that's fine for demoing the UI/UX, but is **not** how auth should work in production
  (use a real API with hashed passwords and JWTs/sessions).
- File uploads (attachments, screenshots) are accepted and listed in the UI but not actually
  sent anywhere.
- All dashboard data starts from static mock datasets in `src/data/`; interactions (approve,
  drag a Kanban card, submit a bug, generate a user story) update local component state so the
  UI feels real, but nothing is persisted beyond the current page session except the CSV
  exports and the SRS PDF, which are real files.
