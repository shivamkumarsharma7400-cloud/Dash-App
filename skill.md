Skill Link: https://github.com/Logiaweb/limora-unslop 3s
Run gradle testDebugUnitTest assembleDebug --no-daemon

Welcome to Gradle 8.7!

Here are the highlights of this release:
 - Compiling and testing with Java 22
 - Cacheable Groovy script compilation
 - New methods in lazy collection properties

For more details see https://docs.gradle.org/8.7/release-notes.html

To honour the JVM settings for this build a single-use Daemon process will be forked. For more on this, please refer to https://docs.gradle.org/8.7/userguide/gradle_daemon.html#sec:disabling_the_daemon in the Gradle documentation.
Daemon will be stopped at the end of the build 
WARNING: We recommend using a newer Android Gradle plugin to use compileSdk = 35

This Android Gradle plugin (8.5.2) was tested up to compileSdk = 34.

You are strongly encouraged to update your project to use a newer
Android Gradle plugin that has been tested with compileSdk = 35.

If you are already using the latest version of the Android Gradle plugin,
you may need to wait until a newer version with support for compileSdk = 35 is available.

For more information refer to the compatibility table:
https://d.android.com/r/tools/api-level-support

To suppress this warning, add/update
    android.suppressUnsupportedCompileSdk=35
to this project's gradle.properties.
> Task :app:checkKotlinGradlePluginConfigurationErrors SKIPPED
> Task :app:preBuild UP-TO-DATE
> Task :app:preDebugBuild UP-TO-DATE
> Task :app:checkDebugAarMetadata FAILED
gradle/actions: Writing build results to /home/runner/work/_temp/.gradle-actions/build-results/__run-1795625618836.json
1 actionable task: 1 executed

FAILURE: Build failed with an exception.

* What went wrong:
Execution failed for task ':app:checkDebugAarMetadata'.
> Could not resolve all files for configuration ':app:debugRuntimeClasspath'.
   > Could not find androidx.navigation.compose:navigation-compose:2.8.2.
     Searched in the following locations:
       - https://dl.google.com/dl/android/maven2/androidx/navigation/compose/navigation-compose/2.8.2/navigation-compose-2.8.2.pom
       - https://repo.maven.apache.org/maven2/androidx/navigation/compose/navigation-compose/2.8.2/navigation-compose-2.8.2.pom
     Required by:
         project :app

* Try:
> Run with --stacktrace option to get the stack trace.
> Run with --info or --debug option to get more log output.
> Run with --scan to get full insights.
> Get more help at https://help.gradle.org.

BUILD FAILED in 1m 2s
Error: Process completed with exit code 1.