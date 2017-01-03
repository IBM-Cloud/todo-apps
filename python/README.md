# bluemix-todo-apps - Python Overview

TodoMVC using Cloudant service running on Bluemix.

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

1. Clone the app to your local environment from your terminal using the following command

  ```
  git clone https://github.com/IBM-Bluemix/todo-apps.git
  ```

1. `cd` into the `python` folder of this newly created directory

  ```
  cd python
  ```

1. Edit the `manifest.yml` file and change the application `host` to something unique.

  The host you use will determinate your application url initially, e.g. `<host>.mybluemix.net`.

1. Create an instance of Cloudant to store the todos

  ```
  cf create-service cloudantNoSQLDB Lite todo-db
  ```

1. Push the application

  ```
  cf push
  ```


### Troubleshooting

To troubleshoot your Bluemix app the main useful source of information is the logs. To see them, run:

  ```
  cf logs <application-name> --recent
  ```

### License

[Apache License, Version 2.0](../LICENSE)


[cloud_foundry_url]: https://github.com/cloudfoundry/cli
[bluemix_signup_url]: https://console.ng.bluemix.net/?cm_mmc=Display-GitHubReadMe-_-BluemixSampleApp-Todo-_-Python-_-BM-DevAd
