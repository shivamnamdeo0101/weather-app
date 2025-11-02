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

# Accept build argument for API URL and set as environment variable for Next.js build
ARG NEXT_PUBLIC_API_BASE_URL
ENV NEXT_PUBLIC_API_BASE_URL=$NEXT_PUBLIC_API_BASE_URL

# Build the Next.js app (env var will be embedded at build time)
RUN npm run build

# Set environment variables
ENV PORT=3001
EXPOSE 3001

# Use tini as entrypoint
ENTRYPOINT ["/usr/bin/tini", "--"]

# Start Next.js in production mode
CMD ["npm", "start"]
