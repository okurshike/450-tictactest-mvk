# Statisches Frontend als S3 Static Website. Öffentlich lesbar: hier darf nie etwas Geheimes liegen.
# Kein aws_s3_bucket: der Provider liest dabei immer die Object-Lock-Konfiguration,
# und das verbietet die Service Control Policy des Learner Labs (AccessDenied).
# Darum legt die AWS CLI den Bucket an und löscht ihn bei tofu destroy samt Inhalt.
data "aws_caller_identity" "current" {}

resource "terraform_data" "frontend_bucket" {
  input = "tictactest-frontend-${data.aws_caller_identity.current.account_id}" # Bucket-Namen sind weltweit eindeutig

  provisioner "local-exec" {
    command = "aws s3 mb s3://${self.input} --region ${var.region}"
  }
  provisioner "local-exec" {
    when    = destroy
    command = "aws s3 rb s3://${self.input} --force"
  }
}

resource "aws_s3_bucket_website_configuration" "frontend" {
  bucket = terraform_data.frontend_bucket.output
  index_document {
    suffix = "index.html"
  }
}

resource "aws_s3_bucket_public_access_block" "frontend" {
  bucket                  = terraform_data.frontend_bucket.output
  block_public_acls       = true
  ignore_public_acls      = true
  block_public_policy     = false
  restrict_public_buckets = false
}

resource "aws_s3_bucket_policy" "frontend" {
  bucket = terraform_data.frontend_bucket.output
  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [{
      Effect    = "Allow"
      Principal = "*"
      Action    = "s3:GetObject"
      Resource  = "arn:aws:s3:::${terraform_data.frontend_bucket.output}/*"
    }]
  })
  depends_on = [aws_s3_bucket_public_access_block.frontend]
}

# Nur die Infrastruktur kennt die Adresse der Instanz, darum schreibt OpenTofu config.js.
# Das Frontend-Deployment muss diese Datei auslassen (aws s3 sync --exclude config.js).
resource "aws_s3_object" "config" {
  bucket        = terraform_data.frontend_bucket.output
  key           = "config.js"
  content_type  = "application/javascript"
  cache_control = "no-store"
  content = templatefile("${path.module}/templates/config.js.tftpl", {
    console_url = "http://${aws_eip.tictactest.public_ip}/"
    version_url = "http://${aws_eip.tictactest.public_ip}/version"
  })
}
