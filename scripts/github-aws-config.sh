#!/bin/sh
# Copies the current Learner Lab credentials and the tofu outputs to GitHub.
# Run in the repository root after every Learner Lab start, and after tofu apply.
# Needs: gh (logged in), aws (credentials in ~/.aws/credentials), tofu.
set -eu

# Secrets: never printed, gh reads the value from stdin
for key in aws_access_key_id aws_secret_access_key aws_session_token; do
  name=$(echo "$key" | tr 'a-z' 'A-Z')
  aws configure get "$key" | gh secret set "$name"
done

# Variables: public values, visible in the logs on purpose
cd infra
gh variable set AWS_REGION --body "$(aws configure get region || echo us-east-1)"
gh variable set INSTANCE_ID --body "$(tofu output -raw instance_id)"
gh variable set FRONTEND_BUCKET --body "$(tofu output -raw frontend_bucket)"
gh variable set VERSION_URL --body "$(tofu output -raw version_url)"
gh variable set FRONTEND_URL --body "$(tofu output -raw frontend_url)"

echo "GitHub secrets and variables updated."
