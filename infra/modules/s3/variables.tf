variable "name_prefix" {
  description = "Prefix for resource names"
  type        = string
}

variable "cloudfront_oai_iam_arn" {
  description = "IAM ARN of the CloudFront Origin Access Identity"
  type        = string
}
