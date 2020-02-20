# Cassandra

## Installation

An instruction is available [here](https://www.liquidweb.com/kb/install-cassandra-ubuntu-16-04-lts/). You need to have `Python` v2.7 and `JDK8` installed in the first place. Commands are as follows:  

```
wget -q -O- https://www.apache.org/dist/cassandra/KEYS >> keys.key
sudo apt-key add keys.key
sudo apt-get update
sudo apt-get install -y cassandra
```


To verify the installation:  
 
```
nodetool status
cqlsh
```

If you face an error by executing cqlsh, do as follows:  


```
sudo apt-get install python-pip
pip install cassandra-driver

# add this to your env (e.g. in .bashrc)
export CQLSH_NO_BUNDLED=true
```


Forward ports:  

```
ssh ubuntu@192.168.104.96 -i vms -L 9042:localhost:9042
```