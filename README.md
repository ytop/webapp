# RDA Vue Application

A full-stack application with Vue.js frontend and Express.js backend.

## Project Structure

```
my-project/
├── client/           # Vue.js 2 frontend
│   ├── src/          # Vue source code
│   ├── public/       # Public assets
│   ├── package.json  # Client dependencies
├── server/           # Express backend
│   ├── app.js        # Main server file
│   ├── routes/       # API routes
│   ├── data/         # Mock data
│   ├── package.json  # Server dependencies
├── package.json      # Root package.json for scripts
├── .gitignore
└── README.md
```

## Setup Instructions

### Installation

1. Install dependencies for all packages:

```bash
npm run install:all
```

Or install them separately:

```bash
# Root dependencies
npm install

# Client dependencies
cd client && npm install

# Server dependencies
cd server && npm install
```

### Fixing Node.js v20 Compatibility Issues

If you encounter issues with fsevents or other dependencies when using Node.js v20, run the fix script:

```bash
./fix-dependencies.sh
```

This script will reinstall the dependencies with the correct versions and configurations for Node.js v20 compatibility.

### Development

To start both the client and server in development mode:

```bash
npm run dev
```

Or start them separately:

```bash
# Start client (Vue.js)
npm run client

# Start server (Express)
npm run server
```

### Production Build

To build the client for production:

```bash
npm run build
```

The build output will be in the `dist` directory.

## API Endpoints

### KRI Endpoints

- `GET /kri/all` - Get all KRIs
- `GET /kri/:kriId` - Get a specific KRI
- `POST /kri/updateDescription` - Update KRI description
- `POST /kri/update` - Update a KRI
- `POST /kri/uploadDocument` - Upload a document for a KRI
- `GET /kri/:kriId/documents` - Get documents for a KRI

### KRI Task Endpoints

- `GET /kri/alltasks` - Get all KRI tasks
- `GET /kri/tasks/:id` - Get a specific KRI task
- `POST /kri/tasks/submitValue` - Submit a KRI value

## Environment Variables

### Client

Create a `.env.development` file in the client directory with the following variables:

```
ENV = 'development'
VUE_APP_BASE_API = '/rdaapi'
VUE_APP_BACKEND_NAME = 'rdaapi'
VUE_APP_PROXY = http://localhost:8888
VUE_APP_MOCK = 'false'
```

### Server

The server uses the following environment variables:

- `PORT` - The port to run the server on (default: 8888)
- `NODE_ENV` - The environment to run the server in (development, production)
