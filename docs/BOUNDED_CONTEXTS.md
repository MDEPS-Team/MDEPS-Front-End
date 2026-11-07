# Vantage PMO — Bounded Contexts

| Context | Package | Responsibility |
|---------|---------|----------------|
| **IAM** | `iam/` | Authentication, profile, biometric gate |
| **PTO** | `pto/` | Projects, tasks, portfolio home, PDF export, evidence capture |
| **GRO** | `gro/` | Risks, milestones, governance screens, background milestone checks |
| **ASS** | `ass/` | Portfolio analytics KPIs aggregating PTO + GRO flows |

## Integration points

- `MainActivity` hosts tab navigation and wires shared `GroViewModel` instances keyed by `projectId`.
- `ProjectDetailScreen` combines PTO task hub with GRO attention chips and quick actions.
- `AnalyticsViewModel` reads project, risk, and milestone repositories for cross-context KPIs.

## Data

- PTO: Room (`PtoDatabase`)
- GRO: Room (`GroDatabase`)
- IAM / onboarding: DataStore preferences
