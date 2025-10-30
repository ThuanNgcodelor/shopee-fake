-- Fix user_roles table column
ALTER TABLE user_roles MODIFY COLUMN role VARCHAR(50) NOT NULL;

