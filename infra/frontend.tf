# Statisches Frontend als S3 Static Website. Öffentlich lesbar: hier darf nie etwas Geheimes liegen.
resource "aws_s3_bucket" "frontend" {
  bucket_prefix = "tictactest-frontend-" # Bucket-Namen sind weltweit eindeutig
  force_destroy = true                   # tofu destroy löscht den Bucket auch mit Inhalt
}

resource "aws_s3_bucket_website_configuration" "frontend" {
  bucket = aws_s3_bucket.frontend.id
  index_document {
    suffix = "index.html"
  }
}

resource "aws_s3_bucket_public_access_block" "frontend" {
  bucket                  = aws_s3_bucket.frontend.id
  block_public_acls       = true
  ignore_public_acls      = true
  block_public_policy     = false
  restrict_public_buckets = false
}

resource "aws_s3_bucket_policy" "frontend" {
  bucket = aws_s3_bucket.frontend.id
  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [{
      Effect    = "Allow"
      Principal = "*"
      Action    = "s3:GetObject"
      Resource  = "${aws_s3_bucket.frontend.arn}/*"
    }]
  })
  depends_on = [aws_s3_bucket_public_access_block.frontend]
}

# Nur die Infrastruktur kennt die Adresse der Instanz, darum schreibt OpenTofu config.js.
# Das Frontend-Deployment muss diese Datei auslassen (aws s3 sync --exclude config.js).
resource "aws_s3_object" "config" {
  bucket        = aws_s3_bucket.frontend.id
  key           = "config.js"
  content_type  = "application/javascript"
  cache_control = "no-store"
  content = templatefile("${path.module}/templates/config.js.tftpl", {
    console_url = "http://${aws_eip.tictactest.public_ip}/"
    version_url = "http://${aws_eip.tictactest.public_ip}/version"
  })
}
