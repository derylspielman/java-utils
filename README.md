# java-utils
Utilities for java applications.
## Dependency Analyzer
Analyzes dependencies of a java project based on import and package statements of .java files.
Can be useful for determining structure and dependencies within your own packages.

### Pre-requisites
Java 17
### Usage
```sh
java ./DependencyAnalyzer.java <path-to-source> <root-package> <dependency-root-package>
```
Example to analyze root package com.derylspielman for any dependencies on package com.derylspielman
```sh
java ./DependencyAnalyzer.java ~/project/path com.derylspielman com.derylspielman
com.derylspielman.api.client
  -> com.derylspielman.common.data
  -> com.derylspielman.common.web  
com.derylspielman.examples.services
  -> com.derylspielman.api.client
```
