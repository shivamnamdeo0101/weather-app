import type { NextConfig } from "next";

const nextConfig: NextConfig = {
  // Produce a minimal, self-contained server output for small Docker images
  output: "standalone",
};

export default nextConfig;
