import React from 'react';
import { render, screen } from '@testing-library/react';
import TemperatureItemCard from '../TemperatureItemCard';

describe('TemperatureItemCard', () => {
  const props = {
    emoji: '☀️',
    formattedDate: 'October 19, 2025',
    time: '14:00',
    temp: 25.7,
    feelsLike: 27.2,
    tempMin: 22.1,
    tempMax: 28.9,
    humidity: 65,
    windSpeed: 5.4,
    description: 'clear sky',
  };

  beforeEach(() => {
    render(<TemperatureItemCard {...props} />);
  });

  it('renders emoji, date, and time', () => {
    expect(screen.getByText(props.emoji)).toBeInTheDocument();
    expect(screen.getByText(props.formattedDate)).toBeInTheDocument();
    expect(screen.getByText(props.time)).toBeInTheDocument();
  });

  it('renders main temperature and feels like', () => {
    expect(screen.getByText(`${Math.round(props.temp)}°C`)).toBeInTheDocument();
    expect(screen.getByText(`Feels like ${Math.round(props.feelsLike)}°C`)).toBeInTheDocument();
  });

  it('renders temperature range', () => {
    expect(screen.getByText(`${Math.round(props.tempMin)}° - ${Math.round(props.tempMax)}°`)).toBeInTheDocument();
  });

  it('renders humidity', () => {
    expect(screen.getByText(`${props.humidity}%`)).toBeInTheDocument();
  });

  it('renders wind speed', () => {
    expect(screen.getByText(`${props.windSpeed} m/s`)).toBeInTheDocument();
  });

  it('renders weather description', () => {
    expect(screen.getByText(props.description)).toBeInTheDocument();
  });
});
