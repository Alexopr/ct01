-- Fix admin password with proper BCrypt hash for password 'admin'
UPDATE users 
SET password_hash = '$2a$10$dXJ3SW6G7P7tK/6C7NK77eLQ6LLKPKKGLxo8xGWvlHfD6.nRWXDwO' 
WHERE username = 'admin';

-- Verify the update
SELECT username, password_hash FROM users WHERE username = 'admin'; 