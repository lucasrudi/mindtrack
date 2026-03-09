output "audio_bucket_arn" {
  description = "ARN of the audio S3 bucket"
  value       = aws_s3_bucket.audio.arn
}

output "audio_bucket_name" {
  description = "Name of the audio S3 bucket"
  value       = aws_s3_bucket.audio.id
}

output "frontend_bucket_name" {
  description = "Name of the frontend S3 bucket"
  value       = aws_s3_bucket.frontend.id
}

output "frontend_bucket_domain" {
  description = "Regional domain name of the frontend bucket"
  value       = aws_s3_bucket.frontend.bucket_regional_domain_name
}

output "frontend_bucket_id" {
  description = "ID of the frontend S3 bucket"
  value       = aws_s3_bucket.frontend.id
}

output "access_logs_bucket_domain" {
  description = "Domain name of the access logs S3 bucket"
  value       = aws_s3_bucket.access_logs.bucket_domain_name
}
