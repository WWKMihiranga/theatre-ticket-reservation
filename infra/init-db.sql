-- Create separate databases per service for proper data isolation
CREATE DATABASE users_db;
CREATE DATABASE theatre_db;
CREATE DATABASE booking_db;

GRANT ALL PRIVILEGES ON DATABASE users_db TO theatre;
GRANT ALL PRIVILEGES ON DATABASE theatre_db TO theatre;
GRANT ALL PRIVILEGES ON DATABASE booking_db TO theatre;
