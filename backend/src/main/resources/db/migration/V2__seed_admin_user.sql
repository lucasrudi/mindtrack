-- =============================================
-- Seed Default Admin User
-- =============================================
-- Creates a default admin user if one does not already exist.
-- This user can be used for initial setup and should have its
-- Google OAuth linked on first login.

INSERT INTO users (email, name, role_id, enabled)
SELECT 'admin@mindtrack.app', 'MindTrack Admin', r.id, TRUE
FROM roles r
WHERE r.name = 'ADMIN'
  AND NOT EXISTS (
    SELECT 1 FROM users u
    JOIN roles r2 ON u.role_id = r2.id
    WHERE r2.name = 'ADMIN'
  );

-- Create a profile for the admin user
INSERT INTO user_profiles (user_id, display_name, timezone)
SELECT u.id, 'Admin', 'UTC'
FROM users u
WHERE u.email = 'admin@mindtrack.app'
  AND NOT EXISTS (
    SELECT 1 FROM user_profiles up WHERE up.user_id = u.id
  );
