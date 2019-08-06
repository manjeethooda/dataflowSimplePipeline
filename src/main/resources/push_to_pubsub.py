import os
import uuid
import json
from google.cloud import pubsub_v1

os.environ['GOOGLE_APPLICATION_CREDENTIALS'] = "/path/to/service-key.json"

def read_account_ids_from_file(filename):
    accounts = []
    with open(filename) as fsock:
        for account in fsock.readlines():
            if not account: continue
            accounts.append(int(account))
    return accounts

def generate_uuids(uuid_count):
    return [uuid.uuid4().hex.upper() for _ in range(uuid_count)]

def publish_message(uuids, x):
    project_id = "project-name"
    batch_settings = pubsub_v1.types.BatchSettings(
        max_bytes=2048,  # One kilobyte
        max_latency=10,   # One second
    )
    publisher = pubsub_v1.PublisherClient(batch_settings)
    topic_name = "topic-name"

    topic_path = publisher.topic_path(project_id, topic_name)
    for _ in range(x):
        for uuid in uuids:
            message_id = uuid.uuid4().hex.upper()
            data = {"uuid" : uuid, "val" : "1"}
            msg = json.dumps(data).encode('utf-8')
            future = publisher.publish(topic_path, data=msg, message_id=message_id)

if __name__ == '__main__':
    uuids = generate_uuids(105)
    publish_message(uuids, 10000)
    print('Published messages.')