package ch.uzh.ifi.hase.soprafs24.controller;

import ch.uzh.ifi.hase.soprafs24.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.rest.dto.UserPostDTO;
import ch.uzh.ifi.hase.soprafs24.service.UserService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * UserControllerTest
 * Tests REST API endpoints for UserController.
 */
@WebMvcTest(UserController.class)
public class UserControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockBean
  private UserService userService;

  @Test
  public void createUser_validInput_userCreated() throws Exception {
      User user = new User();
      user.setId(1L);
      user.setUsername("testUsername");
      user.setStatus(UserStatus.ONLINE);

      UserPostDTO userPostDTO = new UserPostDTO();
      userPostDTO.setUsername("testUsername");
      userPostDTO.setPassword("securePassword");

      given(userService.createUser(Mockito.any())).willReturn(user);

      MockHttpServletRequestBuilder postRequest = post("/users")
          .contentType(MediaType.APPLICATION_JSON)
          .content(asJsonString(userPostDTO));

      mockMvc.perform(postRequest)
          .andExpect(status().isCreated())
          .andExpect(jsonPath("$.id", is(user.getId().intValue())))
          .andExpect(jsonPath("$.username", is(user.getUsername())))
          .andExpect(jsonPath("$.status", is(user.getStatus().toString())));
  }

  @Test
public void createUser_usernameAlreadyExists_conflict() throws Exception {
    UserPostDTO userPostDTO = new UserPostDTO();
    userPostDTO.setUsername("existingUser");
    userPostDTO.setPassword("password123");

    given(userService.createUser(Mockito.any()))
        .willThrow(new ResponseStatusException(HttpStatus.CONFLICT, "Username already taken"));

    MockHttpServletRequestBuilder postRequest = post("/users")
        .contentType(MediaType.APPLICATION_JSON)
        .content(asJsonString(userPostDTO));

    mockMvc.perform(postRequest)
        .andExpect(status().isConflict())
        .andExpect(result -> assertNotNull(result.getResolvedException()))
        .andExpect(result -> assertTrue(result.getResolvedException() instanceof ResponseStatusException));
}

  @Test
  public void getUserById_validUser_returnsUser() throws Exception {
      User user = new User();
      user.setId(1L);
      user.setUsername("testUser");
      user.setStatus(UserStatus.ONLINE);
  
      given(userService.getUserById(Mockito.eq(1L), Mockito.eq(1L), Mockito.anyString()))
          .willReturn(user);
  
      MockHttpServletRequestBuilder getRequest = get("/users/1")
          .header("Authorization", "Bearer validToken")
          .queryParam("userId", "1");
  
      mockMvc.perform(getRequest)
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.id", is(user.getId().intValue())))
          .andExpect(jsonPath("$.username", is(user.getUsername())))
          .andExpect(jsonPath("$.status", is(user.getStatus().toString())));
  }

  @Test
public void getUserById_userNotFound_returns404() throws Exception {
  given(userService.getUserById(Mockito.eq(1L), Mockito.eq(99L), Mockito.anyString()))
    .willThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

    MockHttpServletRequestBuilder getRequest = get("/users/99")
        .header("Authorization", "Bearer validToken")
        .queryParam("userId", "1");

    mockMvc.perform(getRequest)
        .andExpect(status().isNotFound());
}

  @Test
  public void updateUserProfile_success_returns204() throws Exception {
      doNothing().when(userService).updateUserProfile(Mockito.eq(1L), Mockito.any(), Mockito.anyString());
  
      MockHttpServletRequestBuilder putRequest = put("/users/1")
          .contentType(MediaType.APPLICATION_JSON)
          .header("Authorization", "Bearer validToken")
          .content(asJsonString(Collections.singletonMap("username", "updatedUser")));
  
      mockMvc.perform(putRequest)
          .andExpect(status().isNoContent());
  }

  @Test
public void updateUserProfile_userNotFound_returns404() throws Exception {
    doThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"))
      .when(userService).updateUserProfile(Mockito.eq(99L), Mockito.any(), Mockito.any());

    MockHttpServletRequestBuilder putRequest = put("/users/99")
        .contentType(MediaType.APPLICATION_JSON)
        .header("Authorization", "Bearer validToken")
        .content(asJsonString(Collections.singletonMap("username", "updatedUser")));

    mockMvc.perform(putRequest)
        .andExpect(status().isNotFound());
}

  /**
   * Helper method to convert objects to JSON.
   */
  private String asJsonString(final Object object) {
      try {
          return new ObjectMapper().writeValueAsString(object);
      } catch (JsonProcessingException e) {
          throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
              String.format("The request body could not be created.%s", e.toString()));
      }
  }
}
