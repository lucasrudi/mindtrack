output "secret_arns" {
  description = "List of all secret ARNs"
  value = [
    aws_secretsmanager_secret.claude_api_key.arn,
    aws_secretsmanager_secret.google_oauth_client_id.arn,
    aws_secretsmanager_secret.google_oauth_client_secret.arn,
    aws_secretsmanager_secret.telegram_bot_token.arn,
    aws_secretsmanager_secret.whatsapp_api_token.arn,
  ]
}

output "app_encryption_key_arn" {
  description = "ARN of the KMS key used for application-level PII column encryption"
  value       = aws_kms_key.app_encryption.arn
}
