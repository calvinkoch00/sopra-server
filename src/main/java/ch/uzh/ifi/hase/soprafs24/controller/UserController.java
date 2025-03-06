package ch.uzh.ifi.hase.soprafs24.controller;

import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.rest.dto.UserGetDTO;
import ch.uzh.ifi.hase.soprafs24.rest.dto.UserPostDTO;
import ch.uzh.ifi.hase.soprafs24.rest.mapper.DTOMapper;
import ch.uzh.ifi.hase.soprafs24.service.UserService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
public class UserController {

  private static final Logger logger = LoggerFactory.getLogger(UserController.class);
  private final UserService userService;

  UserController(UserService userService) {
    this.userService = userService;
  }

  /**REGISTER A NEW USER */
  @PostMapping("/register")
  public ResponseEntity<UserGetDTO> createUser(@RequestBody UserPostDTO userPostDTO) {
      User newUser = userService.createUser(userPostDTO);
      UserGetDTO response = DTOMapper.INSTANCE.convertEntityToUserGetDTO(newUser);
      return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  /**LOGIN USER - Token is only in Headers */
  @PostMapping("/login")
public ResponseEntity<Map<String, String>> loginUser(@RequestBody UserPostDTO userPostDTO) {
    User authenticatedUser = userService.loginUser(userPostDTO.getUsername(), userPostDTO.getPassword());

    Map<String, String> response = new HashMap<>();
    response.put("token", authenticatedUser.getToken());
    response.put("id", String.valueOf(authenticatedUser.getId()));

    return ResponseEntity.ok(response);
}


/**REGISTER A NEW USER */
  @PostMapping("/users")
  public ResponseEntity<UserGetDTO> createUserViaUsersEndpoint(@RequestBody UserPostDTO userPostDTO) {
      User newUser = userService.createUser(userPostDTO);

      UserGetDTO response = DTOMapper.INSTANCE.convertEntityToUserGetDTO(newUser);
      return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }
  /**GET ALL USERS (WITHOUT TOKENS) */
  @GetMapping("/users")
  public List<UserGetDTO> getUsers(@RequestParam Long userId, @RequestHeader("Authorization") String token) {
      logger.info("Fetching users for userId: {}", userId);

      List<User> users = userService.getUsers(userId, token);

      return users.stream().map(user -> {
          user.setToken(null);
          return DTOMapper.INSTANCE.convertEntityToUserGetDTO(user);
      }).toList();
  }

  /**GET A SINGLE USER (WITHOUT TOKEN) */
  @GetMapping("/users/{id}")
  public UserGetDTO getUserById(
      @RequestParam Long userId,
      @PathVariable("id") Long requestedUserId,
      @RequestHeader("Authorization") String token) {

      User user = userService.getUserById(userId, requestedUserId, token);
      user.setToken(null);
      return DTOMapper.INSTANCE.convertEntityToUserGetDTO(user);
  }

  /**LOGOUT */
  @PutMapping("/users/{userId}/logout")
  public ResponseEntity<Map<String, String>> logoutUser(
      @PathVariable Long userId, 
      @RequestHeader("Authorization") String token) {
      
      userService.logoutUser(userId, token);
      
      Map<String, String> response = new HashMap<>();
      response.put("message", "User successfully logged out.");
      return ResponseEntity.ok(response);
  }
  
  /**UPDATE USER PROFILE */
  @PutMapping("/users/{userId}")
  public ResponseEntity<Void> updateUserProfile(
      @PathVariable Long userId,
      @RequestBody Map<String, Object> updateData,
      @RequestHeader("Authorization") String token) {
  
      userService.updateUserProfile(userId, updateData, token);
      return ResponseEntity.noContent().build();
  }
}

