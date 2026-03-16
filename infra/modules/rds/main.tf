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
  description = "Security group for RDS MySQL"
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

resource "aws_db_instance" "main" {
  identifier        = "${var.name_prefix}-mysql"
  engine            = "mysql"
  engine_version    = "8.0"
  instance_class    = "db.t3.micro"
  allocated_storage = 20
  storage_type      = "gp2"

  db_name  = "mindtrack"
  username = "mindtrack_admin"

  manage_master_user_password = true

  db_subnet_group_name   = aws_db_subnet_group.main.name
  vpc_security_group_ids = [aws_security_group.rds.id]

  multi_az            = false
  publicly_accessible = false

  skip_final_snapshot       = false
  final_snapshot_identifier = "${var.name_prefix}-final-snapshot"
  deletion_protection       = true
  #tfsec:ignore:aws-rds-specify-backup-retention
  backup_retention_period = 1 # free tier maximum
  storage_encrypted       = true
  kms_key_id              = aws_kms_key.rds.arn

  tags = {
    Name = "${var.name_prefix}-mysql"
  }
}
