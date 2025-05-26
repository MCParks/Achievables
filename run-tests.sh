#!/bin/bash

# Compile the test files directly
echo "Compiling tests..."
javac -d /tmp/reference-tests -cp \
  "/home/runner/work/Achievables/Achievables/build/classes/groovy/main:/home/runner/.gradle/caches/modules-2/files-2.1/org.junit.jupiter/junit-jupiter-api/5.8.2/4c21029217adf07e4c0d0c5e192b6bf610c94bdc/junit-jupiter-api-5.8.2.jar:/home/runner/.gradle/caches/modules-2/files-2.1/org.junit.platform/junit-platform-commons/1.8.2/32c8b8617c1342376fd5af2053da6410d8866861/junit-platform-commons-1.8.2.jar" \
  /home/runner/work/Achievables/Achievables/src/test/java/us/mcparks/achievables/reference/ReferenceCoreTests.java

# Run the tests
echo "Running tests..."
java -cp "/tmp/reference-tests:/home/runner/work/Achievables/Achievables/build/classes/groovy/main:/home/runner/.gradle/caches/modules-2/files-2.1/org.junit.jupiter/junit-jupiter-api/5.8.2/4c21029217adf07e4c0d0c5e192b6bf610c94bdc/junit-jupiter-api-5.8.2.jar:/home/runner/.gradle/caches/modules-2/files-2.1/org.junit.platform/junit-platform-commons/1.8.2/32c8b8617c1342376fd5af2053da6410d8866861/junit-platform-commons-1.8.2.jar:/home/runner/.gradle/caches/modules-2/files-2.1/org.junit.jupiter/junit-jupiter-engine/5.8.2/c598b4328d2f397194d11df3b1648d68d7d990e3/junit-jupiter-engine-5.8.2.jar:/home/runner/.gradle/caches/modules-2/files-2.1/org.junit.platform/junit-platform-engine/1.8.2/b737de09f19864bd136805c84df7999a142fec29/junit-platform-engine-1.8.2.jar:/home/runner/.gradle/caches/modules-2/files-2.1/org.opentest4j/opentest4j/1.2.0/28c11eb91f9b6d8e200631d46e20a7f407f2a046/opentest4j-1.2.0.jar" \
  org.junit.platform.console.ConsoleLauncher --class-path /tmp/reference-tests --scan-class-path