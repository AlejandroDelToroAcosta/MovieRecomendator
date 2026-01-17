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
  vpc_id      = "vpc-02d8239087724b96b"

  # Regla para la API (Puerto 8080)
  ingress {
    from_port   = 8080
    to_port     = 8080
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"] # O tu IP específica para mayor seguridad
    description = "API Access"
  }

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

  # Regla para acceso para la lambda
  ingress {
    from_port   = 7687
    to_port     = 7687
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
    description = "Neo4j Bolt Protocol (Global Access)"
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
  subnet_id              = "subnet-03e94c67e8ff11d9a"

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
# Asegurar que acepta conexiones de fuera (v5 syntax)
        "sudo sed -i 's/#server.default_listen_address=0.0.0.0/server.default_listen_address=0.0.0.0/' /etc/neo4j/neo4j.conf",
        "sudo sed -i 's/#server.default_advertised_address=localhost/server.default_advertised_address=${self.public_ip}/' /etc/neo4j/neo4j.conf",
        # 5. Reiniciar Neo4j para aplicar la configuración y empezar a escuchar
        "sudo systemctl restart neo4j",
        # 6. Esperar a que el servicio esté listo

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
# Instancia para la API
resource "aws_instance" "api_server" {
  ami           = data.aws_ami.ubuntu.id
  instance_type = "t2.micro"
  key_name      = "clave-hoy"

  vpc_security_group_ids = [aws_security_group.neo4j_sg.id]
  subnet_id = "subnet-03e94c67e8ff11d9a"

  tags = {
    Name = "API-Movie-Server"
  }

  # 1. CONEXIÓN SSH
  connection {
    type = "ssh"
    user = "ubuntu"
    private_key = file("./clave-hoy.txt")
    host = self.public_ip
  }

  # 2. INSTALAR DOCKER (Remote-exec)
  provisioner "remote-exec" {
    inline = [
      "sudo apt-get update",
      "sudo apt-get install -y docker.io",
      "sudo systemctl start docker",
      "sudo systemctl enable docker",
      "sudo usermod -aG docker ubuntu",
      "mkdir -p ~/app"
    ]
  }
}
  # 2. Output para saber la IP de la nueva API
output "api_public_ip" {
  value = aws_instance.api_server.public_ip
}
# 1. Crear la API Gateway (Tipo HTTP)
resource "aws_apigatewayv2_api" "api_gateway" {
  name          = "movies-api-gateway"
  protocol_type = "HTTP"
}

# 2. Configurar la Integración con tu EC2
resource "aws_apigatewayv2_integration" "api_integration" {
  api_id           = aws_apigatewayv2_api.api_gateway.id
  integration_type = "HTTP_PROXY"

  # Aquí usamos la IP pública de tu instancia y el puerto 8080
  integration_uri    = "http://${aws_instance.api_server.public_ip}:8080/{proxy}"
  integration_method = "ANY"
  payload_format_version = "1.0"
}

# 3. Crear la Ruta (Captura todo: /movie, /actor, etc.)
resource "aws_apigatewayv2_route" "api_route" {
  api_id    = aws_apigatewayv2_api.api_gateway.id
  route_key = "ANY /{proxy+}"
  target    = "integrations/${aws_apigatewayv2_integration.api_integration.id}"
}

# 4. Desplegar la API (Stage $default para que se publique al instante)
resource "aws_apigatewayv2_stage" "api_stage" {
  api_id      = aws_apigatewayv2_api.api_gateway.id
  name        = "$default"
  auto_deploy = true
}

# 5. Output para obtener la URL del Gateway
output "gateway_url" {
  value = aws_apigatewayv2_api.api_gateway.api_endpoint
}
