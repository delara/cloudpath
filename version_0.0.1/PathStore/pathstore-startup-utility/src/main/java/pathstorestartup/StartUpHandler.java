package pathstorestartup;

import com.jcraft.jsch.JSchException;
import pathstore.common.Role;
import pathstore.system.deployment.commands.*;
import pathstore.system.deployment.utilities.SSHUtil;
import pathstorestartup.commands.CreateWebsitePropertiesFile;
import pathstorestartup.commands.FinalizeRootInstallation;

import java.util.*;

/**
 * This class is used to handle the startup sequence and allows a user to deploy the root node for
 * their new pathstore network
 */
public class StartUpHandler {

  /** Denotes the dir where all installation related files are stored */
  private static final String STARTING_DIR =
      System.getProperty("user.dir") + "/src/main/resources/";

  /** Where the pathstore property file will be stored locally. */
  private static final String PATHSTORE_PROPERTIES_LOCATION =
      STARTING_DIR + "pathstore/pathstore.properties";

  /** Where the website property file will be stored locally. */
  private static final String WEBSITE_PROPERTIES_LOCATION =
      STARTING_DIR + "pathstore-admin-panel/pathstore.properties";

  /** Port reference for cassandra */
  private static final int cassandraPort = 9052;

  /** Scanner to get user input */
  private final Scanner scanner;

  /** Setup scanner */
  public StartUpHandler() {
    this.scanner = new Scanner(System.in);
  }

  /**
   * Ask the user if they want to start a new network. If they do run them through the disclaimer
   * else start the spring application
   */
  public void init() {
    String response =
        this.askQuestionWithSpecificResponses(
            "Do you want to create a new PathStore network?: ",
            new String[] {"y", "yes", "n", "no"});

    switch (response) {
      case "y":
      case "yes":
        this.disclaimerPrompt();
        break;
      case "n":
      case "no":
        break;
    }
    this.finished();
  }

  /**
   * This function is used to display a disclaimer prompt to the user. If they don't accept the
   * disclaimer then the program is exited, else they're transitioned to the next stage of
   * deployment
   */
  public void disclaimerPrompt() {
    System.out.println(
        "\n=========================================== [DISCLAIMER] ===========================================\n"
            + "1) All servers that you want to add to the network must have a linux distribution installed\n"
            + "2) Your linux machine must be publicly accessible (You can ssh into the machine remotely on port 22)\n"
            + "3) You must have port 9052 and 1099 open as-well for pathstore communication\n"
            + "4) The user account given should be a separate user account from any other on the system (not root)\n"
            + "5) You must have docker installed and the docker daemon running\n"
            + "6) The user account given must be part of the docker group and shouldn't be a member of sudo\n"
            + "7) The system must have at least 4gb of ram (2gb for cassandra and 2gb for pathstore)\n"
            + "\nTo view our setup guide for how to configure a linux system to run pathstore see our github page\n");
    String response =
        this.askQuestionWithSpecificResponses(
            "Do you want to continue after reading the disclaimer?: ",
            new String[] {"y", "yes", "n", "no"});
    switch (response) {
      case "y":
      case "yes":
        this.createNewNetwork();
        break;
      case "n":
      case "no":
        break;
    }
  }

  /**
   * First get information about the network and then connect to the server. Then run through the
   * list of commands
   *
   * @see #initList(SSHUtil, String, String, int, int, Role, String, int, String, int, String, int,
   *     String, int)
   */
  public void createNewNetwork() {

    System.out.println(
        "\nConnection information\nNote: If the host you're connecting to already has a pathstore instance running it will be deleted.\n");

    String ip = this.askQuestionWithSpecificResponses("Host: ", null);
    String username = this.askQuestionWithInvalidResponse("Username: ", new String[] {"root"});
    String password = this.askQuestionWithInvalidResponse("Password: ", null);
    int sshPort = this.askQuestionWithInvalidResponseInteger("SSH port: ", null);
    int rmiPort =
        this.askQuestionWithInvalidResponseInteger("RMI port (if unsure enter 1099): ", null);
    String branch = this.askQuestionWithInvalidResponse("Branch: ", null);

    try {
      SSHUtil sshUtil = new SSHUtil(ip, username, password, sshPort);
      System.out.println("Connected");

      try {
        // Initalize commands
        List<ICommand> commands =
            this.initList(
                sshUtil,
                ip,
                branch,
                1,
                -1,
                Role.ROOTSERVER,
                "127.0.0.1",
                rmiPort,
                "",
                rmiPort,
                "127.0.0.1",
                cassandraPort,
                "",
                cassandraPort);
        // Add the finalize command
        commands.add(
            new FinalizeRootInstallation(ip, cassandraPort, username, password, sshPort, rmiPort));

        // Execute all commands in the given list
        for (ICommand command : commands) {
          System.out.println(command);
          command.execute();
        }
      } catch (CommandError error) {
        System.err.println(String.format("[ERROR] %s", error.errorMessage));
        System.exit(-1);
      } finally {
        sshUtil.disconnect();
      }

    } catch (JSchException e) {
      System.out.println("\nYour connection information seems to be incorrect");
      this.createNewNetwork();
    }
  }

  /**
   * @param sshUtil used for commands that need to use ssh
   * @param ip ip of new node
   * @param branch branch from github to build from
   * @param nodeID new node's id
   * @param parentNodeId new node's parent id
   * @param role role of new node
   * @param rmiRegistryIP new node's local rmi registry ip
   * @param rmiRegistryPort new node's local rmi registry port
   * @param rmiRegistryParentIP new node's parent rmi registry ip
   * @param rmiRegistryParentPort new node's parent rmi registry port
   * @param cassandraIP new node's local cassandra instance ip
   * @param cassandraPort new node's local cassandra instance port
   * @param cassandraParentIP new node's parent cassandra instance ip
   * @param cassandraParentPort new nodes' parent cassandra instance port
   * @return list of deployment commands to execute
   */
  public List<ICommand> initList(
      final SSHUtil sshUtil,
      final String ip,
      final String branch,
      final int nodeID,
      final int parentNodeId,
      final Role role,
      final String rmiRegistryIP,
      final int rmiRegistryPort,
      final String rmiRegistryParentIP,
      final int rmiRegistryParentPort,
      final String cassandraIP,
      final int cassandraPort,
      final String cassandraParentIP,
      final int cassandraParentPort) {

    List<ICommand> commands = new ArrayList<>();

    // Check for docker access and that docker is online
    commands.add(new Exec(sshUtil, "docker ps", 0));
    // Potentially kill old cassandra container
    commands.add(new Exec(sshUtil, "docker kill cassandra", -1));
    // Potentially remove old cassandra image TODO (1)
    //commands.add(new Exec(sshUtil, "docker image rm cassandra", -1));
    // Potentially kill old pathstore container
    commands.add(new Exec(sshUtil, "docker kill pathstore", -1));
    // Potentially remove old pathstore image TODO (2)
    //commands.add(new Exec(sshUtil, "docker image rm pathstore", -1));
    // Potentially kill old pathstore container
    commands.add(new Exec(sshUtil, "docker kill pathstore-admin-panel", -1));
    // Potentially remove old pathstore image TODO (3)
    //commands.add(new Exec(sshUtil, "docker image rm pathstore-admin-panel", -1));
    // Potentially remove old file associated with install
    commands.add(new Exec(sshUtil, "rm -rf pathstore-install", -1));
    // Potentially remove old pull image
    commands.add(new Exec(sshUtil, "docker image rm pull", -1));
    // Create pathstore install dir
    commands.add(new Exec(sshUtil, "mkdir -p pathstore-install", 0));
    // Create base dir
    commands.add(new Exec(sshUtil, "mkdir -p pathstore-install/base", 0));
    // Create pull dir
    commands.add(new Exec(sshUtil, "mkdir -p pathstore-install/pull", 0));
    // Create cassandra dir
    commands.add(new Exec(sshUtil, "mkdir -p pathstore-install/cassandra", 0));
    // Create pathstore dir
    commands.add(new Exec(sshUtil, "mkdir -p pathstore-install/pathstore", 0));
    // Create website dir
    commands.add(new Exec(sshUtil, "mkdir -p pathstore-install/pathstore-admin-panel", 0));
    // Generate pathstore properties file
    commands.add(
        new GeneratePropertiesFile(
            nodeID,
            ip,
            parentNodeId,
            role,
            rmiRegistryIP,
            rmiRegistryPort,
            rmiRegistryParentIP,
            rmiRegistryParentPort,
            cassandraIP,
            cassandraPort,
            cassandraParentIP,
            cassandraParentPort,
            PATHSTORE_PROPERTIES_LOCATION));
    // Transfer properties file
    commands.add(
        new FileTransfer(
            sshUtil,
            PATHSTORE_PROPERTIES_LOCATION,
            "pathstore-install/pathstore/pathstore.properties"));
    // Remove properties file
    commands.add(new RemoveGeneratedPropertiesFile(PATHSTORE_PROPERTIES_LOCATION));
    // Generate website properties file
    commands.add(
        new CreateWebsitePropertiesFile(
            ip, cassandraPort, rmiRegistryPort, WEBSITE_PROPERTIES_LOCATION));
    // Transfer website properties file
    commands.add(
        new FileTransfer(
            sshUtil,
            WEBSITE_PROPERTIES_LOCATION,
            "pathstore-install/pathstore-admin-panel/pathstore.properties"));
    // Remove website properties file
    commands.add(new RemoveGeneratedPropertiesFile(WEBSITE_PROPERTIES_LOCATION));
    // Transfer deploy key
    commands.add(
        new FileTransfer(sshUtil, STARTING_DIR + "deploy_key", "pathstore-install/deploy_key"));
    // Transfer base docker file
    commands.add(
        new FileTransfer(
            sshUtil, STARTING_DIR + "base/Dockerfile", "pathstore-install/base/Dockerfile"));
    // Transfer pull docker file
    commands.add(
        new FileTransfer(
            sshUtil, STARTING_DIR + "pull/Dockerfile", "pathstore-install/pull/Dockerfile"));
    // Transfer cassandra docker file
    commands.add(
        new FileTransfer(
            sshUtil,
            STARTING_DIR + "cassandra/Dockerfile",
            "pathstore-install/cassandra/Dockerfile"));
    // Transfer pathstore docker file
    commands.add(
        new FileTransfer(
            sshUtil,
            STARTING_DIR + "pathstore/Dockerfile",
            "pathstore-install/pathstore/Dockerfile"));
    // Transfer website docker file
    commands.add(
        new FileTransfer(
            sshUtil,
            STARTING_DIR + "pathstore-admin-panel/Dockerfile",
            "pathstore-install/pathstore-admin-panel/Dockerfile"));
    // Build base
    commands.add(
        new Exec(
            sshUtil,
            String.format(
                "docker build -t base --build-arg key=\"$(cat pathstore-install/deploy_key)\" --build-arg branch=\"%s\" pathstore-install/base",
                branch),
            0));
    // Build pull
    commands.add(new Exec(sshUtil, "docker build -t pull pathstore-install/pull", 0));
    // Build cassandra
    commands.add(new Exec(sshUtil, "docker build -t cassandra pathstore-install/cassandra", 0));
    // Save cassandra to tar file and store in pathstore directory
    commands.add(
        new Exec(sshUtil, "docker save -o pathstore-install/pathstore/cassandra.tar cassandra", 0));
    // Start cassandra
    commands.add(
        new Exec(sshUtil, "docker run --network=host -dit --rm --name cassandra cassandra", 0));
    // Wait for cassandra to start
    commands.add(new WaitForCassandra(ip, cassandraPort));
    // Build pathstore
    commands.add(new Exec(sshUtil, "docker build -t pathstore pathstore-install/pathstore", 0));
    // Save pathstore to tar file and store in pathstore directory
    commands.add(
        new Exec(sshUtil, "docker save -o pathstore-install/pathstore/pathstore.tar pathstore", 0));
    // Start pathstore
    commands.add(
        new Exec(
            sshUtil,
            "docker run --network=host -dit --rm -v ~/pathstore-install/pathstore:/etc/pathstore --name pathstore pathstore",
            0));
    // Wait for pathstore to come online
    commands.add(new WaitForPathStore(ip, cassandraPort));
    // Build wesbite
    commands.add(
        new Exec(
            sshUtil,
            "docker build -t pathstore-admin-panel ~/pathstore-install/pathstore-admin-panel",
            0));
    // Start website
    commands.add(
        new Exec(
            sshUtil,
            "docker run --network=host -dit --rm -v ~/pathstore-install/pathstore-admin-panel:/etc/pathstore --name pathstore-admin-panel pathstore-admin-panel",
            0));

    return commands;
  }

  /**
   * This function takes in a question you want to prompt the user with and a list of
   * validResponses.
   *
   * <p>The user gets prompted with the question, then we take their response and lower case it. If
   * their response is inside the validResponses set then we return their response. If it's not we
   * notify the user that their response must be within the validResponses set and we then make a
   * recursive call to re-prompt the user
   *
   * @param question question to ask user
   * @param validResponses list of accepted answers null then we will always return their response
   * @return response from user
   */
  private String askQuestionWithSpecificResponses(
      final String question, final String[] validResponses) {
    System.out.print(question);

    String response = this.scanner.nextLine().toLowerCase();

    HashSet<String> validResponseSet =
        validResponses != null ? new HashSet<>(Arrays.asList(validResponses)) : new HashSet<>();

    if (validResponseSet.contains(response) || validResponseSet.size() == 0) return response;
    else {
      System.out.println(
          "You're response must be one of the following values: "
              + Arrays.toString(validResponses));
      return this.askQuestionWithSpecificResponses(question, validResponses);
    }
  }

  /**
   * This function is used to ask a question and accept any response accept those in the
   * invalidResponses array
   *
   * @param question question to ask
   * @param invalidResponses responses that aren't accepted
   * @return answer to question
   */
  private String askQuestionWithInvalidResponse(
      final String question, final String[] invalidResponses) {
    System.out.print(question);

    String response = this.scanner.nextLine();
    HashSet<String> inValidResponseSet =
        invalidResponses != null ? new HashSet<>(Arrays.asList(invalidResponses)) : new HashSet<>();
    if (inValidResponseSet.contains(response)) {
      System.out.print(
          "You cannot respond with the following values: " + Arrays.toString(invalidResponses));
      return this.askQuestionWithInvalidResponse(question, invalidResponses);
    } else return response;
  }

  /**
   * Ask a question to the user but get an integer response
   *
   * @param question question to ask
   * @param invalidResponses list of invalid responses
   * @return integer from user
   */
  private int askQuestionWithInvalidResponseInteger(
      final String question, final String[] invalidResponses) {
    int response;

    try {
      response = Integer.parseInt(this.askQuestionWithInvalidResponse(question, invalidResponses));
    } catch (NumberFormatException e) {
      System.out.println(
          "The data you entered is not a number. Please make sure you enter a number");
      return this.askQuestionWithInvalidResponseInteger(question, invalidResponses);
    }

    return response;
  }

  /** close scanner */
  private void finished() {
    this.scanner.close();
  }
}
