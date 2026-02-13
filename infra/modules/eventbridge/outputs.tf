output "rule_arn" {
  description = "EventBridge rule ARN"
  value       = aws_cloudwatch_event_rule.ai_checkin.arn
}
