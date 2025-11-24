Feature: Weather Cache Operations
  As a user
  I want to get weather forecast from cache
  So that I can get faster responses when data is cached

  Background:
    Given the weather cache service is available

  Scenario: Cache HIT - Retrieve weather from cache
    Given the cache contains weather data for "London"
    When I request weather forecast for "London"
    Then I should receive a successful response with X-Cache header "HIT"
    And the response should contain weather data

  Scenario: Cache MISS - Fetch from weather service and cache
    Given the cache does not contain weather data for "Paris"
    And the weather service returns data for "Paris"
    When I request weather forecast for "Paris"
    Then I should receive a successful response with X-Cache header "MISS"
    And the response should contain weather data
    And the data should be saved to cache

