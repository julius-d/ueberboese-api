# Contributing

Contributions are more than welcome :)

## Code contributions

If you want to contribute some code to the project, here are some basic explanations.

The project is build using Java and Spring.

To set up Java this projects uses [mise](https://mise.jdx.dev/).
After installing mise run `mise i` in the root dir of this project.

The API the project provides to replace the Bose server is defined in [ueberboese-api.yaml](ueberboese-api.yaml)
When you have changed that file run `./mvnw clean compile`

When you are done with programming,
execute `./mvnw spotless:apply -o` to reformat the code
then execute `./mvnw verify` to execute all test and checks.

Then create a PR.
