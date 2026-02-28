output "ci_user_name" {
  description = "IAM username for test CI"
  value       = aws_iam_user.ci.name
}

output "ci_access_key_id" {
  description = "Access key ID for test CI user (store in GitHub Actions secret AWS_ACCESS_KEY_ID_TEST)"
  value       = aws_iam_access_key.ci.id
  sensitive   = true
}

output "ci_secret_access_key" {
  description = "Secret access key for test CI user (store in GitHub Actions secret AWS_SECRET_ACCESS_KEY_TEST)"
  value       = aws_iam_access_key.ci.secret
  sensitive   = true
}

output "lambda_role_arn" {
  description = "ARN of the Lambda execution role for test"
  value       = aws_iam_role.lambda_app.arn
}
