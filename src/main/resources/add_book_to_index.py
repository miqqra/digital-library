import sys
import json
import os
from annoy import AnnoyIndex
from TextSplitter import split_text
from sentence_embeddings import get_embeddings
from datetime import datetime


PATH = "D:/study/digital-library-backend/src/main/resources/"
def main():

    book_id = sys.argv[1]
    book_file = sys.argv[2]

    with open(book_file, 'r', encoding="utf-8") as f:
        book_text: str = f.read()

    split_to_chunks(book_id, book_text)
    print('True')


def split_to_chunks(book_id, book_text):
    with open(f'{PATH}books_ids.json', 'r') as f:
        book_ids: dict = json.load(f)
    last_id = max([int(book_id) for book_id in book_ids.keys()])
    chunks = split_text(last_id + 1, book_text)

    for chunk in chunks:
        book_ids[chunk['id']] = book_id
    with open(f'{PATH}books_ids.json', 'w') as f:
        json.dump(book_ids, f, indent=4)

    embeddings = get_embeddings(chunks)
    add_to_index(embeddings)

def add_to_index(embeddings):
    index = AnnoyIndex(1024, 'euclidean')
    index_name = get_latest_file(f'{PATH}indices')
    index.load(f'{index_name}')
    existing_vectors = []
    for i in range(index.get_n_items()):
        existing_vectors.append((i, index.get_item_vector(i)))

    new_index = AnnoyIndex(1024, 'euclidean')
    for vector in existing_vectors:
        new_index.add_item(*vector)
    for vector in embeddings:
        new_index.add_item(*vector)
    timestamp = datetime.now().strftime("%Y%m%d_%H%M%S")
    new_filename = f'{PATH}indices/index_{timestamp}.ann'
    new_index.build(64, n_jobs=-1)
    new_index.save(new_filename)


def get_latest_file(directory):
    files = [os.path.join(directory, f) for f in os.listdir(directory) if os.path.isfile(os.path.join(directory, f))]
    if not files:
        return None
    latest_file = max(files, key=os.path.getctime)
    return latest_file

if __name__ == "__main__":
    main()

