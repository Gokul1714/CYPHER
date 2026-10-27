# ==============================================================================
# CYPHER - Frontend Dockerfile (React 18 + Vite + Tailwind CSS)
# ==============================================================================

FROM node:20-alpine
WORKDIR /app

# Install dependencies
COPY package*.json ./
RUN npm install

# Copy application source
COPY . .

# Expose Vite dev server port
EXPOSE 5173

# Start development server accessible outside container
CMD ["npm", "run", "dev", "--", "--host", "0.0.0.0"]
