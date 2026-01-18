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


## Modifying Connected JSON
How to evolve the CJ spec:
- In `CjConstants`: Check schema version and presence of all used JSON property names.
- Ensure you have updated existing test files .cj.json or created new ones using the new schema features
- Test files are read from classpath, so call mvn clean once after modifying them
- Use SyntheticCjTest to test CJ to JSON and back
- Run all tests in /base
- Run all tests in /cj-reader
- Run remaining test (mvn clean install)
- Use Json2CjAndBackTest
- Update the schema validation code in `CjSchemaValidator`.
- Update the schema validation tests in `CjSchemaValidatorTest`.

