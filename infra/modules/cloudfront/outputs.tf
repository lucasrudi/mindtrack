output "domain_name" {
  description = "CloudFront distribution domain name"
  value       = aws_cloudfront_distribution.frontend.domain_name
}

output "distribution_id" {
  description = "CloudFront distribution ID"
  value       = aws_cloudfront_distribution.frontend.id
}

output "oai_iam_arn" {
  description = "IAM ARN of the Origin Access Identity"
  value       = aws_cloudfront_origin_access_identity.oai.iam_arn
}
