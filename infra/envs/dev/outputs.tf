output "ci_user_name" {
  description = "IAM username for dev CI"
  value       = aws_iam_user.ci.name
}

output "ci_access_key_id" {
  description = "Access key ID for dev CI user (store in GitHub Actions secret AWS_ACCESS_KEY_ID_DEV)"
  value       = aws_iam_access_key.ci.id
  sensitive   = true
}

output "ci_secret_access_key" {
  description = "Secret access key for dev CI user (store in GitHub Actions secret AWS_SECRET_ACCESS_KEY_DEV)"
  value       = aws_iam_access_key.ci.secret
  sensitive   = true
}

output "lambda_role_arn" {
  description = "ARN of the Lambda execution role for dev"
  value       = aws_iam_role.lambda_app.arn
}
