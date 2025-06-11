-- Create roles if not exist
INSERT INTO roles (id, name, description, priority, created_at, updated_at) 
VALUES (1, 'USER', 'Base user role', 1, NOW(), NOW()) 
ON CONFLICT (id) DO NOTHING;

INSERT INTO roles (id, name, description, priority, created_at, updated_at) 
VALUES (4, 'ADMIN', 'Administrator role with full privileges', 4, NOW(), NOW()) 
ON CONFLICT (id) DO NOTHING;

-- Create admin user (password: admin)
INSERT INTO users (username, email, password_hash, status, created_at, updated_at) 
VALUES ('admin', 'admin@crypto-dashboard.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'ACTIVE', NOW(), NOW())
ON CONFLICT (username) DO NOTHING;

-- Get admin user id and assign ADMIN role
INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id 
FROM users u, roles r 
WHERE u.username = 'admin' AND r.name = 'ADMIN'
ON CONFLICT (user_id, role_id) DO NOTHING; 