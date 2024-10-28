# Xsolla Server-Side Integration Example

This repository offers an example of integrating Xsolla PayStation for payment processing using Maven, Java, Servlets,
Tomcat, and Ngrok.

## Prerequisites

Ensure the following tools are installed on your system before proceeding:

- [Docker](https://docs.docker.com/get-docker/)
- [Docker Compose](https://docs.docker.com/compose/install/)
- [Ngrok](https://ngrok.com/)

## Setup Instructions

### 1. Create the .env file

Copy the .env.example file and rename it to .env:

```bash
cp .env.example .env
```

Update the .env file with the appropriate values. Your project ID is available in your Xsolla Publisher Account.

![screenshot](doc/img/where-id.png)

### 2. Build the project with Maven

```markdown
mvn clean install
```

The output xsolla-example.war file will be in the target directory.

**It's crucial to remember that your project needs to be rebuilt whenever changes are made to the codebase or .env variables.**

### 3. Start Docker Containers

Run the project using Docker Compose:

```bash
docker-compose up --build
```

The application will be accessible at http://localhost:8080/xsolla-example/server-side.

**It's crucial to remember that your Docker need to be rebuilt too after every project rebuilt.**

### 4. Configure Ngrok

![screenshot](doc/img/ngrok-url.png)
Ngrok provides a public URL for accessing your local server. You can open the Ngrok dashboard at http://localhost:4040.

Copy the resulting URL and append /xsolla-example/webhook to it (for example, https://<random>.ngrok-free.app/xsolla-example/webhook) and add it to the webhook settings of your project at https://publisher.xsolla.com/<YOUR_MERCHANT>/projects/<YOUR_PROJECT>/edit/webhooks/.

### 5. Add a product in Publisher Account

Create a product in your Xsolla Publisher Account. Navigate to your project > store > virtual items > add item, or open
the page directly at https://publisher.xsolla.com/<YOUR_MERCHANT>/projects/<YOUR_PROJECT>/storefront/virtual-items.

### 6. Test the payment process

Open the example UI at `http://localhost:8080/xsolla-example/server-side` and modify the JSON data to get a token (set
the SKU of the project, user ID, etc.).

Payment process:

1. Server: Create a token using your JSON data.
2. Client: Open the payment page with your token.
3. Server: Receive a webhook to validate the user.
4. Client: Complete the payment.
5. Server: Receive a webhook with the payment result and deliver the product to the customer.
