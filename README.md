Complete Postman API Testing GuideThis guide provides the necessary details to test every endpoint in the marketplace application.Part 1: Prerequisites & Setup1.1. User Authentication (JWT Token)Most endpoints require a JSON Web Token (JWT) for an authenticated user. You must first log in to get this token.Create Users:You can create an OPERATOR user via the /api/auth/register/operator endpoint (see below) or have one created by the DataInitializer.You can create a SELLER user via the POST /api/operators/create-seller endpoint.Get the Token:Method: POSTURL: {{BASE_URL}}/api/auth/loginBody (raw, JSON):{
    "username": "your_operator_username",
    "password": "your_operator_password"
}
Use the Token:Copy the token value from the login response.For all subsequent authenticated requests, go to the Authorization tab.Select Type: Bearer Token.Paste the token into the Token field.Part 2: Auth Controller (/api/auth)These endpoints manage user authentication and registration. They are public.Login UserDescription: Authenticates a user and returns a JWT.Method: POSTURL: {{BASE_URL}}/api/auth/loginBody (raw, JSON):{
    "username": "operator_user",
    "password": "password123"
}
Register New OperatorDescription: Creates a new user with the OPERATOR role.Method: POSTURL: {{BASE_URL}}/api/auth/register/operatorBody (raw, JSON):{
    "username": "new_operator",
    "password": "a_very_strong_password",
    "email": "new.operator@example.com",
    "roles": ["OPERATOR"]
}
Part 3: Operator Controller (/api/operators)All endpoints require an OPERATOR role JWT token.3.1. Seller ManagementCreate Seller:Method: POSTURL: {{BASE_URL}}/api/operators/create-sellerBody (raw, JSON):{
    "user": {
        "username": "seller_one",
        "password": "a_strong_seller_password",
        "email": "seller.one@example.com"
    },
    "name": "Seller One's Emporium",
    "contactPhone": "555-123-4567",
    "address": "100 Commerce St, Bengaluru",
    "rating": 4.5
}
Get All Sellers:Method: GETURL: {{BASE_URL}}/api/operators/sellersGet Seller by ID:Method: GETURL: {{BASE_URL}}/api/operators/sellers/1 (Replace 1 with a valid sellerProfileId)Update Seller Status:Method: PUTURL: {{BASE_URL}}/api/operators/sellers/1/statusBody (raw, JSON):{
    "overallStatus": "ACTIVE",
    "reason": "Manual activation by operator."
}
Initiate ID.me Verification for Seller:Method: POSTURL: {{BASE_URL}}/api/operators/sellers/1/initiate-idmeUpdate Seller Details (Rating/Email):Method: PUTURL: {{BASE_URL}}/api/operators/sellers/1/detailsBody (raw, JSON):{
    "rating": 4,
    "payPalEmail": "new.paypal.email@example.com"
}
3.2. Product Catalog ManagementCreate Master Product:Method: POSTURL: {{BASE_URL}}/api/operators/productsBody (raw, JSON):{
    "name": "Premium Wireless Mouse",
    "description": "Ergonomic wireless mouse with 2-year warranty.",
    "sku": "PROD-MOUSE-W01",
    "category": "Computer Accessories",
    "basePrice": 49.99,
    "imageUrls": ["https://example.com/mouse.jpg"],
    "attributes": {
        "color": "black",
        "dpi": "1600"
    }
}
Get All Master Products:Method: GETURL: {{BASE_URL}}/api/operators/productsGet Master Product by ID:Method: GETURL: {{BASE_URL}}/api/operators/products/1Update Master Product:Method: PUTURL: {{BASE_URL}}/api/operators/products/1Body (raw, JSON): (Same structure as create)Delete Master Product:Method: DELETEURL: {{BASE_URL}}/api/operators/products/13.3. Seller Product AssignmentsAssign Product to Seller:Method: POSTURL: {{BASE_URL}}/api/operators/sellers/1/products/assignBody (raw, JSON):{
    "productId": 1,
    "initialStock": 200
}
Get a Seller's Assigned Products:Method: GETURL: {{BASE_URL}}/api/operators/sellers/1/productsUpdate a Seller's Product Assignment:Method: PUTURL: {{BASE_URL}}/api/operators/sellers/1/products/1 (sellerId/assignmentId)Body (raw, JSON):{
    "stockQuantity": 250,
    "isSellableBySeller": false
}
3.4. Operator Order ManagementGet All Orders:Method: GETURL: {{BASE_URL}}/api/operators/ordersGet Order by ID:Method: GETURL: {{BASE_URL}}/api/operators/orders/1Update Order Status:Method: PUTURL: {{BASE_URL}}/api/operators/orders/1/statusBody (raw, JSON):{
    "status": "CANCELLED_BY_OPERATOR",
    "reason": "Suspected fraudulent activity."
}
3.5. Financials, Payouts & CommissionsInitiate Payouts for All Sellers:Method: POSTURL: {{BASE_URL}}/api/operators/payouts/initiateGet Seller Ledger:Method: GETURL: {{BASE_URL}}/api/operators/sellers/1/ledgerGet All Payouts:Method: GETURL: {{BASE_URL}}/api/operators/payoutsCreate Commission Tier:Method: POSTURL: {{BASE_URL}}/api/operators/commissions/tiersBody (raw, JSON):{
    "tierName": "Bronze Tier",
    "minRatingRequired": 3.0,
    "commissionRate": 0.1000,
    "isActive": true
}
Get All Commission Tiers:Method: GETURL: {{BASE_URL}}/api/operators/commissions/tiersUpdate a Commission Tier:Method: PUTURL: {{BASE_URL}}/api/operators/commissions/tiers/1Body (raw, JSON): (Same structure as create)Delete a Commission Tier:Method: DELETEURL: {{BASE_URL}}/api/operators/commissions/tiers/1Set Seller Commission Override:Method: PUTURL: {{BASE_URL}}/api/operators/sellers/1/commission-overrideBody (raw, text): (Note: Content-Type should be application/json)0.085
Issue a Refund:Method: POSTURL: {{BASE_URL}}/api/operators/refundsBody (raw, JSON):{
    "sellerProfileId": 1,
    "orderId": 1,
    "amount": 15.99,
    "reason": "Customer goodwill for delayed shipment."
}
3.6. Platform ConfigurationGet All Platform Configurations:Method: GETURL: {{BASE_URL}}/api/operators/configUpdate a Platform Configuration:Method: PUTURL: {{BASE_URL}}/api/operators/configBody (raw, JSON):{
    "configKey": "BASE_COMMISSION_RATE",
    "configValue": "0.15",
    "description": "The default platform commission rate (15%) for sellers who do not qualify for a specific tier."
}
Part 4: Seller Controller (/api/sellers)All endpoints require a SELLER role JWT token.Get My Assigned Products:Method: GETURL: {{BASE_URL}}/api/sellers/my-productsUpdate My Stock:Method: PUTURL: {{BASE_URL}}/api/sellers/my-products/1/stock (assignmentId)Body (raw, JSON):{
    "stockQuantity": 195
}
Toggle My Product's Sellable Status:Method: PUTURL: {{BASE_URL}}/api/sellers/my-products/1/toggle-sellableBody (raw, JSON):{
    "isSellableBySeller": true
}
Get My Orders:Method: GETURL: {{BASE_URL}}/api/sellers/my-ordersFulfill an Order Item:Method: PUTURL: {{BASE_URL}}/api/sellers/my-orders/1/fulfill (orderItemId)Body (raw, JSON) for Shipping:{
    "status": "SHIPPED",
    "trackingNumber": "1Z999AA10123456784",
    "shippingCarrier": "UPS",
    "estimatedDeliveryDate": "2025-06-15T18:00:00Z"
}
Body (raw, JSON) for Cancelling:{
    "status": "CANCELLED_BY_SELLER",
    "cancellationReason": "Item is out of stock and will not be replenished."
}
Part 5: Order Controller (/api/orders)Requires a SELLER role JWT token.Create an Order:Method: POSTURL: {{BASE_URL}}/api/ordersBody (raw, JSON):{
    "customerName": "Ravi Kumar",
    "customerEmail": "ravi.kumar@example.com",
    "customerPhone": "+919988776655",
    "shippingAddress": "456 Tech Avenue, Koramangala, Bengaluru, Karnataka 560034",
    "billingAddress": "456 Tech Avenue, Koramangala, Bengaluru, Karnataka 560034",
    "paymentStatus": "PAID_IN_STORE",
    "currency": "INR",
    "items": [
        {
            "sellerProductAssignmentId": 1,
            "quantity": 2
        },
        {
            "sellerProductAssignmentId": 2,
            "quantity": 1
        }
    ]
}
Part 6: Webhook Controller (/api/webhooks)These endpoints are public but require a secret key passed in the header for security.Handle ID.me Status Update:Method: POSTURL: {{BASE_URL}}/api/webhooks/idme/statusHeaders:X-Webhook-Secret: your_idme_webhook_secret_from_propertiesBody (raw, JSON):{
    "sellerProfileId": 1,
    "idMeExternalId": "idme-uuid-12345",
    "status": "APPROVED",
    "verificationDetailsLink": "https://id.me/some_details_link"
}
Handle LMS Completion Update:Method: POSTURL: {{BASE_URL}}/api/webhooks/lms/completionHeaders:X-Webhook-Secret: your_lms_webhook_secret_from_propertiesBody (raw, JSON):{
    "sellerProfileId": 1,
    "lmsExternalId": "lms-user-67890",
    "status": "COMPLETED",
    "courseName": "Marketplace Seller Essentials",
    "completionDate": "2025-06-10T10:00:00Z"
}
