# Limbus Engine - A multi classloader hot-deploy container

# Table of Contents
1. [Overview](#overview)
2. [Modules](#modules)

## Overview

The Limbus Engine is a hot-deployment runtime container that enables Java applications to load isolated classpaths at runtime. The Limbus Engine is an implementation of a plugin mechanism but even more: The Limbus Engine supports loading and unloading of a classpaths at runtime, sandboxing of the code in a way that loaded code cannot just do everything like `System.exit();`, flexible routing of plugin output like logging or `System.out`, a test library that can be used to (un)deploy classpaths in a JUnit tests and features like system monitoring, file-system abstraction and more.

The Limbus Engine uses the same hierarchy of classloaders like the most popular servlet container Apache Tomcat but enables the application to sandbox any code from a classpath. This way the plugin code can be restricted and the rest of the application remains isolated. The Java Security API is used to restrict the code running in a sandbox. A security manager keeps track of what a plugin is doing (read [here](http://openbook.rheinwerk-verlag.de/java7/1507_22_003.html) for more information). The access to a plugin or from a plugin to it's host application is restricted by an interface designed by the host application.

The Limbus Engine provides a framework for developing host applications as well as plugin interfaces and implementations. This is useful for all applications that want to implement a plugin feature.

## Modules

The Limbus Engine consists of many different components that are managed as Maven modules. This is an overview of the Limbus modules with a short description of what they are doing:

- `limbus-depchain-host` Dependency Chain for developing a host application
- `limbus-depchain-plugin`  Dependency Chain for developing plugins
- `limbus-engine-api`  Interfaces and structures for developing plugins as well as the Limbus Engine itself
- `limbus-engine-impl`  Core-Module providing the Limbus Engine implementation
- `limbus-showcase-plugin`  Demonstration of a plugin.
- `limbus-showcase-launcher` Module for bootstrapping a Limbus Engine to demonstrate the (un)deploy feature using `limbus-showcase-plugin`.
- `limbus-event-multicaster`  Core component for multicasting of method calls
- `limbus-jsse`  System component for initializing the Java Secure Socket Extension
- `limbus-logging-api`  Interface definitions for system components integrating logging frameworks.
- `limbus-logging-jdk`  System component integrating the Java Utils Logging Framework
- `limbus-logging-log4j`  System component integrating Log4J Logging Framework.
- `limbus-properties`  Core component to access property configurations
- `limbus-utils`  Util module providing features for accessing ZIP archives and reflective operations.
- `limbus-staging`  Module for developing integration tests and bootstrapping of an embedded Limbus Engine instance
- `limbus-system`  Core component managing system componentents as well as an implementation of a Dependency Injection (CDI) mechanism.
- `limbus-task-scheduler`  System component managing periodically called tasks supporting adaptive scheduling intervals.
- `limbus-vfs`   System component providing an API for the file system abstraction. Provides an implementation to access the real filesystem and an implementation providing an in-memory file system for easy support of integrations tests

