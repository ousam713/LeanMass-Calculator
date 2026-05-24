#!/bin/bash

# ============================================================
# Git History Rewrite Script
# Project: LBM Calculator (Lean Body Mass) - Android Kotlin
# Period: 22 May 2026 → 24 May 2026 (Morocco Timezone)
# ============================================================

set -e

# ── Colors ──────────────────────────────────────────────────
GREEN='\033[0;32m'
CYAN='\033[0;36m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m'

log()  { echo -e "${CYAN}[INFO]${NC} $1"; }
ok()   { echo -e "${GREEN}[OK]${NC}   $1"; }
warn() { echo -e "${YELLOW}[WARN]${NC} $1"; }
err()  { echo -e "${RED}[ERR]${NC}  $1"; exit 1; }

# ── Safety check ────────────────────────────────────────────
if [ ! -d ".git" ]; then
  err "Run this script from the root of your git repository."
fi

warn "This will REWRITE your entire git history."
warn "Make sure you have a backup or are okay with a force-push."
read -rp "Continue? (yes/no): " CONFIRM
[[ "$CONFIRM" != "yes" ]] && { log "Aborted."; exit 0; }

# ── Config ──────────────────────────────────────────────────
AUTHOR_NAME="$(git config user.name)"
AUTHOR_EMAIL="$(git config user.email)"

if [[ -z "$AUTHOR_NAME" || -z "$AUTHOR_EMAIL" ]]; then
  err "Git user.name or user.email not set. Run:\n  git config user.name 'Your Name'\n  git config user.email 'you@example.com'"
fi

log "Author: $AUTHOR_NAME <$AUTHOR_EMAIL>"

# ── Helper: generate random time between 08:00 and 20:00 ────
random_time() {
  local HOUR=$(( RANDOM % 13 + 8 ))  # 8 to 20
  local MINUTE=$(( RANDOM % 60 ))
  local SECOND=$(( RANDOM % 60 ))
  printf "%02d:%02d:%02d" $HOUR $MINUTE $SECOND
}

# ── Helper: make a commit with a fake date ───────────────────
commit_files() {
  local DATE="$1"
  local TIME="$2"
  local MSG="$3"
  shift 3
  local FILES=("$@")

  local DATETIME="$DATE $TIME"
  
  local EXISTING=()
  for f in "${FILES[@]}"; do
    if [ -e "$f" ]; then
      EXISTING+=("$f")
    else
      warn "Skipping (not found): $f"
    fi
  done

  if [ ${#EXISTING[@]} -eq 0 ]; then
    warn "No files found for commit: '$MSG' — skipping."
    return
  fi

  git add "${EXISTING[@]}"

  if git diff --cached --quiet; then
    warn "Nothing staged for: '$MSG' — skipping."
    return
  fi

  GIT_AUTHOR_DATE="$DATETIME +0100" \
  GIT_COMMITTER_DATE="$DATETIME +0100" \
  GIT_AUTHOR_NAME="$AUTHOR_NAME" \
  GIT_AUTHOR_EMAIL="$AUTHOR_EMAIL" \
  GIT_COMMITTER_NAME="$AUTHOR_NAME" \
  GIT_COMMITTER_EMAIL="$AUTHOR_EMAIL" \
  git commit -m "$MSG"

  ok "[$DATETIME +0100] $MSG"
}

# ============================================================
# STEP 1 — Wipe history (orphan branch trick)
# ============================================================
log "Creating clean orphan branch..."
git checkout --orphan clean-history
git rm -rf . --cached > /dev/null 2>&1 || true

# ============================================================
# STEP 2 — Commits over 22–24 May 2026
# ============================================================
log "Creating commits with random times between 08:00 and 20:00 (Morocco timezone)"

# Base path for your Kotlin files
BASE_PATH="app/src/main/java/com/leanmass/calculator"

# ── 22 May 2026 ────────────────────────────────────────────
log "Day 1: 22 May 2026"

commit_files "2026-05-22" "$(random_time)" \
  "feat(db): add DatabaseHelper for LBM data storage" \
  "$BASE_PATH/database/DatabaseHelper.kt"

commit_files "2026-05-22" "$(random_time)" \
  "feat(auth): add LoginActivity" \
  "$BASE_PATH/auth/LoginActivity.kt"

commit_files "2026-05-22" "$(random_time)" \
  "feat(auth): add RegisterActivity" \
  "$BASE_PATH/auth/RegisterActivity.kt"

# ── 23 May 2026 ────────────────────────────────────────────
log "Day 2: 23 May 2026"

commit_files "2026-05-23" "$(random_time)" \
  "feat(calc): implement Lean Body Mass calculation logic" \
  "$BASE_PATH/calculator/CalculatorActivity.kt"

commit_files "2026-05-23" "$(random_time)" \
  "feat(history): add HistoryActivity to display past calculations" \
  "$BASE_PATH/history/HistoryActivity.kt"

# ── 24 May 2026 ────────────────────────────────────────────
log "Day 3: 24 May 2026"

commit_files "2026-05-24" "$(random_time)" \
  "feat(history): add HistoryAdapter for RecyclerView" \
  "$BASE_PATH/history/HistoryAdapter.kt"

# Catch any remaining files (layouts, manifests, resources)
REMAINING=$(git ls-files --others --exclude-standard | grep -v "^git_logs.txt$" || true)
if [ -n "$REMAINING" ]; then
  warn "Found untracked files — adding in final commit:"
  echo "$REMAINING"
  commit_files "2026-05-24" "$(random_time)" \
    "chore: add remaining resources (layouts, manifests, gradle files)" \
    $REMAINING
fi

# ============================================================
# STEP 3 — Rename branch and wrap up
# ============================================================
log "Renaming branch to 'main'..."
git branch -m main

echo ""
ok "✅ History rewritten with $(git rev-list --count HEAD) clean commits."
echo ""
log "Preview your new log:"
git log --oneline --graph
echo ""
warn "To push to remote (force-push):"
echo "  git push origin main --force"
