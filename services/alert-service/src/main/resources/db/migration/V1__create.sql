-- Crie o banco de dados (execute apenas se ainda não existir)
CREATE DATABASE "alert-service";

-- Conecte-se ao banco antes de criar a tabela
\c alert-service

-- Crie a tabela alert
CREATE TABLE IF NOT EXISTS alert (
    id SERIAL PRIMARY KEY,
    truck_id VARCHAR(50) NOT NULL,
    temperature NUMERIC(5,2) NOT NULL,
    latitude NUMERIC(9,6),
    longitude NUMERIC(9,6),
    alert_timestamp TIMESTAMP NOT NULL
);