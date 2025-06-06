# Marketplace API

This document provides a comprehensive list of all API endpoints for the Marketplace Backend project. It includes scenarios, request/response bodies, and authorization requirements for testing with a tool like Postman.

## Overview

This document provides instructions on how to use the "Marketplace API" Postman collection to test the functionalities of the Spring Boot backend. The API simulates a marketplace environment with three main roles:

* **Operator**: The administrator of the platform. Responsible for managing sellers, products, and financials.
* **Seller (IWC)**: An "Independent Warketing Consultant" who sells products on the platform.
* **Customer**: The end-user who purchases products from a seller.

The collection is organized into a logical workflow, guiding you from the initial setup to complex financial transactions.

## Prerequisites

Before you begin, ensure you have the following:

* **Running Backend Application**: The Spring Boot Marketplace Backend application must be running and accessible.
* **Postman**: You need the Postman desktop client or web version to import and use the collection.

## Setup & Configuration

Follow these steps to configure your Postman environment.

#### 1. Import the Collection
Import the provided JSON file (`Marketplace API postman_collection.json`) into Postman.

#### 2. Set the Base URL
The collection uses a variable `{{baseUrl}}` for the server's address.
In Postman, go to the collection variables and set the `Current Value` of `baseUrl` to your running application's address (e.g., `http://localhost:8080`).

#### 3. Initial Authentication Workflow
The requests in the **"1. Authentication & Setup"** folder are designed to be run first. They will authenticate the primary users and set up the necessary collection variables automatically.

1.  **Register Initial Operator**: Run this request to create the first `OPERATOR` user. This should typically only be run once.
2.  **Login (Operator)**: This authenticates the operator. The test script will automatically capture the returned JWT and save it to the `jwt_token_operator` collection variable.
3.  **Create Seller (IWC)**: Found in folder **"2. Operator: Seller & Product Management"**, this request creates a new seller account. The test script saves the new seller's ID to the `sellerProfileId` variable.
4.  **Login (Seller)**: Use this request to log in as the newly created seller. The test script saves the seller's JWT to the `jwt_token_seller` variable.
5.  **Update Platform Base Commission Rate**: As the operator, run this request to set the global commission rate, which is crucial for financial calculations.

After completing these steps, the required authentication tokens and IDs will be stored in Postman's collection variables, allowing you to seamlessly run the other requests in the collection.

---

## API Endpoints

The collection is divided into several modules that represent the core functionalities of the marketplace.

### Module 1: Authentication & Setup
*Start here. This module handles the creation of the initial operator and the login process for both operators and sellers, storing their JWTs for subsequent requests.*

#### Register Initial Operator
* **Request**: `POST {{baseUrl}}/api/auth/register/operator`
* **Description**: Creates the first administrative user. Should typically only be run once.
* **Body**:
    ```json
    {
        "username": "superoperator",
        "password": "StrongOpPassword123!",
        "email": "operator.admin@example.com",
        "roles": ["OPERATOR"]
    }
    ```

#### Login (Operator)
* **Request**: `POST {{baseUrl}}/api/auth/login`
* **Description**: Authenticates an Operator and saves the JWT to a collection variable `jwt_token_operator`.
* **Body**:
    ```json
    {
        "username": "superoperator",
        "password": "StrongOpPassword123!"
    }
    ```

#### Login (Seller)
* **Request**: `POST {{baseUrl}}/api/auth/login`
* **Description**: Authenticates a Seller (IWC) and saves the JWT to a collection variable `jwt_token_seller`.
* **Body**:
    ```json
    {
        "username": "iwc_john_doe",
        "password": "IWCsecurePass789!"
    }
    ```

#### Operator: Update Platform Base Commission Rate
* **Request**: `PUT {{baseUrl}}/api/operators/config`
* **Authorization**: Operator JWT (`{{jwt_token_operator}}`)
* **Description**: Sets the global base commission rate. Essential for commission calculations.
* **Body**:
    ```json
    {
        "configKey": "BASE_COMMISSION_RATE",
        "configValue": "0.02",
        "description": "Base rate multiplied by product price and seller rating."
    }
    ```

### Module 2: Operator - Seller & Product Management
*Contains requests for the operator to manage sellers and the product catalog.*

#### Create Seller (IWC)
* **Request**: `POST {{baseUrl}}/api/operators/create-seller`
* **Authorization**: Operator JWT (`{{jwt_token_operator}}`)
* **Description**: Onboards a new seller. Saves the returned `id` to the `sellerProfileId` variable.
* **Body**:
    ```json
    {
        "user": {
            "username": "iwc_john_doe",
            "password": "IWCsecurePass789!",
            "email": "john.doe.iwc@example.com",
            "roles": ["SELLER"]
        },
        "name": "John Doe (IWC)",
        "contactPhone": "555-101-2020",
        "address": "123 Consultant Row, Tech City",
        "rating": 3,
        "payPalEmail": "john.doe.paypal@example.com"
    }
    ```

#### Update Seller Status to ACTIVE
* **Request**: `PUT {{baseUrl}}/api/operators/sellers/{{sellerProfileId}}/status`
* **Authorization**: Operator JWT (`{{jwt_token_operator}}`)
* **Description**: Activates a seller's account, allowing them to participate in the marketplace.
* **Body**:
    ```json
    {
        "overallStatus": "ACTIVE",
        "reason": "Manually activated for testing."
    }
    ```

#### Create Master Product
* **Request**: `POST {{baseUrl}}/api/operators/products`
* **Authorization**: Operator JWT (`{{jwt_token_operator}}`)
* **Description**: Adds a new base product to the platform's catalog. Saves the `id` to `productId`.
* **Body**:
    ```json
    {
        "name": "Smartphone Model Pro",
        "description": "Latest generation smartphone.",
        "sku": "PHONE-PRO-256GB-GRY",
        "category": "Mobile Devices",
        "basePrice": "999.00",
        "attributes": { "Storage": "256GB" }
    }
    ```

#### Assign Product to Seller
* **Request**: `POST {{baseUrl}}/api/operators/sellers/{{sellerProfileId}}/products/assign`
* **Authorization**: Operator JWT (`{{jwt_token_operator}}`)
* **Description**: Links a master product to a specific seller and sets their initial stock. Saves the `id` to `assignmentId`.
* **Body**:
    ```json
    {
        "productId": "{{productId}}",
        "initialStock": 10
    }
    ```

### Module 3: Seller (IWC) - Actions
*Covers the actions a logged-in seller can perform.*

#### Get My Assigned Products
* **Request**: `GET {{baseUrl}}/api/sellers/my-products`
* **Authorization**: Seller JWT (`{{jwt_token_seller}}`)

#### Update My Stock
* **Request**: `PUT {{baseUrl}}/api/sellers/my-products/{{assignmentId}}/stock`
* **Authorization**: Seller JWT (`{{jwt_token_seller}}`)
* **Body**:
    ```json
    {
        "stockQuantity": 8
    }
    ```

#### Create Order for Customer
* **Request**: `POST {{baseUrl}}/api/orders`
* **Authorization**: Seller JWT (`{{jwt_token_seller}}`)
* **Description**: A seller can create an order on behalf of a customer (e.g., for POS transactions). Saves `orderId` and `orderItemId`.
* **Body**:
    ```json
    {
        "customerName": "Jane Smith (IWC Customer)",
        "customerEmail": "jane.smith.customer@example.com",
        "shippingAddress": "123 Main St, Anytown, USA",
        "items": [
            {
                "sellerProductAssignmentId": "{{assignmentId}}",
                "quantity": 1
            }
        ],
        "paymentId": "POS-TXN-IWCSALE001",
        "paymentStatus": "PAID_IN_STORE",
        "paymentMethodDetails": "Credit Card via POS"
    }
    ```

#### Get My Orders
* **Request**: `GET {{baseUrl}}/api/sellers/my-orders`
* **Authorization**: Seller JWT (`{{jwt_token_seller}}`)

#### Fulfill an Order Item
* **Request**: `PUT {{baseUrl}}/api/sellers/my-orders/{{orderItemId}}/fulfill`
* **Authorization**: Seller JWT (`{{jwt_token_seller}}`)
* **Body**:
    ```json
    {
        "status": "SHIPPED",
        "trackingNumber": "IN_STORE_HANDOVER_RECEIPT_884321",
        "shippingCarrier": "In-Store Pickup"
    }
    ```

### Module 4: Operator - Order & Financials
*Contains endpoints for the operator to oversee all orders and manage the financial ledger.*

#### Get All Orders
* **Request**: `GET {{baseUrl}}/api/operators/orders`
* **Authorization**: Operator JWT (`{{jwt_token_operator}}`)

#### Get a Specific Order
* **Request**: `GET {{baseUrl}}/api/operators/orders/{{orderId}}`
* **Authorization**: Operator JWT (`{{jwt_token_operator}}`)

#### Update Order Status to DELIVERED
* **Request**: `PUT {{baseUrl}}/api/operators/orders/{{orderId}}/status`
* **Authorization**: Operator JWT (`{{jwt_token_operator}}`)
* **Description**: Critical step. When an operator marks an order as "DELIVERED", the system automatically triggers financial calculations and creates ledger entries for the seller's sales credit and the platform's commission debit.
* **Body**:
    ```json
    {
        "status": "DELIVERED",
        "reason": "Customer confirmed receipt in-store."
    }
    ```

#### Get Seller Ledger
* **Request**: `GET {{baseUrl}}/api/operators/sellers/{{sellerProfileId}}/ledger`
* **Authorization**: Operator JWT (`{{jwt_token_operator}}`)
* **Description**: After an order is delivered, check the seller's ledger. You should see a `SALE_CREDIT` and a `COMMISSION_DEBIT`.

#### Initiate Payout Run
* **Request**: `POST {{baseUrl}}/api/operators/payouts/initiate`
* **Authorization**: Operator JWT (`{{jwt_token_operator}}`)
* **Description**: Triggers the process to pay sellers their outstanding balances.

#### Get All Payouts
* **Request**: `GET {{baseUrl}}/api/operators/payouts`
* **Authorization**: Operator JWT (`{{jwt_token_operator}}`)

### Module 5: Webhooks (Simulated)
*This module contains requests to simulate incoming webhooks from external services to test asynchronous status updates.*

#### ID.me Status Update
* **Request**: `POST {{baseUrl}}/api/webhooks/idme/status`
* **Headers**: Requires `X-Webhook-Secret`. Set the value to the secret key configured in your backend application.
* **Body**:
    ```json
    {
        "sellerProfileId": 1,
        "idMeExternalId": "idme-real-ext-id-777",
        "status": "APPROVED",
        "reason": "ID.me verification passed.",
        "verificationDetailsLink": "https://id.me/details/mocklink123"
    }
    ```

#### LMS Completion Update
* **Request**: `POST {{baseUrl}}/api/webhooks/lms/completion`
* **Headers**: Requires `X-Webhook-Secret`. Set the value to the secret key configured in your backend application.
* **Body**:
    ```json
    {
        "sellerProfileId": 1,
        "lmsExternalId": "lms-real-course-id-888",
        "status": "COMPLETED",
        "courseName": "Official IWC Onboarding Program",
        "completionDate": "2025-01-20T11:30:00Z"
    }
    ```
