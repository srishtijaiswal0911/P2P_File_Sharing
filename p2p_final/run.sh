#!/bin/bash
# Parse command line arguments
WORKING_DIR=$1
PEER_ID=$2

# Move to the working directory
cd $WORKING_DIR

# Build PeerProcess
javac -cp lib/log4j-api-2.19.0.jar:lib/log4j-core-2.19.0.jar:. PeerProcess.java

# Run PeerProcess
java -cp lib/log4j-api-2.19.0.jar:lib/log4j-core-2.19.0.jar:. PeerProcess $PEER_ID 