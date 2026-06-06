INSERT INTO users (employee_id, email, full_name, password_hash, active)
VALUES
    (
        'ADMIN001',
        'admin@learninghub.local',
        'Learning Hub Admin',
        '$2a$12$BqBjIL7MdBWvugBulgTb6uXDJ3GvIeHJ5FJEvFf1jp5sBvVh4mU32',
        TRUE
    ),
    (
        'EMP001',
        'employee@learninghub.local',
        'Learning Hub Employee',
        '$2a$12$A1WYhg0cbue7plMFiYquXuvigmsGj8FbhL4mJi2V02k3yqgbx5rAK',
        TRUE
    )
ON CONFLICT (employee_id) DO NOTHING;

INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id
FROM users u
JOIN roles r ON r.name = 'ADMIN'
WHERE u.email = 'admin@learninghub.local'
ON CONFLICT (user_id, role_id) DO NOTHING;

INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id
FROM users u
JOIN roles r ON r.name = 'EMPLOYEE'
WHERE u.email = 'employee@learninghub.local'
ON CONFLICT (user_id, role_id) DO NOTHING;

