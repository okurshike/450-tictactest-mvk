terraform {
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 6.0"
    }
    random = {
      source  = "hashicorp/random"
      version = "~> 3.0"
    }
  }
  # shortcut: State liegt lokal und ist nicht geteilt, bei Arbeit im Team auf ein S3-Backend wechseln
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

# Im Learner Lab sind eigene IAM-Rollen verboten. Das vorhandene Profil gibt der Instanz
# AWS-Zugriff (z. B. für SSM) ohne Access Keys auf der Instanz.
data "aws_iam_instance_profile" "lab" {
  name = "LabInstanceProfile"
}

resource "aws_key_pair" "tictactest" {
  key_name   = "tictactest"
  public_key = file(pathexpand(var.public_key_path))
}

resource "aws_security_group" "tictactest" {
  name        = "tictactest"
  description = "TicTacTest web console"

  # nginx: Webkonsole und Versionsabfrage, muss für alle Spielenden offen sein
  ingress {
    description = "HTTP"
    from_port   = 80
    to_port     = 80
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  # Nur zur Fehlersuche, nur von der eigenen IP. ttyd (7681) ist nicht offen, nur über nginx erreichbar.
  ingress {
    description = "SSH"
    from_port   = 22
    to_port     = 22
    protocol    = "tcp"
    cidr_blocks = [var.ssh_cidr]
  }

  # Pakete, ttyd, JARs von GitHub und SSM brauchen ausgehende Verbindungen
  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }
}

resource "aws_instance" "tictactest" {
  ami                    = data.aws_ami.amazon_linux.id
  instance_type          = var.instance_type
  key_name               = aws_key_pair.tictactest.key_name
  vpc_security_group_ids = [aws_security_group.tictactest.id]
  iam_instance_profile   = data.aws_iam_instance_profile.lab.name

  # Richtet nur die Laufzeit ein. Welche Version läuft, entscheidet das Deployment
  # (/etc/tictactest.env), deshalb steht hier keine Version.
  user_data = templatefile("${path.module}/templates/user_data.sh.tftpl", {
    github_repo     = var.github_repo
    ttyd_version    = var.ttyd_version
    ttyd_sha256     = var.ttyd_sha256
    frontend_origin = "http://${aws_s3_bucket_website_configuration.frontend.website_endpoint}"
  })
  # user_data läuft nur beim ersten Start. Ohne Neuerstellung würde eine Änderung nie wirken.
  user_data_replace_on_change = true

  tags = {
    Name = "tictactest"
  }
}

# Feste Adresse: überlebt Neustarts im Learner Lab und sogar eine neu erstellte Instanz
resource "aws_eip" "tictactest" {
  domain = "vpc"
  tags = {
    Name = "tictactest"
  }
}

resource "aws_eip_association" "tictactest" {
  instance_id   = aws_instance.tictactest.id
  allocation_id = aws_eip.tictactest.id
}
