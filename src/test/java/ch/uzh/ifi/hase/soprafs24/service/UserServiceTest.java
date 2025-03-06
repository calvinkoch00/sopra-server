package ch.uzh.ifi.hase.soprafs24.service;

import ch.uzh.ifi.hase.soprafs24.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.repository.UserRepository;
import ch.uzh.ifi.hase.soprafs24.rest.dto.UserPostDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.*;

public class UserServiceTest {

  @Mock
  private UserRepository userRepository;

  @InjectMocks
  private UserService userService;

  private UserPostDTO testUserDTO;

  @BeforeEach
  public void setup() {
    MockitoAnnotations.openMocks(this);

    testUserDTO = new UserPostDTO();
    testUserDTO.setUsername("testUsername");
    testUserDTO.setPassword("securePassword");

    User savedUser = new User();
    savedUser.setId(1L);
    savedUser.setUsername(testUserDTO.getUsername());
    savedUser.setPassword(testUserDTO.getPassword());
    savedUser.setToken("randomToken");
    savedUser.setStatus(UserStatus.OFFLINE);

    Mockito.when(userRepository.save(Mockito.any())).thenReturn(savedUser);
  }

  @Test
  public void createUser_validInputs_success() {
    User createdUser = userService.createUser(testUserDTO);

    Mockito.verify(userRepository, Mockito.times(1)).save(Mockito.any());

    assertNotNull(createdUser.getId());
    assertEquals(testUserDTO.getUsername(), createdUser.getUsername());
    assertNotNull(createdUser.getToken());
    assertEquals(UserStatus.OFFLINE, createdUser.getStatus());
  }

  @Test
  public void createUser_duplicateUsername_throwsException() {
    Mockito.when(userRepository.findByUsername(Mockito.any())).thenReturn(new User());

    assertThrows(ResponseStatusException.class, () -> userService.createUser(testUserDTO));
  }
}