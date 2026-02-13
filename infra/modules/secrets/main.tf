resource "aws_secretsmanager_secret" "claude_api_key" {
  name        = "${var.name_prefix}/claude-api-key"
  description = "Anthropic Claude API key"
}

resource "aws_secretsmanager_secret" "google_oauth_client_id" {
  name        = "${var.name_prefix}/google-oauth-client-id"
  description = "Google OAuth2 client ID"
}

resource "aws_secretsmanager_secret" "google_oauth_client_secret" {
  name        = "${var.name_prefix}/google-oauth-client-secret"
  description = "Google OAuth2 client secret"
}

resource "aws_secretsmanager_secret" "telegram_bot_token" {
  name        = "${var.name_prefix}/telegram-bot-token"
  description = "Telegram Bot API token"
}

resource "aws_secretsmanager_secret" "whatsapp_api_token" {
  name        = "${var.name_prefix}/whatsapp-api-token"
  description = "WhatsApp Business API token"
}
