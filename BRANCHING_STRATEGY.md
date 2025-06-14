# BRANCHING_STRATEGY.md

## Git Flow Branching Strategy

This repo follows the **Git Flow** branching model. It defines how feats, rels, and hotfixes are handled to keep code organized and production-ready.

---

## Branches Overview

| Branch      | Purpose                                  | From        | Merges into     |
|-------------|------------------------------------------|-------------|-----------------|
| `main`      | Production-ready, deployable code        | `rel-*`, `hotfix-*` | —         |
| `dev`   | Integration of completed feats         | `feat-*` | `rel-*`     |
| `feat-*` | Individual feats or tasks             | `dev`   | `dev`       |
| `rel-*` | Final polish before production rel   | `dev`   | `main`, `dev` |
| `hotfix-*`  | Critical fixes in production             | `main`      | `main`, `dev` |

---

## Branch Naming Conventions

| Type      | Format                    | Examples                        |
|-----------|---------------------------|---------------------------------|
| feature   | `feat-<name>` or `feature-<name>` | `feature-login-form`, `feat-add-auth` |
| Release      | `rel-v<version>`      | `rel-v1.2.0`, `rel-v2.0.1`  |
| hotfix    | `hotfix-<desc>` or `fix-<desc>` | `hotfix-crash-fix`, `fix-navbar` |

---

## Workflow Summary

1. Start feats → `dev` ➜ `feat-<name>`
2. Finish feat → Merge `feat-*` ➜ `dev`
3. Prepare rel → Create `rel-*` from `dev`
4. Finalize → Merge `rel-*` ➜ `main` & `dev`, tag `main`
5. Emergency fix → Create `hotfix-*` from `the branch`, merge back to both

---

## Diagram

```text

                           +-----------------+
                           |   Production    |
                           |      main       |
                           +---------+-------+
                                     ▲
                        +------------+------------+
                        |                         |
                  +-----+-----+             +-----+-----+
                  |   rel-*   |<------------|  hotfix-* |
                  +-----+-----+             +-----+-----+
                        ▲                         ▲
                        |                         |
                  +-----+-----+                   |
                  |   dev     |<------------------+
                  +-----+-----+
                        ▲
        +---------------+---------------+
        |               |               |
  +-----+-----+   +-----+-----+   +-----+-----+
  |   feat-*  |   |   feat-*  |   |   feat-*  |
  +-----------+   +-----------+   +-----------+

