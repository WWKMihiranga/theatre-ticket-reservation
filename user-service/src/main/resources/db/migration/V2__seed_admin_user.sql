-- Seed an admin user for development and testing.
-- Email:    admin@theatre.local
-- Password: AdminPass123
-- (BCrypt hash of "AdminPass123" with default cost 10)
INSERT INTO users (nic, name, surname, email, password_hash, role)
VALUES (
    'ADMIN000001',
    'System',
    'Admin',
    'admin@theatre.local',
    '$2b$10$1WfJ.zSFghVkktNEftS8k.ANWXU4g2dmBJXuXraDIE1mH.cH/94sa',
    'ADMIN'
);
