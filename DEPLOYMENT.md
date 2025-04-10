# Deploying to Vercel

This document explains how to deploy the KRI Management System to Vercel.

## Prerequisites

1. [Node.js](https://nodejs.org/) (version 14 or higher)
2. [npm](https://www.npmjs.com/) (version 6 or higher)
3. [Vercel CLI](https://vercel.com/cli) (optional for dashboard deployment)
4. A [Vercel](https://vercel.com/) account

## Deployment Steps

### Option 1: Deploy via Vercel Dashboard (Recommended)

The easiest way to deploy is directly through the Vercel dashboard:

1. Go to [vercel.com](https://vercel.com/) and sign in or create an account
2. Click "New Project"
3. Import your GitHub repository or upload your project files
4. For the framework preset, select "Vue.js"
5. Configure the following settings:
   - Root Directory: `client`
   - Build Command: `npm run build:prod`
   - Output Directory: `dist`
6. Click "Deploy"

### Option 2: Deploy via Vercel CLI

If you prefer using the command line:

1. Install Vercel CLI globally:
   ```bash
   npm install -g vercel
   ```

2. Log in to Vercel:
   ```bash
   vercel login
   ```

3. Navigate to the client directory:
   ```bash
   cd client
   ```

4. Deploy a preview version:
   ```bash
   vercel
   ```

5. Deploy to production:
   ```bash
   vercel --prod
   ```

## Configuration

The deployment configuration is defined in the `vercel.json` file in the client directory. This file specifies:

- Routing rules to ensure the Vue.js router works correctly

### Environment Variables

You may need to set up environment variables in your Vercel project. You can do this through the Vercel dashboard or by using the Vercel CLI:

```bash
vercel env add
```

Important environment variables to consider:

- `NODE_ENV`: Set to "production" for production deployments
- `VUE_APP_BACKEND_NAME`: The name of your backend API path (default: "rdaapi")

## Backend Deployment

For this initial deployment, we're focusing on the frontend only. The backend API will need to be deployed separately. Options include:

1. Deploy the server to a separate Vercel project as a serverless function
2. Deploy the server to a traditional hosting service like Heroku, DigitalOcean, or AWS
3. Use a mock API for demonstration purposes

## Troubleshooting

If you encounter issues during deployment:

1. Check the Vercel deployment logs in the Vercel dashboard
2. Ensure all required environment variables are set
3. Verify that the build commands are correct
4. Check that the Node.js version specified in `package.json` is supported by Vercel

For more help, refer to the [Vercel documentation](https://vercel.com/docs).
