// src/components/__tests__/WeatherDetails.test.tsx
import React from 'react';
import { render, screen } from '@testing-library/react';
import WeatherDetails from '../WeatherDetails';

describe('WeatherDetails', () => {
  const mockProps = {
    temp: 25,
    feelsLike: 26,
    tempMin: 22,
    tempMax: 28,
    humidity: 60,
    windSpeed: 5,
    description: 'clear sky',
  };

  it('renders temperature range correctly', () => {
    render(<WeatherDetails {...mockProps} />);
    expect(screen.getByText(`${Math.round(mockProps.tempMin)}° - ${Math.round(mockProps.tempMax)}°`)).toBeInTheDocument();
  });

  it('renders humidity correctly', () => {
    render(<WeatherDetails {...mockProps} />);
    expect(screen.getByText(`${mockProps.humidity}%`)).toBeInTheDocument();
  });

  it('renders wind speed correctly', () => {
    render(<WeatherDetails {...mockProps} />);
    expect(screen.getByText(`${mockProps.windSpeed} m/s`)).toBeInTheDocument();
  });

  it('renders description correctly and capitalized', () => {
    render(<WeatherDetails {...mockProps} />);
    expect(screen.getByText(mockProps.description)).toBeInTheDocument();
  });

  it('applies custom className if provided', () => {
    const className = 'custom-class';
    const { container } = render(<WeatherDetails {...mockProps} className={className} />);
    expect(container.firstChild).toHaveClass(className);
  });
});
