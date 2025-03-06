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
import org.springframework.web.server.ResponseStatusException;

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
  public ResponseEntity<UserGetDTO> createUser(@RequestBody UserPostDTO userPostDTO) {
    User newUser = userService.createUser(userPostDTO);

    // Convert entity to DTO to include `creationDate`
    UserGetDTO response = DTOMapper.INSTANCE.convertEntityToUserGetDTO(newUser);
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

@PostMapping("/users")
public ResponseEntity<UserGetDTO> createUserViaUsersEndpoint(@RequestBody UserPostDTO userPostDTO) {
    User newUser = userService.createUser(userPostDTO);

    UserGetDTO response = DTOMapper.INSTANCE.convertEntityToUserGetDTO(newUser);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
}

  @GetMapping("/users")
public List<UserGetDTO> getUsers(
    @RequestParam(required = false) Long userId, 
    @RequestHeader(value = "Authorization", required = false) String token,
    @RequestParam(required = false) String username,
    @RequestParam(required = false) String password) {
    
    logger.info("Received request to /users with userId: {}, token: {}, username: {}, password: {}", userId, token, username, password);

    // If username and password are provided, create a new user
    if (username != null && password != null) {
        logger.info("Adding a new user: {}", username);
        UserPostDTO newUser = new UserPostDTO();
        newUser.setUsername(username);
        newUser.setPassword(password);
        userService.createUser(newUser);
    }

    // If userId and token are provided, return users
    if (userId != null && token != null) {
        List<User> users = userService.getUsers(userId, token);
        return users.stream().map(DTOMapper.INSTANCE::convertEntityToUserGetDTO).toList();
    }

    // If neither condition is met, return an error
    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid request: Provide either (username & password) to add a user or (userId & token) to fetch users.");
}


  /**GET Specific User (Requires Token)** */
    /**GET Specific User (Requires Token) */
    @GetMapping("/users/{id}")
    public UserGetDTO getUserById(
    @RequestParam Long userId,
    @PathVariable("id") Long requestedUserId,
    @RequestHeader("Authorization") String token) {

    User user = userService.getUserById(userId, requestedUserId, token);
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
  /** PUT Update User (Requires Token & ID Verification) **/
  @PutMapping("/users/{userId}")
  public ResponseEntity<Void> updateUserProfile(
      @PathVariable Long userId,
      @RequestBody Map<String, Object> updateData,
      @RequestHeader("Authorization") String token) {
  
      // Validate that the token belongs to the user being updated
      userService.updateUserProfile(userId, updateData, token);
      
      return ResponseEntity.noContent().build();
  }
}