locals {
  name_prefix = "mindtrack-${var.environment}"
}

module "iam" {
  source = "./modules/iam"

  name_prefix          = local.name_prefix
  audio_bucket_arn     = module.s3.audio_bucket_arn
  secrets_arns         = module.secrets.secret_arns
  github_org           = var.github_org
  github_repo          = var.github_repo
  create_oidc_provider = var.create_oidc_provider
}

module "s3" {
  source = "./modules/s3"

  name_prefix            = local.name_prefix
  environment            = var.environment
  cloudfront_oai_iam_arn = module.cloudfront.oai_iam_arn
}

module "rds" {
  source = "./modules/rds"

  name_prefix  = local.name_prefix
  min_capacity = var.db_min_capacity
  max_capacity = var.db_max_capacity
  lambda_sg_id = module.lambda.security_group_id
}

module "lambda" {
  source = "./modules/lambda"

  name_prefix  = local.name_prefix
  memory_size  = var.lambda_memory_size
  role_arn     = module.iam.lambda_role_arn
  rds_endpoint = module.rds.cluster_endpoint
  rds_port     = module.rds.cluster_port
  secrets_arns = module.secrets.secret_arns
}

module "api_gateway" {
  source = "./modules/api-gateway"

  name_prefix         = local.name_prefix
  lambda_function_arn = module.lambda.function_arn
  lambda_invoke_arn   = module.lambda.invoke_arn
  cloudfront_domain   = module.cloudfront.domain_name
}

module "cloudfront" {
  source = "./modules/cloudfront"

  name_prefix               = local.name_prefix
  frontend_bucket_domain    = module.s3.frontend_bucket_domain
  frontend_bucket_id        = module.s3.frontend_bucket_id
  domain_name               = var.domain_name
  acm_certificate_arn       = var.acm_certificate_arn
  access_logs_bucket_domain = module.s3.access_logs_bucket_domain

  providers = {
    aws           = aws
    aws.us_east_1 = aws.us_east_1
  }
}

module "eventbridge" {
  source = "./modules/eventbridge"

  name_prefix         = local.name_prefix
  lambda_function_arn = module.lambda.function_arn
}

module "secrets" {
  source = "./modules/secrets"

  name_prefix = local.name_prefix
}

module "monitoring" {
  source = "./modules/monitoring"

  name_prefix                = local.name_prefix
  environment                = var.environment
  aws_region                 = var.aws_region
  lambda_function_name       = module.lambda.function_name
  api_gateway_id             = module.api_gateway.api_id
  rds_cluster_identifier     = module.rds.cluster_identifier
  rds_instance_identifier    = module.rds.instance_identifier
  audio_bucket_name          = module.s3.audio_bucket_name
  frontend_bucket_name       = module.s3.frontend_bucket_name
  cloudfront_distribution_id = module.cloudfront.distribution_id
  alarm_email                = var.alarm_email
  api_gateway_log_group_name = module.api_gateway.access_log_group_name
}
