#!/usr/bin/env bash
set -e

echo "=== MindTrack Project Setup ==="

# Configure git hooks
git config core.hooksPath .githooks
chmod +x .githooks/*
echo ">> Git hooks configured."

# Backend setup
if [ -d "backend" ]; then
    echo ">> Building backend..."
    cd backend
    mvn compile -q
    cd ..
    echo ">> Backend ready."
fi

# Frontend setup
if [ -d "frontend" ]; then
    echo ">> Installing frontend dependencies..."
    cd frontend
    npm ci
    cd ..
    echo ">> Frontend ready."
fi

echo "=== Setup complete ==="
echo "Run 'cd backend && mvn spring-boot:run -Dspring-boot.run.profiles=local' to start backend"
echo "Run 'cd frontend && npm run dev' to start frontend"
