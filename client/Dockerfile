# ---------- Base Builder Stage ----------
  FROM node:22-bookworm-slim AS builder

  WORKDIR /app
  
  # Copy dependency files first (cache optimization)
  COPY package*.json ./
  COPY tsconfig.json next.config.ts postcss.config.mjs eslint.config.mjs next-env.d.ts ./
  
  # Install deps (with devDependencies for build)
  RUN npm ci --no-audit --no-fund
  
  # Copy app source
  COPY src ./src
  COPY public ./public
  
  # Disable telemetry
  ENV NEXT_TELEMETRY_DISABLED=1
  ENV NODE_ENV=production
  
  # Build the app
  RUN npm run build
  
  # ---------- Runner Stage ----------
  FROM node:22-bookworm-slim AS runner
  
  WORKDIR /app
  
  # Copy only the built output and production deps
  COPY --from=builder /app/node_modules ./node_modules
  COPY --from=builder /app/package*.json ./
  COPY --from=builder /app/.next ./.next
  COPY --from=builder /app/public ./public
  COPY --from=builder /app/next.config.ts ./
  
  # Disable telemetry
  ENV NEXT_TELEMETRY_DISABLED=1
  ENV NODE_ENV=production
  
  EXPOSE 3000
  
  CMD ["npm", "start"]
  