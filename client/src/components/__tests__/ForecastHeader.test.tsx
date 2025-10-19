import { render, screen } from '@testing-library/react';
import ForecastHeader from '../ForecastHeader';

describe('ForecastHeader', () => {
  const city = 'New York';
  const count = 5;

  it('renders the city name in the heading', () => {
    render(<ForecastHeader city={city} count={count} />);
    expect(screen.getByRole('heading', { level: 2 })).toHaveTextContent(
      `Weather Forecast for ${city}`
    );
  });

  it('renders the count of forecast entries', () => {
    render(<ForecastHeader city={city} count={count} />);
    expect(screen.getByText(`${count} forecast entries with predictions`)).toBeInTheDocument();
  });
});
