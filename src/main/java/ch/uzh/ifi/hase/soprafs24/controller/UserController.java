package ch.uzh.ifi.hase.soprafs24.controller;

import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.rest.dto.UserGetDTO;
import ch.uzh.ifi.hase.soprafs24.rest.dto.UserPostDTO;
import ch.uzh.ifi.hase.soprafs24.rest.mapper.DTOMapper;
import ch.uzh.ifi.hase.soprafs24.service.UserService;

import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * User Controller
 * Handles user-related HTTP requests.
 */import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
public class UserController {

  private static final Logger logger = LoggerFactory.getLogger(UserController.class);

  private final UserService userService;

  UserController(UserService userService) {
    this.userService = userService;
  }

  

  /**POST Register (No Token Required)** */
  @PostMapping("/register")
  public ResponseEntity<Map<String, String>> createUser(@RequestBody UserPostDTO userPostDTO) {
    userService.createUser(userPostDTO);

    // Return an empty response body with a success message
    Map<String, String> response = new HashMap<>();
    response.put("message", "User successfully registered.");
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  /** 
   * Login (RETURNS TOKEN AND ID)
   */
  @PostMapping("/login")
  public ResponseEntity<UserGetDTO> loginUser(@RequestBody UserPostDTO userPostDTO) {
      User authenticatedUser = userService.loginUser(userPostDTO.getUsername(), userPostDTO.getPassword());

      UserGetDTO response = DTOMapper.INSTANCE.convertEntityToUserGetDTO(authenticatedUser);
      return ResponseEntity.ok(response); // Returns token and ID
  }

  /**GET All Users (Requires Token & ID Verification)** */
  @GetMapping("/users")
  public List<UserGetDTO> getUsers(
      @RequestParam Long userId, 
      @RequestHeader(value = "Authorization", required = false) String token) {
      
      logger.info("Received request to /users with userId: {} and token: {}", userId, token);

      if (token == null) {
          logger.error("Authorization header is missing");
          throw new RuntimeException("Authorization header is missing");
      }

      List<User> users = userService.getUsers(userId, token);
      return users.stream().map(DTOMapper.INSTANCE::convertEntityToUserGetDTO).toList();
  }


  /**GET Specific User (Requires Token)** */
  @GetMapping("/users/{id}")
  public UserGetDTO getUserById(
      @PathVariable Long id, 
      @RequestHeader("Authorization") String token) {
      
      User user = userService.getUserById(id, token);
      return DTOMapper.INSTANCE.convertEntityToUserGetDTO(user);
  }

  /**PUT Logout (Requires Token & ID Verification)** */
  @PutMapping("/users/{userId}/logout")
  public ResponseEntity<Map<String, String>> logoutUser(
      @PathVariable Long userId, 
      @RequestHeader("Authorization") String token) {
      
      userService.logoutUser(userId, token);
      
      Map<String, String> response = new HashMap<>();
      response.put("message", "User successfully logged out.");
      return ResponseEntity.ok(response);
  }
}