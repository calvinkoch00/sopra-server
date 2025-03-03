package ch.uzh.ifi.hase.soprafs24.rest.dto;

import com.fasterxml.jackson.annotation.ObjectIdGenerators.None;

public class UserPostDTO {

  private String password;

  private String username;

  public String getPassword() {
    return password;
  }

  public void setName(String password) {
    this.password = password;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }
}
