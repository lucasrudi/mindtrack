package test

import (
	"testing"

	"github.com/gruntwork-io/terratest/modules/terraform"
)

func TestTerraformPlan(t *testing.T) {
	t.Parallel()

	terraformOptions := terraform.WithDefaultRetryableErrors(t, &terraform.Options{
		TerraformDir: "../../",
		VarFiles:     []string{"environments/dev.tfvars"},
		NoColor:      true,
	})

	// Run terraform init and plan only (no apply)
	terraform.InitAndPlan(t, terraformOptions)
}
