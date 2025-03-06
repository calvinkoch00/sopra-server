package ch.uzh.ifi.hase.soprafs24.service;

import ch.uzh.ifi.hase.soprafs24.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.repository.UserRepository;
import ch.uzh.ifi.hase.soprafs24.rest.dto.UserPostDTO;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Map;


@Service
@Transactional
public class UserService {
  
  private final UserRepository userRepository;

  public UserService(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  public List<User> getUsers(Long userId, String token) {
    validateUserSession(userId, token);
    return userRepository.findAll();
}


public User createUser(UserPostDTO userPostDTO) {
  if (userRepository.findByUsername(userPostDTO.getUsername()) != null) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, 
          "A user with this username already exists. Please choose a different username.");
  }

  User newUser = new User();
  newUser.setUsername(userPostDTO.getUsername());
  newUser.setPassword(userPostDTO.getPassword());
  newUser.setToken(UUID.randomUUID().toString());
  newUser.setStatus(UserStatus.OFFLINE);
  newUser.setCreationDate(LocalDate.now());

  return userRepository.save(newUser);
}


  public User loginUser(String username, String password) {
    User user = userRepository.findByUsername(username);

    if (user == null || !user.getPassword().equals(password)) {
        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid username or password.");
    }

    
    user.setToken(UUID.randomUUID().toString());
    user.setStatus(UserStatus.ONLINE);
    userRepository.save(user);

    return user;
}

  public User getUserById(Long userId, Long requestedUserId, String token) {
    User requestingUser = validateUserSession(userId, token);

    User targetUser = userRepository.findById(requestedUserId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found."));

    if (!userId.equals(requestedUserId)) {
        User publicUser = new User();
        publicUser.setId(targetUser.getId());
        publicUser.setUsername(targetUser.getUsername());
        publicUser.setStatus(targetUser.getStatus());
        publicUser.setBirthdate(targetUser.getBirthdate());
        return publicUser;
    }

    return targetUser;
}


  public void logoutUser(Long userId, String token) {
      User user = validateUserSession(userId, token);
      user.setStatus(UserStatus.OFFLINE);
      user.setToken(null);
      userRepository.save(user);
  }

  
  private static final Logger logger = LoggerFactory.getLogger(UserService.class);
  private User validateUserSession(Long userId, String token) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found."));

    String storedToken = user.getToken();
    String receivedToken = (token != null) ? token.replace("Bearer ", "").trim() : null;

    logger.info("🔍 Stored Token: \"{}\"", storedToken);
    logger.info("🔍 Received Token: \"{}\"", receivedToken);

    if (receivedToken == null || !storedToken.equals(receivedToken)) {
        logger.warn("🚨 Token Mismatch! Stored: \"{}\", Received: \"{}\"", storedToken, receivedToken);
        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid session token.");
    }

    return user;
}

public void updateUserProfile(Long userId, Map<String, Object> updateData, String token) {
  User userToUpdate = validateUserSession(userId, token);

  if (updateData.containsKey("username")) {
      userToUpdate.setUsername(updateData.get("username").toString());
  }

  if (updateData.containsKey("birthdate")) {
      userToUpdate.setBirthdate(LocalDate.parse(updateData.get("birthdate").toString()));
  }

  userRepository.save(userToUpdate);
}
}