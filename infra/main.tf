terraform {
  required_providers {
    azurerm = {
      source  = "hashicorp/azurerm"
      version = "~> 3.0"
    }
  }
}

provider "azurerm" {
  features {}
}

resource "azurerm_resource_group" "rg" {
  name     = var.resource_group_name
  location = var.location
}

resource "azurerm_log_analytics_workspace" "law" {
  name                = "law-aimata"
  location            = azurerm_resource_group.rg.location
  resource_group_name = azurerm_resource_group.rg.name
  sku                 = "PerGB2018"
  retention_in_days   = 30
}

resource "azurerm_container_registry" "acr" {
  name                = "acraimatavision"
  resource_group_name = azurerm_resource_group.rg.name
  location            = azurerm_resource_group.rg.location
  sku                 = "Basic"
  admin_enabled       = true
}

resource "azurerm_container_app_environment" "env" {
  name                       = "cae-aimata"
  location                   = azurerm_resource_group.rg.location
  resource_group_name        = azurerm_resource_group.rg.name
  log_analytics_workspace_id = azurerm_log_analytics_workspace.law.id
}

resource "azurerm_container_app" "app" {
  name                         = "ca-aimata-backend"
  container_app_environment_id = azurerm_container_app_environment.env.id
  resource_group_name          = azurerm_resource_group.rg.name
  revision_mode                = "Single"

  registry {
    server               = azurerm_container_registry.acr.login_server
    username             = azurerm_container_registry.acr.admin_username
    password_secret_name = "registry-password"
  }

  ingress {
    allow_insecure_connections = false
    external_enabled           = true
    target_port                = 8000
    traffic_weight {
      latest_revision = true
      percentage      = 100
    }
  }

  secret {
    name  = "registry-password"
    value = azurerm_container_registry.acr.admin_password
  }
  secret {
    name  = "stream-api-key"
    value = var.stream_api_key
  }
  secret {
    name  = "stream-api-secret"
    value = var.stream_api_secret
  }
  secret {
    name  = "google-api-key"
    value = var.google_api_key
  }
  secret {
    name  = "elevenlabs-api-key"
    value = var.elevenlabs_api_key
  }
  secret {
    name  = "deepgram-api-key"
    value = var.deepgram_api_key
  }

  template {
    container {
      name   = "agent-container"
      image  = "${azurerm_container_registry.acr.login_server}/ai-mata-agent:latest"
      cpu    = 0.5
      memory = "1.0Gi"

      env {
        name        = "STREAM_API_KEY"
        secret_name = "stream-api-key"
      }
      env {
        name        = "STREAM_API_SECRET"
        secret_name = "stream-api-secret"
      }
      env {
        name        = "GOOGLE_API_KEY"
        secret_name = "google-api-key"
      }
      env {
        name        = "ELEVENLABS_API_KEY"
        secret_name = "elevenlabs-api-key"
      }
      env {
        name        = "DEEPGRAM_API_KEY"
        secret_name = "deepgram-api-key"
      }
    }
  }
}
