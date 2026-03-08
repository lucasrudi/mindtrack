data "aws_availability_zones" "available" {
  state = "available"
}

resource "aws_db_subnet_group" "main" {
  name       = "${var.name_prefix}-db-subnet-group"
  subnet_ids = data.aws_subnets.private.ids

  tags = {
    Name = "${var.name_prefix}-db-subnet-group"
  }
}

data "aws_vpc" "default" {
  default = true
}

data "aws_subnets" "private" {
  filter {
    name   = "vpc-id"
    values = [data.aws_vpc.default.id]
  }
}

resource "aws_security_group" "rds" {
  name        = "${var.name_prefix}-rds-sg"
  description = "Security group for Aurora Serverless v2"
  vpc_id      = data.aws_vpc.default.id

  ingress {
    from_port       = 3306
    to_port         = 3306
    protocol        = "tcp"
    security_groups = [var.lambda_sg_id]
    description     = "MySQL access from Lambda"
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
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

  tags = {
    Name = "${var.name_prefix}-aurora"
  }
}

resource "aws_rds_cluster_instance" "main" {
  cluster_identifier = aws_rds_cluster.main.id
  instance_class     = "db.serverless"
  engine             = aws_rds_cluster.main.engine
  engine_version     = aws_rds_cluster.main.engine_version

  tags = {
    Name = "${var.name_prefix}-aurora-instance"
  }
}
