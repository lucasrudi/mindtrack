CREATE TABLE activity_goals (
    activity_id BIGINT NOT NULL,
    goal_id BIGINT NOT NULL,
    PRIMARY KEY (activity_id, goal_id),
    CONSTRAINT fk_activity_goals_activity FOREIGN KEY (activity_id) REFERENCES activities(id) ON DELETE CASCADE,
    CONSTRAINT fk_activity_goals_goal FOREIGN KEY (goal_id) REFERENCES goals(id) ON DELETE CASCADE
);
