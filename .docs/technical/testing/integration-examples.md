# API Integration Examples

This document provides practical examples for integrating with the Car Sharing API using various programming languages and tools.

## Table of Contents

1. [cURL Examples](#curl-examples)
2. [JavaScript/Node.js Examples](#javascriptnodejs-examples)
3. [Python Examples](#python-examples)
4. [Java Examples](#java-examples)
5. [Common Integration Patterns](#common-integration-patterns)

---

## cURL Examples

### Authentication

#### Login
```bash
curl -X POST http://localhost:8081/auth-service/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "john.driver@example.com",
    "password": "SecurePassword123!"
  }'
```

**Response:**
```json
{
  "success": true,
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ1c2VyXzEyMyIsInJvbGUiOiJEUklWRVIiLCJleHAiOjE2OTk4NzcyMDB9.signature",
    "email": "john.driver@example.com",
    "role": "DRIVER"
  },
  "message": "Login successful"
}
```

#### Signup
```bash
curl -X POST http://localhost:8081/auth-service/api/auth/signup \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "John",
    "lastName": "Driver",
    "email": "john.driver@example.com",
    "password": "SecurePassword123!",
    "userType": "DRIVER",
    "phoneNumber": "+49 123 456789",
    "age": 32,
    "licenseNumber": "LD-123456789"
  }'
```

#### Validate Token
```bash
curl -X POST http://localhost:8081/auth-service/api/auth/validate \
  -H "Content-Type: application/json" \
  -d '{
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
  }'
```

### User Service

#### Get User Profile
```bash
TOKEN="eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."

curl -X GET http://localhost:8082/user-service/api/users/user_123 \
  -H "Authorization: Bearer $TOKEN"
```

#### Create User Profile
```bash
TOKEN="eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."

curl -X POST http://localhost:8082/user-service/api/users/profile \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "userId": "user_123",
    "firstName": "John",
    "lastName": "Driver",
    "age": 32,
    "phoneNumber": "+49 123 456789"
  }'
```

### Vehicle Management (User Service)

#### Register Vehicle
```bash
TOKEN="eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."

curl -X POST http://localhost:8082/user-service/api/vehicles/register \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "userId": "user_123",
    "vehicleName": "Tesla Model 3",
    "vehicleNumber": "HE-XX-1234",
    "vehicleType": "sedan",
    "vehicleColor": "white",
    "seatingCapacity": "5"
  }'
```

#### Get User Vehicles
```bash
TOKEN="eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."

curl -X GET http://localhost:8082/user-service/api/vehicles/user_123 \
  -H "Authorization: Bearer $TOKEN"
```

### Trip Service

#### Offer a Trip
```bash
TOKEN="eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."

curl -X POST http://localhost:8084/trip-service/api/trips/offer \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "driverId": "user_123",
    "vehicleId": "vehicle_001",
    "sourceLatitude": 50.8154,
    "sourceLongitude": 8.6747,
    "destinationLatitude": 50.7265,
    "destinationLongitude": 7.0895,
    "departureTime": "2024-03-25T08:00:00Z",
    "availableSeats": 3,
    "pricePerSeat": 15.50,
    "notes": "Morning commute"
  }'
```

#### Search Matching Trips
```bash
TOKEN="eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."

curl -X GET "http://localhost:8084/trip-service/api/trips/search/matching-route?sourceLatitude=50.8154&sourceLongitude=8.6747&destinationLatitude=50.7265&destinationLongitude=7.0895&requestedSeats=2&effectiveUserId=user_456" \
  -H "Authorization: Bearer $TOKEN"
```

#### Get Active Trips
```bash
TOKEN="eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."

curl -X GET http://localhost:8084/trip-service/api/trips/active/driver/user_123 \
  -H "Authorization: Bearer $TOKEN"
```

#### Cancel Trip
```bash
TOKEN="eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."

curl -X POST http://localhost:8084/trip-service/api/trips/cancel \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "tripId": "trip_001",
    "userId": "user_123",
    "reason": "Change of plans"
  }'
```

### Booking (Trip Service)

#### Join a Trip
```bash
TOKEN="eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."

curl -X POST http://localhost:8084/trip-service/api/bookings/join \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "passengerId": "user_456",
    "tripId": "trip_001",
    "numberOfSeats": 2,
    "specialRequests": "I have a large suitcase"
  }'
```

#### Get Active Bookings
```bash
TOKEN="eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."

curl -X GET http://localhost:8084/trip-service/api/bookings/active/passenger/user_456 \
  -H "Authorization: Bearer $TOKEN"
```

---

## JavaScript/Node.js Examples

### Using Fetch API

#### Login
```javascript
async function login(email, password) {
  const response = await fetch('http://localhost:8081/auth-service/api/auth/login', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json'
    },
    body: JSON.stringify({
      email,
      password
    })
  });

  if (!response.ok) {
    throw new Error(`Login failed: ${response.statusText}`);
  }

  const data = await response.json();
  return data.data.token; // Returns JWT token
}
```

#### Register
```javascript
async function register(userData) {
  const response = await fetch('http://localhost:8081/auth-service/api/auth/signup', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json'
    },
    body: JSON.stringify({
      firstName: userData.firstName,
      lastName: userData.lastName,
      email: userData.email,
      password: userData.password,
      userType: userData.userType,
      phoneNumber: userData.phoneNumber,
      age: userData.age,
      licenseNumber: userData.licenseNumber
    })
  });

  const data = await response.json();
  if (data.success) {
    return data.data.token;
  } else {
    throw new Error(data.message);
  }
}
```

#### API Client Class
```javascript
class CarSharingAPI {
  constructor(baseURL, token = null) {
    this.baseURL = baseURL;
    this.token = token;
  }

  setToken(token) {
    this.token = token;
  }

  async request(endpoint, method = 'GET', body = null) {
    const headers = {
      'Content-Type': 'application/json'
    };

    if (this.token) {
      headers['Authorization'] = `Bearer ${this.token}`;
    }

    const options = {
      method,
      headers
    };

    if (body) {
      options.body = JSON.stringify(body);
    }

    const response = await fetch(`${this.baseURL}${endpoint}`, options);
    const data = await response.json();

    if (!data.success) {
      throw new Error(data.message || 'API request failed');
    }

    return data.data;
  }

  // Auth endpoints
  async login(email, password) {
    const token = await this.request('/auth-service/api/auth/login', 'POST', {
      email,
      password
    });
    this.token = token;
    return token;
  }

  // User endpoints
  async getUserProfile(userId) {
    return this.request(`/user-service/api/users/${userId}`);
  }

  async createUserProfile(profileData) {
    return this.request('/user-service/api/users/profile', 'POST', profileData);
  }

  // Vehicle endpoints (User Service)
  async registerVehicle(vehicleData) {
    return this.request('/user-service/api/vehicles/register', 'POST', vehicleData);
  }

  async getUserVehicles(userId) {
    return this.request(`/user-service/api/vehicles/${userId}`);
  }

  // Trip endpoints
  async offerTrip(tripData) {
    return this.request('/trip-service/api/trips/offer', 'POST', tripData);
  }

  async searchMatchingTrips(searchParams) {
    const queryString = new URLSearchParams(searchParams).toString();
    return this.request(
      `/trip-service/api/trips/search/matching-route?${queryString}`
    );
  }

  async getActiveTrips(driverId) {
    return this.request(`/trip-service/api/trips/active/driver/${driverId}`);
  }

  // Booking endpoints (Trip Service)
  async joinTrip(bookingData) {
    return this.request('/trip-service/api/bookings/join', 'POST', bookingData);
  }

  async getActiveBookings(passengerId) {
    return this.request(`/trip-service/api/bookings/active/passenger/${passengerId}`);
  }
}
```

#### Usage Example
```javascript
const api = new CarSharingAPI('http://localhost');

// Login
const token = await api.login('john.driver@example.com', 'SecurePassword123!');

// Get user profile
const profile = await api.getUserProfile('user_123');
console.log(profile);

// Offer a trip
const trip = await api.offerTrip({
  driverId: 'user_123',
  vehicleId: 'vehicle_001',
  sourceLatitude: 50.8154,
  sourceLongitude: 8.6747,
  destinationLatitude: 50.7265,
  destinationLongitude: 7.0895,
  departureTime: '2024-03-25T08:00:00Z',
  availableSeats: 3,
  pricePerSeat: 15.50
});

console.log('Trip created:', trip.tripId);
```

### Using Axios

```javascript
const axios = require('axios');

const api = axios.create({
  baseURL: 'http://localhost',
  headers: {
    'Content-Type': 'application/json'
  }
});

// Intercept requests to add token
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

// Login
async function login(email, password) {
  try {
    const response = await api.post('/auth-service/api/auth/login', {
      email,
      password
    });
    const token = response.data.data.token;
    localStorage.setItem('token', token);
    return token;
  } catch (error) {
    console.error('Login failed:', error.response.data.message);
    throw error;
  }
}

// Get user profile
async function getUserProfile(userId) {
  try {
    const response = await api.get(`/user-service/api/users/${userId}`);
    return response.data.data;
  } catch (error) {
    console.error('Failed to get profile:', error.response.data.message);
    throw error;
  }
}
```

---

## Python Examples

### Using Requests Library

```python
import requests
from typing import Optional, Dict, Any

class CarSharingAPI:
    def __init__(self, base_url: str):
        self.base_url = base_url
        self.token: Optional[str] = None
        self.session = requests.Session()
        self.session.headers.update({'Content-Type': 'application/json'})

    def _get_headers(self) -> Dict[str, str]:
        headers = {}
        if self.token:
            headers['Authorization'] = f'Bearer {self.token}'
        return headers

    def login(self, email: str, password: str) -> str:
        """Login and return JWT token"""
        response = self.session.post(
            f'{self.base_url}/auth-service/api/auth/login',
            json={'email': email, 'password': password},
            headers=self._get_headers()
        )
        response.raise_for_status()

        data = response.json()
        if data['success']:
            self.token = data['data']['token']
            return self.token
        else:
            raise Exception(data['message'])

    def register(self, user_data: Dict[str, Any]) -> str:
        """Register new user and return JWT token"""
        response = self.session.post(
            f'{self.base_url}/auth-service/api/auth/signup',
            json=user_data,
            headers=self._get_headers()
        )
        response.raise_for_status()

        data = response.json()
        if data['success']:
            self.token = data['data']['token']
            return self.token
        else:
            raise Exception(data['message'])

    def get_user_profile(self, user_id: str) -> Dict[str, Any]:
        """Get user profile information"""
        response = self.session.get(
            f'{self.base_url}/user-service/api/users/{user_id}',
            headers=self._get_headers()
        )
        response.raise_for_status()

        data = response.json()
        if data['success']:
            return data['data']
        else:
            raise Exception(data['message'])

    def offer_trip(self, trip_data: Dict[str, Any]) -> Dict[str, Any]:
        """Create a new trip offer"""
        response = self.session.post(
            f'{self.base_url}/trip-service/api/trips/offer',
            json=trip_data,
            headers=self._get_headers()
        )
        response.raise_for_status()

        data = response.json()
        if data['success']:
            return data['data']
        else:
            raise Exception(data['message'])

    def search_matching_trips(self, search_params: Dict[str, Any]) -> list:
        """Search for matching trips"""
        response = self.session.get(
            f'{self.base_url}/trip-service/api/trips/search/matching-route',
            params=search_params,
            headers=self._get_headers()
        )
        response.raise_for_status()

        data = response.json()
        if data['success']:
            return data['data']
        else:
            raise Exception(data['message'])

    def join_trip(self, booking_data: Dict[str, Any]) -> Dict[str, Any]:
        """Join a trip as passenger"""
        response = self.session.post(
            f'{self.base_url}/trip-service/api/bookings/join',
            json=booking_data,
            headers=self._get_headers()
        )
        response.raise_for_status()

        data = response.json()
        if data['success']:
            return data['data']
        else:
            raise Exception(data['message'])

# Usage
api = CarSharingAPI('http://localhost')

# Login
api.login('john.driver@example.com', 'SecurePassword123!')

# Get profile
profile = api.get_user_profile('user_123')
print(f"User: {profile['firstName']} {profile['lastName']}")

# Offer a trip
trip = api.offer_trip({
    'driverId': 'user_123',
    'vehicleId': 'vehicle_001',
    'sourceLatitude': 50.8154,
    'sourceLongitude': 8.6747,
    'destinationLatitude': 50.7265,
    'destinationLongitude': 7.0895,
    'departureTime': '2024-03-25T08:00:00Z',
    'availableSeats': 3,
    'pricePerSeat': 15.50
})
print(f"Trip created: {trip['tripId']}")
```

---

## Java Examples

### Using RestTemplate

```java
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import com.fasterxml.jackson.databind.ObjectMapper;

public class CarSharingAPIClient {
    private String baseUrl;
    private RestTemplate restTemplate;
    private String token;
    private ObjectMapper objectMapper;

    public CarSharingAPIClient(String baseUrl) {
        this.baseUrl = baseUrl;
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    public String login(String email, String password) {
        String url = baseUrl + "/auth-service/api/auth/login";

        Map<String, String> request = Map.of(
            "email", email,
            "password", password
        );

        HttpEntity<Map<String, String>> entity = new HttpEntity<>(
            request,
            getHeaders()
        );

        ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);

        if ((Boolean) ((Map) response.getBody().get("success"))) {
            Map data = (Map) response.getBody().get("data");
            token = (String) data.get("token");
            return token;
        } else {
            throw new RuntimeException((String) response.getBody().get("message"));
        }
    }

    public Map<String, Object> getUserProfile(String userId) {
        String url = baseUrl + "/user-service/api/users/" + userId;

        HttpEntity<?> entity = new HttpEntity<>(getHeadersWithToken());
        ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class, entity);

        Map<String, Object> body = response.getBody();
        if ((Boolean) body.get("success")) {
            return (Map<String, Object>) body.get("data");
        } else {
            throw new RuntimeException((String) body.get("message"));
        }
    }

    public Map<String, Object> offerTrip(Map<String, Object> tripData) {
        String url = baseUrl + "/trip-service/api/trips/offer";

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(
            tripData,
            getHeadersWithToken()
        );

        ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);

        Map<String, Object> body = response.getBody();
        if ((Boolean) body.get("success")) {
            return (Map<String, Object>) body.get("data");
        } else {
            throw new RuntimeException((String) body.get("message"));
        }
    }

    private HttpHeaders getHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    private HttpHeaders getHeadersWithToken() {
        HttpHeaders headers = getHeaders();
        if (token != null) {
            headers.setBearerAuth(token);
        }
        return headers;
    }
}

// Usage
CarSharingAPIClient client = new CarSharingAPIClient("http://localhost");
client.login("john.driver@example.com", "SecurePassword123!");

Map<String, Object> profile = client.getUserProfile("user_123");
System.out.println("User: " + profile.get("firstName"));
```

---

## Common Integration Patterns

### Pattern 1: Error Handling

```javascript
async function apiCall(endpoint, method = 'GET', data = null) {
  try {
    const response = await fetch(endpoint, {
      method,
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${getToken()}`
      },
      body: data ? JSON.stringify(data) : null
    });

    const result = await response.json();

    if (!response.ok) {
      handleError(response.status, result.error, result.message);
      return null;
    }

    if (!result.success) {
      throw new Error(result.message);
    }

    return result.data;
  } catch (error) {
    console.error('API Error:', error);
    showNotification('error', error.message);
    return null;
  }
}

function handleError(status, errorCode, message) {
  switch (status) {
    case 400:
      showNotification('error', 'Invalid input: ' + message);
      break;
    case 401:
      showNotification('error', 'Authentication failed. Please login again.');
      clearToken();
      redirectToLogin();
      break;
    case 404:
      showNotification('error', 'Resource not found.');
      break;
    case 409:
      showNotification('warning', message);
      break;
    default:
      showNotification('error', 'Server error. Please try again later.');
  }
}
```

### Pattern 2: Token Refresh

```javascript
class AuthManager {
  constructor() {
    this.token = localStorage.getItem('token');
  }

  async refreshToken() {
    try {
      const response = await fetch('/auth-service/api/auth/login', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          email: localStorage.getItem('userEmail'),
          password: localStorage.getItem('userPassword') // Not recommended for production
        })
      });

      const data = await response.json();
      if (data.success) {
        this.token = data.data.token;
        localStorage.setItem('token', this.token);
        return this.token;
      }
    } catch (error) {
      console.error('Token refresh failed:', error);
      this.logout();
    }
  }

  logout() {
    this.token = null;
    localStorage.removeItem('token');
    redirectToLogin();
  }

  getToken() {
    return this.token;
  }
}
```

### Pattern 3: Pagination Handler

```python
class PaginatedAPI:
    def __init__(self, api_client):
        self.api = api_client

    def get_all_items(self, endpoint, page_size=20):
        """Fetch all items with pagination"""
        items = []
        page = 0

        while True:
            params = {
                'page': page,
                'size': page_size
            }

            response = self.api.request(endpoint, params=params)

            if not response:
                break

            items.extend(response.get('content', []))

            # Check if there are more pages
            if response.get('last', True):
                break

            page += 1

        return items

# Usage
api = CarSharingAPI('http://localhost')
api.login('user@example.com', 'password')

paginated_api = PaginatedAPI(api)
all_trips = paginated_api.get_all_items('/trip-service/api/trips')
```

---

## Testing

### Using Jest

```javascript
describe('Car Sharing API', () => {
  let api;

  beforeEach(() => {
    api = new CarSharingAPI('http://localhost');
  });

  test('Login should return token', async () => {
    const token = await api.login('test@example.com', 'password');
    expect(token).toBeTruthy();
    expect(token).toMatch(/^eyJ/); // JWT format
  });

  test('Get user profile should return user data', async () => {
    await api.login('test@example.com', 'password');
    const profile = await api.getUserProfile('user_123');

    expect(profile).toHaveProperty('userId');
    expect(profile).toHaveProperty('firstName');
    expect(profile).toHaveProperty('email');
  });

  test('Offer trip should create trip', async () => {
    await api.login('driver@example.com', 'password');

    const trip = await api.offerTrip({
      driverId: 'user_123',
      vehicleId: 'vehicle_001',
      sourceLatitude: 50.8154,
      sourceLongitude: 8.6747,
      destinationLatitude: 50.7265,
      destinationLongitude: 7.0895,
      departureTime: '2024-03-25T08:00:00Z',
      availableSeats: 3,
      pricePerSeat: 15.50
    });

    expect(trip).toHaveProperty('tripId');
    expect(trip.status).toBe('SCHEDULED');
  });
});
```

---

## Rate Limiting Handling

```javascript
class RateLimitedAPI {
  constructor(api) {
    this.api = api;
    this.requestQueue = [];
    this.requestsInWindow = 0;
    this.windowStart = Date.now();
    this.maxRequestsPerMinute = 100;
  }

  async request(endpoint, options) {
    // Wait if rate limit approaching
    while (this.requestsInWindow >= this.maxRequestsPerMinute) {
      const timeSinceWindowStart = Date.now() - this.windowStart;
      if (timeSinceWindowStart < 60000) {
        await new Promise(resolve =>
          setTimeout(resolve, 60000 - timeSinceWindowStart)
        );
      } else {
        this.windowStart = Date.now();
        this.requestsInWindow = 0;
      }
    }

    const response = await this.api.request(endpoint, options);

    // Update rate limit tracking
    if (response.headers['x-ratelimit-remaining']) {
      this.requestsInWindow =
        this.maxRequestsPerMinute -
        parseInt(response.headers['x-ratelimit-remaining']);
    }

    return response;
  }
}
```

---

Last Updated: March 24, 2024
