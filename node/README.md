# bluemix-todo-apps - node overview

TodoMVC using Cloudant and Compose for MongoDB services running on Bluemix.

Refer to the [README.md](../README.md) file in the parent directory
for general instructions regarding this application.

## How it Works

1. Add items to the todo list by typing into the box and pressing `Enter`

1. Mark items as complete by clicking the checkmark to the left of the corresponding item.

1. Delete items by clicking the 'X' to the right of the corresponding item that appears on hover.

1. Edit existing todo items by double-clicking on the corresponding item.

## Running the app on Bluemix

1. Create a Bluemix Account

    [Sign up][bluemix_signup_url] for Bluemix, or use an existing account.

1. Download and install the [Cloud-foundry CLI][cloud_foundry_url] tool

1. If you have not already, [download node.js 6.7.0 or later][download_node_url] and install it on your local machine.

1. Clone the app to your local environment from your terminal using the following command

  ```
  git clone https://github.com/IBM-Bluemix/todo-apps.git
  ```

1. `cd` into the `node` folder of this newly created directory

  ```
  cd node
  ```

1. Edit the `manifest.yml` file and change the application `host` to something unique.

  The host you use will determinate your application url initially, e.g. `<host>.mybluemix.net`.

  ### To use Cloudant as database

  1. Create an instance of Cloudant to store the todos

    ```
    cf create-service cloudantNoSQLDB Lite todo-db
    ```

  ### To use Compose for MongoDB as database

  1. Create an instance of MongoDB to store the todos

    ```
    cf create-service compose-for-mongodb Standard todo-db
    ```

1. Push the application

  ```
  cf push
  ```

## Running the app locally

1. Clone the app to your local environment from your terminal using the following command

  ```
  git clone https://github.com/IBM-Bluemix/todo-apps.git
  ```

1. Configure a database

  ### To use Cloudant as database

  1. Create an instance of Cloudant to store the todos

    ```
    cf create-service cloudantNoSQLDB Lite todo-db
    ```

  1. Create a set of credentials for this service

    ```
    cf create-service-key todo-db for-local
    ```

  1. View the credentials and take note of the `url` value

    ```
    cf service-key todo-db for-local
    ```

  1. Create a file name `vcap-local.json` in the `node` directory with the following content:

    ```
    {
      "services": {
        "cloudantNoSQLDB": [
          {
            "credentials": {
              "url":"<URL-FROM-THE-SERVICE-KEY-ABOVE>"
            },
            "label": "cloudantNoSQLDB",
            "plan": "Lite",
            "name": "todo-db"
          }
        ]
      }
    }
    ```

    Replace the url with the value retrieved from the service key.

  ### To use Compose for MongoDB as database

  1. Create an instance of Compose for MongoDB to store the todos

    ```
    cf create-service compose-for-mongodb Standard todo-db
    ```

  1. Create a set of credentials for this service

    ```
    cf create-service-key todo-db for-local
    ```

  1. View the credentials and take note of the `uri` and `ca_certificate_base64` values

    ```
    cf service-key todo-db for-local
    ```

  1. Create a file name `vcap-local.json` in the `node` directory with the following content:

    ```
    {
      "services": {
        "compose-for-mongodb": [
          {
            "credentials": {
              "ca_certificate_base64": "<CERTIFICATE>",
              "uri": "<URI>"
            },
            "label": "compose-for-mongodb",
            "plan": "Standard",
            "name": "todo-db"
          }
        ]
      }
    }
    ```

    Replace the placeholders with the values retrieved from the service key.

1. Get the application dependencies

  ```
  npm install
  ```

1. Start the application

  ```
  npm start
  ```

### Troubleshooting

To troubleshoot your Bluemix app the main useful source of information is the logs. To see them, run:

  ```
  cf logs <application-name> --recent
  ```

### License

[Apache License, Version 2.0](../LICENSE)

### Privacy Notice

The TodoMVC node sample web application includes code to track deployments to Bluemix and other Cloud Foundry platforms. The following information is sent to a [Deployment Tracker][deploy_track_url] service on each deployment:

* Application Name (`application_name`)
* Space ID (`space_id`)
* Application Version (`application_version`)
* Application URIs (`application_uris`)

This data is collected from the `VCAP_APPLICATION` environment variable in IBM Bluemix and other Cloud Foundry platforms. This data is used by IBM to track metrics around deployments of sample applications to IBM Bluemix. Only deployments of sample applications that include code to ping the Deployment Tracker service will be tracked.

### Disabling Deployment Tracking

Deployment tracking can be disabled by removing `require("cf-deployment-tracker-client").track();` from the beginning of the `server.js` file at the root of this repo.

[bluemix_signup_url]: https://console.ng.bluemix.net/?cm_mmc=Display-GitHubReadMe-_-BluemixSampleApp-Todo-_-Node-Compose-_-BM-DevAd
[cloud_foundry_url]: https://github.com/cloudfoundry/cli
[download_node_url]: https://nodejs.org/download/
[deploy_track_url]: https://github.com/cloudant-labs/deployment-tracker
