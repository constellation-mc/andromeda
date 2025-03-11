package me.melontini.andromeda.api;

public record ApiDeclaration<I, O>(Status status) {

  public enum Status {
    STABLE,
    DEPRECATED,
    DEAD
  }
}
