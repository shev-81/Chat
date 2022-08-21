package com.client.pak.render;

import lombok.Data;

import java.io.Serializable;

/**
 * Класс описывающий содержимое статуса пользователя.
 */
@Data
public class UserCell implements Serializable {

  /**
   * Имя пользователя.
   */
  private String name;

  /**
   * Статус пользователя.
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
