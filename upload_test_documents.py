import requests
import os

base_url = 'http://localhost:8090'


def get_url(path):
    return '{}/{}'.format(base_url, path)


def get_folder_id():
    r = requests.get(get_url("api/content/folder/scopes")).json()
    for folder in r['scopes']:
        if folder['name'] == 'Default':
            return folder['id']
    return None


def create_page(name, content, folder_id):
    r = requests.put(get_url('api/content/page'), json={
        'parentId': folder_id,
        'title': name,
        'content': content
    })
    if r.status_code == 200:
        print('success uploadig document ' + name)
    else:
        print("error uploading document " + name)


folder_id = get_folder_id()

for root, _, file_names in os.walk('doc'):
    for file_name in file_names:
        if not file_name.endswith('.md'):
            continue
        with open(os.path.join(root, file_name), 'r', encoding='utf-8') as file:
            content = file.read()
            title = content.partition('\n')[0][2:]
            create_page(title, content, folder_id)
