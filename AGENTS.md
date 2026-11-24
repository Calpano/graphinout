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



## Changing foundation-* APIs
Other projects use the foundation modules, and the build here doesn't check them. They are listed in `downstream.ddot`
(repo root, git-ignored, regenerate with `python3 ../_play/downstream.py`).
Before removing, renaming, moving or changing the behavior of public foundation API, follow
`foundation-pure/README.adoc#downstream`: look up the package in `downstream.ddot`, grep the projects that use it, and
keep `@Deprecated` delegates.
