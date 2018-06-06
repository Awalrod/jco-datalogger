# jco-datalogger
This is java based datalogger that uses the java canopen stack to connect and record data from a canopen bus.

[CAN-Open](http://www.can-cia.org/index.php?id=canopen) is protocol that sits above the 
[CAN-Bus Physical/Data layer](http://www.can-cia.org/index.php?id=systemdesign-can-physicallayer),
but it can also sit above other data layers.  By a using a clever tool such as [socat](http://linux.die.net/man/1/socat),
we can route CAN-Bus data traffic onto a datagram port, allowing devices such as cell phones and tablets direct access to the
CAN-Bus data layer.

This project uses a socket based can bus interface available on Linux type operating systems.  The socket based approach coupled with socat 
allows the datalogger to collect data from anywhere that the canbus socket is visable (ussually the local local ip subnet).  

The Canopen jar is not yet available as open source code, due to my own lazyness.  If you are interesting in working with it, let me know.

This fork is intended to implement livestreaming data

