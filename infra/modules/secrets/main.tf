resource "aws_kms_key" "secrets" {
  description             = "${var.name_prefix} Secrets Manager encryption key"
  deletion_window_in_days = 7
  enable_key_rotation     = true
}

resource "aws_secretsmanager_secret" "claude_api_key" {
  name        = "${var.name_prefix}/claude-api-key"
  description = "Anthropic Claude API key"
  kms_key_id  = aws_kms_key.secrets.arn
}

resource "aws_secretsmanager_secret" "google_oauth_client_id" {
  name        = "${var.name_prefix}/google-oauth-client-id"
  description = "Google OAuth2 client ID"
  kms_key_id  = aws_kms_key.secrets.arn
}

resource "aws_secretsmanager_secret" "google_oauth_client_secret" {
  name        = "${var.name_prefix}/google-oauth-client-secret"
  description = "Google OAuth2 client secret"
  kms_key_id  = aws_kms_key.secrets.arn
}

resource "aws_secretsmanager_secret" "telegram_bot_token" {
  name        = "${var.name_prefix}/telegram-bot-token"
  description = "Telegram Bot API token"
  kms_key_id  = aws_kms_key.secrets.arn
}

resource "aws_secretsmanager_secret" "whatsapp_api_token" {
  name        = "${var.name_prefix}/whatsapp-api-token"
  description = "WhatsApp Business API token"
  kms_key_id  = aws_kms_key.secrets.arn
}
