terraform {
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 6.0"
    }
  }
  # shortcut: State liegt lokal und ist nicht geteilt, bei Arbeit im Team auf ein S3-Backend wechseln
}

variable "region" {
  type    = string
  default = "us-east-1"
}

variable "instance_type" {
  type    = string
  default = "t2.micro"
}

variable "public_key_path" {
  type    = string
  default = "~/.ssh/tictactest.pub"
}

# Bewusst ohne Default, damit Port 22 nicht aus Versehen für die ganze Welt offen ist
variable "ssh_cidr" {
  type        = string
  description = "IP-Bereich mit SSH-Zugriff, z. B. 203.0.113.7/32"
}

provider "aws" {
  region = var.region
}

data "aws_ami" "amazon_linux" {
  most_recent = true
  owners      = ["amazon"]

  filter {
    name   = "name"
    values = ["al2023-ami-2023.*-x86_64"]
  }
}

resource "aws_key_pair" "tictactest" {
  key_name   = "tictactest"
  public_key = file(pathexpand(var.public_key_path))
}

resource "aws_security_group" "tictactest" {
  name        = "tictactest"
  description = "SSH access to the TicTacTest instance"

  ingress {
    description = "SSH"
    from_port   = 22
    to_port     = 22
    protocol    = "tcp"
    cidr_blocks = [var.ssh_cidr]
  }

  # Terraform entfernt die Standard-Egress-Regel, ohne diese kämen keine Pakete oder Images auf die Instanz
  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }
}

resource "aws_instance" "tictactest" {
  ami                         = data.aws_ami.amazon_linux.id
  instance_type               = var.instance_type
  key_name                    = aws_key_pair.tictactest.key_name
  vpc_security_group_ids      = [aws_security_group.tictactest.id]
  associate_public_ip_address = true

  tags = {
    Name = "tictactest"
  }
}

output "public_ip" {
  value = aws_instance.tictactest.public_ip
}

output "public_dns" {
  value = aws_instance.tictactest.public_dns
}

output "ssh_command" {
  value = "ssh -i ~/.ssh/tictactest ec2-user@${aws_instance.tictactest.public_ip}"
}
