import { render, screen, fireEvent } from '@testing-library/react';
import ErrorAlert from '../ErrorAlert';
import UI from '@/constants/ui';

describe('ErrorAlert', () => {
  const errorMessage = 'Network timeout error';
  const city = 'London';
  const mockOnClose = jest.fn();

  it('renders error alert with default error icon', () => {
    render(<ErrorAlert error="Some error" />);
    expect(screen.getByRole('alert')).toBeInTheDocument();
    expect(screen.getByText(UI.Labels.ERROR_TITLE)).toBeInTheDocument();
    expect(screen.getByText('Some error')).toBeInTheDocument();
  });

  it('renders network icon when error includes network-related words', () => {
    render(<ErrorAlert error={errorMessage} />);
    // Because icon is text from UI.Icons, check that it is rendered
    expect(screen.getAllByText(UI.Icons.NETWORK)[0]).toBeInTheDocument();
  });

  it('renders searchingCity message if provided', () => {
    render(<ErrorAlert error={errorMessage} searchingCity={city} />);
    expect(screen.getByText(`Failed to load weather for: ${city}`)).toBeInTheDocument();
  });

  it('calls onClose when close button is clicked', () => {
    render(<ErrorAlert error={errorMessage} onClose={mockOnClose} />);
    const button = screen.getByRole('button', { name: /close error/i });
    fireEvent.click(button);
    expect(mockOnClose).toHaveBeenCalledTimes(1);
  });
});
