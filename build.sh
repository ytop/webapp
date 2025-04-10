#!/bin/bash

# Build script for Vercel deployment

echo "Starting build process..."

# Install client dependencies
echo "Installing client dependencies..."
cd client
npm install

# Build client
echo "Building client application..."
npm run build:prod

# Return to root directory
cd ..

echo "Build process completed!"
