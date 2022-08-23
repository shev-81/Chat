package com.client.pak.render;

import lombok.Data;

import java.io.Serializable;

/**
 * A class describing the content of the user's status.
 */
@Data
public class UserCell implements Serializable {

  /**
   * Username.
   */
  private String name;

  /**
   * User status.
   */
  private String status;

  public UserCell(String name, String status) {
    this.name = name;
    this.status = status;
  }

  @Override
  public String toString() {
    return "UserCell{" +
        "name='" + name + '\'' +
        ", status='" + status + '\'' +
        '}';
  }
}
