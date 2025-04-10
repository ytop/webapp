#!/usr/bin/env node

/**
 * Script to deploy the application to Vercel
 */

const { execSync } = require('child_process');
const fs = require('fs');
const path = require('path');

// Colors for console output
const colors = {
  reset: '\x1b[0m',
  bright: '\x1b[1m',
  dim: '\x1b[2m',
  underscore: '\x1b[4m',
  blink: '\x1b[5m',
  reverse: '\x1b[7m',
  hidden: '\x1b[8m',
  
  fg: {
    black: '\x1b[30m',
    red: '\x1b[31m',
    green: '\x1b[32m',
    yellow: '\x1b[33m',
    blue: '\x1b[34m',
    magenta: '\x1b[35m',
    cyan: '\x1b[36m',
    white: '\x1b[37m',
  },
  
  bg: {
    black: '\x1b[40m',
    red: '\x1b[41m',
    green: '\x1b[42m',
    yellow: '\x1b[43m',
    blue: '\x1b[44m',
    magenta: '\x1b[45m',
    cyan: '\x1b[46m',
    white: '\x1b[47m',
  }
};

// Helper function to execute commands
function runCommand(command, options = {}) {
  console.log(`${colors.fg.cyan}> ${command}${colors.reset}`);
  return execSync(command, { 
    stdio: 'inherit',
    ...options
  });
}

// Check if Vercel CLI is installed
function checkVercelCLI() {
  try {
    execSync('vercel --version', { stdio: 'ignore' });
    return true;
  } catch (error) {
    return false;
  }
}

// Main function
async function deploy() {
  console.log(`\n${colors.bright}${colors.fg.cyan}=== Deploying to Vercel ===${colors.reset}\n`);
  
  // Check if Vercel CLI is installed
  if (!checkVercelCLI()) {
    console.log(`${colors.fg.yellow}Vercel CLI not found. Installing...${colors.reset}`);
    runCommand('npm install -g vercel');
  }
  
  // Check if user is logged in to Vercel
  try {
    execSync('vercel whoami', { stdio: 'ignore' });
  } catch (error) {
    console.log(`${colors.fg.yellow}You need to login to Vercel first.${colors.reset}`);
    runCommand('vercel login');
  }
  
  // Check if vercel.json exists
  if (!fs.existsSync(path.join(process.cwd(), 'vercel.json'))) {
    console.error(`${colors.fg.red}Error: vercel.json not found in the project root.${colors.reset}`);
    process.exit(1);
  }
  
  // Deploy to Vercel
  console.log(`\n${colors.fg.green}Deploying to Vercel...${colors.reset}`);
  
  try {
    // First deploy without production flag to preview
    runCommand('vercel');
    
    // Ask for confirmation before deploying to production
    console.log(`\n${colors.fg.yellow}Do you want to deploy to production? (y/n)${colors.reset}`);
    const readline = require('readline').createInterface({
      input: process.stdin,
      output: process.stdout
    });
    
    readline.question('', (answer) => {
      if (answer.toLowerCase() === 'y' || answer.toLowerCase() === 'yes') {
        console.log(`\n${colors.fg.green}Deploying to production...${colors.reset}`);
        runCommand('vercel --prod');
        console.log(`\n${colors.bright}${colors.fg.green}Deployment to production completed!${colors.reset}`);
      } else {
        console.log(`\n${colors.fg.yellow}Production deployment cancelled.${colors.reset}`);
      }
      readline.close();
    });
  } catch (error) {
    console.error(`\n${colors.fg.red}Deployment failed:${colors.reset}`, error);
    process.exit(1);
  }
}

// Run the deployment
deploy().catch(error => {
  console.error(`\n${colors.fg.red}Deployment script error:${colors.reset}`, error);
  process.exit(1);
});
