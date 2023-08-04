from typing import Dict, List, Any


class EsUploadResult:
    """
    Represents the result of an Elasticsearch upload operation.

    Attributes:
        total (int): The total number of documents attempted to be uploaded.
        successful (int): The number of documents that were successfully uploaded.
        failed (int): The number of documents that failed to be uploaded.
        actions (List[Dict[str, Any]]): The list of Elasticsearch bulk request actions to be executed.
    """

    def __init__(self, total: int, successful: int, failed: int, actions: List[Dict[str, Any]]):
        self.total = total
        self.successful = successful
        self.failed = failed
        self.actions = actions
