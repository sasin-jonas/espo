import csv
import io
import logging
import uuid
from typing import List, Dict, Any

from elasticsearch import helpers, Elasticsearch
from werkzeug.datastructures import FileStorage

from es.esConfig import es_schema_ch, es_schema_mu
from es.esUploadResult import EsUploadResult

logger = logging.getLogger(__name__)


class EsDataLoader:
    """
    A class for loading CSV data into Elasticsearch.

    Attributes:
        es (EsClient): An instance of Elasticsearch client.
    """

    def __init__(self, es: Elasticsearch):
        self.es: Elasticsearch = es

    def load_ch_csv_data(self, file: FileStorage) -> EsUploadResult:
        """
        Loads data from a CSV file into Elasticsearch crowdhelix index.

        Args:
            file (FileStorage): The CSV file to upload.
        Returns:
            EsUploadResult: An object representing the result of the upload.
        """
        self.create_index(es_schema_ch.get('index'), es_schema_ch.get('es'))

        stream = io.StringIO(file.stream.read().decode(), newline=None)
        csv_reader = csv.DictReader(stream, delimiter=';')
        upload_result = self.create_ch_bulk_request(csv_reader, es_schema_ch.get('index'))

        self.load_to_elastic(upload_result)

        return upload_result

    def load_mu_csv_data(self, file: FileStorage) -> EsUploadResult:
        """
        Loads data from a CSV file into Elasticsearch MU data index.

        Args:
            file (FileStorage): The CSV file to upload.
        Returns:
            EsUploadResult: An object representing the result of the upload.
        """
        self.create_index(es_schema_mu.get('index'), es_schema_mu.get('es'))

        stream = io.StringIO(file.stream.read().decode(), newline=None)
        csv_reader = csv.DictReader(stream, delimiter=';')
        upload_result = self.create_mu_bulk_request(csv_reader, es_schema_mu.get('index'))

        self.load_to_elastic(upload_result)

        return upload_result

    def load_to_elastic(self, upload_result):
        if len(upload_result.actions) > 0:
            # (successful, unsuccessful)
            stats: tuple[int, int] = helpers.bulk(self.es, upload_result.actions, stats_only=True)
            upload_result.successful = upload_result.successful - stats[1]
            upload_result.failed = upload_result.failed + stats[1]

    def create_index(self, index: str, config: Dict[str, Any]) -> None:
        """
        Creates an Elasticsearch index if it does not exist.

        Args:
            index (str): The name of the Elasticsearch index to create.
            config (Dict[str, Any]): The configuration scheme of the index.

        """
        self.es.indices.create(index=index, **config, ignore=400)

    @staticmethod
    def create_ch_bulk_request(csv_reader: csv.DictReader, index: str) -> EsUploadResult:
        """
        Creates a bulk request for uploading Crowdhelix data to Elasticsearch.

        Args:
           csv_reader (csv.DictReader): A DictReader instance for the CSV data.
           index: str: The name of the Elastic index.
        Returns:
           EsUploadResult: An object representing the result of the upload.
        """
        actions: List[Dict[str, Any]] = []
        line_count: int = 0
        failed: int = 0
        for data_line in csv_reader:
            try:
                if line_count == 0:
                    logger.info(f'Column names are {", ".join(data_line)}')

                data_line = {k: v.strip() for k, v in data_line.items()}

                EsDataLoader.parse_as_keywords(data_line, "helix")
                EsDataLoader.parse_as_keywords(data_line, "role")
                EsDataLoader.parse_as_keywords(data_line, "expertise")
                EsDataLoader.parse_as_integer(data_line, "ID")

                action = {
                    "_index": index,
                    "_id": str(uuid.uuid4()),
                    "_source": data_line
                }
                actions.append(action)
                line_count += 1
            except Exception:
                failed += 1
                logger.exception(f"failed to process {data_line}")
        logger.info(f'Processed {line_count} lines.')
        return EsUploadResult(line_count + failed, line_count, failed, actions)

    @staticmethod
    def create_mu_bulk_request(csv_reader: csv.DictReader, index: str) -> EsUploadResult:
        """
        Creates a bulk request for uploading MU data to Elasticsearch.

        Args:
           csv_reader (csv.DictReader): A DictReader instance for the CSV data.
           index: str: The name of the Elastic index.
        Returns:
           EsUploadResult: An object representing the result of the upload.
        """
        actions: List[Dict[str, Any]] = []
        line_count: int = 0
        failed: int = 0
        for data_line in csv_reader:
            try:
                if line_count == 0:
                    logger.info(f'Column names are {", ".join(data_line)}')

                data_line = {k: v.strip() for k, v in data_line.items()}

                # only load data in English
                if data_line.pop("annotationLanguage").lower() != "en":
                    continue

                EsDataLoader.rename_field(data_line, "Id", "projId")
                EsDataLoader.rename_field(data_line, "annotation", "description")

                action = {
                    "_index": index,
                    "_id": str(uuid.uuid4()),
                    "_source": data_line
                }
                actions.append(action)
                line_count += 1
            except Exception:
                failed += 1
                logger.exception(f"failed to process {data_line}")
        logger.info(f'Processed {line_count} lines.')
        return EsUploadResult(line_count + failed, line_count, failed, actions)

    @staticmethod
    def rename_field(data: Dict[str, Any], old_name: str, new_name: str) -> None:
        """
        Renames the field

        Args:
            data
            :param data: A dictionary containing the item to parse.
            :param old_name: The old name of the field
            :param new_name: The new name of the field
        Returns:
            None: This method modifies the given dictionary in place.
        """
        string: str = data.pop(old_name)
        data[new_name] = string

    @staticmethod
    def parse_as_integer(data: Dict[str, Any], field: str) -> None:
        """
        Parses a field as an integer.

        Args:
            data (Dict[str, Any]): A dictionary containing the item to parse.
            field (str): The name of the field to parse as an integer.
        Returns:
            None: This method modifies the given dictionary in place.
        """
        string: str = data.pop(field)
        data[field] = int(string)

    @staticmethod
    def parse_as_keywords(data: Dict[str, Any], field: str, delimiter=",") -> None:
        """
        Parse a string value in the given field of the given dictionary as a list of keywords.

        Args:
            data (Dict[str, Any]): The dictionary to modify.
            field (str): The name of the field containing the string value to parse.
        Returns:
            None: This method modifies the given dictionary in place.

        Example:
            item = {"name": "John", "interests": "reading, writing, coding"}
            parse_as_keywords(item, "interests")
            print(item)
            {"name": "John", "interests": ["reading", "writing", "coding"]}
            :param field: field name
            :param data: the whole data item
            :param delimiter: delimiter of keywords
        """
        string: str = data.pop(field)
        keywords: List[str] = []
        for h in string.split(delimiter):
            keywords.append(h.strip())
        data[field] = keywords
