{ pkgs, ... }: {
  # Which nixpkgs channel to use.
  channel = "stable-23.11"; # or "unstable"

  # Use https://search.nixos.org/packages to find packages
  packages = [
    pkgs.jdk17
    pkgs.gradle
    pkgs.android-tools
  ];

  # Sets environment variables in the workspace
  env = {
    JAVA_HOME = "${pkgs.jdk17}/lib/openjdk";
  };

  idx = {
    # Search for the extensions you want on https://open-vsx.org/ and use "publisher.id"
    extensions = [
      "mathiasfrohlich.Kotlin"
      "vscjava.vscode-java-pack"
    ];

    # Workspace lifecycle hooks
    workspace = {
      # Runs when a workspace is first created
      onCreate = {
        # Generate the Gradle wrapper if it doesn't exist
        setup = "gradle wrapper";
      };
      # Runs when the workspace is (re)started
      onStart = {
        # Ensure it is executable
        make-executable = "chmod +x ./gradlew";
      };
    };

    # Enable previews
    previews = {
      enable = true;
      previews = {
        android = {
          # The manager 'android' launches the emulator
          manager = "android";
          # Wait until the build is finished before trying to install/launch
          command = ["./gradlew" ":app:assembleDebug"];
        };
      };
    };
  };
}
