# RDA Vue Application

A full-stack application with Vue.js frontend and Express.js backend for banking Key Risk Indicator (KRI) management.

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


### Development

Start server and client separately:

```bash
# Start client (Vue.js)
npm run client

# Start server (Express)
npm run server
```



## API Endpoints



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
