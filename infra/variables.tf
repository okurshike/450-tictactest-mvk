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

# Woher die Instanz die Release-JARs lädt (öffentliches Repository)
variable "github_repo" {
  type    = string
  default = "okurshike/450-tictactest-mvk"
}

# Feste Version und Prüfsumme, damit jede neue Instanz genau dasselbe ttyd bekommt
variable "ttyd_version" {
  type    = string
  default = "1.7.7"
}

variable "ttyd_sha256" {
  type    = string
  default = "8a217c968aba172e0dbf3f34447218dc015bc4d5e59bf51db2f2cd12b7be4f55"
}
