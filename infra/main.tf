locals {
  name_prefix = "mindtrack-${var.environment}"
}

module "iam" {
  source = "./modules/iam"

  name_prefix      = local.name_prefix
  audio_bucket_arn = module.s3.audio_bucket_arn
  secrets_arns     = module.secrets.secret_arns
}

module "s3" {
  source = "./modules/s3"

  name_prefix             = local.name_prefix
  cloudfront_oai_iam_arn  = module.cloudfront.oai_iam_arn
}

module "rds" {
  source = "./modules/rds"

  name_prefix        = local.name_prefix
  min_capacity       = var.db_min_capacity
  max_capacity       = var.db_max_capacity
  lambda_sg_id       = module.lambda.security_group_id
}

module "lambda" {
  source = "./modules/lambda"

  name_prefix    = local.name_prefix
  memory_size    = var.lambda_memory_size
  role_arn       = module.iam.lambda_role_arn
  rds_endpoint   = module.rds.cluster_endpoint
  rds_port       = module.rds.cluster_port
  rds_sg_id      = module.rds.security_group_id
  secrets_arns   = module.secrets.secret_arns
}

module "api_gateway" {
  source = "./modules/api-gateway"

  name_prefix         = local.name_prefix
  lambda_function_arn = module.lambda.function_arn
  lambda_invoke_arn   = module.lambda.invoke_arn
}

module "cloudfront" {
  source = "./modules/cloudfront"

  name_prefix             = local.name_prefix
  frontend_bucket_domain  = module.s3.frontend_bucket_domain
  frontend_bucket_id      = module.s3.frontend_bucket_id
  domain_name             = var.domain_name
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
