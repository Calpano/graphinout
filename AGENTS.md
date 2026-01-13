# Where to Read
- Read source code only from files in /src/main and /src/test. 
- Don't read from /target/ directories.
- All .adoc files may contain relevant docs.

# Tool Usage
For fetching URLs from Wikipedia, use curl.

# Coding Guidelines
## Accessors (Record-style)
- **Getters**: Use `fieldName()` (not `getFieldName()`).
- **Setters (Mutable)**: Use `fieldName(T value)` returning `this` (fluent).
- **Immutable**: Use Java records or `withFieldName(T value)` returning new instance.
- **No new** `get*`/`set*` methods.

## Imports
Prefer class-level imports over inline fully qualified names.

## Style
- **Imports**: Use imports, avoid fully qualified names.
- **Comments**: Use JavaDoc (`/** ... */`), prefer compact one-liners. No end-of-line `//`.

## Frameworks
- Configure JSON libs for field/record access or use `@JsonProperty`.
