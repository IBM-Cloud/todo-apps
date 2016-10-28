# Swift Todo List Backend

*An example using the Kitura web framework and HTTP Server to develop a Swift backend*

> Note, requires the 05-03-DEVELOPMENT-SNAPSHOT of the Swift compiler .

## Tutorial

This project accompanies the tutorial on IBM Developer Works: [Build End-to-End Cloud Apps using Swift with Kitura](https://developer.ibm.com/swift/2016/02/22/building-end-end-cloud-apps-using-swift-kitura/)

## Quick start for running locally

1. Install the [05-03-DEVELOPMENT Swift toolchain](https://swift.org/download/) 

2. Install Kitura dependencies:

  1. Mac OS X: 
  
    `brew install curl`
  
  2. Linux (Ubuntu 15.10):
   
    `sudo apt-get install libcurl4-openssl-dev`

3. Clone and move to TodoList application

    `git clone https://github.com/IBM-Swift/Kitura-TodoList && cd Kitura-TodoList`

4. Build TodoList application

  1. Mac OS X: 
	
	`swift build`
	
  2. Linux:
  
    	`swift build -Xcc -fblocks`
	
5. Install couchdb:

    If on OS X, install with Homebrew with:
    
    `brew install couchdb`
    
    If on Ubuntu, install with apt-get:
    
    `apt-get install couchdb`
    
    Follow your distribution's directions for starting the CouchDB server
    
6. Create the necessary design and views for CouchDB:

  ```
  cd deploy
  ./setupDB.sh
  ```

7. Run the TodoList application:

	`./.build/debug/TodoList`
	
8. Open up your browser, and view: 

   `frontend/index.html`

## Developing and Running in XCode:

Make sure you are running at least XCode 7.3. 

1. Automatically generate an XCode project from the Package.swift:

  `swift build -X`

2. Open XCode project

  `open TodoList.xcodeproj`

3. Switch the toolchain to the open source version of Swift.

4. Add Library search paths *This is a temporary work around*

    Currently 05-03 snapshot of Swift has trouble finding the compiled C libraries that are located in .build/debug. You must manually add a search path to the XCode project. Open the XCode project and in both the ***Kitura*** and ***Kitura-net*** modules, add the following to your ***Library Search Paths***:
    
    `$SRCROOT/.build/debug`

## Tests

  To run unit tests, run:
  
  `swift test`
  
  If you are using XCode, you can run the Test Cases as normal in the IDE.

## Deploying to BlueMix

1. Get an account for [Bluemix](https://new-console.ng.bluemix.net/?direct=classic)

2. Dowload and install the [Cloud Foundry tools](https://new-console.ng.bluemix.net/docs/starters/install_cli.html):

    ```
    cf login
    bluemix api https://api.ng.bluemix.net
    bluemix login -u username -o org_name -s space_name
    ```

    Be sure to change the directory to the Kitura-TodoList directory where the manifest.yml file is located.

3. Run `cf push`

    ***Note** The uploading droplet stage should take a long time, roughly 5-7 minutes. If it worked correctly, it should say:

    ```
    1 of 1 instances running 

    App started
    ```

4. Create the Cloudant backend and attach it to your instance.

    ```
    cf create-service cloudantNoSQLDB Lite bluemix-todo
    cf bind-service TodoList bluemix-todo
    cf restage
    ```

5. Create a views on Bluemix:

    Open the `deploy/setupDB.sh` file to edit the URL, username, and password if setting up the views on a Bluemix Cloudant database. Then run:

    ```
    cd deploy
    ./setupDB.sh
    ```




## License 

This library is licensed under Apache 2.0. Full license text is available in [LICENSE](LICENSE).
