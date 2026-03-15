resource "aws_cloudwatch_event_rule" "ai_checkin" {
  name                = "${var.name_prefix}-ai-checkin"
  description         = "Trigger AI check-in conversations"
  schedule_expression = "rate(1 day)"
}

resource "aws_cloudwatch_event_target" "lambda" {
  rule = aws_cloudwatch_event_rule.ai_checkin.name
  arn  = var.lambda_function_arn

  input = jsonencode({
    action = "scheduled-checkin"
  })
}

resource "aws_lambda_permission" "eventbridge" {
  statement_id  = "AllowEventBridgeInvoke"
  action        = "lambda:InvokeFunction"
  function_name = var.lambda_function_arn
  principal     = "events.amazonaws.com"
  source_arn    = aws_cloudwatch_event_rule.ai_checkin.arn
}

# Hard-delete accounts whose 30-day retention window has elapsed (issue #166 — M-1 data retention)
resource "aws_cloudwatch_event_rule" "hard_delete_accounts" {
  name                = "${var.name_prefix}-hard-delete-accounts"
  description         = "Trigger nightly hard-deletion of accounts past their retention window"
  schedule_expression = "cron(0 3 * * ? *)"
}

resource "aws_cloudwatch_event_target" "hard_delete_lambda" {
  rule = aws_cloudwatch_event_rule.hard_delete_accounts.name
  arn  = var.lambda_function_arn

  input = jsonencode({
    action = "hard-delete-accounts"
  })
}

resource "aws_lambda_permission" "eventbridge_hard_delete" {
  statement_id  = "AllowEventBridgeHardDeleteInvoke"
  action        = "lambda:InvokeFunction"
  function_name = var.lambda_function_arn
  principal     = "events.amazonaws.com"
  source_arn    = aws_cloudwatch_event_rule.hard_delete_accounts.arn
}
