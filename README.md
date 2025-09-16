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

```
 mvn clean package azure-functions:package
 mvn azure-functions:run
```