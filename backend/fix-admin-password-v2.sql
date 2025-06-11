-- Fix admin password with proper BCrypt hash for password 'admin' with strength 12
UPDATE users 
SET password_hash = '$2a$12$LQv3c1yqBWVHxkd0LQ1bce/.S/d/eXElGUlQk.c0YhL.6Q.C8cZr6' 
WHERE username = 'admin';

-- Verify the update
SELECT username, LEFT(password_hash, 15) as hash_start FROM users WHERE username = 'admin'; 