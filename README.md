# CNT5106C - Computer Networks 
# Peer-to-Peer (P2P) File Sharing Project

## Overview

This is a P2P file sharing project that enables peers to exchange files within a network. Peers can share, request, and download file pieces from each other, creating a distributed and decentralized file-sharing system.

Group Name: Rashi Pandey's Group

Group Members: Rashi Pandey, Aadithya Kandeth, Yash Goel, Srishti Jaiswal


## Project Structure

The project comprises several Java files, each with a specific role in the system:

-  `CommonCfg.java`: Defines common configuration parameters for the network.

-  `FilePieces.java`: Manages file splitting, joining, and deletion.

-  `Helper.java`: Provides utility functions, such as creating handshake messages and converting between data types.

-  `MessageSender.java`: Sends messages to other peers within the network.

-  `EndPoint.java` : Represents the endpoint where communication with a peer occurs. Handles incoming and outgoing messages between peers.

-  `RandomUnchokeNeighborSchedule.java`: Selects optimistically unchoked neighbors at regular intervals.

-  `Peer.java`: Represents an individual peer, including their bitfield, communication, and neighbors.

-  `PeerInfoCfg.java`: Parses and holds information about peers within the network.

-  `PeerProcess.java`: The main class that initiates the project, creates peers, and schedules tasks.

-  `PieceIndex.java`: Manages piece indexing with time delays for requested pieces.

-  `TopDownloadSpeedNeighborsScheduler.java`: Selects and manages preferred neighbors.

-  `ReadFile.java`: Reads data from configuration files.

-  `RequestedPiecesScheduler.java`: Schedules the tracking of requested pieces and corresponding actions.

-  `StartRemotePeers.java`: Responsible for starting remote peer processes. It reads the PeerInfo.cfg file using the PeerInfo class and then initiates SSH connections to remote hosts to start the peer processes.

- `LogHelper.java`: Helper class for implementing logging functionality throughout the project. Logs messages to a file based on the PeerID. Uses inbuilt Java.Utils.Logger.
  

## Project Flow And Current Implementation  

The project follows a defined flow:

1. Initialization: Reads configuration files and initializes peer directories.

2. Peer Initialization: Prepares peers, copies the shared file, and splits it into pieces.

3. Concurrency Setup: Sets up threads and thread pools for concurrent operations.

4. File Splitting: Splits the shared file into smaller pieces for sharing.

5. Server and Client Threads: Establishes connections and communication.

6. Message Exchange: Peers exchange control and data messages.

7. Preferred Neighbors Selection: Periodically selects preferred neighbors for efficient data transfer.

8. Optimistic Unchoking: Randomly unchokes one choked neighbor to explore new downloads.

9. Requested Pieces Scheduler: Tracks requested pieces and sends `INTERESTED` messages.

10. Data Transfer: Peers exchange data pieces to assemble files.

11. Completion Check: Verifies if all pieces have been received.

12. Log Messages: Logs key events for monitoring and analysis.

13. Cleanup: Gracefully shuts down threads and connections.

14. Scheduled Tasks: Manages periodic tasks.

15. Overall Control: Coordinates the flow of data and interactions.



## Project Highlights

- **Peer Process Management:**
  - Successfully initialized and coordinated all peer processes, ensuring correct startup.
  - Established reliable connections between peers using TCP handshake, enabling communication within the network.

- **Messaging and Protocol:**
  - Facilitated seamless exchange of crucial messages:
    - Achieved successful transmission of bitfield messages for tracking available pieces.
    - Implemented INTERESTED and NOT INTERESTED messages for efficient resource allocation.
    - Timely sending of UNCHOKE and CHOKE messages at the specified intervals, optimizing data flow among peers.
    - Implemented optimistic neighbor selection, enhancing network performance at regular intervals.

- **Data Transfer and Integrity:**
  - Enabled the exchange of REQUEST and HAVE messages, ensuring accurate piece distribution among peers.
  - Successful transfer of PIECE messages, allowing peers to gather all necessary fragments.
  - Merged all received pieces to reconstruct the complete file accurately.

- **Automated Peer Deployment:**
   - Developed tools/scripts for seamless peer setup and management, reducing manual configuration efforts.

- **Migration to CISE Machines:**
   - Deployed the P2P system on CISE Machines for wider deployment and scalability.

- **Logging and Monitoring:**
   - Implemented a robust logging system to capture runtime details, errors, and debugging information, facilitating troubleshooting and network monitoring.

- **Advanced File Management:**
   - Enabled file handling capabilities for efficient transfer, organization, and synchronization among peers, optimizing resource utilization.

- **Termination and Completion:**
  - Orchestrated termination of processes upon successful file completion, ensuring all peers received the entire file.


## How to Run

### Project Setup 
1. Unzip the Project
2. ssh to cise machines and login with your username
3. copy the project folder to the cise machine

To run the application, follow these steps:

### Compilation:
1. Open the terminal on your local host.

2. Find the current working directory using the `pwd` command.
3. Run the following command in either a bash terminal(windows)/ zsh terminal(Mac):

    `javac *.java`

4. The above command will compile all files
![Compilation](https://github.com/Aadithya97/CN_Project/blob/main/compile.png?raw=true)


### Manual Peer Start (Mac):

1. Open the terminal on your local host.

2. Find the current working directory using the `pwd` command.

3. To manually start each peer, run the shell file with the following command, replacing `<working_dir>` with your working directory and `<peer_id>` with your peer's ID:

	`./run.sh <working_dir> <peer_id>`

![Mac Manual](https://github.com/Aadithya97/CN_Project/blob/main/WhatsApp%20Image%202023-10-24%20at%2011.27.56%20PM.jpeg?raw=true)

  
### Manual Peer Start (Windows):
  
1. Open the terminal on your local host.

2. Run the following command to start a peer, replacing <peer_id> with your peer's ID:

    `java PeerProcess <peer_id>`

![Win Manual](https://github.com/Aadithya97/CN_Project/blob/main/Peer%20Manual%20Start.png?raw=true)

### For automated deployment:

`javac StartRemotePeers.java` 

`java -Dusername=<username> -DworkingDir=<directory> StartRemotePeers`

### File sharing:
Create multiple peers using the above mentioned method to initiate file sharing
  
![File Sharing](https://github.com/Aadithya97/CN_Project/blob/main/File%20Sharing.png?raw=true)

### How to run the make file: (Will run only on MAC or Linux based system)

1. The make command, without any arguments, will execute the default target (all in this case). It will compile the Java files specified in the JAVA_FILES variable using the javac compiler. To compile, navigate to the directory containing this Makefile in your terminal and type (Also, make sure to enter the directory manually): `make run`

2. The ``make run` command opens multiple different terminals to execute the java PeerProcess <PeerID> command. 

3. Note: The directory will have to be specified in each terminal to run the command succesfully.


## Results after Implementation on CISE Machine

`Showing that Peer 1001 has the file:`

![Peer 1001 contains file](https://github.com/srishtijaiswal0911/cn-images/blob/main/cise_peer1001_hasfile.jpeg?raw=true)


`Showing that Peer 1005 does not have file:`

![Peer 1005 does not contain file](https://github.com/srishtijaiswal0911/cn-images/blob/main/cise_peer1005_nofile.jpeg?raw=true)

`Showing that Peer 1001 received interested msg from Peer 1005 and sent complete file:`

![Interested Message](https://github.com/srishtijaiswal0911/cn-images/blob/main/cise_peer1005_interested.jpeg?raw=true)

`Showing that Peer 1001 has the Randomly Unchoked Neighbour Peer 1005:`

![Randomply Unchoked Message](https://github.com/srishtijaiswal0911/cn-images/blob/main/cise_peer1005_randomlyunchoked.jpeg?raw=true)

`Showing that Peer 1005 is Unchoked by Peer 1001:`

![Unchoked Message](https://github.com/srishtijaiswal0911/cn-images/blob/main/cise_peer1005_unchoked.jpeg?raw=true)

`Showing Peer 1005 recieved the complete file:`

![Complete File Recieved](https://github.com/srishtijaiswal0911/cn-images/blob/main/cise_peer1005_compfile.jpeg?raw=true)

`Showing Log messages:`

![Complete File Recieved](https://github.com/srishtijaiswal0911/cn-images/blob/main/log_messages.jpeg?raw=true)

## Contributors and their Contributions:

- Yash Goel - 51939756
-- Focused on structuring the project and played a key role in implementing the switch case functions for various message types. Led the efforts in organizing the codebase, establishing clear structures, and implementing functions for handling INTERESTED, NOT INTERESTED, HAVE, REQUEST, PIECE, and related helper functions. Their contributions ensured a well-organized and functional system capable of handling different message types effectively.

- Aadithya Kandeth  - 69802791
-- Contributed significantly to the core functionality of the project. Took the lead in implementing crucial components related to TCP connections, handshake establishment, bitfield messages, and the handling of piece transfer. Their code formed the backbone of the communication protocol, ensuring reliable data exchange and accurate file reconstruction. Additionally, they were responsible for building the logging component and logging functionality for visualizing the file transfer messages.

- Srishti Jaiswal - 80385159
-- Collaborated in the project's design phase and was instrumental in implementing crucial scheduling functionalities. Developed schedulers responsible for selecting k preferred neighbors based on download rates at regular intervals (p seconds). They also implemented the logic for choosing optimistically unchoked neighbors, managing the exchange of choke and unchoke messages, and handling piece requests after timeouts.

- Rashi Pandey - 88124446
-- Played a significant role in project design and collaborated on various aspects of the implementation. This individual contributed to the project design, providing valuable insights. They actively participated in refining ideas, offering suggestions, and providing support across different implementation phases. Their collaboration ensured cohesive teamwork and contributed to the overall success of the project by providing valuable input and assistance as needed.

## Project Demo Link

- [Project Demo Youtube Link](https://www.youtube.com/watch?v=p0au472Sz4k)
- [Project Demo OneDrive Link](https://uflorida-my.sharepoint.com/personal/aadithya_kandeth_ufl_edu/_layouts/15/stream.aspx?id=%2Fpersonal%2Faadithya%5Fkandeth%5Fufl%5Fedu%2FDocuments%2FRashiPandey%5FCNProject%2Emp4&nav=eyJyZWZlcnJhbEluZm8iOnsicmVmZXJyYWxBcHAiOiJPbmVEcml2ZUZvckJ1c2luZXNzIiwicmVmZXJyYWxBcHBQbGF0Zm9ybSI6IldlYiIsInJlZmVycmFsTW9kZSI6InZpZXciLCJyZWZlcnJhbFZpZXciOiJNeUZpbGVzTGlua0RpcmVjdCJ9fQ&ga=1&referrer=StreamWebApp%2EWeb&referrerScenario=AddressBarCopied%2Eview)
