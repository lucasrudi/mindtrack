resource "aws_kms_key" "s3" {
  description             = "${var.name_prefix} S3 encryption key"
  deletion_window_in_days = 7
  enable_key_rotation     = true
}

# Audio bucket with 7-day lifecycle
resource "aws_s3_bucket" "audio" {
  bucket = "${var.name_prefix}-audio"
}

resource "aws_s3_bucket_ownership_controls" "audio" {
  bucket = aws_s3_bucket.audio.id

  rule {
    object_ownership = "BucketOwnerPreferred"
  }
}

resource "aws_s3_bucket_acl" "audio" {
  depends_on = [aws_s3_bucket_ownership_controls.audio]

  bucket = aws_s3_bucket.audio.id
  acl    = "private"
}

data "aws_iam_policy_document" "audio_policy" {
  statement {
    sid    = "DenyInsecureTransport"
    effect = "Deny"

    principals {
      type        = "*"
      identifiers = ["*"]
    }

    actions = ["s3:*"]
    resources = [
      aws_s3_bucket.audio.arn,
      "${aws_s3_bucket.audio.arn}/*",
    ]

    condition {
      test     = "Bool"
      variable = "aws:SecureTransport"
      values   = ["false"]
    }
  }
}

resource "aws_s3_bucket_policy" "audio_policy" {
  bucket = aws_s3_bucket.audio.id
  policy = data.aws_iam_policy_document.audio_policy.json
}

resource "aws_s3_bucket_lifecycle_configuration" "audio_lifecycle" {
  bucket = aws_s3_bucket.audio.id

  rule {
    id     = "retain-audio-14-days"
    status = "Enabled"

    expiration {
      days = 14
    }

    noncurrent_version_expiration {
      noncurrent_days = 14
    }
  }
}

resource "aws_s3_bucket_versioning" "audio_versioning" {
  bucket = aws_s3_bucket.audio.id

  versioning_configuration {
    status = "Enabled"
  }
}

resource "aws_s3_bucket_server_side_encryption_configuration" "audio_encryption" {
  bucket = aws_s3_bucket.audio.id

  rule {
    apply_server_side_encryption_by_default {
      sse_algorithm     = "aws:kms"
      kms_master_key_id = aws_kms_key.s3.arn
    }
  }
}

resource "aws_s3_bucket_public_access_block" "audio_block" {
  bucket = aws_s3_bucket.audio.id

  block_public_acls       = true
  block_public_policy     = true
  ignore_public_acls      = true
  restrict_public_buckets = true
}

# Frontend bucket for static hosting
resource "aws_s3_bucket" "frontend" {
  bucket = "${var.name_prefix}-frontend"
}

resource "aws_s3_bucket_ownership_controls" "frontend" {
  bucket = aws_s3_bucket.frontend.id

  rule {
    object_ownership = "BucketOwnerPreferred"
  }
}

resource "aws_s3_bucket_acl" "frontend" {
  depends_on = [aws_s3_bucket_ownership_controls.frontend]

  bucket = aws_s3_bucket.frontend.id
  acl    = "private"
}

resource "aws_s3_bucket_lifecycle_configuration" "frontend_lifecycle" {
  bucket = aws_s3_bucket.frontend.id

  rule {
    id     = "retain-noncurrent-frontend-14-days"
    status = "Enabled"

    noncurrent_version_expiration {
      noncurrent_days = 14
    }

    abort_incomplete_multipart_upload {
      days_after_initiation = 14
    }
  }
}

resource "aws_s3_bucket_public_access_block" "frontend_block" {
  bucket = aws_s3_bucket.frontend.id

  block_public_acls       = true
  block_public_policy     = true
  ignore_public_acls      = true
  restrict_public_buckets = true
}

resource "aws_s3_bucket_versioning" "frontend_versioning" {
  bucket = aws_s3_bucket.frontend.id

  versioning_configuration {
    status = "Enabled"
  }
}

resource "aws_s3_bucket_server_side_encryption_configuration" "frontend_encryption" {
  bucket = aws_s3_bucket.frontend.id

  rule {
    apply_server_side_encryption_by_default {
      sse_algorithm     = "aws:kms"
      kms_master_key_id = aws_kms_key.s3.arn
    }
  }
}

data "aws_iam_policy_document" "frontend_policy" {
  statement {
    sid       = "AllowCloudFrontRead"
    effect    = "Allow"
    actions   = ["s3:GetObject"]
    resources = ["${aws_s3_bucket.frontend.arn}/*"]

    principals {
      type        = "AWS"
      identifiers = [var.cloudfront_oai_iam_arn]
    }
  }

  statement {
    sid    = "DenyInsecureTransport"
    effect = "Deny"

    principals {
      type        = "*"
      identifiers = ["*"]
    }

    actions = ["s3:*"]
    resources = [
      aws_s3_bucket.frontend.arn,
      "${aws_s3_bucket.frontend.arn}/*",
    ]

    condition {
      test     = "Bool"
      variable = "aws:SecureTransport"
      values   = ["false"]
    }
  }
}

resource "aws_s3_bucket_policy" "frontend_policy" {
  bucket = aws_s3_bucket.frontend.id
  policy = data.aws_iam_policy_document.frontend_policy.json
}

#tfsec:ignore:aws-s3-enable-bucket-logging
resource "aws_s3_bucket" "access_logs" {
  bucket        = "${var.name_prefix}-access-logs"
  force_destroy = true

  tags = {
    Environment = var.environment
    Purpose     = "access-logs"
  }
}

data "aws_iam_policy_document" "access_logs_policy" {
  statement {
    sid    = "DenyInsecureTransport"
    effect = "Deny"

    principals {
      type        = "*"
      identifiers = ["*"]
    }

    actions = ["s3:*"]
    resources = [
      aws_s3_bucket.access_logs.arn,
      "${aws_s3_bucket.access_logs.arn}/*",
    ]

    condition {
      test     = "Bool"
      variable = "aws:SecureTransport"
      values   = ["false"]
    }
  }
}

resource "aws_s3_bucket_policy" "access_logs_policy" {
  bucket = aws_s3_bucket.access_logs.id
  policy = data.aws_iam_policy_document.access_logs_policy.json
}

resource "aws_s3_bucket_ownership_controls" "access_logs" {
  bucket = aws_s3_bucket.access_logs.id

  rule {
    object_ownership = "BucketOwnerPreferred"
  }
}

resource "aws_s3_bucket_acl" "access_logs" {
  depends_on = [aws_s3_bucket_ownership_controls.access_logs]

  bucket = aws_s3_bucket.access_logs.id
  acl    = "log-delivery-write"
}

resource "aws_s3_bucket_lifecycle_configuration" "access_logs_lifecycle" {
  bucket = aws_s3_bucket.access_logs.id

  rule {
    id     = "retain-access-logs-14-days"
    status = "Enabled"

    expiration {
      days = 14
    }

    noncurrent_version_expiration {
      noncurrent_days = 14
    }
  }
}

resource "aws_s3_bucket_public_access_block" "access_logs_block" {
  bucket = aws_s3_bucket.access_logs.id

  block_public_acls       = true
  block_public_policy     = true
  ignore_public_acls      = true
  restrict_public_buckets = true
}

resource "aws_s3_bucket_server_side_encryption_configuration" "access_logs_encryption" {
  bucket = aws_s3_bucket.access_logs.id

  rule {
    apply_server_side_encryption_by_default {
      sse_algorithm     = "aws:kms"
      kms_master_key_id = aws_kms_key.s3.arn
    }
  }
}

resource "aws_s3_bucket_versioning" "access_logs_versioning" {
  bucket = aws_s3_bucket.access_logs.id

  versioning_configuration {
    status = "Enabled"
  }
}

resource "aws_s3_bucket_logging" "audio_logging" {
  depends_on = [aws_s3_bucket_acl.access_logs]

  bucket        = aws_s3_bucket.audio.id
  target_bucket = aws_s3_bucket.access_logs.id
  target_prefix = "audio/"
}

resource "aws_s3_bucket_logging" "frontend_logging" {
  depends_on = [aws_s3_bucket_acl.access_logs]

  bucket        = aws_s3_bucket.frontend.id
  target_bucket = aws_s3_bucket.access_logs.id
  target_prefix = "frontend/"
}
