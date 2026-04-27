# Eagle Bank API

```bash
curl -s -X POST http://localhost:8080/v1/users \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Jane Doe",
    "address": {
      "line1": "1 High Street",
      "town": "London",
      "county": "Greater London",
      "postcode": "SW1A 1AA"
    },
    "phoneNumber": "+441234567890",
    "email": "jane@example.com",
    "password": "secret123"
  }' | jq .
```