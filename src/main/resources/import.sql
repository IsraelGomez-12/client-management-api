INSERT INTO clients (id, uuid, first_name, second_name, first_surname, second_surname, email, address, phone, country_code, demonym, active, created_at, updated_at)
VALUES (NEXT VALUE FOR clients_seq, '7b2a4e8f-3c1d-4a5b-9e6f-8d7c2b1a0e3f', 'John', 'Michael', 'Doe', 'Smith', 'john.doe@example.com', '123 Main Street, New York, NY 10001', '+1-555-123-4567', 'US', 'American', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO clients (id, uuid, first_name, second_name, first_surname, second_surname, email, address, phone, country_code, demonym, active, created_at, updated_at)
VALUES (NEXT VALUE FOR clients_seq, 'a1f5d9c3-6e2b-4d8a-b7c4-5f3e1a9d6b2c', 'María', 'Elena', 'García', 'López', 'maria.garcia@example.com', 'Calle Principal 456, Ciudad de México', '+52-55-1234-5678', 'MX', 'Mexican', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO clients (id, uuid, first_name, second_name, first_surname, second_surname, email, address, phone, country_code, demonym, active, created_at, updated_at)
VALUES (NEXT VALUE FOR clients_seq, 'e4c8b2a6-9d1f-4e7c-a3b5-2d6f8c4a1e9b', 'Carlos', NULL, 'Rodríguez', 'Martínez', 'carlos.rodriguez@example.com', 'Av. Libertador 789, Buenos Aires', '+54-11-4567-8901', 'AR', 'Argentine', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO clients (id, uuid, first_name, second_name, first_surname, second_surname, email, address, phone, country_code, demonym, active, created_at, updated_at)
VALUES (NEXT VALUE FOR clients_seq, 'b3d7f1e5-8a2c-4b9d-c6e8-1f4a7d3b5e9c', 'Ana', 'Lucía', 'Hernández', NULL, 'ana.hernandez@example.com', 'Carrera 10 #23-45, Bogotá', '+57-1-234-5678', 'CO', 'Colombian', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO clients (id, uuid, first_name, second_name, first_surname, second_surname, email, address, phone, country_code, demonym, active, created_at, updated_at)
VALUES (NEXT VALUE FOR clients_seq, 'c9e2a4f6-1b3d-4c7e-d5a8-6f2b9e1c4d7a', 'Pedro', 'José', 'Sánchez', 'Torres', 'pedro.sanchez@example.com', 'Calle Gran Vía 100, Madrid', '+34-91-234-5678', 'ES', 'Spanish', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
