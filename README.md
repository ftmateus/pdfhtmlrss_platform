# PDF HTML Redactable Signatures Schemes (RSS) Platform
### Setup development environment
* This project is only compatible with Linux. It is recommended to use Debian or 
its derivatives (Ubuntu, Mint, etc...) but it is expected to also work on other distros, like Fedora.
macOS compatibility is not guaranteed. 
* On Windows, you can use WSL or a Linux VM (A headless, minimal or server edition is recommended, 
alongside the remote development feature of your IDE using SSH).
* It's recommended to use IntelliJ IDEA, since Visual Studio Code support for Kotlin language is very limited as 
of the time of writing.
* Java 8 and 11 must be installed.
* You must have `poppler-utils` and `tidy` packages installed. In Ubuntu/Debian:

      sudo apt install -y poppler-utils tidy
 
* Create an `application-override.properties` file by copying 
`application-override-example.properties` file and populate the values, such 
as the admin password.
* A `.jks` keystore file must be created. You can generate it running `generate_keystore.sh`.
Don't forget to set the necessary keystore properties in the `application-override.properties`.
* Redis and MariaDB local instances are required. Spring Boot should automatically 
create the tables for MariaDB database.
* Clone the `xmlrss` repository submodule using: 

      git submodule init xmlrss

### Future work
You can consult the project GitHub issues page for more details.

### Warning / Disclaimer
This is a proof of concept application developed in the scope of a dissertation project 
and intended solely for demonstration and research purposes. Never use this application
in a production environment! This application lacks the necessary robustness, 
security and scalability requirements for a real-world environment. 

