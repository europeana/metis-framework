/*
e.g. account_role values 'METIS_ADMIN', 'EUROPEANA_DATA_OFFICER', 'PROVIDER_VIEWER'.
The values are checked in the application.
Creating the first admin requires a user to register through the application and then manually update the user role to METIS_ADMIN.
*/
CREATE TABLE IF NOT EXISTS metis_users (
 user_id VARCHAR(100),
 email VARCHAR(40) PRIMARY KEY,
 last_name VARCHAR(40),
 first_name VARCHAR(40),
 password VARCHAR(255),
 organization_id VARCHAR(100),
 organization_name VARCHAR(100),
 account_role VARCHAR(40),
 country VARCHAR(40),
 network_member BOOLEAN,
 metis_user BOOLEAN,
 created_date TIMESTAMPTZ,
 updated_date TIMESTAMPTZ
);

CREATE TABLE IF NOT EXISTS metis_user_access_tokens (
 email VARCHAR(40) PRIMARY KEY REFERENCES metis_users (email),
 access_token VARCHAR(255) UNIQUE NOT NULL,
 timestamp TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS metis_zoho_oauth_tokens (
 user_identifier VARCHAR(100) PRIMARY KEY,
 access_token VARCHAR(100) NOT NULL,
 refresh_token VARCHAR(100) NOT NULL,
 expiry_time bigint NOT NULL
);