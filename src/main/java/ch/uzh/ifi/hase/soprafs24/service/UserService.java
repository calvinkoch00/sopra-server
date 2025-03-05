package ch.uzh.ifi.hase.soprafs24.service;

import ch.uzh.ifi.hase.soprafs24.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.repository.UserRepository;
import ch.uzh.ifi.hase.soprafs24.rest.dto.UserPostDTO;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

/**
 * User Service
 * Handles all user-related business logic.
 */
@Service
@Transactional
public class UserService {
  
  private final UserRepository userRepository;

  public UserService(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  /**
   * Retrieves all users but only if token matches the given user ID.
   */
  public List<User> getUsers(Long userId, String token) {
      validateUserSession(userId, token);
      return userRepository.findAll();
  }


  public void createUser(UserPostDTO userPostDTO) {
    User newUser = new User();
    newUser.setUsername(userPostDTO.getUsername());
    newUser.setPassword(userPostDTO.getPassword());
    newUser.setToken(UUID.randomUUID().toString()); // Token is set but not returned
    newUser.setStatus(UserStatus.OFFLINE); // Default to offline

    userRepository.save(newUser);
  }

  /**
  * Login a user (RETURNS TOKEN AND ID)
  */
  public User loginUser(String username, String password) {
    User user = userRepository.findByUsername(username);

    if (user == null || !user.getPassword().equals(password)) {
        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid username or password.");
    }

    // Generate a new session token for the logged-in user
    user.setToken(UUID.randomUUID().toString());
    user.setStatus(UserStatus.ONLINE);
    userRepository.save(user);

    return user; // Return WITH token and ID
  }

  /**
   * Retrieves user by ID but ensures token matches.
   */
  public User getUserById(Long userId, String token) {
      validateUserSession(userId, token);
      return userRepository.findById(userId)
          .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found."));
  }

  /**
   * Logs out user only if token matches.
   */
  public void logoutUser(Long userId, String token) {
      User user = validateUserSession(userId, token);
      user.setStatus(UserStatus.OFFLINE);
      user.setToken(null);
      userRepository.save(user);
  }

  /**
   * Validates if a user's token matches the provided ID.
   */
  private User validateUserSession(Long userId, String token) {
      User user = userRepository.findById(userId)
          .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found."));
      
      if (!user.getToken().equals(token)) {
          throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid session token.");
      }
      return user;
  }
}