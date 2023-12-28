#!/bin/bash

# Loop to start peers from 1001 to 1009
for i in {1001..1009}
do
   echo "Starting PeerProcess with peer ID $i"
   java PeerProcess $i
done

echo "All peers have been started."
