FROM python:3.12-slim

# Install system dependencies for WebRTC/av
RUN apt-get update && apt-get install -y --no-install-recommends \
    curl \
    ca-certificates \
    gcc \
    libffi-dev \
    && rm -rf /var/lib/apt/lists/*

# Install uv
COPY --from=ghcr.io/astral-sh/uv:0.4.10 /uv /bin/uv

# Set working directory
WORKDIR /app

# Create non-root user
RUN groupadd -r appuser && useradd -r -g appuser appuser
RUN chown -R appuser:appuser /app

# Copy dependency files
COPY pyproject.toml uv.lock ./

# Install dependencies into system Python environment
RUN uv pip install --system -r pyproject.toml

# Copy project files
COPY app/ ./app/

# Expose server port
EXPOSE 8000

# Switch to non-root user
USER appuser

# Start the Vision Agent server
CMD ["python", "-m", "app.agent", "serve", "--host", "0.0.0.0", "--port", "8000"]
