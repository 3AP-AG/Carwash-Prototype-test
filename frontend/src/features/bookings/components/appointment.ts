// The slots a customer can book and how an appointment is shown in German.
// The wash bay takes one car per half hour, from 08:00 to 18:00, up to two weeks ahead.

const BOOKABLE_DAYS = 14;
const FIRST_SLOT_HOUR = 8;
const LAST_SLOT_HOUR = 18;

/** An ISO date (2026-10-13) from the local calendar day, never shifted into another time zone. */
function toIsoDate(date: Date): string {
  const month = `${date.getMonth() + 1}`.padStart(2, '0');
  const day = `${date.getDate()}`.padStart(2, '0');
  return `${date.getFullYear()}-${month}-${day}`;
}

export function formatDate(isoDate: string): string {
  const [year, month, day] = isoDate.split('-');
  return new Date(Number(year), Number(month) - 1, Number(day)).toLocaleDateString('de-CH', {
    weekday: 'short',
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
  });
}

/** 09:30:00 → 09:30 */
export function formatTime(isoTime: string): string {
  return isoTime.slice(0, 5);
}

/** The day a fresh booking form starts on: today. */
export function firstBookableDate(today = new Date()): string {
  return toIsoDate(today);
}

/** The first slot of the day, the one a fresh booking form starts on. */
export const FIRST_TIME_SLOT = `${`${FIRST_SLOT_HOUR}`.padStart(2, '0')}:00:00`;

/** Today and the next two weeks, as select options. */
export function dateOptions(today = new Date()): { value: string; label: string }[] {
  return Array.from({ length: BOOKABLE_DAYS }, (_, offset) => {
    const date = new Date(today.getFullYear(), today.getMonth(), today.getDate() + offset);
    const value = toIsoDate(date);
    return { value, label: formatDate(value) };
  });
}

/** Every half hour the wash bay runs, as select options. */
export const TIME_OPTIONS = Array.from({ length: (LAST_SLOT_HOUR - FIRST_SLOT_HOUR) * 2 + 1 }, (_, step) => {
  const hour = `${FIRST_SLOT_HOUR + Math.floor(step / 2)}`.padStart(2, '0');
  const minute = step % 2 === 0 ? '00' : '30';
  return { value: `${hour}:${minute}:00`, label: `${hour}:${minute}` };
});
