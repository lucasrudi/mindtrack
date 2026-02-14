output "sns_topic_arn" {
  description = "SNS topic ARN for alarm notifications"
  value       = var.alarm_email != "" ? aws_sns_topic.alarms[0].arn : null
}

output "dashboard_url" {
  description = "CloudWatch dashboard URL"
  value       = "https://${var.aws_region}.console.aws.amazon.com/cloudwatch/home?region=${var.aws_region}#dashboards/dashboard/${aws_cloudwatch_dashboard.main.dashboard_name}"
}

output "lambda_dashboard_url" {
  description = "CloudWatch Lambda dashboard URL"
  value       = "https://${var.aws_region}.console.aws.amazon.com/cloudwatch/home?region=${var.aws_region}#dashboards/dashboard/${aws_cloudwatch_dashboard.lambda.dashboard_name}"
}

output "database_dashboard_url" {
  description = "CloudWatch Database dashboard URL"
  value       = "https://${var.aws_region}.console.aws.amazon.com/cloudwatch/home?region=${var.aws_region}#dashboards/dashboard/${aws_cloudwatch_dashboard.database.dashboard_name}"
}
