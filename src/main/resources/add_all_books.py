# -*- coding: windows-1251 -*-
import json

import requests
from add_book_to_index import split_to_chunks
import time

while True:
    response = requests.get('http://localhost:8080/api/book/id')
    if response.status_code == 200:
        ids = response.json()['ids']
        break
    else:
        print(f"Ошибка {response.status_code}, повтор запроса для получения всех id")
        time.sleep(20)


with open('books_ids.json') as f:
    added_books = set(json.load(f).values())
print(f'{len(ids) - len(added_books)} books found')

for i, id in enumerate(ids, start=1):
    print(i)
    if not (id in added_books):
        while True:
            response = requests.get(f'http://localhost:8080/api/book/text?id={id}')
            if response.status_code == 200:
                book_text = response.json()['text']
                split_to_chunks(id, book_text)
                break
            else:
                print(f"Ошибка {response.status_code}, повтор запроса для id {id}")
                time.sleep(20)
