#!/bin/bash

# Build script for Vercel deployment
echo "Building client for Vercel deployment..."

# Build the client application
npm run build:prod

# List the contents of the dist directory
echo "Contents of dist directory:"
ls -la dist/

echo "Build completed successfully!"
