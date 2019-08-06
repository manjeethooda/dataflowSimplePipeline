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

def publish_message(accounts,x):
    project_id = "project-name"
    batch_settings = pubsub_v1.types.BatchSettings(
        max_bytes=2048,  # One kilobyte
        max_latency=10,   # One second
    )
    publisher = pubsub_v1.PublisherClient(batch_settings)
    topic_name = "topic-name"
    
    topic_path = publisher.topic_path(project_id, topic_name)

    for n in range(x):
        for account in accounts :
            m_id = uuid.uuid4().hex.upper()
            data = {"uuid" : account, "val" : "1"}
            msg = json.dumps(data).encode('utf-8')
            future = publisher.publish(topic_path,data = msg , m_id = m_id)

if __name__ == '__main__':
    filename = 'accounts.txt'
    acc_ids = read_account_ids_from_file(filename)
    publish_message(acc_ids,10000)
    print('Published messages.')