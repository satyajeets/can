#!/bin/bash

rmiregistry 60000 &
java Peer 129.21.30.38 &

echo "Use PeerClient for further commands"