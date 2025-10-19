// src/components/__tests__/WeatherIcon.test.tsx
import React from 'react';
import { render, screen } from '@testing-library/react';
import WeatherIcon from '../WeatherIcon';

describe('WeatherIcon', () => {
  it('renders the correct emoji for a given iconCode', () => {
    const { rerender } = render(<WeatherIcon iconCode="01d" />);
    expect(screen.getByText('☀️')).toBeInTheDocument();

    rerender(<WeatherIcon iconCode="01n" />);
    expect(screen.getByText('🌙')).toBeInTheDocument();

    rerender(<WeatherIcon iconCode="09d" />);
    expect(screen.getByText('🌧️')).toBeInTheDocument();
  });

  it('renders default emoji if iconCode is unknown', () => {
    render(<WeatherIcon iconCode="unknown" />);
    expect(screen.getByText('🌤️')).toBeInTheDocument();
  });

  it('applies default size class (md) if no size is provided', () => {
    const { container } = render(<WeatherIcon iconCode="01d" />);
    expect(container.firstChild).toHaveClass('text-2xl');
  });

  it('applies correct size class when size prop is provided', () => {
    const { rerender, container } = render(<WeatherIcon iconCode="01d" size="sm" />);
    expect(container.firstChild).toHaveClass('text-lg');

    rerender(<WeatherIcon iconCode="01d" size="lg" />);
    expect(container.firstChild).toHaveClass('text-3xl');
  });

  it('applies custom className if provided', () => {
    const { container } = render(<WeatherIcon iconCode="01d" className="custom-class" />);
    expect(container.firstChild).toHaveClass('custom-class');
  });
});
