environment        = "prod"
aws_region         = "us-east-1"
lambda_memory_size = 1024
domain_name        = ""

github_org  = "lucasrudi"
github_repo = "mindtrack"
# The account-wide GitHub OIDC provider already exists in prod and is referenced by ARN in the role trust policy.
create_oidc_provider = false
