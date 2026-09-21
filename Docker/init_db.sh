#!/bin/bash
# Se ejecuta una sola vez, cuando el volumen de datos esta vacio.
# Crea la base que espera application.yml (DB_URL -> .../outfitmaker_sql).
set -e

psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "postgres" <<-EOSQL
    SELECT 'CREATE DATABASE outfitmaker_sql'
    WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'outfitmaker_sql')\gexec
EOSQL
