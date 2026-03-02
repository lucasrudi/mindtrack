package com.mindtrack.onboarding.dto;

import jakarta.validation.constraints.NotNull;
import java.util.List;

/**
 * Request DTO for the onboarding survey.
 */
public class SurveyRequest {

    @NotNull
    private Integer moodBaseline;

    @NotNull
    private Integer anxietyLevel;

    @NotNull
    private Integer sleepQuality;

    private Integer depressionScore;  // 1-10, higher = better (less depressed)
    private Integer stressLevel;      // 1-10, higher = better (less stressed)
    private Integer eatingHabits;     // 1-10, higher = better

    private List<String> lifeAreas;
    private List<String> challenges;
    private List<String> goalCategories;

    public Integer getMoodBaseline() {
        return moodBaseline;
    }

    public void setMoodBaseline(Integer moodBaseline) {
        this.moodBaseline = moodBaseline;
    }

    public Integer getAnxietyLevel() {
        return anxietyLevel;
    }

    public void setAnxietyLevel(Integer anxietyLevel) {
        this.anxietyLevel = anxietyLevel;
    }

    public Integer getSleepQuality() {
        return sleepQuality;
    }

    public void setSleepQuality(Integer sleepQuality) {
        this.sleepQuality = sleepQuality;
    }

    public Integer getDepressionScore() {
        return depressionScore;
    }

    public void setDepressionScore(Integer depressionScore) {
        this.depressionScore = depressionScore;
    }

    public Integer getStressLevel() {
        return stressLevel;
    }

    public void setStressLevel(Integer stressLevel) {
        this.stressLevel = stressLevel;
    }

    public Integer getEatingHabits() {
        return eatingHabits;
    }

    public void setEatingHabits(Integer eatingHabits) {
        this.eatingHabits = eatingHabits;
    }

    public List<String> getLifeAreas() {
        return lifeAreas;
    }

    public void setLifeAreas(List<String> lifeAreas) {
        this.lifeAreas = lifeAreas;
    }

    public List<String> getChallenges() {
        return challenges;
    }

    public void setChallenges(List<String> challenges) {
        this.challenges = challenges;
    }

    public List<String> getGoalCategories() {
        return goalCategories;
    }

    public void setGoalCategories(List<String> goalCategories) {
        this.goalCategories = goalCategories;
    }
}
