# Contributing

Thanks for your interest in contributing! We welcome bug reports, feature requests, and PRs.

## How to contribute

1. Fork the repository.

2. Create a branch for your change:

   ```bash
   git checkout -b feat/my-feature
   ```

3. Make changes, add tests if applicable, and run the build:

   ```bash
   mvn clean test
   ```

4. Commit with a clear message:

   ```
   type(scope): short description

   - longer explanation if needed
   ```

   Example: `fix(player): handle null metadata on join`

5. Push your branch and open a Pull Request.

## Pull request process

* Base your PR on the `main` (or `develop`) branch.
* Include a clear description of the change and link the issue if one exists.
* Keep PRs small & focused when possible.
* A maintainer will review and may request changes. Once approved, it will be merged.

## Coding style

* Use the same style as the project (follow existing code formatting).
* Java code: use `camelCase` for variables and `PascalCase` for classes.
* Keep methods short and single-responsibility.

## Tests

* Add unit tests for new functionality where possible.
* Run `mvn test` before opening a PR.

## Issues

* Search existing issues before opening a new one.
* For bug reports, include reproduction steps and relevant logs.

## License

By contributing, you agree that your contributions will be licensed under the project’s Apache-2.0 license.
