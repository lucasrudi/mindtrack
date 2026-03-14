resource "aws_kms_key" "rds" {
  description             = "${var.name_prefix} RDS encryption key"
  deletion_window_in_days = 7
  enable_key_rotation     = true
}

resource "aws_db_subnet_group" "main" {
  name       = "${var.name_prefix}-db-subnet-group"
  subnet_ids = var.subnet_ids

  tags = {
    Name = "${var.name_prefix}-db-subnet-group"
  }
}

resource "aws_security_group" "rds" {
  name        = "${var.name_prefix}-rds-sg"
  description = "Security group for Aurora Serverless v2"
  vpc_id      = var.vpc_id

  ingress {
    from_port       = 3306
    to_port         = 3306
    protocol        = "tcp"
    security_groups = [var.lambda_sg_id]
    description     = "MySQL access from Lambda"
  }

  tags = {
    Name = "${var.name_prefix}-rds-sg"
  }
}

resource "aws_rds_cluster" "main" {
  cluster_identifier = "${var.name_prefix}-aurora"
  engine             = "aurora-mysql"
  engine_mode        = "provisioned"
  engine_version     = "8.0.mysql_aurora.3.07.1"
  database_name      = "mindtrack"
  master_username    = "mindtrack_admin"

  manage_master_user_password = true

  db_subnet_group_name   = aws_db_subnet_group.main.name
  vpc_security_group_ids = [aws_security_group.rds.id]

  serverlessv2_scaling_configuration {
    min_capacity = var.min_capacity
    max_capacity = var.max_capacity
  }

  skip_final_snapshot       = false
  final_snapshot_identifier = "${var.name_prefix}-final-snapshot"
  deletion_protection       = true
  backup_retention_period   = 30
  storage_encrypted         = true
  kms_key_id                = aws_kms_key.rds.arn

  tags = {
    Name = "${var.name_prefix}-aurora"
  }
}

resource "aws_rds_cluster_instance" "main" {
  cluster_identifier = aws_rds_cluster.main.id
  instance_class     = "db.serverless"
  engine             = aws_rds_cluster.main.engine
  engine_version     = aws_rds_cluster.main.engine_version

  performance_insights_enabled    = true
  performance_insights_kms_key_id = aws_kms_key.rds.arn

  tags = {
    Name = "${var.name_prefix}-aurora-instance"
  }
}
