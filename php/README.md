# Bluemix-todo-apps - PHP Overview
The backend for the ToDo app is written in PHP.  
ToDos can be stored in Couch DB using Cloudant..

## Prerequisites
Before running this app or deploying it to Bluemix you need to have

[PHP](http://us1.php.net/downloads.php), and the
[CloudFoundry Command Line](https://github.com/cloudfoundry/cli) installed.


## How it Works

1. Add items to the todo list by typing into the box and pressing `Enter`

1. Mark items as complete by clicking the checkmark to the left of the corresponding item.


## Running the app on Bluemix

1. Create a Bluemix Account

    [Sign up][bluemix_signup_url] for Bluemix, or use an existing account.

1. Download and install the [Cloud-foundry CLI][cloud_foundry_url] tool


1. Clone the app to your local environment from your terminal using the following command

  ```
  git clone https://github.com/IBM-Bluemix/todo-apps.git
  ```

1. `cd` into the `php` folder of this newly created directory

  ```
  cd php
  ```

1. Edit the `manifest.yml` file and change the application `host` to something unique.

  The host you use will determinate your application url initially, e.g. `<host>.mybluemix.net`.

  ### To use Cloudant as database to store the todos

  1. Create an instance of Cloudant to store the todos

    ```
    cf create-service cloudantNoSQLDB Lite todo-db-php
    ```

  1. Push the application

    ```
    cf push
    ```

    Done.. Go to your bluemix staging domain created and see your app running on Bluemix.


## Running the app locally
  1. Install dependencies
    ```
    curl -s https://getcomposer.org/installer | php
    php composer.phar install
    ```

  1. Create an instance of Cloudant to store the todos

    ```
    cf create-service cloudantNoSQLDB Lite todo-db-php
    ```

  1. Create a set of credentials for this service

    ```
    cf create-service-key todo-db-php for-local
    ```

  1. View the credentials and take note of the `url` value

    ```
    cf service-key todo-db-php for-local
    ```

  1. Edit a file name `app.php` in the `php` directory with the your database credentials.


  1. Run your local server and go to localhost/localport/php/public. In my case it looks like this http://localhost:8888/php/public/



### Additional Information
This app currently uses the PHP buildpack from [CloudFoundry](https://github.com/dmikusa-pivotal/cf-php-build-pack).

By default it is run unsing an Apache server however you can also use NGINX.  To do this open the options.json file in the .bp-config directory and change the WEB_SERVER property from httpd to nginx.

You may try other PHP buildpacks but the app has only been tested using the CloudFoundry buildpack so it may not work.

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

[bluemix_PHP_Docs]: https://www.ng.bluemix.net/docs/#runtimes/php/index.html
[cloud_foundry_url]: https://github.com/cloudfoundry/cli
[deploy_track_url]: https://github.com/cloudant-labs/deployment-tracker
