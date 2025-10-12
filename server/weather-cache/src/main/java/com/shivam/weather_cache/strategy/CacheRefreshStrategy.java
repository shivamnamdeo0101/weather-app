package com.shivam.weather_cache.strategy;

public interface CacheRefreshStrategy {
    /**
     * Decides whether city needs refresh and refreshes if required.
     *
     * @param cityKey e.g., "weather:navi_mumbai"
     * @return true if refresh happened, false otherwise
     */
    boolean refreshIfRequired(String cityKey);
}
