# CI user for dev (used by local dev scripts and GitHub Actions dev jobs)
resource "aws_iam_user" "ci" {
  name = "${local.name_prefix}-ci"
  path = "/mindtrack/"

  tags = {
    Purpose = "CI/CD programmatic access for ${var.environment} environment"
  }
}

resource "aws_iam_access_key" "ci" {
  user = aws_iam_user.ci.name
}

# Lambda execution role for dev
data "aws_iam_policy_document" "lambda_assume" {
  statement {
    effect  = "Allow"
    actions = ["sts:AssumeRole"]
    principals {
      type        = "Service"
      identifiers = ["lambda.amazonaws.com"]
    }
  }
}

resource "aws_iam_role" "lambda_app" {
  name               = "${local.name_prefix}-app"
  assume_role_policy = data.aws_iam_policy_document.lambda_assume.json
}

# Dev-scoped permissions — only /mindtrack/dev/* secrets, dev-* S3, dev CloudWatch
data "aws_iam_policy_document" "lambda_app" {
  statement {
    sid    = "CloudWatchLogs"
    effect = "Allow"
    actions = [
      "logs:CreateLogGroup",
      "logs:CreateLogStream",
      "logs:PutLogEvents",
    ]
    resources = ["arn:aws:logs:*:*:log-group:/aws/lambda/${local.name_prefix}-*:*"]
  }

  statement {
    sid       = "SecretsManager"
    effect    = "Allow"
    actions   = ["secretsmanager:GetSecretValue"]
    resources = ["arn:aws:secretsmanager:${var.aws_region}:*:secret:/mindtrack/${var.environment}/*"]
  }

  statement {
    sid       = "DenyOtherEnvSecrets"
    effect    = "Deny"
    actions   = ["secretsmanager:GetSecretValue"]
    resources = ["arn:aws:secretsmanager:${var.aws_region}:*:secret:/mindtrack/prod/*"]
  }
}

resource "aws_iam_role_policy" "lambda_app" {
  name   = "${local.name_prefix}-app-policy"
  role   = aws_iam_role.lambda_app.id
  policy = data.aws_iam_policy_document.lambda_app.json
}

# Dev CI user permissions
data "aws_iam_policy_document" "ci_user" {
  statement {
    sid    = "LambdaDeploy"
    effect = "Allow"
    actions = [
      "lambda:UpdateFunctionCode",
      "lambda:GetFunction",
    ]
    resources = ["arn:aws:lambda:${var.aws_region}:*:function:${local.name_prefix}-*"]
  }

  statement {
    sid    = "S3Deploy"
    effect = "Allow"
    actions = [
      "s3:PutObject",
      "s3:GetObject",
      "s3:DeleteObject",
      "s3:ListBucket",
    ]
    resources = [
      "arn:aws:s3:::${local.name_prefix}-*",
      "arn:aws:s3:::${local.name_prefix}-*/*",
    ]
  }

  statement {
    sid       = "CloudFrontInvalidate"
    effect    = "Allow"
    actions   = ["cloudfront:CreateInvalidation"]
    resources = ["*"]
  }

  statement {
    sid     = "DenyProdAccess"
    effect  = "Deny"
    actions = ["*"]
    resources = [
      "arn:aws:s3:::mindtrack-prod-*",
      "arn:aws:s3:::mindtrack-prod-*/*",
      "arn:aws:lambda:${var.aws_region}:*:function:mindtrack-prod-*",
      "arn:aws:secretsmanager:${var.aws_region}:*:secret:/mindtrack/prod/*",
    ]
  }
}

resource "aws_iam_user_policy" "ci" {
  name   = "${local.name_prefix}-ci-policy"
  user   = aws_iam_user.ci.name
  policy = data.aws_iam_policy_document.ci_user.json
}
