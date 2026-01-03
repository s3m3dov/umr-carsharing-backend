# Environment Variables Setup

This project uses a `.env` file to manage sensitive configuration and environment-specific settings.

## Quick Start

1. **Create your `.env` file** from the example:

   ```bash
   cp .env.example .env
   ```

2. **Update the MongoDB URI** in `.env`:
   - Replace the placeholder MongoDB URI with your actual MongoDB Atlas connection string
   - Get your connection string from [MongoDB Atlas](https://cloud.mongodb.com/)

3. **Configure other variables** as needed (see below)

## Required Environment Variables

### MongoDB Configuration (Required)

```bash
MONGODB_URI=mongodb+srv://<username>:<password>@<your-cluster>.mongodb.net/carsharingDB?retryWrites=true&w=majority&appName=Cluster0
```

**How to get your MongoDB URI:**

1. Go to [MongoDB Atlas](https://cloud.mongodb.com/)
2. Navigate to your cluster
3. Click "Connect" → "Connect your application"
4. Copy the connection string
5. Replace `<username>` and `<password>` with your credentials
6. Replace `<your-cluster>` with your cluster name

### JWT Configuration (Required)

```bash
JWT_SECRET_KEY=MjAxOTIwRVlRSTE5MDAxSktOMjgxOTgyNzM4OTFISks=
```

**Note:** Change this to a secure random base64-encoded string in production.

### Google Maps API (Required for trip-service)

```bash
GOOGLE_API_KEY=your-google-maps-api-key
```

**How to get a Google Maps API key:**

1. Go to [Google Cloud Console](https://console.cloud.google.com/)
2. Create a new project or select an existing one
3. Enable the "Maps JavaScript API" and "Directions API"
4. Create credentials (API Key)
5. Copy the API key to your `.env` file

### Firebase Configuration (Optional - for notification-service)

```bash
FIREBASE_CONFIG_PATH=/path/to/firebase-config.json
FIREBASE_DATABASE_URL=https://your-project.firebaseio.com
```

**Note:** If you're not using Firebase notifications, you can comment out these lines or leave them empty.

### CORS Configuration

> [!NOTE]
> CORS is now managed at the **Kubernetes Ingress level** (local) or via **GKE BackendConfig** (production). The `ALLOWED_ORIGINS` environment variable is no longer used by the application containers.

## Using the .env File

The `.env` file is automatically loaded by Docker Compose. All services defined in `docker-compose.yml` will have access to these environment variables.

### Starting the Services

```bash
# Make sure your .env file is configured
docker-compose up -d
```

### Verifying Environment Variables

```bash
# Check if environment variables are loaded correctly
docker-compose config
```

## Security Notes

⚠️ **IMPORTANT:**

- The `.env` file is gitignored and should **NEVER** be committed to version control
- Keep your MongoDB credentials, API keys, and secrets secure
- Use different `.env` files for development, staging, and production
- In production, consider using secret management services (AWS Secrets Manager, HashiCorp Vault, etc.)

## Troubleshooting

### MongoDB Connection Issues

If you see errors like `DNS name not found` or `Failed looking up SRV record`:

1. **Verify your MongoDB cluster exists:**
   - Log in to MongoDB Atlas
   - Check if your cluster is running

2. **Check your connection string:**
   - Ensure the username and password are correct
   - Ensure the cluster name matches your actual cluster

3. **Whitelist your IP:**
   - In MongoDB Atlas, go to Network Access
   - Add your current IP address or use `0.0.0.0/0` for development (not recommended for production)

4. **Test the connection:**

   ```bash
   # View logs for a specific service
   docker logs user-service
   ```

### Firebase Configuration Issues

If Firebase is not configured:

- Comment out the Firebase environment variables in `.env`
- The notification service will start but Firebase features won't work

## Example .env File

See `.env.example` for a complete example with all available configuration options.
