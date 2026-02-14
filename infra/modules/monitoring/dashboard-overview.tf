# =============================================================================
# Overview Dashboard — High-level health across all services
# =============================================================================

resource "aws_cloudwatch_dashboard" "main" {
  dashboard_name = "${var.name_prefix}-overview"

  dashboard_body = jsonencode({
    widgets = [
      # ── Row 1: Key Health Indicators ────────────────────────────
      {
        type   = "metric"
        x      = 0
        y      = 0
        width  = 6
        height = 6
        properties = {
          title  = "Lambda Invocations & Errors"
          region = var.aws_region
          metrics = [
            ["AWS/Lambda", "Invocations", "FunctionName", var.lambda_function_name,
            { stat = "Sum", color = "#2ca02c", label = "Invocations" }],
            ["AWS/Lambda", "Errors", "FunctionName", var.lambda_function_name,
            { stat = "Sum", color = "#d62728", label = "Errors" }],
            ["AWS/Lambda", "Throttles", "FunctionName", var.lambda_function_name,
            { stat = "Sum", color = "#ff7f0e", label = "Throttles" }]
          ]
          view    = "timeSeries"
          stacked = false
          period  = 300
        }
      },
      {
        type   = "metric"
        x      = 6
        y      = 0
        width  = 6
        height = 6
        properties = {
          title  = "API Gateway Requests"
          region = var.aws_region
          metrics = [
            ["AWS/ApiGateway", "Count", "ApiId", var.api_gateway_id,
            { stat = "Sum", color = "#1f77b4", label = "Total Requests" }],
            ["AWS/ApiGateway", "4xx", "ApiId", var.api_gateway_id,
            { stat = "Sum", color = "#ff7f0e", label = "4XX" }],
            ["AWS/ApiGateway", "5xx", "ApiId", var.api_gateway_id,
            { stat = "Sum", color = "#d62728", label = "5XX" }]
          ]
          view    = "timeSeries"
          stacked = false
          period  = 300
        }
      },
      {
        type   = "metric"
        x      = 12
        y      = 0
        width  = 6
        height = 6
        properties = {
          title  = "RDS Aurora — CPU & Connections"
          region = var.aws_region
          metrics = [
            ["AWS/RDS", "CPUUtilization", "DBClusterIdentifier", var.rds_cluster_identifier,
            { stat = "Average", color = "#1f77b4", label = "CPU %" }],
            ["AWS/RDS", "DatabaseConnections", "DBClusterIdentifier", var.rds_cluster_identifier,
            { stat = "Maximum", color = "#2ca02c", label = "Connections", yAxis = "right" }]
          ]
          view    = "timeSeries"
          stacked = false
          period  = 300
          yAxis = {
            left  = { min = 0, max = 100, label = "CPU %" }
            right = { min = 0, label = "Connections" }
          }
        }
      },
      {
        type   = "metric"
        x      = 18
        y      = 0
        width  = 6
        height = 6
        properties = {
          title  = "CloudFront — Cache & Errors"
          region = "us-east-1"
          metrics = [
            ["AWS/CloudFront", "CacheHitRate", "DistributionId", var.cloudfront_distribution_id, "Region", "Global",
            { stat = "Average", color = "#2ca02c", label = "Cache Hit %" }],
            ["AWS/CloudFront", "TotalErrorRate", "DistributionId", var.cloudfront_distribution_id, "Region", "Global",
            { stat = "Average", color = "#d62728", label = "Error Rate %", yAxis = "right" }]
          ]
          view    = "timeSeries"
          stacked = false
          period  = 300
          yAxis = {
            left  = { min = 0, max = 100, label = "Cache Hit %" }
            right = { min = 0, label = "Error Rate %" }
          }
        }
      },

      # ── Row 2: Latency & Performance ────────────────────────────
      {
        type   = "metric"
        x      = 0
        y      = 6
        width  = 8
        height = 6
        properties = {
          title  = "Lambda Duration (ms)"
          region = var.aws_region
          metrics = [
            ["AWS/Lambda", "Duration", "FunctionName", var.lambda_function_name,
            { stat = "Average", color = "#1f77b4", label = "Average" }],
            ["AWS/Lambda", "Duration", "FunctionName", var.lambda_function_name,
            { stat = "p90", color = "#ff7f0e", label = "p90" }],
            ["AWS/Lambda", "Duration", "FunctionName", var.lambda_function_name,
            { stat = "p99", color = "#d62728", label = "p99" }],
            ["AWS/Lambda", "Duration", "FunctionName", var.lambda_function_name,
            { stat = "Maximum", color = "#9467bd", label = "Max" }]
          ]
          view    = "timeSeries"
          stacked = false
          period  = 300
        }
      },
      {
        type   = "metric"
        x      = 8
        y      = 6
        width  = 8
        height = 6
        properties = {
          title  = "API Gateway Latency (ms)"
          region = var.aws_region
          metrics = [
            ["AWS/ApiGateway", "Latency", "ApiId", var.api_gateway_id,
            { stat = "Average", color = "#1f77b4", label = "Average" }],
            ["AWS/ApiGateway", "Latency", "ApiId", var.api_gateway_id,
            { stat = "p90", color = "#ff7f0e", label = "p90" }],
            ["AWS/ApiGateway", "Latency", "ApiId", var.api_gateway_id,
            { stat = "p99", color = "#d62728", label = "p99" }],
            ["AWS/ApiGateway", "IntegrationLatency", "ApiId", var.api_gateway_id,
            { stat = "Average", color = "#17becf", label = "Integration Avg" }]
          ]
          view    = "timeSeries"
          stacked = false
          period  = 300
        }
      },
      {
        type   = "metric"
        x      = 16
        y      = 6
        width  = 8
        height = 6
        properties = {
          title  = "RDS Aurora — ACU & Storage"
          region = var.aws_region
          metrics = [
            ["AWS/RDS", "ServerlessDatabaseCapacity", "DBClusterIdentifier", var.rds_cluster_identifier,
            { stat = "Average", color = "#1f77b4", label = "ACU" }],
            ["AWS/RDS", "VolumeBytesUsed", "DBClusterIdentifier", var.rds_cluster_identifier,
            { stat = "Average", color = "#2ca02c", label = "Storage (bytes)", yAxis = "right" }]
          ]
          view    = "timeSeries"
          stacked = false
          period  = 300
        }
      },

      # ── Row 3: Alarm Status Widget ──────────────────────────────
      {
        type   = "alarm"
        x      = 0
        y      = 12
        width  = 24
        height = 3
        properties = {
          title = "Alarm Status"
          alarms = [
            aws_cloudwatch_metric_alarm.lambda_errors.arn,
            aws_cloudwatch_metric_alarm.lambda_duration.arn,
            aws_cloudwatch_metric_alarm.lambda_throttles.arn,
            aws_cloudwatch_metric_alarm.api_5xx_errors.arn,
            aws_cloudwatch_metric_alarm.api_latency.arn,
            aws_cloudwatch_metric_alarm.rds_cpu.arn,
            aws_cloudwatch_metric_alarm.rds_connections.arn,
            aws_cloudwatch_metric_alarm.rds_acu_utilization.arn,
          ]
        }
      },

      # ── Row 4: S3 Storage ───────────────────────────────────────
      {
        type   = "metric"
        x      = 0
        y      = 15
        width  = 12
        height = 6
        properties = {
          title  = "S3 Bucket Sizes"
          region = var.aws_region
          metrics = [
            ["AWS/S3", "BucketSizeBytes", "BucketName", var.audio_bucket_name, "StorageType", "StandardStorage",
            { stat = "Average", color = "#ff7f0e", label = "Audio Bucket", period = 86400 }],
            ["AWS/S3", "BucketSizeBytes", "BucketName", var.frontend_bucket_name, "StorageType", "StandardStorage",
            { stat = "Average", color = "#1f77b4", label = "Frontend Bucket", period = 86400 }]
          ]
          view    = "timeSeries"
          stacked = false
          period  = 86400
        }
      },
      {
        type   = "metric"
        x      = 12
        y      = 15
        width  = 12
        height = 6
        properties = {
          title  = "S3 Object Counts"
          region = var.aws_region
          metrics = [
            ["AWS/S3", "NumberOfObjects", "BucketName", var.audio_bucket_name, "StorageType", "AllStorageTypes",
            { stat = "Average", color = "#ff7f0e", label = "Audio Objects", period = 86400 }],
            ["AWS/S3", "NumberOfObjects", "BucketName", var.frontend_bucket_name, "StorageType", "AllStorageTypes",
            { stat = "Average", color = "#1f77b4", label = "Frontend Objects", period = 86400 }]
          ]
          view    = "timeSeries"
          stacked = false
          period  = 86400
        }
      }
    ]
  })
}
