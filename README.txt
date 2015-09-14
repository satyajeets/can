
For Bootstrap server put all source files - 
1. javac *.java
2. rmic Bootstrap
3. rmiregistry 60000 &
4. java Bootstrap &

For Peer copy all source files and run can.sh. Change boostrap IP in can.sh.
Preferably execute the following commands by hand instead of the script

1. javac *.java
2. rmic Bootstrap 
3. rmic Peer
4. rmiregistry 60000 &
5. java Peer 192.168.1.1 &     - entr IP of bootstrap

for peer to JOIN the can - 
java PeerClient join

to VIEW data of a peer - 
java PeerClient view 192.168.2.1:50001

NOTE: the IP is that of the peer whoes information you wanto see.
port 50001 is same for all peers so dont change that. If no parameter 
is provided - java PeerClient view
information of the peer on which it is invoked will be shown

to INSERT entry into hash table -
%java PeerClient insert filename  

,where filename is any string. Please note that actually files are not 
entere here. the dht is a map of the type String->String which is just 
storing the file name in both spots

to SEARCH a hashtable entry - 
%java PeerClient search filename

,where filename is the name to be searched.

for LEAVE functionality - 
%java PeerClient leave

To see peer information after leave command user the VIEW command
make the peer on whoes terminal it is invoked leave the CAN. This feature has been
test on a CAN of 3-4 peers only.
