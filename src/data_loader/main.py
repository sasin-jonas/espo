from elasticsearch import Elasticsearch
from flask import Flask, request, Response
from werkzeug.datastructures import FileStorage

from es.esConfig import elastic_config
from es.esDataLoader import EsDataLoader
from es.esUploadResult import EsUploadResult

app: Flask = Flask(__name__)


@app.route('/loadCrowdhelixData', methods=['POST'])
def upload_ch_data() -> str:
    """
    Uploads Crowdhelix CSV data to Elasticsearch.
    """

    f: FileStorage = request.files['file']
    if not f:
        return "No file detected"

    es_client: Elasticsearch = Elasticsearch([elastic_config])
    es: EsDataLoader = EsDataLoader(es_client)
    upload_result: EsUploadResult = es.load_ch_csv_data(f)
    es_client.close()
    return "Successfully processed {}/{} records ({} failed)".format(
        upload_result.successful, upload_result.total, upload_result.failed)


@app.route('/loadMuProjects', methods=['POST'])
def upload_mu_data() -> str:
    """
    Uploads MU CSV data to Elasticsearch.
    """

    f: FileStorage = request.files['file']
    if not f:
        return "No file detected"

    es_client: Elasticsearch = Elasticsearch([elastic_config])
    es: EsDataLoader = EsDataLoader(es_client)
    upload_result: EsUploadResult = es.load_mu_csv_data(f)
    es_client.close()
    return "Successfully processed {}/{} records ({} failed)".format(
        upload_result.successful, upload_result.total, upload_result.failed)


@app.route('/example-csv')
def download_csv() -> Response:
    """
    Generates an example CSV file for download.
    """

    csv_content: str = 'ID;title;url;author;institutionName;institutionUrl;helix;role;expertise;description;appendixUrl\n' \
                       '1;Opportunity 1 title;https://linkToTheCrowdhelixOpportunity.com;John Doe;Some institute name;https://linkToTheInstitutionUrl.com;Health, Vascular, Mission Cancer;Work Package Leader, Consortium Partner;Hepatology, Cardiovascular, Clinical research, Clinical trial, Cns;Some lengthy annotation;https://linkToTheAttachmentUrl.com\n' \
                       '2;Opportunity 2 title;https://linkToTheCrowdhelixOpportunity.com;Jenna Doe;Some institute name;https://linkToTheInstitutionUrl.com;Health, Mission Cancer;Consortium Partner;Clinical research, Clinical trial, Cns;Some lengthy annotation 2;\n'
    response: Response = Response(csv_content, mimetype='text/csv')
    response.headers.set('Content-Disposition', 'attachment', filename='example.csv')
    return response


if __name__ == "__main__":
    from waitress import serve

    print("Server up")
    serve(app, host="0.0.0.0", port=5001)
