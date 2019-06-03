workflow "Release Main Version" {
  on = "push"
  resolves = ["Build and Release Main Version"]
}

action "Tag Filter Main Version" {
  uses = "actions/bin/filter@3c0b4f0e63ea54ea5df2914b4fabf383368cd0da"
  args = "tag v*"
}

action "Actor Filter Main Version" {
  uses = "actions/bin/filter@3c0b4f0e63ea54ea5df2914b4fabf383368cd0da"
  needs = ["Tag Filter Main Version"]
  args = ["actor", "kingthorin", "psiinon", "thc202"]
}

action "Build and Release Main Version" {
  uses = "docker://openjdk:8"
  needs = ["Actor Filter Main Version"]
  runs = "./gradlew"
  args = ["-Dorg.gradle.jvmargs=-Xmx2g -XX:MaxMetaspaceSize=512m", ":zap:createMainReleaseFromGitHubRef"]
  secrets = ["GITHUB_TOKEN", "INSTALL4J_LICENSE"]
}

workflow "Release Weekly" {
  on = "push"
  resolves = ["Build and Release Weekly"]
}

action "Tag Filter Weekly" {
  uses = "actions/bin/filter@3c0b4f0e63ea54ea5df2914b4fabf383368cd0da"
  args = "tag w*"
}

action "Actor Filter Weekly" {
  uses = "actions/bin/filter@3c0b4f0e63ea54ea5df2914b4fabf383368cd0da"
  needs = ["Tag Filter Weekly"]
  args = ["actor", "kingthorin", "psiinon", "thc202"]
}

action "Build and Release Weekly" {
  uses = "docker://openjdk:8"
  needs = ["Actor Filter Weekly"]
  runs = "./gradlew"
  args = ["-Dorg.gradle.jvmargs=-Xmx2g -XX:MaxMetaspaceSize=512m", ":zap:createWeeklyReleaseFromGitHubRef"]
  secrets = ["GITHUB_TOKEN"]
}