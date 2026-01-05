# Junie codegen guideline: record-style named accessors

This project prefers record-style named accessors over classic JavaBean get/set methods.
Junie should generate and refactor code accordingly.

## Key rules

Getter/Setter::
- Use record-style getters: fieldName() instead of getFieldName().
- Prefer record-style setters/mutators for mutable classes: fieldName(T value) returning this for fluent chaining, instead of setFieldName(T value).
- For immutable designs, prefer either:
    - Java records; or
    - withFieldName(T value) that returns a new instance.
- New code should follow these rules. Existing get*/set* methods may remain for backward compatibility but should not be introduced in new APIs.

Imports::
- Prefer imports, e.g. code should never be
  int x = java.lang.reflect.Array.getLength(value);
but better just 
  int x = Array.getLength(value);
with
  import java.lang.reflect.Array;

Comments::
- don't use end-of-line comments like `// a bad comment`
- DO use JavaDoc.
- Short comments in one compact line: `/** A perfect comment */`


## Examples

// before (OLD JavaBean style)
// String getId(); void setId(String id)

// after (record-style)
String id();                      // getter
Endpoint id(String id);           // fluent setter on mutable types, returns this

// or, for immutable types
Endpoint withId(String id);       // returns a new instance with updated value

## Rationale

- Aligns the object model with Java record conventions, making APIs terse and consistent.
- Enables a fluent DSL-like style when mutators return this.

## Notes for frameworks

- JSON libraries that expect get*/set* can be configured to use field access or record accessors. If needed, annotate fields or accessors with @JsonProperty to keep serialization stable.

## Prompting Junie

- When asking Junie to add fields or classes, explicitly say: "Use record-style named accessors: fieldName() getter and fieldName(value) fluent setter (or withFieldName(value) for immutable). Avoid get*/set*."
