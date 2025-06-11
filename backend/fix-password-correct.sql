-- Fix admin password with proper escaping
UPDATE users 
SET password_hash = '$2a$12$Whi714tc34lFfFRsleP9gOnag4PE3HuYIt2E2EOHU1pS9b2ForOQC' 
WHERE username = 'admin';

-- Verify the update
SELECT username, password_hash FROM users WHERE username = 'admin'; 