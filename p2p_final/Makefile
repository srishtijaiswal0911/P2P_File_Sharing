# Makefile for compiling and running PeerProcess

# Compiler and flags
JAVAC = javac
JAVA = java

# Java files
JAVA_FILES = $(wildcard *.java)

# Configuration file
PEER_CONFIG = PeerInfo.cfg

.PHONY: all clean run

all: compile

compile:
	$(JAVAC) $(JAVA_FILES)

run:
	@while read line; do \
		peerID=$$(echo $$line | cut -d' ' -f1); \
		osascript -e "tell application \"Terminal\" to do script \"$(JAVA) PeerProcess $$peerID\"" & \
	done < $(PEER_CONFIG)

clean:
	@rm -f *.class