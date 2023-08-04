# Web application supporting effective search of project opportunities and collaboration
**Master's thesis project**
- student: Jonáš Sasín
- advisor: Martin Komenda, Ph.D.

**Abstract:**
This thesis deals with designing, implementing, testing, and deploying a web application supporting the effective search of project opportunities and collaborations.  
The manual search for relevant project opportunities is tiring, and a considerable amount of time could be saved by automating the process. The primary purpose of this work is to wrap such automation in a user-friendly application that researchers from Masaryk University could use.  
The goal is achieved by building a system that uses Elasticsearch at its core and is accessible through a responsive web user interface. The resulting application is deployed in the MU environment and lets the users search for opportunities using various prompts and filtering. It also allows administrators to manage the data available in the system.  
The thesis text describes the problem domain and the system design and explains the usage of technologies and techniques utilized to achieve the desired outcome. Problems of testing and deploying such a system are discussed, and possible expansions as part of future work are proposed.

The application is available at [https://espo.iba.muni.cz](https://espo.iba.muni.cz).

## How to run the application

(You need to have Docker and Docker Compose installed.)

From the root source of this project, run (you can use either `docker-compose` or `docker compose`):

```docker compose --project-name espo up```

(or ```docker compose --project-name espo up -d``` to run in the background)

This builds the Docker images and runs the containers. The application will be available at [http://localhost:3000](http://localhost:3000).

To stop the application, run:  
```docker compose --project-name espo down```

To rebuild the application, run:  
```docker compose --project-name espo build```

## How to run for development

For the development process, it is recommended to run the application locally. You should still run Elasticsearch and 
PostgreSQL in Docker containers, but the application itself should be run locally. This allows for hot reloading of 
the application code.  
To run these containers, you can use the `docker-localdev-compose.yml` file. Rename it to `docker-compose.yml`, 
move it to a different folder and run:  
```docker compose up```  
This should set up the Elastic and PostgreSQL containers.   
You can optionally expose the Elastic and Postgres ports in the original docker-compose file and use them directly.

To run the web-app, web-backend and data_loader applications, please refer to their respective README files.

## Files and folders
- **web-app**: React application for the web user interface
- **web-backend**: Java Spring Boot application for the backend API
- **data_loader**: Python Flask application for loading data into Elasticsearch
- *docker-compose.yml*: Docker compose file for running the application
- *Dockerfile-fe*: Dockerfile for building the app-fe image
- *Dockerfile-be*: Dockerfile for building the app-api image
- *Dockerfile-loader*: Dockerfile for building the data-loader image
- *nginx.conf*: Nginx configuration file for the web server proxy
- *docker-localdev-compose.yml*: Docker compose file for running the ES and Postgres containers for local development
- *data_examples*: Example data used for application testing and development
- *term_analysis*: Term-frequency analysis script and output
- *README.md*: This file

## Environment variables
The docker-compose file uses various environment variables. They are set to defaults for local development, but you can
override them by changing the placeholder values in the docker-compose file.

