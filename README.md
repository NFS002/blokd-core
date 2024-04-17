## Blokd
### (Core framework)

### Building the project

#### System requirements
- Java
- Kotlin
- Docker
- Gradle

#### Setting up a development environment

1. Create the configuration directory and configure blokd to look for it:
The main configuration directory needs to have the following file/directory structure
- config/
  - keys/
    - public
    - private
  - blokd.properties (json format also works)
  - client.properties (json format also works)
  - log4j.properties

The default location that blokd will look for this configuration directory is in <PROJECT_ROOT>/config
although you can configure another directory by setting the `BLOKD_CONFIG_DIR` environment variable.

So for example, to copy one of the sample configurations to the default location:
```shell
mkdir config
cp -r sample-configs/1sig config
```


#### Common build problems

1. Cannot create task ':testJar'

```
Tasks for root project 'blokd-core' cannot be collected due to plugin exception.

org.gradle.api.internal.tasks.DefaultTaskContainer$TaskCreationException: Could not create task ':testJar'
```

This is todo with the `org.unbroken-dome.test-sets` gradle plugin, which is only used for the `integrationTest` task.
Try commenting out the `testSets`, configuration in the main gradle build file, as well as the `integrationTestImplementation` dependencies, and try again.