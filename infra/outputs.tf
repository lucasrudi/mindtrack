output "api_gateway_url" {
  description = "API Gateway endpoint URL"
  value       = module.api_gateway.api_endpoint
}

output "cloudfront_domain" {
  description = "CloudFront distribution domain name"
  value       = module.cloudfront.domain_name
}

output "rds_endpoint" {
  description = "RDS Aurora cluster endpoint"
  value       = module.rds.cluster_endpoint
  sensitive   = true
}

output "frontend_bucket_name" {
  description = "S3 bucket name for frontend assets"
  value       = module.s3.frontend_bucket_name
}

output "lambda_function_name" {
  description = "Lambda function name"
  value       = module.lambda.function_name
}

output "monitoring_dashboard_url" {
  description = "CloudWatch overview dashboard URL"
  value       = module.monitoring.dashboard_url
}

output "monitoring_lambda_dashboard_url" {
  description = "CloudWatch Lambda dashboard URL"
  value       = module.monitoring.lambda_dashboard_url
}

output "monitoring_database_dashboard_url" {
  description = "CloudWatch Database dashboard URL"
  value       = module.monitoring.database_dashboard_url
}
