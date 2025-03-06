package ch.uzh.ifi.hase.soprafs24.service;

import ch.uzh.ifi.hase.soprafs24.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.repository.UserRepository;
import ch.uzh.ifi.hase.soprafs24.rest.dto.UserPostDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.*;

@WebAppConfiguration
@SpringBootTest
public class UserServiceIntegrationTest {

  @Qualifier("userRepository")
  @Autowired
  private UserRepository userRepository;

  @Autowired
  private UserService userService;

  @BeforeEach
  public void setup() {
    userRepository.deleteAll();
  }

  @Test
  public void createUser_validInputs_success() {
    assertNull(userRepository.findByUsername("testUsername"));

    UserPostDTO testUserDTO = new UserPostDTO();
    testUserDTO.setUsername("testUsername");
    testUserDTO.setPassword("securePassword");

    User createdUser = userService.createUser(testUserDTO);

    assertNotNull(createdUser.getId());
    assertEquals(testUserDTO.getUsername(), createdUser.getUsername());
    assertNotNull(createdUser.getToken());
    assertEquals(UserStatus.OFFLINE, createdUser.getStatus());
  }

  @Test
  public void createUser_duplicateUsername_throwsException() {
    assertNull(userRepository.findByUsername("testUsername"));

    UserPostDTO testUserDTO = new UserPostDTO();
    testUserDTO.setUsername("testUsername");
    testUserDTO.setPassword("securePassword");
    userService.createUser(testUserDTO);

    UserPostDTO testUser2DTO = new UserPostDTO();
    testUser2DTO.setUsername("testUsername");
    testUser2DTO.setPassword("securePassword");

    assertThrows(ResponseStatusException.class, () -> userService.createUser(testUser2DTO));
  }
}