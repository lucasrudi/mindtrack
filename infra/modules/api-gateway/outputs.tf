output "api_endpoint" {
  description = "API Gateway endpoint URL"
  value       = aws_apigatewayv2_api.main.api_endpoint
}

output "api_id" {
  description = "API Gateway API ID"
  value       = aws_apigatewayv2_api.main.id
}

output "access_log_group_name" {
  description = "API Gateway access log group name"
  value       = aws_cloudwatch_log_group.api_access.name
}
