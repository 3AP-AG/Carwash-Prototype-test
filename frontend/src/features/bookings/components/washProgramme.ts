// Display labels for WashProgramme, in one place for the form and the confirmation.
import type { BookingResponseWashProgramme } from '@/api/generated/model';

export const WASH_PROGRAMME_LABELS: Record<BookingResponseWashProgramme, string> = {
  BASIC: 'Basiswäsche',
  COMFORT: 'Komfortwäsche',
  PREMIUM: 'Premiumwäsche',
};

const WASH_PROGRAMME_PRICES: Record<BookingResponseWashProgramme, number> = {
  BASIC: 12,
  COMFORT: 19,
  PREMIUM: 29,
};

export function washProgrammePrice(programme: BookingResponseWashProgramme): string {
  return `CHF ${WASH_PROGRAMME_PRICES[programme]}.–`;
}

export const WASH_PROGRAMME_OPTIONS = Object.entries(WASH_PROGRAMME_LABELS).map(([value, label]) => ({
  value,
  label: `${label} – ${washProgrammePrice(value as BookingResponseWashProgramme)}`,
}));
