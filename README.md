# RiskSphere - KRI Management Application

A Vue.js application for managing Key Risk Indicators (KRIs) with an Express backend.

## Project Setup

### Install Dependencies

```bash
npm install
```

### Start Development Server

To start both the Vue frontend and Express API server:

```bash
npm run dev:full
```

Or start them separately:

```bash
# Start Vue frontend
npm run dev

# Start Express API server
npm run api
```

### Build for Production

```bash
npm run build
```

## Architecture

### Frontend

- Vue.js 2.6
- Element UI for components
- Axios for API requests

### Backend

- Express.js for API server
- lowdb for in-memory database
- nanoid for generating unique IDs

## API Endpoints

### KRI Endpoints

- `GET /kri/all` - Get all KRIs
- `GET /kri/:kriId` - Get a specific KRI
- `POST /kri/updateDescription` - Update KRI description
- `POST /kri/update` - Update a KRI
- `POST /kri/uploadDocument` - Upload a document for a KRI
- `GET /kri/:kriId/documents` - Get documents for a KRI

### KRI Task Endpoints

- `GET /kri/tasks` - Get all KRI tasks
- `GET /kri/tasks/:id` - Get a specific KRI task
- `POST /kri/tasks/submitValue` - Submit a KRI value

## Development Notes

- The application uses an in-memory database (lowdb) for development
- All mock data has been moved from the frontend to the backend
- File uploads are stored in the `server/uploads` directory
