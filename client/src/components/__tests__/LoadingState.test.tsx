// src/components/__tests__/LoadingState.test.tsx
import { render, screen } from '@testing-library/react';
import LoadingState from '../LoadingState';

jest.mock('lucide-react', () => ({
  Loader2: (props: any) => <svg data-testid="loader-icon" {...props} />,
}));

describe('LoadingState', () => {
  it('renders the loader icon', () => {
    render(<LoadingState />);
    expect(screen.getByTestId('loader-icon')).toBeInTheDocument();
  });

  it('shows default text when no city is provided', () => {
    render(<LoadingState />);
    expect(screen.getByText(/Fetching forecast for your city/i)).toBeInTheDocument();
  });

  it('shows correct city name if provided', () => {
    render(<LoadingState searchingCity="Paris" />);
    expect(screen.getByText(/Fetching forecast for Paris/i)).toBeInTheDocument();
  });
});
