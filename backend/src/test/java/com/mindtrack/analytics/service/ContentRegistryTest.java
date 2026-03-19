package com.mindtrack.analytics.service;

import org.junit.jupiter.api.Test;
import java.util.Set;
import static org.assertj.core.api.Assertions.assertThat;

class ContentRegistryTest {

    private final ContentRegistry registry = new ContentRegistry();

    @Test
    void returnsContentForKnownCategory() {
        var items = registry.getContent(Set.of("Mental Health"), 10);
        assertThat(items).isNotEmpty();
    }

    @Test
    void returnsDefaultContentForEmptyCategories() {
        var items = registry.getContent(Set.of(), 10);
        assertThat(items).isNotEmpty();
    }

    @Test
    void respectsMaxItemsLimit() {
        var items = registry.getContent(Set.of("Mental Health", "Wellness", "Sleep", "Fitness"), 3);
        assertThat(items).hasSizeLessThanOrEqualTo(3);
    }

    @Test
    void alwaysIncludesDailyTip() {
        var items = registry.getContent(Set.of(), 10);
        assertThat(items).anyMatch(i -> "TIP".equals(i.getType()));
    }
}
