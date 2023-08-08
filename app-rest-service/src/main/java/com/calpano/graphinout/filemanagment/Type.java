package com.calpano.graphinout.filemanagment;

public enum Type {
  INPUT,
  RESULT;

  public static Type getType(String type) {
    return type != null && type.toUpperCase().equals(RESULT.name()) ? RESULT : INPUT;
  }
}
