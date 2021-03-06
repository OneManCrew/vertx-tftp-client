# Vert.x TFTP Client
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Maven Central](https://img.shields.io/maven-central/v/io.github.onemancrew/vertx-tftp-client.svg?label=Maven%20Central)](https://search.maven.org/search?q=g:%22io.github.onemancrew%22%20AND%20a:%22vertx-tftp-client%22)
[ ![Download](https://api.bintray.com/packages/onemancrew/vertx/vertx-tftp-client/images/download.svg?version=1.0) ](https://bintray.com/onemancrew/vertx/vertx-tftp-client/1.0/link)

vertx-tftp-client is a simple  async tftp client which works with vert.x v3.

The Following Java project implements the TFTP protocol and builds a TFTP client. Scope of the client is to upload and download files from a remote TFTP server.
I have used Vert.x to implement this project as asynchronously.

To use this component, add the following dependency to the dependencies section of your build descriptor:

Maven (in your pom.xml):
````
<dependency>
  <groupId>io.github.onemancrew</groupId>
  <artifactId>vertx-tftp-client</artifactId>
  <version>1.0</version>
</dependency>
````
Gradle:
````
implementation 'io.github.onemancrew:vertx-tftp-client:1.0'
````
ivy:
````
<dependency org="io.github.onemancrew" name="vertx-tftp-client" rev="1.0">
	<artifact name="vertx-tftp-client" ext="pom"></artifact>
</dependency>
````
#### Creating TFTP Client
````
Vertx vertx = Vertx.vertx();
TftpClient client =new TftpClient(vertx,tftpServerIp,port);//default port 69
``````

#### Upload files
````
client.upload("filePath",(progress)->{
    //progress will update every change in the upload progress.
    },
    (result)->{
    if (result.succeeded()) {
        System.out.println("upload succeeded");
      } else {
        System.out.println("error upload file" + result.cause().getMessage());
      }
});
````

#### Download file
````
client.download(fileName,downloadFoler,(result)->{
    (result)->{
        if (result.succeeded()) {
            System.out.println("download succeeded");
          } else {
            System.out.println("error download file" + result.cause().getMessage());
          }
});
````
#### Error Code Description
In case of TttpError Exception this id the description for each error code:

| ErrorCode |            Description            |
|:---------:|:---------------------------------:|
|     1     | File not found.                   |
|     2     | Access violation.                 |
|     3     | Disk full or allocation exceeded. |
|     4     | Illegal TFTP operation.           |
|     5     | Unknown transfer ID.              |
|     6     | File already exists.              |
|     7     | No such user.                     |

#### TFTP Upload Protocol:
![Upload diagram](upload.svg)

#### TFTP Download Protocol:
![Download diagram](download.svg)
