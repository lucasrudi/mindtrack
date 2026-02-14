output "repository_full_name" {
  description = "Full name of the repository (org/name)"
  value       = github_repository.this.full_name
}

output "repository_html_url" {
  description = "URL to the repository on GitHub"
  value       = github_repository.this.html_url
}

output "repository_ssh_clone_url" {
  description = "SSH clone URL"
  value       = github_repository.this.ssh_clone_url
}
