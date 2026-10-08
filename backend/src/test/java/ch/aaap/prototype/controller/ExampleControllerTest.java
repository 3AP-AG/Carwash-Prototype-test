package ch.aaap.prototype.controller;

import static ch.aaap.prototype.mock.ExampleMock.aResponse;
import static ch.aaap.prototype.mock.ExampleMock.aSummary;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import ch.aaap.prototype.dto.CreateExampleRequest;
import ch.aaap.prototype.dto.UpdateExampleRequest;
import ch.aaap.prototype.platform.error.ConflictException;
import ch.aaap.prototype.platform.error.NotFoundException;
import ch.aaap.prototype.platform.web.PageResponse;
import ch.aaap.prototype.service.ExampleService;
import java.util.List;
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
@WebMvcTest(ExampleController.class)
@WithMockUser
class ExampleControllerTest {

  private static final UUID ID = UUID.fromString("00000000-0000-0000-0000-000000000001");

  @Autowired private MockMvc mvc;

  @MockitoBean private ExampleService exampleService;

  @Nested
  class ListExamples {

    @Test
    void returnsAPageOfExamples() throws Exception {
      // given
      when(exampleService.list(0, 20))
          .thenReturn(new PageResponse<>(List.of(aSummary("First")), 0, 20, 1));

      // when / then
      mvc.perform(get("/api/examples"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.items[0].title").value("First"))
          .andExpect(jsonPath("$.items[0].status").value("OPEN"))
          .andExpect(jsonPath("$.page").value(0))
          .andExpect(jsonPath("$.totalItems").value(1));
    }

    @Test
    void sizeAboveTheLimitIsAValidationProblem() throws Exception {
      // when / then
      mvc.perform(get("/api/examples").param("size", "500"))
          .andExpect(status().isBadRequest())
          .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
          .andExpect(jsonPath("$.errors[0].field").value("size"));
    }
  }

  @Nested
  class GetExample {

    @Test
    void returnsTheExample() throws Exception {
      // given
      when(exampleService.get(ID)).thenReturn(aResponse(ID, "First"));

      // when / then
      mvc.perform(get("/api/examples/{id}", ID))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.id").value(ID.toString()))
          .andExpect(jsonPath("$.description").value("A description."));
    }

    @Test
    void unknownIdIsANotFoundProblem() throws Exception {
      // given
      when(exampleService.get(ID)).thenThrow(new NotFoundException("Example does not exist."));

      // when / then
      mvc.perform(get("/api/examples/{id}", ID))
          .andExpect(status().isNotFound())
          .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
          .andExpect(jsonPath("$.detail").value("Example does not exist."));
    }
  }

  @Nested
  class CreateExample {

    @Test
    void returns201WithTheCreatedExample() throws Exception {
      // given
      when(exampleService.create(any(CreateExampleRequest.class))).thenReturn(aResponse(ID, "New"));

      // when / then
      mvc.perform(
              post("/api/examples")
                  .with(csrf())
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(
                      """
                      {"title": "New", "status": "OPEN", "description": ""}\
                      """))
          .andExpect(status().isCreated())
          .andExpect(jsonPath("$.id").value(ID.toString()));
    }

    @Test
    void blankTitleIsAFieldError() throws Exception {
      // when / then
      mvc.perform(
              post("/api/examples")
                  .with(csrf())
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(
                      """
                      {"title": " ", "status": "OPEN", "description": ""}\
                      """))
          .andExpect(status().isBadRequest())
          .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
          .andExpect(jsonPath("$.errors[0].field").value("title"));
      verifyNoInteractions(exampleService);
    }
  }

  @Nested
  class UpdateExample {

    @Test
    void returnsTheUpdatedExample() throws Exception {
      // given
      when(exampleService.update(eq(ID), any(UpdateExampleRequest.class)))
          .thenReturn(aResponse(ID, "Changed"));

      // when / then
      mvc.perform(
              put("/api/examples/{id}", ID)
                  .with(csrf())
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(
                      """
                      {"title": "Changed", "status": "CLOSED", "description": "", "version": 0}\
                      """))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.title").value("Changed"));
    }

    @Test
    void returns409ProblemForAStaleVersion() throws Exception {
      // given
      when(exampleService.update(eq(ID), any(UpdateExampleRequest.class)))
          .thenThrow(new ConflictException("Changed by someone else."));

      // when / then
      mvc.perform(
              put("/api/examples/{id}", ID)
                  .with(csrf())
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(
                      """
                      {"title": "Changed", "status": "CLOSED", "description": "", "version": 0}\
                      """))
          .andExpect(status().isConflict())
          .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
          .andExpect(jsonPath("$.detail").value("Changed by someone else."));
    }
  }
}
