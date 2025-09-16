## ProductProcessingFunctionApp

An Azure Function App (Java 21) that accepts HTTP requests, parses JSON payloads into a
ProductInvoice object,
and saves it into a PostgreSQL database using a stored procedure.

```angular2html
Prerequisites

Java 21

Maven 3+

PostgreSQL- running locally or in Azure

express js backed endpoints(product API- https://github.com/priyodas12/express-routing.git)

Azure Functions Core Tools
```

### Features

- Written in Java 21 with Azure Functions SDK

- Reads configuration from environment variables (local.settings.json locally, App Settings in
  Azure)

- Connects to PostgreSQL using JDBC

- Calls a stored procedure (save_product_invoice) to insert or update invoices

- Supports BIGINT IDs for large invoice numbers

- Demonstrates best practices for env vars and logging

```angular2html
DROP TABLE IF EXISTS product_invoice;

-- Table with BIGINT id
CREATE TABLE product_invoice (
id BIGINT PRIMARY KEY,
product_name VARCHAR(255),
product_type VARCHAR(255),
product_description TEXT,
product_price NUMERIC(15,2)
);

-- Stored Procedure
CREATE OR REPLACE PROCEDURE save_product_invoice(
p_id BIGINT,
p_name VARCHAR,
p_type VARCHAR,
p_desc TEXT,
p_price NUMERIC
)
LANGUAGE plpgsql
AS $$
BEGIN
INSERT INTO product_invoice(id, product_name, product_type, product_description, product_price)
VALUES (p_id, p_name, p_type, p_desc, p_price)
ON CONFLICT (id) DO UPDATE
SET product_name = EXCLUDED.product_name,
product_type = EXCLUDED.product_type,
product_description = EXCLUDED.product_description,
product_price = EXCLUDED.product_price;
END;
$$;
```

```
 mvn clean package azure-functions:package
 mvn azure-functions:run
```