# Log Loader

Log Loader is a java application for fast tailing and loading logs from the server.

## Getting Started

To quick use - download latest executable jar from [builds](https://github.com/serhiibh/log-loader/tree/master/builds) directory. To run application use double click or enter as default exe application.

![alt text](https://github.com/serhiibh/log-loader/blob/master/docs/opened.png)

### Functional fields:

#### Host - yes, it's host/ip of a server 

#### Port - ssh port, default is 22 

#### Username - ssh user name

#### Password - ssh user password

#### Server log - full path to server log

#### Server tail file - file into which will be tailing log stream 

#### Local directory - directory path 

#### ![NOTE] If select 'use temporary file for result log' program will create a temporary file, for now its '/tmp', with timestamp name. After complition temporary file will be deleted.

Click start and application will start connection with server and tailing log into you temporary file.

![alt text](https://github.com/serhiibh/log-loader/blob/master/docs/done.png)

Then log file loaded you can find it and zipped version in described directory path 'Local directory'.
Button 'Open' for quick opening log file in the system.

## Preparing arguments

```

```

## Installing

Download and open as gradle project.

## Built With

* [Gradle](https://gradle.org/) - Dependency Management


## License

This project is licensed under the MIT License - see the [LICENSE.md](LICENSE.md) file for details
