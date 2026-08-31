-- Correct the known local accounts created before registration persisted the selected role.
-- Matching by exact, unique email keeps the update deterministic and preserves user IDs.
WITH role_corrections(email, role_name) AS (
    VALUES
        ('abhaysonone0@gmail.com', 'ADMIN'),
        ('sumitshidole@gmail.com', 'DEVELOPER'),
        ('siyadalal@gmail.com', 'BUSINESS_ANALYST')
), target_users AS (
    SELECT users.id AS user_id, roles.id AS role_id
    FROM role_corrections
    JOIN users ON LOWER(users.email) = role_corrections.email
    JOIN roles ON roles.name = role_corrections.role_name
)
UPDATE user_roles
SET role_id = target_users.role_id,
    assigned_at = CURRENT_TIMESTAMP
FROM target_users
WHERE user_roles.user_id = target_users.user_id
  AND user_roles.role_id IS DISTINCT FROM target_users.role_id;

