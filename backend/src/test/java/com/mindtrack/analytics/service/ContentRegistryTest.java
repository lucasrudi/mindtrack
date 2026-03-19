package com.mindtrack.analytics.service;

import com.mindtrack.analytics.dto.ContentItemResponse;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ContentRegistryTest {

    private static final Set<String> UNAVAILABLE_VIDEOS = Set.of(
            "d9avpnRRXU4",
            "wuh5B3_y8nY",
            "qzR62JJCMBQ");

    private final ContentRegistry registry = new ContentRegistry(
            videoId -> !UNAVAILABLE_VIDEOS.contains(videoId));

    @Test
    void returnsContentForKnownCategory() {
        var items = registry.getContent(Set.of("Mental Health"), 10);
        assertThat(items).isNotEmpty();
        assertThat(items).anyMatch(i -> "Mental Health".equals(i.getCategory()));
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

    @Test
    void replacesUnavailableVideosWithPlayableAlternatives() {
        var items = registry.getContent(Set.of("Mental Health", "Wellness", "Sleep", "Fitness"), 10);

        assertThat(items).hasSize(10);
        assertThat(items)
                .filteredOn(item -> "VIDEO".equals(item.getType()))
                .allSatisfy(item -> assertThat(UNAVAILABLE_VIDEOS).doesNotContain(item.getUrl()));
    }

    @Test
    void keepsCategoryOrderDeterministicRegardlessOfSetIterationOrder() {
        Set<String> firstOrder = new LinkedHashSet<>(List.of("Fitness", "Sleep", "Wellness", "Mental Health"));
        Set<String> secondOrder = new LinkedHashSet<>(List.of("Mental Health", "Wellness", "Sleep", "Fitness"));

        List<ContentItemResponse> first = registry.getContent(firstOrder, 10);
        List<ContentItemResponse> second = registry.getContent(secondOrder, 10);

        assertThat(second).extracting(ContentItemResponse::getTitle)
                .containsExactlyElementsOf(first.stream().map(ContentItemResponse::getTitle).toList());
    }
}
