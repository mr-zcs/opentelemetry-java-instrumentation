plugins {
  id("otel.javaagent-instrumentation")
}

muzzle {
  pass {
    group.set("javax.servlet")
    module.set("servlet-api")
    versions.set("[2.2, 3.0)")
    assertInverse.set(true)
  }

  fail {
    group.set("javax.servlet")
    module.set("javax.servlet-api")
    versions.set("[3.0,)")
  }
}

dependencies {
  compileOnly("javax.servlet:servlet-api:2.2")
  implementation(project(":instrumentation:servlet:servlet-common:javaagent"))
  implementation(project(":instrumentation:servlet:servlet-javax-common:library"))

  testInstrumentation(project(":instrumentation:servlet:servlet-javax-common:javaagent"))

  testLibrary("org.eclipse.jetty:jetty-server:7.0.0.v20091005")
  testLibrary("org.eclipse.jetty:jetty-servlet:7.0.0.v20091005")

  latestDepTestLibrary("org.eclipse.jetty:jetty-server:7.+")
  latestDepTestLibrary("org.eclipse.jetty:jetty-servlet:7.+")
}
