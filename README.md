# InCal Elastic + Akka HTTP Seed Project

This project is meant to demonstrate functionality of InCal Elastic library by integrating it with a simple Akka HTTP server and providing a skeleton / seed app to fork and further customize.

## Installation

*  ### _Java 1.8_
(e.g. Oracle JDK)

```bash
sudo add-apt-repository ppa:webupd8team/java
sudo apt-get update
sudo apt-get install oracle-java8-installer
sudo apt install oracle-java8-set-default
```

* ### _Elastic Search_
  
  * Install ES (5.6.10)
  
  ```sh
  sudo apt-get update
  wget https://artifacts.elastic.co/downloads/elasticsearch/elasticsearch-5.6.10.deb
  sudo dpkg -i elasticsearch-5.6.10.deb
  sudo systemctl enable elasticsearch.service
  ```
  
  * Modify the configuration in `/etc/elasticsearch/elasticsearch.yml`
  
  ```sh
    cluster.name: seed_app-cluster (if not changed "elasticsearch" is used by default)
    bootstrap.memory_lock: true
    network.host: x.x.x.x           (set to a non-localhost ip address if the db should be accessible within a network)
    thread_pool:
      index:
        queue_size: 8000
      search:
        queue_size: 8000 
      bulk:
        queue_size: 500
    indices.query.bool.max_clause_count: 4096
  ```
  
  * If you want to store data in a special directory set also
   
  ```
  path.data: /your_custom_path
  ```
  (Note that you need to make `your_custom_path` writeable for the `elasticsearch` user)
  
  * Create a limits file `/etc/security/limits.d/elasticsearch.conf`
  
  ```sh
  elasticsearch    soft    nofile          1625538
  elasticsearch    hard    nofile          1625538
  elasticsearch    soft    memlock         unlimited
  elasticsearch    hard    memlock         unlimited
  ```
 
  * To force your new limits to be loaded log out of all your current sessions and log back in.
  
  * Configure open-file and locked memory constraints in `/etc/init.d/elasticsearch` (and/or `/etc/default/elasticsearch`)
  ```
  MAX_OPEN_FILES=1625538
  MAX_LOCKED_MEMORY=unlimited
  ```

  * Depending on the Linux installation configure also `/usr/lib/systemd/system/elasticsearch.service`
  ```
  LimitNOFILE=1625538
  LimitMEMLOCK=infinity
  ```

  * Set a reasonable heap size; recommended to 50% of available RAM, but no more than 31g in `/etc/elasticsearch/jvm.options`, e.g.
  ```
  -Xms4g
  -Xmx4g
  ```
  *  and finally apply the settings for `systemd`
  ```
  sudo systemctl daemon-reload
  ```
  
  * Start ES
  ```
  sudo service elasticsearch start
  ```
  
  * To check if everything works as expected see the log file(s) at `/var/log/elasticsearch/` and/or curl the server info by `curl -XGET localhost:9200`.

  * *Recommendation*: For convenient DB exploration and query execution you might want to install the *cerebro* app:

  ```sh
   sudo wget https://github.com/lmenezes/cerebro/releases/download/v0.8.3/cerebro-0.8.3.zip
   sudo unzip cerebro-0.8.3.zip
  ```
  * Open the configuration file `conf/application.conf` and add the host and name of your Elastic server to the `hosts` section, e.g.,
  ```
    {
      host = "http://127.0.0.1:9200"
      name = "seed_app-cluster"
    }
  ```
  * Run Cerebro, for example at the port 9209
  ```
   sudo ./bin/cerebro -Dhttp.port=9209
  ```
  (Cerebro web client is then accessible at [http://localhost:9209](http://localhost:9209)) 

  * Alternatively you can install *Kibana*, which also allows execution of Elastic queries and many more, as follows (more info [here](https://www.elastic.co/guide/en/kibana/5.6/deb.html)):
  ```
  echo "deb https://artifacts.elastic.co/packages/5.x/apt stable main" | sudo tee -a /etc/apt/sources.list.d/elastic-5.x.list
  sudo apt-get update
  sudo apt-get install kibana
  sudo /bin/systemctl daemon-reload
  sudo /bin/systemctl enable kibana.service
  sudo service start kibana
  ```
  
## Configuration
  
To configure the example app server pls. set the host and port properties in `application.conf`. Example:

```
############
# Seed App #
############

seed_app.host = "localhost"

seed_app.port = 8080

##################
# Elastic search #
##################

elastic {
  host = "127.0.0.1"
  port = 9200
  cluster.name = "seed_app-cluster"

  type = "transport"
  client.transport.sniff = true
  client.transport.ping_timeout = "80s"
  thread_pool.index.size = 4
  thread_pool.index.queue_size = 10000
  thread_pool.bulk.size = 4
  thread_pool.bulk.queue_size = 1000
}
```

## Deployment

To create an executable jar with all dependencies run

```
sbt assembly
```

This will produce a file such as `incal-access-elastic-akka-http-seed-assembly-0.0.2.jar`

## Usage / Examples

By default the seed app runs at `http://localhost:8080`, which is the url used in all the examples bellow. 

&nbsp;

1. Add a person to the app and returns its id\
\
Endpoint: `add`\
Example: `curl -X POST --header "Content-Type: application/json" --data '{"name": "Robert Mugabe", "age": 95, "gender": "Male", "died": true, "timeCreated": 312415324324532}' http://localhost:8080/add`\
Response: `2bc51223-613c-42d4-8254-de23654b9f34`

2. Count the stored persons\
\
Endpoint: `count`\
Example: `curl http://localhost:8080/count`\
Response: `3`

3. Get a person by id\
\
Endpoint: `get`\
Example: `curl http://localhost:8080/get/2bc51223-613c-42d4-8254-de23654b9f34`\
Response: `{"age":95,"died":true,"gender":"Male","id":"2bc51223-613c-42d4-8254-de23654b9f34","name":"Robert Mugabe","timeCreated":312415324324532}`

4. Search persons by a given name\
\
Endpoint: `search`\
Example: `curl http://localhost:8080/search/John`\
Response: `["2025e587-2656-4ea2-9f26-6b6f75840461","b2332005-ab0e-4210-8c42-55b4fa9230e1","c04a3785-bbc9-40a4-a1f6-4dd96f028e62"]`

6. Clear/remove the stored persons\
\
Endpoint: `clear`\
Example: `curl -X POST http://localhost:8080/clear`\
Response: `All persons deleted.`

Note: You can upload items from a resource csv `persons_upload.csv` or a csv of your choice by executing `UploadPersonsFromFile`. 
