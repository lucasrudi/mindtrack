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
