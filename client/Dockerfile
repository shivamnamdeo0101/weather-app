# -----------------------------
# 1. Use official Node.js LTS image
# -----------------------------
FROM node:20-alpine AS builder

# -----------------------------
# 2. Set working directory inside the container
# -----------------------------
WORKDIR /app

# -----------------------------
# 3. Copy package.json and lockfile first (for caching)
# -----------------------------
COPY package.json package-lock.json* yarn.lock* ./

# -----------------------------
# 4. Install dependencies
# -----------------------------
RUN npm install

# -----------------------------
# 5. Copy the rest of the application code
# -----------------------------
COPY . .

# -----------------------------
# 6. Build the Next.js app
# -----------------------------
RUN npm run build

# -----------------------------
# 7. Production image
# -----------------------------
FROM node:20-alpine AS runner

WORKDIR /app

# Copy only necessary files for production
COPY --from=builder /app/package.json ./
COPY --from=builder /app/node_modules ./node_modules
COPY --from=builder /app/.next ./.next
COPY --from=builder /app/public ./public

# Set environment variable
ENV NODE_ENV production

# Expose the default Next.js port
EXPOSE 3001

# Start the Next.js app
CMD ["npm", "start"]
