import type { NextConfig } from "next";

const nextConfig: NextConfig = {
  // ✅ For small Docker builds
  output: "standalone",
  // ✅ Proxy API requests to backend (bypass CORS)
  async rewrites() {
    return [
      {
        source: "/api/weather-cache/:path*",
        destination: "http://48.194.32.29:8081/api/weather-cache/:path*", // your backend endpoint
      },
    ];
  },
};

export default nextConfig;
