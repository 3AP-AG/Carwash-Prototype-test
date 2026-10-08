# Backlog

Owned by the builder; drafted and kept up to date by the method skill.
The format, including the per-item state block (CAAS-1373), is defined by the plugin, not by the
template. The template only fixes the location of this file.

<!-- The method skill adds backlog items (D-01, D-02, …) below. -->

UI language: German

## D-01 · Must · A customer books a wash appointment
Status: open
Built on pull request #1: the gates pass in CI and the acceptance tests below pass against the
built image locally. Template version 0.1.0 has no deploy job yet (CAAS-1380), so there is no demo
URL to check the item against; it stays `open` until the prototype can be opened and verified.
- [ ] The booking page offers a choice of at least three wash programmes, each shown with its name and price
- [ ] The customer can pick a date and a time and enter their name and licence plate
- [ ] Submitting the form with the name or the licence plate empty keeps the customer on the page and shows an error next to the empty field
- [ ] After a successful booking a confirmation appears showing the chosen programme, the date and the time

## D-02 · Must · The operator sees all bookings in one list
Status: open
- [ ] The operator's list shows one row per booking with date, time, wash programme, customer name and licence plate
- [ ] A booking made on the booking page appears in that list
- [ ] The list is sorted by date and time, earliest appointment first
- [ ] When there is no booking at all, the list shows a message saying that there are no bookings yet

## D-03 · Should · The operator opens one booking and cancels it
Status: open
- [ ] Opening a booking from the list shows that booking with programme, date, time, name and licence plate
- [ ] The booking has a cancel button, and after cancelling it is marked as cancelled in the list
- [ ] Opening a booking that does not exist shows a "does not exist" message

## D-04 · Should · A time slot can only be booked once
Status: open
- [ ] Booking a date and time that is already taken shows an error message and does not create a second booking
- [ ] The same time on another date can still be booked
