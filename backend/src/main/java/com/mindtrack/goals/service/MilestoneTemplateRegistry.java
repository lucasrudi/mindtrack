package com.mindtrack.goals.service;

import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

/**
 * Provides category-specific milestone templates for auto-generating suggested milestones.
 */
@Component
public class MilestoneTemplateRegistry {

    public record MilestoneTemplate(String title, String notes, int daysOffset) {
    }

    private static final Map<String, List<MilestoneTemplate>> TEMPLATES = Map.of(
            "Mental Health", List.of(
                    new MilestoneTemplate("Journal for 7 consecutive days",
                            "Write at least 3 sentences each day", 14),
                    new MilestoneTemplate("Identify 3 mood triggers",
                            "Track what situations or thoughts affect your mood", 21),
                    new MilestoneTemplate("Practice one coping technique daily for 2 weeks",
                            "Choose a technique: breathing, grounding, or journaling", 28)
            ),
            "Wellness", List.of(
                    new MilestoneTemplate("Establish consistent bedtime for 1 week",
                            "Aim for the same bedtime \u00b130 minutes each night", 14),
                    new MilestoneTemplate("Try 3 relaxation techniques",
                            "Examples: progressive muscle relaxation, guided meditation, yoga", 21),
                    new MilestoneTemplate("Complete a 30-day wellness challenge",
                            "Pick one small habit and do it every day for 30 days", 35)
            ),
            "Health", List.of(
                    new MilestoneTemplate("Plan balanced meals for 1 week",
                            "Prepare a weekly meal plan with fruits, vegetables, and protein", 7),
                    new MilestoneTemplate("Track eating habits for 14 days",
                            "Note what, when, and how much you eat each day", 21),
                    new MilestoneTemplate("Try 3 new healthy recipes",
                            "Explore new cuisines or cooking methods", 28)
            ),
            "Sleep", List.of(
                    new MilestoneTemplate("Establish a sleep routine for 1 week",
                            "Same bedtime and wake time every day", 14),
                    new MilestoneTemplate("Reduce screen time 1 hour before bed",
                            "Replace with reading or light stretching", 21),
                    new MilestoneTemplate("Track sleep quality for 2 weeks",
                            "Note sleep duration and how rested you feel each morning", 28)
            ),
            "Fitness", List.of(
                    new MilestoneTemplate("Complete 3 workouts in first week",
                            "Any activity counts \u2014 walking, gym, yoga, cycling", 7),
                    new MilestoneTemplate("Maintain 3x per week routine for 1 month",
                            "Consistency matters more than intensity at first", 30),
                    new MilestoneTemplate("Increase workout duration by 25%",
                            "Gradually extend each session as your fitness improves", 60)
            )
    );

    private static final List<MilestoneTemplate> DEFAULT_TEMPLATES = List.of(
            new MilestoneTemplate("Define specific success criteria",
                    "What does achieving this goal look like concretely?", 7),
            new MilestoneTemplate("Complete first action step",
                    "Take one concrete action toward your goal this week", 14),
            new MilestoneTemplate("Review and adjust approach",
                    "Reflect on what is working and what needs changing", 30)
    );

    /**
     * Returns milestone templates for the given category, or default templates if unknown.
     */
    public List<MilestoneTemplate> getTemplates(String category) {
        return TEMPLATES.getOrDefault(category, DEFAULT_TEMPLATES);
    }
}
