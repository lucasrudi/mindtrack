data "aws_vpc" "default" {
  default = true
}

data "aws_subnets" "private" {
  filter {
    name   = "vpc-id"
    values = [data.aws_vpc.default.id]
  }
}

resource "aws_security_group" "lambda" {
  name        = "${var.name_prefix}-lambda-sg"
  description = "Security group for Lambda function"
  vpc_id      = data.aws_vpc.default.id

  #tfsec:ignore:aws-ec2-no-public-egress-sgr
  egress {
    from_port   = 443
    to_port     = 443
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
    description = "HTTPS to AWS services (Secrets Manager, Transcribe)"
  }

  egress {
    from_port   = 3306
    to_port     = 3306
    protocol    = "tcp"
    cidr_blocks = [data.aws_vpc.default.cidr_block]
    description = "MySQL to RDS"
  }

  tags = {
    Name = "${var.name_prefix}-lambda-sg"
  }
}

resource "aws_lambda_function" "api" {
  function_name = "${var.name_prefix}-api"
  role          = var.role_arn
  handler       = "com.mindtrack.StreamLambdaHandler::handleRequest"
  runtime       = "java21"
  memory_size   = var.memory_size
  timeout       = 30

  filename         = "${path.module}/placeholder.zip"
  source_code_hash = filebase64sha256("${path.module}/placeholder.zip")

  snap_start {
    apply_on = "PublishedVersions"
  }

  tracing_config {
    mode = "Active"
  }

  vpc_config {
    subnet_ids         = data.aws_subnets.private.ids
    security_group_ids = [aws_security_group.lambda.id]
  }

  environment {
    variables = {
      SPRING_PROFILES_ACTIVE = "prod"
      DB_URL                 = "jdbc:mysql://${var.rds_endpoint}:${var.rds_port}/mindtrack"
      SECRETS_ARNS           = join(",", var.secrets_arns)
    }
  }

  tags = {
    Name = "${var.name_prefix}-api"
  }
}

# Create a placeholder zip for initial deployment
resource "local_file" "placeholder" {
  filename = "${path.module}/placeholder.zip"
  content  = "placeholder"

  lifecycle {
    ignore_changes = [content]
  }
}
