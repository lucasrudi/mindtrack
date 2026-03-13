# =============================================================================
# CloudWatch Log Group — Lambda
# =============================================================================

#tfsec:ignore:aws-cloudwatch-log-group-customer-key
resource "aws_cloudwatch_log_group" "lambda" {
  name              = "/aws/lambda/${var.lambda_function_name}"
  retention_in_days = 365

  tags = {
    Environment = var.environment
  }
}

# =============================================================================
# SNS Topic for Alarms
# =============================================================================

resource "aws_sns_topic" "alarms" {
  count             = var.alarm_email != "" ? 1 : 0
  name              = "${var.name_prefix}-alarms"
  kms_master_key_id = "alias/aws/sns"

  tags = {
    Name = "${var.name_prefix}-alarms"
  }
}

resource "aws_sns_topic_subscription" "email" {
  count     = var.alarm_email != "" ? 1 : 0
  topic_arn = aws_sns_topic.alarms[0].arn
  protocol  = "email"
  endpoint  = var.alarm_email
}

locals {
  alarm_actions = var.alarm_email != "" ? [aws_sns_topic.alarms[0].arn] : []
}

# =============================================================================
# CloudWatch Alarms — Lambda
# =============================================================================

resource "aws_cloudwatch_metric_alarm" "lambda_errors" {
  alarm_name          = "${var.name_prefix}-lambda-errors"
  alarm_description   = "Lambda function error count exceeds threshold"
  comparison_operator = "GreaterThanOrEqualToThreshold"
  evaluation_periods  = 2
  metric_name         = "Errors"
  namespace           = "AWS/Lambda"
  period              = 300
  statistic           = "Sum"
  threshold           = var.lambda_error_threshold
  treat_missing_data  = "notBreaching"
  actions_enabled     = var.alarm_actions_enabled
  alarm_actions       = local.alarm_actions
  ok_actions          = local.alarm_actions

  dimensions = {
    FunctionName = var.lambda_function_name
  }

  tags = {
    Name = "${var.name_prefix}-lambda-errors"
  }
}

resource "aws_cloudwatch_metric_alarm" "lambda_duration" {
  alarm_name          = "${var.name_prefix}-lambda-duration"
  alarm_description   = "Lambda p99 duration exceeds threshold"
  comparison_operator = "GreaterThanOrEqualToThreshold"
  evaluation_periods  = 2
  metric_name         = "Duration"
  namespace           = "AWS/Lambda"
  period              = 300
  extended_statistic  = "p99"
  threshold           = var.lambda_duration_threshold_ms
  treat_missing_data  = "notBreaching"
  actions_enabled     = var.alarm_actions_enabled
  alarm_actions       = local.alarm_actions
  ok_actions          = local.alarm_actions

  dimensions = {
    FunctionName = var.lambda_function_name
  }

  tags = {
    Name = "${var.name_prefix}-lambda-duration"
  }
}

resource "aws_cloudwatch_metric_alarm" "lambda_throttles" {
  alarm_name          = "${var.name_prefix}-lambda-throttles"
  alarm_description   = "Lambda function is being throttled"
  comparison_operator = "GreaterThanOrEqualToThreshold"
  evaluation_periods  = 1
  metric_name         = "Throttles"
  namespace           = "AWS/Lambda"
  period              = 300
  statistic           = "Sum"
  threshold           = var.lambda_throttle_threshold
  treat_missing_data  = "notBreaching"
  actions_enabled     = var.alarm_actions_enabled
  alarm_actions       = local.alarm_actions
  ok_actions          = local.alarm_actions

  dimensions = {
    FunctionName = var.lambda_function_name
  }

  tags = {
    Name = "${var.name_prefix}-lambda-throttles"
  }
}

# =============================================================================
# CloudWatch Alarms — API Gateway
# =============================================================================

resource "aws_cloudwatch_metric_alarm" "api_5xx_errors" {
  alarm_name          = "${var.name_prefix}-api-5xx"
  alarm_description   = "API Gateway 5XX error count exceeds threshold"
  comparison_operator = "GreaterThanOrEqualToThreshold"
  evaluation_periods  = 2
  metric_name         = "5xx"
  namespace           = "AWS/ApiGateway"
  period              = 300
  statistic           = "Sum"
  threshold           = var.api_gateway_5xx_threshold
  treat_missing_data  = "notBreaching"
  actions_enabled     = var.alarm_actions_enabled
  alarm_actions       = local.alarm_actions
  ok_actions          = local.alarm_actions

  dimensions = {
    ApiId = var.api_gateway_id
    Stage = var.api_gateway_stage
  }

  tags = {
    Name = "${var.name_prefix}-api-5xx"
  }
}

resource "aws_cloudwatch_metric_alarm" "api_latency" {
  alarm_name          = "${var.name_prefix}-api-latency"
  alarm_description   = "API Gateway p99 latency exceeds threshold"
  comparison_operator = "GreaterThanOrEqualToThreshold"
  evaluation_periods  = 2
  metric_name         = "Latency"
  namespace           = "AWS/ApiGateway"
  period              = 300
  extended_statistic  = "p99"
  threshold           = var.api_gateway_latency_threshold_ms
  treat_missing_data  = "notBreaching"
  actions_enabled     = var.alarm_actions_enabled
  alarm_actions       = local.alarm_actions
  ok_actions          = local.alarm_actions

  dimensions = {
    ApiId = var.api_gateway_id
    Stage = var.api_gateway_stage
  }

  tags = {
    Name = "${var.name_prefix}-api-latency"
  }
}

# =============================================================================
# CloudWatch Alarms — RDS Aurora
# =============================================================================

resource "aws_cloudwatch_metric_alarm" "rds_cpu" {
  alarm_name          = "${var.name_prefix}-rds-cpu"
  alarm_description   = "RDS Aurora CPU utilization exceeds threshold"
  comparison_operator = "GreaterThanOrEqualToThreshold"
  evaluation_periods  = 3
  metric_name         = "CPUUtilization"
  namespace           = "AWS/RDS"
  period              = 300
  statistic           = "Average"
  threshold           = var.rds_cpu_threshold
  treat_missing_data  = "notBreaching"
  actions_enabled     = var.alarm_actions_enabled
  alarm_actions       = local.alarm_actions
  ok_actions          = local.alarm_actions

  dimensions = {
    DBClusterIdentifier = var.rds_cluster_identifier
  }

  tags = {
    Name = "${var.name_prefix}-rds-cpu"
  }
}

resource "aws_cloudwatch_metric_alarm" "rds_connections" {
  alarm_name          = "${var.name_prefix}-rds-connections"
  alarm_description   = "RDS database connections exceed threshold"
  comparison_operator = "GreaterThanOrEqualToThreshold"
  evaluation_periods  = 2
  metric_name         = "DatabaseConnections"
  namespace           = "AWS/RDS"
  period              = 300
  statistic           = "Maximum"
  threshold           = var.rds_connections_threshold
  treat_missing_data  = "notBreaching"
  actions_enabled     = var.alarm_actions_enabled
  alarm_actions       = local.alarm_actions
  ok_actions          = local.alarm_actions

  dimensions = {
    DBClusterIdentifier = var.rds_cluster_identifier
  }

  tags = {
    Name = "${var.name_prefix}-rds-connections"
  }
}

resource "aws_cloudwatch_metric_alarm" "rds_acu_utilization" {
  alarm_name          = "${var.name_prefix}-rds-acu"
  alarm_description   = "RDS Aurora ACU utilization exceeds threshold"
  comparison_operator = "GreaterThanOrEqualToThreshold"
  evaluation_periods  = 3
  metric_name         = "ServerlessDatabaseCapacity"
  namespace           = "AWS/RDS"
  period              = 300
  statistic           = "Maximum"
  threshold           = var.rds_acu_threshold
  treat_missing_data  = "notBreaching"
  actions_enabled     = var.alarm_actions_enabled
  alarm_actions       = local.alarm_actions
  ok_actions          = local.alarm_actions

  dimensions = {
    DBClusterIdentifier = var.rds_cluster_identifier
  }

  tags = {
    Name = "${var.name_prefix}-rds-acu"
  }
}

# =============================================================================
# CloudWatch Security Alarms — Unauthorized / Forbidden Spikes (M-9)
# =============================================================================

resource "aws_cloudwatch_log_metric_filter" "api_401" {
  name           = "${var.name_prefix}-api-401"
  log_group_name = var.api_gateway_log_group_name
  pattern        = "[requestId, method, route, status=401, ...]"

  metric_transformation {
    name          = "Api401Count"
    namespace     = "${var.name_prefix}/Security"
    value         = "1"
    default_value = "0"
  }
}

resource "aws_cloudwatch_log_metric_filter" "api_403" {
  name           = "${var.name_prefix}-api-403"
  log_group_name = var.api_gateway_log_group_name
  pattern        = "[requestId, method, route, status=403, ...]"

  metric_transformation {
    name          = "Api403Count"
    namespace     = "${var.name_prefix}/Security"
    value         = "1"
    default_value = "0"
  }
}

resource "aws_cloudwatch_metric_alarm" "api_401_spike" {
  alarm_name          = "${var.name_prefix}-api-401-spike"
  alarm_description   = "Spike in HTTP 401 Unauthorized responses — possible credential brute-force"
  comparison_operator = "GreaterThanOrEqualToThreshold"
  evaluation_periods  = 2
  metric_name         = "Api401Count"
  namespace           = "${var.name_prefix}/Security"
  period              = 300
  statistic           = "Sum"
  threshold           = var.api_security_401_threshold
  treat_missing_data  = "notBreaching"
  actions_enabled     = var.alarm_actions_enabled
  alarm_actions       = local.alarm_actions
  ok_actions          = local.alarm_actions

  tags = {
    Name = "${var.name_prefix}-api-401-spike"
  }
}

resource "aws_cloudwatch_metric_alarm" "api_403_spike" {
  alarm_name          = "${var.name_prefix}-api-403-spike"
  alarm_description   = "Spike in HTTP 403 Forbidden responses — possible authorization bypass attempt"
  comparison_operator = "GreaterThanOrEqualToThreshold"
  evaluation_periods  = 2
  metric_name         = "Api403Count"
  namespace           = "${var.name_prefix}/Security"
  period              = 300
  statistic           = "Sum"
  threshold           = var.api_security_403_threshold
  treat_missing_data  = "notBreaching"
  actions_enabled     = var.alarm_actions_enabled
  alarm_actions       = local.alarm_actions
  ok_actions          = local.alarm_actions

  tags = {
    Name = "${var.name_prefix}-api-403-spike"
  }
}
