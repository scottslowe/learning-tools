//

//% hadoop fs -mkdir /tmp
//% hadoop fs -chmod a+w /tmp
//% hadoop fs -mkdir /user/hive/warehouse
//% hadoop fs -chmod a+w /user/hive/warehouse
// use the shell (made available under variable fsh)
if (!fsh.test("/tmp")) {
  fsh.mkdir("/tmp")
}
fsh.chmod("a+w", "tmp")
if (!fsh.test("/user/hive/warehouse")) {
  fsh.mkdir("/user/hive/warehouse")
}
fsh.chmod("a+w", "/user/hive/warehouse")
