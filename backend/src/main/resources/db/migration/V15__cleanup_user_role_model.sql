ALTER TABLE users
    ADD COLUMN IF NOT EXISTS username VARCHAR(100);

CREATE UNIQUE INDEX IF NOT EXISTS uq_users_username_lower
    ON users (LOWER(username))
    WHERE username IS NOT NULL;

INSERT INTO user_roles (user_id, role_id)
SELECT users.id, users.role_id
FROM users
WHERE users.role_id IS NOT NULL
  AND NOT EXISTS (
      SELECT 1
      FROM user_roles
      WHERE user_roles.user_id = users.id
  )
ON CONFLICT (user_id, role_id) DO NOTHING;

DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM user_roles
        GROUP BY user_id
        HAVING COUNT(*) > 1
    ) THEN
        RAISE EXCEPTION 'Cannot enforce one role per user: duplicate user_roles assignments exist.';
    END IF;
END
$$;

DROP INDEX IF EXISTS idx_users_role_id;

ALTER TABLE users
    DROP CONSTRAINT IF EXISTS fk_users_role;

ALTER TABLE users
    DROP COLUMN IF EXISTS role_id;

ALTER TABLE user_roles
    ADD CONSTRAINT uq_user_roles_user_id UNIQUE (user_id);
