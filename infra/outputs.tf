output "frontend_url" {
  description = "Hier spielt man"
  value       = "http://${aws_s3_bucket_website_configuration.frontend.website_endpoint}"
}

output "console_url" {
  value = "http://${aws_eip.tictactest.public_ip}/"
}

output "version_url" {
  value = "http://${aws_eip.tictactest.public_ip}/version"
}

# Für das Deployment (Auftrag 3)
output "instance_id" {
  value = aws_instance.tictactest.id
}

output "frontend_bucket" {
  value = aws_s3_bucket.frontend.id
}

output "public_ip" {
  value = aws_eip.tictactest.public_ip
}

output "ssh_command" {
  value = "ssh -i ~/.ssh/tictactest ec2-user@${aws_eip.tictactest.public_ip}"
}
