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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
public class UserController {

  private static final Logger logger = LoggerFactory.getLogger(UserController.class);

  private final UserService userService;

  UserController(UserService userService) {
    this.userService = userService;
  }

  

  @PostMapping("/register")
  public ResponseEntity<UserGetDTO> createUser(@RequestBody UserPostDTO userPostDTO) {
    User newUser = userService.createUser(userPostDTO);

    
    UserGetDTO response = DTOMapper.INSTANCE.convertEntityToUserGetDTO(newUser);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
}

  
  @PostMapping("/login")
  public ResponseEntity<UserGetDTO> loginUser(@RequestBody UserPostDTO userPostDTO) {
      User authenticatedUser = userService.loginUser(userPostDTO.getUsername(), userPostDTO.getPassword());

      UserGetDTO response = DTOMapper.INSTANCE.convertEntityToUserGetDTO(authenticatedUser);
      return ResponseEntity.ok(response);
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

    if (username != null && password != null) {
        logger.info("Adding a new user: {}", username);
        UserPostDTO newUser = new UserPostDTO();
        newUser.setUsername(username);
        newUser.setPassword(password);
        userService.createUser(newUser);
    }

    if (userId != null && token != null) {
        List<User> users = userService.getUsers(userId, token);
        return users.stream().map(DTOMapper.INSTANCE::convertEntityToUserGetDTO).toList();
    }

    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid request: Provide either (username & password) to add a user or (userId & token) to fetch users.");
}


    @GetMapping("/users/{id}")
    public UserGetDTO getUserById(
    @RequestParam Long userId,
    @PathVariable("id") Long requestedUserId,
    @RequestHeader("Authorization") String token) {

    User user = userService.getUserById(userId, requestedUserId, token);
    return DTOMapper.INSTANCE.convertEntityToUserGetDTO(user);
}

  @PutMapping("/users/{userId}/logout")
  public ResponseEntity<Map<String, String>> logoutUser(
      @PathVariable Long userId, 
      @RequestHeader("Authorization") String token) {
      
      userService.logoutUser(userId, token);
      
      Map<String, String> response = new HashMap<>();
      response.put("message", "User successfully logged out.");
      return ResponseEntity.ok(response);
  }
  
  @PutMapping("/users/{userId}")
  public ResponseEntity<Void> updateUserProfile(
      @PathVariable Long userId,
      @RequestBody Map<String, Object> updateData,
      @RequestHeader("Authorization") String token) {
  
      userService.updateUserProfile(userId, updateData, token);
      
      return ResponseEntity.noContent().build();
  }
}