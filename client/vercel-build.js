#!/usr/bin/env node

/**
 * Custom build script for Vercel deployment
 */

const { execSync } = require('child_process');
const path = require('path');
const fs = require('fs');

// Colors for console output
const colors = {
  reset: '\x1b[0m',
  bright: '\x1b[1m',
  fg: {
    green: '\x1b[32m',
    yellow: '\x1b[33m',
    red: '\x1b[31m',
  }
};

console.log(`${colors.bright}${colors.fg.green}Starting Vercel build process for client...${colors.reset}`);

// Ensure we're in the client directory
const clientDir = __dirname;
process.chdir(clientDir);

try {
  // Install dependencies if needed
  if (!fs.existsSync(path.join(clientDir, 'node_modules'))) {
    console.log(`${colors.fg.yellow}Installing dependencies...${colors.reset}`);
    execSync('npm install', { stdio: 'inherit' });
  }

  // Run the build command
  console.log(`${colors.fg.green}Building client application...${colors.reset}`);
  execSync('NODE_OPTIONS=--openssl-legacy-provider vue-cli-service build', { stdio: 'inherit' });

  // Verify the build output
  const distDir = path.join(clientDir, 'dist');
  if (fs.existsSync(distDir)) {
    console.log(`${colors.fg.green}Build successful! Output directory: ${distDir}${colors.reset}`);
    
    // List the contents of the dist directory
    const files = fs.readdirSync(distDir);
    console.log(`${colors.fg.green}Files in dist directory:${colors.reset}`);
    files.forEach(file => console.log(`- ${file}`));
  } else {
    console.error(`${colors.fg.red}Build failed! Output directory not found: ${distDir}${colors.reset}`);
    process.exit(1);
  }
} catch (error) {
  console.error(`${colors.fg.red}Build error:${colors.reset}`, error);
  process.exit(1);
}
