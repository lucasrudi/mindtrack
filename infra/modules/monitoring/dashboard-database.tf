# =============================================================================
# Database Deep-Dive Dashboard — Aurora Serverless v2
# =============================================================================

resource "aws_cloudwatch_dashboard" "database" {
  dashboard_name = "${var.name_prefix}-database"

  dashboard_body = jsonencode({
    widgets = [
      # ── Row 1: Capacity & CPU ───────────────────────────────────
      {
        type   = "metric"
        x      = 0
        y      = 0
        width  = 8
        height = 6
        properties = {
          title  = "Serverless ACU Capacity"
          region = var.aws_region
          metrics = [
            ["AWS/RDS", "ServerlessDatabaseCapacity", "DBClusterIdentifier", var.rds_cluster_identifier,
            { stat = "Average", color = "#1f77b4", label = "Average ACU" }],
            ["AWS/RDS", "ServerlessDatabaseCapacity", "DBClusterIdentifier", var.rds_cluster_identifier,
            { stat = "Maximum", color = "#d62728", label = "Max ACU" }]
          ]
          view    = "timeSeries"
          stacked = false
          period  = 300
          annotations = {
            horizontal = [
              {
                label = "Min Capacity"
                value = 0.5
                color = "#2ca02c"
              },
              {
                label = "Max Capacity"
                value = 2
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
          title  = "CPU Utilization (%)"
          region = var.aws_region
          metrics = [
            ["AWS/RDS", "CPUUtilization", "DBClusterIdentifier", var.rds_cluster_identifier,
            { stat = "Average", color = "#1f77b4", label = "Average CPU" }],
            ["AWS/RDS", "CPUUtilization", "DBClusterIdentifier", var.rds_cluster_identifier,
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
        x      = 16
        y      = 0
        width  = 8
        height = 6
        properties = {
          title  = "Database Connections"
          region = var.aws_region
          metrics = [
            ["AWS/RDS", "DatabaseConnections", "DBClusterIdentifier", var.rds_cluster_identifier,
            { stat = "Average", color = "#1f77b4", label = "Average" }],
            ["AWS/RDS", "DatabaseConnections", "DBClusterIdentifier", var.rds_cluster_identifier,
            { stat = "Maximum", color = "#d62728", label = "Maximum" }]
          ]
          view    = "timeSeries"
          stacked = false
          period  = 300
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
            ["AWS/RDS", "ReadLatency", "DBClusterIdentifier", var.rds_cluster_identifier,
            { stat = "Average", color = "#1f77b4", label = "Read Latency" }],
            ["AWS/RDS", "WriteLatency", "DBClusterIdentifier", var.rds_cluster_identifier,
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
            ["AWS/RDS", "ReadIOPS", "DBClusterIdentifier", var.rds_cluster_identifier,
            { stat = "Average", color = "#1f77b4", label = "Read IOPS" }],
            ["AWS/RDS", "WriteIOPS", "DBClusterIdentifier", var.rds_cluster_identifier,
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
            ["AWS/RDS", "ReadThroughput", "DBClusterIdentifier", var.rds_cluster_identifier,
            { stat = "Average", color = "#1f77b4", label = "Read" }],
            ["AWS/RDS", "WriteThroughput", "DBClusterIdentifier", var.rds_cluster_identifier,
            { stat = "Average", color = "#ff7f0e", label = "Write" }]
          ]
          view    = "timeSeries"
          stacked = false
          period  = 300
        }
      },

      # ── Row 3: Storage & Memory ─────────────────────────────────
      {
        type   = "metric"
        x      = 0
        y      = 12
        width  = 8
        height = 6
        properties = {
          title  = "Aurora Storage (bytes)"
          region = var.aws_region
          metrics = [
            ["AWS/RDS", "VolumeBytesUsed", "DBClusterIdentifier", var.rds_cluster_identifier,
            { stat = "Average", color = "#1f77b4", label = "Volume Size" }]
          ]
          view    = "timeSeries"
          stacked = false
          period  = 3600
        }
      },
      {
        type   = "metric"
        x      = 8
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
        x      = 16
        y      = 12
        width  = 8
        height = 6
        properties = {
          title  = "Buffer Cache Hit Ratio (%)"
          region = var.aws_region
          metrics = [
            ["AWS/RDS", "BufferCacheHitRatio", "DBClusterIdentifier", var.rds_cluster_identifier,
            { stat = "Average", color = "#2ca02c", label = "Cache Hit %" }]
          ]
          view    = "timeSeries"
          stacked = false
          period  = 300
          yAxis = {
            left = { min = 0, max = 100 }
          }
        }
      },

      # ── Row 4: DML & Deadlocks ─────────────────────────────────
      {
        type   = "metric"
        x      = 0
        y      = 18
        width  = 12
        height = 6
        properties = {
          title  = "DML Throughput (operations/s)"
          region = var.aws_region
          metrics = [
            ["AWS/RDS", "SelectThroughput", "DBClusterIdentifier", var.rds_cluster_identifier,
            { stat = "Average", color = "#1f77b4", label = "SELECT" }],
            ["AWS/RDS", "InsertThroughput", "DBClusterIdentifier", var.rds_cluster_identifier,
            { stat = "Average", color = "#2ca02c", label = "INSERT" }],
            ["AWS/RDS", "UpdateThroughput", "DBClusterIdentifier", var.rds_cluster_identifier,
            { stat = "Average", color = "#ff7f0e", label = "UPDATE" }],
            ["AWS/RDS", "DeleteThroughput", "DBClusterIdentifier", var.rds_cluster_identifier,
            { stat = "Average", color = "#d62728", label = "DELETE" }]
          ]
          view    = "timeSeries"
          stacked = false
          period  = 300
        }
      },
      {
        type   = "metric"
        x      = 12
        y      = 18
        width  = 12
        height = 6
        properties = {
          title  = "Deadlocks & Aborted Connections"
          region = var.aws_region
          metrics = [
            ["AWS/RDS", "Deadlocks", "DBClusterIdentifier", var.rds_cluster_identifier,
            { stat = "Sum", color = "#d62728", label = "Deadlocks" }],
            ["AWS/RDS", "AbortedClients", "DBClusterIdentifier", var.rds_cluster_identifier,
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
