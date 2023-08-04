# Data loader

Python Flask application for loading data into Elasticsearch.

## How to run
(You need to have Python 3 installed.)

First install the dependencies:
```pip3 install -r requirements.txt```

Then run the application:
```python3 main.py```

The service will be available at [http://localhost:5001](http://localhost:5001).

You can use the endpoint `/example-csv` ([http://localhost:5001/example-csv](http://localhost:5001/example-csv)) to get an example CSV file.
You can then use the endpoint `/load` to load the data from the CSV file into Elasticsearch.
