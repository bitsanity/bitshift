export LD_LIBRARY_PATH=$HOME/secp256k1/.libs

JLIB=-Djava.library.path=../../cryptils/lib

JARS=\
./lib/hsqldb.jar:\
../../cryptils/lib/tbox.jar
