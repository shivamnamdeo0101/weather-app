# Use official Node.js LTS image
FROM node:18-slim

# Install dependencies (like curl for healthcheck)
RUN apt-get update && \
    apt-get install -y --no-install-recommends curl tini && \
    rm -rf /var/lib/apt/lists/*

# Set working directory
WORKDIR /usr/src/app

# Copy package.json and package-lock.json
COPY package*.json ./

# Install dependencies
RUN npm ci && npm cache clean --force

# Copy app source
COPY . .

# Build the Next.js app
RUN npm run build

# Set environment variables
ENV PORT=3001
EXPOSE 3001

# Use tini as entrypoint
ENTRYPOINT ["/usr/bin/tini", "--"]

# Start Next.js in production mode
CMD ["npm", "start"]
