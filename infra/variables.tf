variable "resource_group_name" {
  type    = string
  default = "rg-aimata-hackathon"
}

variable "location" {
  type    = string
  default = "eastus2"
}

variable "stream_api_key" {
  type      = string
  sensitive = true
}

variable "stream_api_secret" {
  type      = string
  sensitive = true
}

variable "google_api_key" {
  type      = string
  sensitive = true
}

variable "elevenlabs_api_key" {
  type      = string
  sensitive = true
}

variable "deepgram_api_key" {
  type      = string
  sensitive = true
}
