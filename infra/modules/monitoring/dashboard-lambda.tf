# =============================================================================
# Lambda Deep-Dive Dashboard
# =============================================================================

resource "aws_cloudwatch_dashboard" "lambda" {
  dashboard_name = "${var.name_prefix}-lambda"

  dashboard_body = jsonencode({
    widgets = [
      # ── Row 1: Invocations & Errors ─────────────────────────────
      {
        type   = "metric"
        x      = 0
        y      = 0
        width  = 8
        height = 6
        properties = {
          title  = "Invocations"
          region = var.aws_region
          metrics = [
            ["AWS/Lambda", "Invocations", "FunctionName", var.lambda_function_name,
            { stat = "Sum", color = "#2ca02c", label = "Invocations" }]
          ]
          view    = "timeSeries"
          stacked = false
          period  = 60
        }
      },
      {
        type   = "metric"
        x      = 8
        y      = 0
        width  = 8
        height = 6
        properties = {
          title  = "Errors & Throttles"
          region = var.aws_region
          metrics = [
            ["AWS/Lambda", "Errors", "FunctionName", var.lambda_function_name,
            { stat = "Sum", color = "#d62728", label = "Errors" }],
            ["AWS/Lambda", "Throttles", "FunctionName", var.lambda_function_name,
            { stat = "Sum", color = "#ff7f0e", label = "Throttles" }]
          ]
          view    = "timeSeries"
          stacked = false
          period  = 60
        }
      },
      {
        type   = "metric"
        x      = 16
        y      = 0
        width  = 8
        height = 6
        properties = {
          title  = "Concurrent Executions"
          region = var.aws_region
          metrics = [
            ["AWS/Lambda", "ConcurrentExecutions", "FunctionName", var.lambda_function_name,
            { stat = "Maximum", color = "#9467bd", label = "Concurrent" }]
          ]
          view    = "timeSeries"
          stacked = false
          period  = 60
        }
      },

      # ── Row 2: Duration Distribution ────────────────────────────
      {
        type   = "metric"
        x      = 0
        y      = 6
        width  = 12
        height = 6
        properties = {
          title  = "Duration Percentiles (ms)"
          region = var.aws_region
          metrics = [
            ["AWS/Lambda", "Duration", "FunctionName", var.lambda_function_name,
            { stat = "p50", color = "#2ca02c", label = "p50" }],
            ["AWS/Lambda", "Duration", "FunctionName", var.lambda_function_name,
            { stat = "p90", color = "#ff7f0e", label = "p90" }],
            ["AWS/Lambda", "Duration", "FunctionName", var.lambda_function_name,
            { stat = "p99", color = "#d62728", label = "p99" }],
            ["AWS/Lambda", "Duration", "FunctionName", var.lambda_function_name,
            { stat = "Maximum", color = "#9467bd", label = "Max" }]
          ]
          view    = "timeSeries"
          stacked = false
          period  = 60
        }
      },
      {
        type   = "metric"
        x      = 12
        y      = 6
        width  = 12
        height = 6
        properties = {
          title  = "Duration Average vs Timeout"
          region = var.aws_region
          metrics = [
            ["AWS/Lambda", "Duration", "FunctionName", var.lambda_function_name,
            { stat = "Average", color = "#1f77b4", label = "Average Duration" }]
          ]
          view    = "timeSeries"
          stacked = false
          period  = 60
          annotations = {
            horizontal = [
              {
                label = "Timeout (30s)"
                value = 30000
                color = "#d62728"
              },
              {
                label = "Warning (15s)"
                value = 15000
                color = "#ff7f0e"
              }
            ]
          }
        }
      },

      # ── Row 3: Cold Starts (SnapStart) & Memory ────────────────
      {
        type   = "metric"
        x      = 0
        y      = 12
        width  = 8
        height = 6
        properties = {
          title  = "Init Duration — Cold Starts (ms)"
          region = var.aws_region
          metrics = [
            ["AWS/Lambda", "InitDuration", "FunctionName", var.lambda_function_name,
            { stat = "Average", color = "#1f77b4", label = "Avg Init" }],
            ["AWS/Lambda", "InitDuration", "FunctionName", var.lambda_function_name,
            { stat = "Maximum", color = "#d62728", label = "Max Init" }]
          ]
          view    = "timeSeries"
          stacked = false
          period  = 300
        }
      },
      {
        type   = "metric"
        x      = 8
        y      = 12
        width  = 8
        height = 6
        properties = {
          title  = "SnapStart Restore Duration (ms)"
          region = var.aws_region
          metrics = [
            ["AWS/Lambda", "SnapshotRestoreDuration", "FunctionName", var.lambda_function_name,
            { stat = "Average", color = "#17becf", label = "Avg Restore" }],
            ["AWS/Lambda", "SnapshotRestoreDuration", "FunctionName", var.lambda_function_name,
            { stat = "Maximum", color = "#9467bd", label = "Max Restore" }]
          ]
          view    = "timeSeries"
          stacked = false
          period  = 300
        }
      },
      {
        type   = "metric"
        x      = 16
        y      = 12
        width  = 8
        height = 6
        properties = {
          title  = "Error Rate (%)"
          region = var.aws_region
          metrics = [
            [{
              expression = "100 * errors / invocations"
              label      = "Error Rate %"
              id         = "errorRate"
              color      = "#d62728"
            }],
            ["AWS/Lambda", "Errors", "FunctionName", var.lambda_function_name,
            { stat = "Sum", id = "errors", visible = false }],
            ["AWS/Lambda", "Invocations", "FunctionName", var.lambda_function_name,
            { stat = "Sum", id = "invocations", visible = false }]
          ]
          view    = "timeSeries"
          stacked = false
          period  = 300
          yAxis = {
            left = { min = 0, max = 100, label = "%" }
          }
        }
      },

      # ── Row 4: Success Rate & Availability KPIs ────────────────
      {
        type   = "metric"
        x      = 0
        y      = 18
        width  = 8
        height = 4
        properties = {
          title  = "Total Invocations (24h)"
          region = var.aws_region
          metrics = [
            ["AWS/Lambda", "Invocations", "FunctionName", var.lambda_function_name,
            { stat = "Sum", color = "#2ca02c" }]
          ]
          view   = "singleValue"
          period = 86400
        }
      },
      {
        type   = "metric"
        x      = 8
        y      = 18
        width  = 8
        height = 4
        properties = {
          title  = "Total Errors (24h)"
          region = var.aws_region
          metrics = [
            ["AWS/Lambda", "Errors", "FunctionName", var.lambda_function_name,
            { stat = "Sum", color = "#d62728" }]
          ]
          view   = "singleValue"
          period = 86400
        }
      },
      {
        type   = "metric"
        x      = 16
        y      = 18
        width  = 8
        height = 4
        properties = {
          title  = "Average Duration (24h)"
          region = var.aws_region
          metrics = [
            ["AWS/Lambda", "Duration", "FunctionName", var.lambda_function_name,
            { stat = "Average", color = "#1f77b4" }]
          ]
          view   = "singleValue"
          period = 86400
        }
      }
    ]
  })
}
