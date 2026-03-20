-- Backfill goal started state for any NOT_STARTED goal that already has milestone activity.
UPDATE goals g
SET g.status = 'IN_PROGRESS',
    g.updated_at = CURRENT_TIMESTAMP
WHERE g.status = 'NOT_STARTED'
  AND EXISTS (
      SELECT 1
      FROM milestones m
      WHERE m.goal_id = g.id
  );
