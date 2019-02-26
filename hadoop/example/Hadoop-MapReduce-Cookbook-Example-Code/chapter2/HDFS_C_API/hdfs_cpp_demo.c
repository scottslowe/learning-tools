// Following is a libhdfs sample adapted from the src/c++/libhdfs/hdfs_write.c of the Hadoop distribution.

#include "hdfs.h" 

int main(int argc, char **argv) {
    
    hdfsFS fs = hdfsConnect("namenode_hostname",namenode_port);
    if (!fs) {
        fprintf(stderr, "Cannot connect to HDFS.\n");
        exit(-1);
    } 
 
    char* fileName = "demo_c.txt";
    char* message = "Welcome to HDFS C API!!!";
    int size = strlen(message);
    
    int exists = hdfsExists(fs, fileName);

    if (exists > -1) {
        fprintf(stdout, "File %s exists!\n", fileName);
    }else{
	// Create and open file for writing
        hdfsFile outFile = hdfsOpenFile(fs, fileName, O_WRONLY|O_CREAT, 0, 0, 0);
        if (!outFile) {
            fprintf(stderr, "Failed to open %s for writing!\n", fileName);
            exit(-2);
        }
	// write to file
        hdfsWrite(fs, outFile, (void*)message, size); 
	hdfsCloseFile(fs, outFile);
    }
    
    // Open file for reading
    hdfsFile inFile = hdfsOpenFile(fs, fileName, O_RDONLY, 0, 0, 0);
    if (!inFile) {
        fprintf(stderr, "Failed to open %s for reading!\n", fileName);
        exit(-2);
    }

    char* data = malloc(sizeof(char) * size); 
    // Read from file. 
    tSize readSize = hdfsRead(fs, inFile, (void*)data, size);
    fprintf(stdout, "%s\n", data);
    free(data);

    hdfsCloseFile(fs, inFile);
    hdfsDisconnect(fs);
    return 0;
}
