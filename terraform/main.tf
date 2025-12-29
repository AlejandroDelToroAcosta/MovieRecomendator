# Define el proveedor AWS
terraform {
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"
    }
  }
}

# Configura la región de AWS (ej: eu-west-1)
provider "aws" {
  region = "us-east-1"
}

# 1. Búsqueda de la AMI de Ubuntu más reciente (Común para Neo4j)
data "aws_ami" "ubuntu" {
  most_recent = true
  filter {
    name   = "name"
    values = ["ubuntu/images/hvm-ssd/ubuntu-jammy-22.04-amd64-server-*"]
  }
  filter {
    name   = "virtualization-type"
    values = ["hvm"]
  }
  owners = ["099720109477"] # Canonical
}

# 2. Creación del Security Group (Reglas de Inbound)
resource "aws_security_group" "neo4j_sg" {
  name        = "neo4j-server-sg"
  description = "Allow SSH (22) and Neo4j Bolt (7687)"
  vpc_id      = "vpc-09369dd8d8f1b641d"

  # Regla 1: Acceso SSH (Puerto 22) - [IP Pública corregida con /32]
  ingress {
    from_port   = 22
    to_port     = 22
    protocol    = "tcp"
    cidr_blocks = ["83.59.186.130/32"]
    description = "SSH Access"
  }

  # Regla 2: Protocolo Bolt (Puerto 7687) - [IP Pública corregida con /32]
  ingress {
    from_port   = 7687
    to_port     = 7687
    protocol    = "tcp"
    cidr_blocks = ["83.59.186.130/32"]
    description = "Neo4j Bolt Protocol"
  }

  # Regla 3 (Opcional): Neo4j Browser (Puerto 7474) - [IP Pública corregida con /32]
  ingress {
    from_port   = 7474
    to_port     = 7474
    protocol    = "tcp"
    cidr_blocks = ["83.59.186.130/32"]
    description = "Neo4j Browser HTTP"
  }

  # Regla de Salida (Permitir todo el tráfico saliente)
  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = {
    Name = "Neo4j-Server-SG"
  }
}

# 3. Creación de la Instancia EC2
resource "aws_instance" "neo4j_server" {
  ami           = data.aws_ami.ubuntu.id
  instance_type = "t2.small"
  key_name      = "clave-hoy"

  # Adjuntar el Security Group y especificar la subred
  vpc_security_group_ids = [aws_security_group.neo4j_sg.id]
  subnet_id              = "subnet-07a260d7818c42e1e"

  tags = {
    Name = "Neo4j-Datamart-Server"
  }
  provisioner "remote-exec" {
      inline = [
        "sudo apt update",
        "sudo apt install -y openjdk-17-jdk", # 1. Instalar Java (requerido por Neo4j)

        # 2. Configurar Repositorio de Neo4j (asumiendo Ubuntu 22.04)
        "wget -O - https://debian.neo4j.com/neotechnology.gpg.key | sudo gpg --dearmor | sudo tee /usr/share/keyrings/neo4j.gpg >/dev/null",
        "echo 'deb [signed-by=/usr/share/keyrings/neo4j.gpg] https://debian.neo4j.com stable latest' | sudo tee /etc/apt/sources.list.d/neo4j.list",

        # 3. Instalar y habilitar el servicio Neo4j
        "sudo apt update",
        "sudo apt install -y neo4j",
        "sudo systemctl enable neo4j",

        # 4. Configurar el acceso remoto (Bolt: 7687 y HTTP: 7474)
        # Cambia la escucha de localhost a 0.0.0.0
        "sudo sed -i 's/#server.bolt.listen_address=:7687/server.bolt.listen_address=0.0.0.0:7687/' /etc/neo4j/neo4j.conf",
        "sudo sed -i 's/#server.http.listen_address=:7474/server.http.listen_address=0.0.0.0:7474/' /etc/neo4j/neo4j.conf",
        "sudo sed -i 's/#dbms.connector.bolt.listen_address=0.0.0.0:7687/dbms.connector.bolt.listen_address=0.0.0.0:7687/' /etc/neo4j/neo4j.conf",
        "sudo sed -i 's/#dbms.connector.http.listen_address=0.0.0.0:7474/dbms.connector.http.listen_address=0.0.0.0:7474/' /etc/neo4j/neo4j.conf",

        # 5. Reiniciar Neo4j para aplicar la configuración y empezar a escuchar
        "sudo systemctl restart neo4j",
      ]
  connection {
        type        = "ssh"
        user        = "ubuntu" # Usuario estándar para la AMI de Ubuntu
        private_key = file("./clave-hoy.txt") # Ruta a tu archivo .pem
        host        = self.public_ip
      }
  }
}

# 4. Salida: Dirección IP Pública
output "neo4j_public_ip" {
  description = "Public IP address of the Neo4j server"
  value       = aws_instance.neo4j_server.public_ip
}

# 1. Cola de mensajes fallidos (Dead Letter Queue)
resource "aws_sqs_queue" "movies_dlq" {
  name = "movies-ingestion-dlq"
}

# 2. Cola principal SQS
resource "aws_sqs_queue" "movies_queue" {
  name                      = "movies-ingestion-queue"
  delay_seconds             = 0
  max_message_size          = 262144
  message_retention_seconds = 86400


  visibility_timeout_seconds = 100

  redrive_policy = jsonencode({
    deadLetterTargetArn = aws_sqs_queue.movies_dlq.arn
    maxReceiveCount     = 5 # Si falla 5 veces, va a la DLQ
  })

  tags = {
    Environment = "Dev"
    Project     = "Datamart-Movies"
  }
}

output "sqs_queue_url" {
  value       = aws_sqs_queue.movies_queue.url
  description = "URL de la cola SQS"
}