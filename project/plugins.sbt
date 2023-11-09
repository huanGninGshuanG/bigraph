logLevel := Level.Warn


resolvers += "Typesafe repository" at "https://repo.typesafe.com/typesafe/ivy-releases/"

resolvers += "Maven Central Server" at "https://repo1.maven.org/maven2"

addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.7.2")
//addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "0.14.10")

//这部分内容主要是对sbt进行设置 包括打包 命名等信息