# =============================================================================
# Database Deep-Dive Dashboard — RDS MySQL
# =============================================================================

resource "aws_cloudwatch_dashboard" "database" {
  dashboard_name = "${var.name_prefix}-database"

  dashboard_body = jsonencode({
    widgets = [
      # ── Row 1: CPU & Connections ─────────────────────────────────
      {
        type   = "metric"
        x      = 0
        y      = 0
        width  = 8
        height = 6
        properties = {
          title  = "CPU Utilization (%)"
          region = var.aws_region
          metrics = [
            ["AWS/RDS", "CPUUtilization", "DBInstanceIdentifier", var.rds_instance_identifier,
            { stat = "Average", color = "#1f77b4", label = "Average CPU" }],
            ["AWS/RDS", "CPUUtilization", "DBInstanceIdentifier", var.rds_instance_identifier,
            { stat = "Maximum", color = "#d62728", label = "Max CPU" }]
          ]
          view    = "timeSeries"
          stacked = false
          period  = 300
          yAxis = {
            left = { min = 0, max = 100 }
          }
          annotations = {
            horizontal = [
              {
                label = "Alarm Threshold"
                value = 80
                color = "#d62728"
              }
            ]
          }
        }
      },
      {
        type   = "metric"
        x      = 8
        y      = 0
        width  = 8
        height = 6
        properties = {
          title  = "Database Connections"
          region = var.aws_region
          metrics = [
            ["AWS/RDS", "DatabaseConnections", "DBInstanceIdentifier", var.rds_instance_identifier,
            { stat = "Average", color = "#1f77b4", label = "Average" }],
            ["AWS/RDS", "DatabaseConnections", "DBInstanceIdentifier", var.rds_instance_identifier,
            { stat = "Maximum", color = "#d62728", label = "Maximum" }]
          ]
          view    = "timeSeries"
          stacked = false
          period  = 300
        }
      },
      {
        type   = "metric"
        x      = 16
        y      = 0
        width  = 8
        height = 6
        properties = {
          title  = "Free Storage Space (bytes)"
          region = var.aws_region
          metrics = [
            ["AWS/RDS", "FreeStorageSpace", "DBInstanceIdentifier", var.rds_instance_identifier,
            { stat = "Minimum", color = "#2ca02c", label = "Free Storage" }]
          ]
          view    = "timeSeries"
          stacked = false
          period  = 300
          annotations = {
            horizontal = [
              {
                label = "Alarm Threshold (2 GB)"
                value = 2147483648
                color = "#d62728"
              }
            ]
          }
        }
      },

      # ── Row 2: I/O & Latency ────────────────────────────────────
      {
        type   = "metric"
        x      = 0
        y      = 6
        width  = 8
        height = 6
        properties = {
          title  = "Read / Write Latency (ms)"
          region = var.aws_region
          metrics = [
            ["AWS/RDS", "ReadLatency", "DBInstanceIdentifier", var.rds_instance_identifier,
            { stat = "Average", color = "#1f77b4", label = "Read Latency" }],
            ["AWS/RDS", "WriteLatency", "DBInstanceIdentifier", var.rds_instance_identifier,
            { stat = "Average", color = "#ff7f0e", label = "Write Latency" }]
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
          title  = "Read / Write IOPS"
          region = var.aws_region
          metrics = [
            ["AWS/RDS", "ReadIOPS", "DBInstanceIdentifier", var.rds_instance_identifier,
            { stat = "Average", color = "#1f77b4", label = "Read IOPS" }],
            ["AWS/RDS", "WriteIOPS", "DBInstanceIdentifier", var.rds_instance_identifier,
            { stat = "Average", color = "#ff7f0e", label = "Write IOPS" }]
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
          title  = "Read / Write Throughput (bytes/s)"
          region = var.aws_region
          metrics = [
            ["AWS/RDS", "ReadThroughput", "DBInstanceIdentifier", var.rds_instance_identifier,
            { stat = "Average", color = "#1f77b4", label = "Read" }],
            ["AWS/RDS", "WriteThroughput", "DBInstanceIdentifier", var.rds_instance_identifier,
            { stat = "Average", color = "#ff7f0e", label = "Write" }]
          ]
          view    = "timeSeries"
          stacked = false
          period  = 300
        }
      },

      # ── Row 3: Memory & Disk ─────────────────────────────────────
      {
        type   = "metric"
        x      = 0
        y      = 12
        width  = 8
        height = 6
        properties = {
          title  = "Freeable Memory (bytes)"
          region = var.aws_region
          metrics = [
            ["AWS/RDS", "FreeableMemory", "DBInstanceIdentifier", var.rds_instance_identifier,
            { stat = "Average", color = "#2ca02c", label = "Freeable Memory" }]
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
          title  = "Disk Queue Depth"
          region = var.aws_region
          metrics = [
            ["AWS/RDS", "DiskQueueDepth", "DBInstanceIdentifier", var.rds_instance_identifier,
            { stat = "Average", color = "#9467bd", label = "Queue Depth" }]
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
          title  = "Network Throughput (bytes/s)"
          region = var.aws_region
          metrics = [
            ["AWS/RDS", "NetworkReceiveThroughput", "DBInstanceIdentifier", var.rds_instance_identifier,
            { stat = "Average", color = "#1f77b4", label = "Receive" }],
            ["AWS/RDS", "NetworkTransmitThroughput", "DBInstanceIdentifier", var.rds_instance_identifier,
            { stat = "Average", color = "#ff7f0e", label = "Transmit" }]
          ]
          view    = "timeSeries"
          stacked = false
          period  = 300
        }
      },

      # ── Row 4: Errors ─────────────────────────────────────────
      {
        type   = "metric"
        x      = 0
        y      = 18
        width  = 12
        height = 6
        properties = {
          title  = "Deadlocks & Aborted Connections"
          region = var.aws_region
          metrics = [
            ["AWS/RDS", "Deadlocks", "DBInstanceIdentifier", var.rds_instance_identifier,
            { stat = "Sum", color = "#d62728", label = "Deadlocks" }],
            ["AWS/RDS", "AbortedClients", "DBInstanceIdentifier", var.rds_instance_identifier,
            { stat = "Sum", color = "#ff7f0e", label = "Aborted Clients" }]
          ]
          view    = "timeSeries"
          stacked = false
          period  = 300
        }
      }
    ]
  })
}
