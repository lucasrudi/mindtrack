#!/usr/bin/env bash
# create-github-issues.sh — Creates GitHub Issues for all MindTrack backlog tasks.
# Requires: gh CLI authenticated (gh auth login)
# Usage: bash scripts/create-github-issues.sh [--dry-run]
#
# Open issues are created for To Do tasks.
# Closed issues are created for Done tasks.

set -e

DRY_RUN=false
if [[ "${1}" == "--dry-run" ]]; then
    DRY_RUN=true
    echo "=== DRY RUN — no issues will be created ==="
fi

# Label constants to avoid duplicated literals
LABEL_BACKEND_ENH="backend,enhancement"
LABEL_BACKEND_FE_ENH="backend,frontend,enhancement"
LABEL_FE_ENH="frontend,enhancement"
LABEL_DEVOPS="devops"
LABEL_DEVOPS_ENH="devops,enhancement"
LABEL_SECURITY="security,devops,enhancement"
LABEL_INFRA="infra,backend,devops,enhancement"
STATE_CLOSED="closed"
STATE_OPEN="open"

if ! command -v gh &>/dev/null; then
    echo "Error: gh CLI is not installed. Install from https://cli.github.com/" >&2
    exit 1
fi

gh_issue() {
    local title="$1"
    local body="$2"
    local labels="$3"
    local state="$4"  # open or closed

    if $DRY_RUN; then
        echo "  [DRY RUN] Would create issue: $title (state: $state, labels: $labels)"
        return
    fi

    issue_url=$(gh issue create \
        --title "$title" \
        --body "$body" \
        --label "$labels" 2>/dev/null)
    echo "  Created: $issue_url"

    if [[ "$state" == "closed" ]]; then
        issue_number=$(echo "$issue_url" | grep -o '[0-9]*$')
        gh issue close "$issue_number" --comment "Completed and merged to main." 2>/dev/null
        echo "  Closed:  #$issue_number"
    fi
}

echo "Creating GitHub Issues for MindTrack backlog..."
echo ""

echo "--- Task 1: User authentication with Google OAuth2 (Done) ---"
gh_issue "User authentication with Google OAuth2" \
"Implement Google OAuth2 login with Spring Security. JWT issued on success, stored in HTTP-only cookie. RBAC roles: ADMIN, USER, THERAPIST." \
"$LABEL_BACKEND_ENH" "$STATE_CLOSED"

echo "--- Task 2: Admin panel with RBAC (Done) ---"
gh_issue "Admin panel with RBAC" \
"Admin panel for user management. List users, change roles, activate/deactivate accounts. Restricted to ADMIN role." \
"$LABEL_BACKEND_FE_ENH" "$STATE_CLOSED"

echo "--- Task 3: Interview logging with structured notes (Done) ---"
gh_issue "Interview logging with structured notes" \
"Allow users to log psychiatrist interview sessions with structured notes: mood, topics discussed, medication changes, follow-up actions." \
"$LABEL_BACKEND_FE_ENH" "$STATE_CLOSED"

echo "--- Task 4: Audio upload and transcription (Done) ---"
gh_issue "Audio upload and transcription" \
"Enable audio recording upload for interview sessions. Store in S3 with 7-day expiry. Placeholder for AWS Transcribe integration." \
"$LABEL_BACKEND_ENH" "$STATE_CLOSED"

echo "--- Task 5: Activity tracking (Done) ---"
gh_issue "Activity tracking" \
"Track therapist-assigned homework, daily habits, and custom activities. Daily completion logs with mood ratings." \
"$LABEL_BACKEND_FE_ENH" "$STATE_CLOSED"

echo "--- Task 6: Journal entries (Done) ---"
gh_issue "Journal entries" \
"Free-form journal with mood tagging and optional sharing with therapist. Rich text or plain text entries." \
"$LABEL_BACKEND_FE_ENH" "$STATE_CLOSED"

echo "--- Task 7: Goals and milestones (Done) ---"
gh_issue "Goals and milestones" \
"Set personal goals, break them into milestones, track progress with completion percentage." \
"$LABEL_BACKEND_FE_ENH" "$STATE_CLOSED"

echo "--- Task 8: AI chat with Claude API (Done) ---"
gh_issue "AI chat with Claude API" \
"Conversational coaching powered by Claude API with context from recent mood, activities, and goals data." \
"$LABEL_BACKEND_FE_ENH" "$STATE_CLOSED"

echo "--- Task 9: Telegram bot integration (Done) ---"
gh_issue "Telegram bot integration" \
"Telegram bot for spontaneous AI check-ins. Webhook-based, stores chat ID per user, sends proactive messages via EventBridge." \
"$LABEL_BACKEND_ENH" "$STATE_CLOSED"

echo "--- Task 10: WhatsApp Business API integration (Done) ---"
gh_issue "WhatsApp Business API integration" \
"WhatsApp Business API integration for check-ins. Webhook verification, incoming message handling, outbound messaging via Meta API." \
"$LABEL_BACKEND_ENH" "$STATE_CLOSED"

echo "--- Task 11: Analytics dashboard (Done) ---"
gh_issue "Analytics dashboard" \
"Dashboard with mood trends, activity completion rates, goal progress charts using Chart.js." \
"$LABEL_BACKEND_FE_ENH" "$STATE_CLOSED"

echo "--- Task 12: Therapist read-only view (Done) ---"
gh_issue "Therapist read-only view" \
"Read-only view for therapists to monitor shared patient data (interviews, activities, journal entries, goals)." \
"$LABEL_BACKEND_FE_ENH" "$STATE_CLOSED"

echo "--- Task 13: User profile configuration (Done) ---"
gh_issue "User profile configuration" \
"User profile settings: display name, timezone, notification preferences, connected messaging accounts, tutorial replay." \
"$LABEL_BACKEND_FE_ENH" "$STATE_CLOSED"

echo "--- Task 14: Improved landing page (Done) ---"
gh_issue "Improved landing page" \
"Marketing landing page with feature highlights, screenshots, call-to-action for login/signup." \
"$LABEL_FE_ENH" "$STATE_CLOSED"

echo "--- Task 15: Login and connect with Google OAuth (Done) ---"
gh_issue "Login and connect with Google OAuth" \
"Login page with Google OAuth button, redirect flow, loading state, error handling." \
"$LABEL_BACKEND_FE_ENH" "$STATE_CLOSED"

echo "--- Task 16: Quick tutorial for new users (Done) ---"
gh_issue "Quick tutorial for new users" \
"Interactive onboarding tutorial shown on first login. Step-by-step walkthrough of key features with skip/replay option." \
"$LABEL_FE_ENH" "$STATE_CLOSED"

echo "--- Task 17: Review and fix all TODOs and FIXMEs (To Do) ---"
gh_issue "Review and fix all TODOs and FIXMEs" \
"Audit the entire codebase for TODO and FIXME comments and implement or resolve each one. Covers both backend (Java) and frontend (TypeScript/Vue)." \
"backend,frontend" "$STATE_OPEN"

echo "--- Task 18: Integration and functional tests for the backend (To Do) ---"
gh_issue "Integration and functional tests for the backend" \
"Create a comprehensive suite of integration and functional tests for all backend modules. Tests should cover REST endpoints, service logic, database interactions, and security rules." \
"backend" "$STATE_OPEN"

echo "--- Task 19: Externalize environment config and provision via Terraform (Done) ---"
gh_issue "Externalize environment config and provision via Terraform" \
"Externalize all environment-specific configuration. Manage GitHub Actions secrets/variables via Terraform. Create .env.example." \
"$LABEL_INFRA" "$STATE_CLOSED"

echo "--- Task 20: Add Snyk scanning, alerts, and code style enforcement (Done) ---"
gh_issue "Add Snyk scanning, alerts, and code style enforcement" \
"Make Snyk blocking in CI (remove continue-on-error). Enforce Checkstyle on pre-commit. Tighten CI security job conditions." \
"$LABEL_SECURITY" "$STATE_CLOSED"

echo "--- Task 21: Create product icon (Done) ---"
gh_issue "Create product icon" \
"MindTrack product icon: M lettermark + EKG pulse line on blue-teal gradient. SVG favicon and logo lockup for app header." \
"$LABEL_FE_ENH" "$STATE_CLOSED"

echo "--- Task 22: Set up GitHub Issues for task tracking with commit prefixes (To Do) ---"
gh_issue "Set up GitHub Issues for task tracking with commit prefixes" \
"Create GitHub Issues for all backlog tasks. Add commit-msg hook validating #<issue-id> prefix. Update CONTRIBUTING.md." \
"$LABEL_DEVOPS" "$STATE_OPEN"

echo "--- Task 23: SonarQube quality thresholds and pre-commit enforcement (Done) ---"
gh_issue "SonarQube quality thresholds and pre-commit enforcement" \
"Define SonarCloud quality gate thresholds. Document in docs/sonar-quality-gates.md. Ensure CI fails on gate failure." \
"$LABEL_DEVOPS_ENH" "$STATE_CLOSED"

echo "--- Task 24: Push code after each completed task or feature (Done) ---"
gh_issue "Push code after each completed task or feature" \
"Add 'Code pushed to remote' to Definition of Done in backlog/config.yml. Document push policy in CONTRIBUTING.md." \
"$LABEL_DEVOPS" "$STATE_CLOSED"

echo ""
echo "=== Done! All issues processed. ==="
echo "Next: run 'gh issue list' to verify."
