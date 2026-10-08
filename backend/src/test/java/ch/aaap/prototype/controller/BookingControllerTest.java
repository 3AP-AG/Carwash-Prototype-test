package ch.aaap.prototype.controller;

import static ch.aaap.prototype.mock.BookingMock.aResponse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import ch.aaap.prototype.dto.CreateBookingRequest;
import ch.aaap.prototype.platform.error.NotFoundException;
import ch.aaap.prototype.service.BookingService;
import java.util.UUID;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/** Slice test: HTTP mapping and error shape. Protection is tested in platform/. */
@WebMvcTest(BookingController.class)
@WithMockUser
class BookingControllerTest {

  private static final UUID ID = UUID.fromString("00000000-0000-0000-0000-000000000001");

  @Autowired private MockMvc mvc;

  @MockitoBean private BookingService bookingService;

  @Nested
  class CreateBooking {

    @Test
    void returns201WithTheCreatedBooking() throws Exception {
      // given
      when(bookingService.create(any(CreateBookingRequest.class)))
          .thenReturn(aResponse(ID, "Anna Muster"));

      // when / then
      mvc.perform(
              post("/api/bookings")
                  .with(csrf())
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(
                      """
                      {"washProgramme": "COMFORT", "appointmentDate": "2026-11-03", \
                      "appointmentTime": "09:30:00", "customerName": "Anna Muster", \
                      "licencePlate": "ZH 123456"}\
                      """))
          .andExpect(status().isCreated())
          .andExpect(jsonPath("$.id").value(ID.toString()))
          .andExpect(jsonPath("$.washProgramme").value("COMFORT"))
          .andExpect(jsonPath("$.appointmentDate").value("2026-11-03"))
          .andExpect(jsonPath("$.appointmentTime").value("09:30:00"));
    }

    @Test
    void blankCustomerNameIsAFieldError() throws Exception {
      // when / then
      mvc.perform(
              post("/api/bookings")
                  .with(csrf())
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(
                      """
                      {"washProgramme": "COMFORT", "appointmentDate": "2026-11-03", \
                      "appointmentTime": "09:30:00", "customerName": " ", \
                      "licencePlate": "ZH 123456"}\
                      """))
          .andExpect(status().isBadRequest())
          .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
          .andExpect(jsonPath("$.errors[0].field").value("customerName"));
      verifyNoInteractions(bookingService);
    }

    @Test
    void blankLicencePlateIsAFieldError() throws Exception {
      // when / then
      mvc.perform(
              post("/api/bookings")
                  .with(csrf())
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(
                      """
                      {"washProgramme": "COMFORT", "appointmentDate": "2026-11-03", \
                      "appointmentTime": "09:30:00", "customerName": "Anna Muster", \
                      "licencePlate": ""}\
                      """))
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.errors[0].field").value("licencePlate"));
      verifyNoInteractions(bookingService);
    }
  }

  @Nested
  class GetBooking {

    @Test
    void returnsTheBooking() throws Exception {
      // given
      when(bookingService.get(ID)).thenReturn(aResponse(ID, "Anna Muster"));

      // when / then
      mvc.perform(get("/api/bookings/{id}", ID))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.customerName").value("Anna Muster"))
          .andExpect(jsonPath("$.licencePlate").value("ZH 123456"));
    }

    @Test
    void unknownIdIsANotFoundProblem() throws Exception {
      // given
      when(bookingService.get(ID)).thenThrow(new NotFoundException("Booking does not exist."));

      // when / then
      mvc.perform(get("/api/bookings/{id}", ID))
          .andExpect(status().isNotFound())
          .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
          .andExpect(jsonPath("$.detail").value("Booking does not exist."));
    }
  }
}
