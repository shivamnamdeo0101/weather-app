Feature: Weather Forecast API
  As a user
  I want to get weather forecast for a city
  So that I can plan my activities

  Background:
    Given the weather service is available

  Scenario: Successfully retrieve weather forecast for a valid city
    Given a valid city name "London"
    When I request the weather forecast
    Then I should receive a successful response with forecast data

  Scenario: Handle rate limit exceeded
    Given the rate limit has been exceeded
    When I request the weather forecast for "Paris"
    Then I should receive a rate limit error response

  Scenario: Handle city not found
    Given an invalid city name "UnknownCity123"
    When I request the weather forecast
    Then I should receive a city not found error response

  Scenario: Handle empty city name
    Given an empty city name
    When I request the weather forecast
    Then I should receive a bad request error response

