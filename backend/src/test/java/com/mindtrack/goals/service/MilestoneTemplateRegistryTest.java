package com.mindtrack.goals.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class MilestoneTemplateRegistryTest {

    private final MilestoneTemplateRegistry registry = new MilestoneTemplateRegistry();

    @Test
    void returnsTemplatesForKnownCategory() {
        var templates = registry.getTemplates("Mental Health");
        assertThat(templates).isNotEmpty();
        assertThat(templates).allMatch(t -> t.title() != null && !t.title().isBlank());
    }

    @Test
    void returnsDefaultTemplatesForUnknownCategory() {
        var templates = registry.getTemplates("Unknown Category");
        assertThat(templates).isNotEmpty();
        assertThat(templates).hasSize(3);
    }

    @Test
    void allTemplatesHavePositiveDaysOffset() {
        var templates = registry.getTemplates("Fitness");
        assertThat(templates).allMatch(t -> t.daysOffset() > 0);
    }

    @Test
    void returnsTemplatesForWellness() {
        var templates = registry.getTemplates("Wellness");
        assertThat(templates).isNotEmpty();
    }
}
