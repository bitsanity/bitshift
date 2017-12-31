source ../env.src

export LD_LIBRARY_PATH=/usr/local/lib

JLIB=-Djava.library.path=../cryptils/lib

JARS=\
../cryptils/lib/scrypt-1.4.0.jar:\
../cryptils/lib/zxing-core-3.2.1.jar:\
../cryptils/lib/zxing-javase-3.2.1.jar:\
./lib/hsqldb.jar:\
../cryptils/lib/tbox.jar
